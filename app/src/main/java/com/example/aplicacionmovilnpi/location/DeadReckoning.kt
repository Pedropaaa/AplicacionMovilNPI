package com.example.aplicacionmovilnpi.location

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Representa una coordenada bidimensional (x, y) en las unidades del plano de la facultad (0 a 1000).
 *
 * @property x Coordenada horizontal en unidades de pantalla.
 * @property y Coordenada vertical en unidades de pantalla.
 */
data class MapPoint(val x: Float, val y: Float)

/**
 * Información sobre el progreso de un usuario a lo largo de un trayecto de navegación.
 *
 * @property position Posición actual proyectada sobre el segmento del plano.
 * @property nextIndex Índice del próximo nodo/punto de la polilínea al que se dirige.
 * @property metersToNext Distancia en metros hasta el siguiente nodo.
 * @property metersToEnd Distancia total en metros restante hasta el destino final.
 */
data class RouteProgress(
    val position: MapPoint,
    val nextIndex: Int,
    val metersToNext: Float,
    val metersToEnd: Float
) {
    /** Determina si el usuario ha llegado al destino respetando el radio de tolerancia [ARRIVAL_RADIUS_M]. */
    val hasArrived: Boolean get() = metersToEnd <= ARRIVAL_RADIUS_M

    companion object {
        /** Radio de tolerancia en metros para dar por completada la ruta (2.0 metros). */
        const val ARRIVAL_RADIUS_M = 2f
    }
}

/**
 * Motor de navegación por estima (Pedestrian Dead Reckoning - PDR) y proyección sobre mapa.
 *
 * Realiza el cálculo del desplazamiento punto a punto basándose en la brújula y la longitud de zancada.
 * Proyecta la posición resultante sobre la polilínea de la ruta para evitar desvíos o trazar caminatas sobre paredes.
 */
object DeadReckoning {

    /** Escala de conversión: unidades del plano por cada metro real (~6 unidades/metro). */
    const val UNITS_PER_METER = 6f

    /** Longitud promedio estimada de una zancada al caminar en metros (1.1 m). */
    const val STEP_LENGTH_M = 1.1f

    /** Orientación por defecto del norte del plano en grados (0° = Arriba). */
    const val DEFAULT_MAP_NORTH_DEG = 0f

    /**
     * Calcula la nueva posición teórica tras avanzar una zancada en la dirección del azimut de la brújula.
     *
     * @param from Punto de origen.
     * @param azimuthDeg Grados del rumbo reportados por la brújula.
     * @param mapNorthDeg Desviación del norte del plano respecto al norte magnético.
     * @param stepLengthM Longitud de la zancada en metros.
     * @return Nueva coordenada [MapPoint] tras el desplazamiento.
     */
    fun step(
        from: MapPoint,
        azimuthDeg: Float,
        mapNorthDeg: Float,
        stepLengthM: Float = STEP_LENGTH_M
    ): MapPoint {
        // 1. Convertir la diferencia angular a radianes
        val angle = Math.toRadians((azimuthDeg - mapNorthDeg).toDouble())
        
        // 2. Escalar la longitud de zancada a unidades del plano
        val distance = stepLengthM * UNITS_PER_METER
        
        // 3. Calcular nuevas coordenadas (en el plano el eje Y aumenta hacia abajo)
        return MapPoint(
            x = from.x + (sin(angle) * distance).toFloat(),
            y = from.y - (cos(angle) * distance).toFloat()
        )
    }

    /**
     * Proyecta un punto sobre la polilínea de la ruta y calcula la distancia restante al destino.
     *
     * @param point Punto a proyectar.
     * @param path Polilínea de la ruta como lista de puntos [MapPoint].
     * @return Objeto [RouteProgress] con la proyección y distancias calculadas, o `null` si el camino está vacío.
     */
    fun progressOnPath(point: MapPoint, path: List<MapPoint>): RouteProgress? {
        // 1. Validar el trayecto
        if (path.isEmpty()) return null
        if (path.size == 1) {
            val d = distance(point, path[0]) / UNITS_PER_METER
            return RouteProgress(path[0], 0, d, d)
        }

        // 2. Buscar el segmento de la polilínea más cercano al punto
        var best: MapPoint = path[0]
        var bestIndex = 0
        var bestDist = Float.MAX_VALUE
        for (i in 0 until path.size - 1) {
            val candidate = projectOnSegment(point, path[i], path[i + 1])
            val d = distance(point, candidate)
            if (d < bestDist) {
                bestDist = d
                best = candidate
                bestIndex = i
            }
        }

        // 3. Acumular distancias restantes hasta el final de la ruta
        val toNext = distance(best, path[bestIndex + 1])
        var toEnd = toNext
        for (i in bestIndex + 1 until path.size - 1) toEnd += distance(path[i], path[i + 1])

        return RouteProgress(
            position = best,
            nextIndex = bestIndex + 1,
            metersToNext = toNext / UNITS_PER_METER,
            metersToEnd = toEnd / UNITS_PER_METER
        )
    }

    /**
     * Calcula el ángulo en grados sobre el plano entre dos puntos (0° = arriba, 90° = derecha).
     *
     * @param from Punto de origen.
     * @param to Punto de destino.
     * @return Grados en el rango [0..360).
     */
    fun mapAngle(from: MapPoint, to: MapPoint): Float =
        normalize(Math.toDegrees(atan2((to.x - from.x).toDouble(), (from.y - to.y).toDouble())).toFloat())

    /**
     * Convierte una dirección sobre el plano a azimut de brújula magnética.
     *
     * @param mapAngleDeg Ángulo sobre el plano.
     * @param mapNorthDeg Norte del plano.
     * @return Azimut equivalente en grados.
     */
    fun mapAngleToAzimuth(mapAngleDeg: Float, mapNorthDeg: Float): Float = normalize(mapAngleDeg + mapNorthDeg)

    /**
     * Calcula la desviación del norte del plano [mapNorthDeg] dada la orientación actual del teléfono.
     *
     * @param currentAzimuthDeg Azimut reportado por la brújula.
     * @param routeMapAngleDeg Ángulo del segmento de ruta en el plano.
     * @return Nuevo valor del norte del plano en grados.
     */
    fun calibrateMapNorth(currentAzimuthDeg: Float, routeMapAngleDeg: Float): Float =
        normalize(currentAzimuthDeg - routeMapAngleDeg)

    /**
     * Genera una indicación hablada o textual de giro basada en el rumbo actual y el objetivo.
     *
     * @param headingDeg Rumbo actual de la persona.
     * @param targetAzimuthDeg Azimut del objetivo.
     * @return Texto de indicación ("Sigue recto", "Gira a la derecha", "Gira a la izquierda", "Date la vuelta").
     */
    fun turnInstruction(headingDeg: Float, targetAzimuthDeg: Float): String {
        val diff = signedDifference(headingDeg, targetAzimuthDeg)
        return when {
            abs(diff) <= 45f -> "Sigue recto"
            abs(diff) >= 125f -> "Date la vuelta"
            diff > 0 -> "Gira a la derecha"
            else -> "Gira a la izquierda"
        }
    }

    /**
     * Calcula la diferencia angular con signo entre dos rumbos [-180..180].
     *
     * @param from Ángulo de origen.
     * @param to Ángulo de destino.
     * @return Diferencia con signo (positivo = giro a la derecha).
     */
    fun signedDifference(from: Float, to: Float): Float {
        var d = normalize(to - from)
        if (d > 180f) d -= 360f
        return d
    }

    /** Normaliza un ángulo en grados al intervalo [0..360). */
    fun normalize(deg: Float): Float = ((deg % 360f) + 360f) % 360f

    /**
     * Formatea el azimut de brújula en una cadena legible con orientación cardinal (ej: "NE 45°").
     *
     * @param azimuthDeg Rumbo en grados.
     * @return Texto formateado.
     */
    fun compassLabel(azimuthDeg: Float): String {
        val names = listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
        val index = ((normalize(azimuthDeg) + 22.5f) / 45f).toInt() % 8
        return "${names[index]} ${normalize(azimuthDeg).toInt()}°"
    }

    private fun projectOnSegment(p: MapPoint, a: MapPoint, b: MapPoint): MapPoint {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val lengthSq = dx * dx + dy * dy
        if (lengthSq == 0f) return a
        val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSq).coerceIn(0f, 1f)
        return MapPoint(a.x + t * dx, a.y + t * dy)
    }

    private fun distance(a: MapPoint, b: MapPoint): Float = hypot(a.x - b.x, a.y - b.y)
}
