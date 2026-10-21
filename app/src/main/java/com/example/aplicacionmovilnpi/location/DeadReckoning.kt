package com.example.aplicacionmovilnpi.location

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Punto en las coordenadas del plano (las mismas que usan los nodos de EtsiitData, de 0 a 1000). */
data class MapPoint(val x: Float, val y: Float)

/** Dónde ha quedado la persona respecto a la ruta del tramo actual. */
data class RouteProgress(
    val position: MapPoint,
    /** Índice del siguiente punto de la ruta al que hay que ir. */
    val nextIndex: Int,
    val metersToNext: Float,
    val metersToEnd: Float
) {
    val hasArrived: Boolean get() = metersToEnd <= ARRIVAL_RADIUS_M

    companion object {
        const val ARRIVAL_RADIUS_M = 2f
    }
}

/**
 * Navegación a estima (PDR): cada paso desplaza la posición una zancada en la dirección de la
 * brújula. Como la brújula y la zancada tienen error, después del paso se proyecta la posición
 * sobre la ruta dibujada; así el punto azul avanza por el pasillo en vez de atravesar paredes.
 */
object DeadReckoning {

    /** Unidades del plano por metro real. Estimado con la distancia entre aulas consecutivas (~6 m). */
    const val UNITS_PER_METER = 6f

    /** Longitud media de una zancada al caminar. */
    const val STEP_LENGTH_M = 0.7f

    /**
     * Azimut (grados respecto al norte) que corresponde a «hacia arriba» en el plano.
     * Es un valor de partida; se ajusta en el propio edificio con «Calibrar brújula».
     */
    const val DEFAULT_MAP_NORTH_DEG = 0f

    /** Nueva posición tras un paso con la brújula marcando [azimuthDeg]. */
    fun step(
        from: MapPoint,
        azimuthDeg: Float,
        mapNorthDeg: Float,
        stepLengthM: Float = STEP_LENGTH_M
    ): MapPoint {
        val angle = Math.toRadians((azimuthDeg - mapNorthDeg).toDouble())
        val distance = stepLengthM * UNITS_PER_METER
        // En el plano la y crece hacia abajo, por eso «arriba» es -y
        return MapPoint(
            x = from.x + (sin(angle) * distance).toFloat(),
            y = from.y - (cos(angle) * distance).toFloat()
        )
    }

    /** Proyecta [point] sobre la polilínea [path] y calcula cuánto falta para el siguiente punto y el final. */
    fun progressOnPath(point: MapPoint, path: List<MapPoint>): RouteProgress? {
        if (path.isEmpty()) return null
        if (path.size == 1) {
            val d = distance(point, path[0]) / UNITS_PER_METER
            return RouteProgress(path[0], 0, d, d)
        }

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

    /** Dirección en el plano de [from] a [to], en grados: 0 = arriba, 90 = derecha. */
    fun mapAngle(from: MapPoint, to: MapPoint): Float =
        normalize(Math.toDegrees(atan2((to.x - from.x).toDouble(), (from.y - to.y).toDouble())).toFloat())

    /** Convierte una dirección del plano en azimut de brújula. */
    fun mapAngleToAzimuth(mapAngleDeg: Float, mapNorthDeg: Float): Float = normalize(mapAngleDeg + mapNorthDeg)

    /**
     * Calibración de la brújula: la persona se pone mirando en el sentido de la ruta y pulsa
     * el botón. El desfase entre lo que marca la brújula y la dirección del tramo es el norte del plano.
     */
    fun calibrateMapNorth(currentAzimuthDeg: Float, routeMapAngleDeg: Float): Float =
        normalize(currentAzimuthDeg - routeMapAngleDeg)

    /** Indicación de giro para ir hacia [targetAzimuthDeg] mirando hacia [headingDeg]. */
    fun turnInstruction(headingDeg: Float, targetAzimuthDeg: Float): String {
        val diff = signedDifference(headingDeg, targetAzimuthDeg)
        return when {
            abs(diff) <= 25f -> "Sigue recto"
            abs(diff) >= 150f -> "Date la vuelta"
            diff > 0 && diff < 65f -> "Gira un poco a la derecha"
            diff > 0 -> "Gira a la derecha"
            diff > -65f -> "Gira un poco a la izquierda"
            else -> "Gira a la izquierda"
        }
    }

    /** Diferencia de [from] a [to] en grados, entre -180 y 180 (positivo = a la derecha). */
    fun signedDifference(from: Float, to: Float): Float {
        var d = normalize(to - from)
        if (d > 180f) d -= 360f
        return d
    }

    fun normalize(deg: Float): Float = ((deg % 360f) + 360f) % 360f

    /** Rumbo de brújula como texto corto: "NE 45°". */
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
