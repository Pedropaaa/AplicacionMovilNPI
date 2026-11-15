package com.example.aplicacionmovilnpi.location

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.aplicacionmovilnpi.model.Node

/**
 * Gestor del seguimiento de posición del usuario dentro del plano del edificio.
 *
 * Integra la acumulación de pasos (PDR) con la brújula orientada sobre el tramo activo de la ruta.
 * Las propiedades se exponen como estados reactivos de Compose para actualizar la UI en tiempo real.
 *
 * @param initialMapNorthDeg Grados de desviación del norte del mapa respecto al norte magnético.
 */
class IndoorTracker(initialMapNorthDeg: Float = DeadReckoning.DEFAULT_MAP_NORTH_DEG) {

    private var path: List<MapPoint> = emptyList()

    /** Planta estimada o seleccionada manualmente por el usuario. */
    var estimatedFloor by mutableStateOf<Int?>(null)
        private set

    /** Número acumulado de pasos detectados durante la sesión actual. */
    var steps by mutableIntStateOf(0)
        private set

    /** Estado del progreso actual a lo largo del trayecto seleccionado. */
    var progress by mutableStateOf<RouteProgress?>(null)
        private set

    /** Desviación del norte magnético respecto al eje vertical del plano en grados. */
    var mapNorthDeg by mutableFloatStateOf(initialMapNorthDeg)
        private set

    /** Coordenadas [MapPoint] de la posición actual del usuario proyectada en el plano. */
    val position: MapPoint? get() = progress?.position

    /**
     * Establece la planta actual del usuario mediante calibración explícita o selección manual.
     *
     * @param floor Número entero de la planta (-1 a 4).
     */
    fun calibrateFloor(floor: Int) {
        estimatedFloor = floor
    }

    /**
     * Inicia el seguimiento de una ruta dada como una lista ordenada de nodos [Node].
     *
     * @param nodes Lista de nodos que componen la ruta.
     */
    fun followPath(nodes: List<Node>) {
        path = nodes.map { MapPoint(it.x, it.y) }
        progress = path.firstOrNull()?.let { DeadReckoning.progressOnPath(it, path) }
    }

    /** Reinicia a cero el contador acumulado de pasos. */
    fun resetSteps() {
        steps = 0
    }

    /** Cancela la ruta activa y limpia el progreso de navegación. */
    fun stopFollowing() {
        path = emptyList()
        progress = null
    }

    /**
     * Procesa la detección de un paso actualizando las coordenadas de posición por estima.
     *
     * Avanza una zancada en la dirección del azimut de la brújula y proyecta el resultado sobre la polilínea.
     *
     * @param azimuthDeg Grados del rumbo de la brújula reportado por los sensores.
     */
    fun onStep(azimuthDeg: Float?) {
        // 1. Incrementar contador de pasos
        steps++
        val from = position ?: return
        val heading = azimuthDeg ?: return
        
        // 2. Avanzar la posición teórica mediante DeadReckoning
        val moved = DeadReckoning.step(from, heading, mapNorthDeg)
        
        // 3. Proyectar la posición resultante sobre los segmentos de la ruta
        progress = DeadReckoning.progressOnPath(moved, path)
    }

    /**
     * Obtiene la dirección del tramo activo de la ruta expresada en grados del plano.
     *
     * @return Ángulo del tramo en grados decimales [0..360), o `null` si no hay ruta activa.
     */
    fun currentLegMapAngle(): Float? {
        val p = progress ?: return null
        if (path.size < 2) return null
        val to = path[p.nextIndex.coerceAtMost(path.lastIndex)]
        // Si el usuario ya está muy próximo al siguiente punto, usar el segmento anterior para evitar saltos
        return if (p.metersToNext > 0.5f) DeadReckoning.mapAngle(p.position, to)
        else DeadReckoning.mapAngle(path[(p.nextIndex - 1).coerceAtLeast(0)], to)
    }

    /**
     * Calcula el azimut objetivo hacia el cual debería orientarse el usuario para seguir la ruta.
     *
     * @return Azimut deseado en grados magnéticos, o `null` si no hay tramo activo.
     */
    fun targetAzimuth(): Float? = currentLegMapAngle()?.let { DeadReckoning.mapAngleToAzimuth(it, mapNorthDeg) }

    /**
     * Calibra la brújula asumiendo que el usuario está caminando orientado en el sentido exacto de la ruta.
     *
     * @param azimuthDeg Azimut actual que marca la brújula del teléfono.
     * @return El nuevo ángulo de desviación del norte del plano [mapNorthDeg].
     */
    fun alignCompass(azimuthDeg: Float): Float? {
        val legAngle = currentLegMapAngle() ?: return null
        mapNorthDeg = DeadReckoning.calibrateMapNorth(azimuthDeg, legAngle)
        return mapNorthDeg
    }
}

/**
 * Gestor de almacenamiento en preferencias locales del sistema para la calibración de la brújula.
 */
object LocationPrefs {
    private const val PREFS = "location"
    private const val KEY_MAP_NORTH = "map_north_deg"

    /**
     * Obtiene el ángulo del norte del plano guardado en preferencias.
     *
     * @param context Contexto de la aplicación.
     * @return Grados del norte del plano (por defecto [DeadReckoning.DEFAULT_MAP_NORTH_DEG]).
     */
    fun mapNorth(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_MAP_NORTH, DeadReckoning.DEFAULT_MAP_NORTH_DEG)

    /**
     * Guarda la calibración del norte del plano en las preferencias.
     *
     * @param context Contexto de la aplicación.
     * @param deg Ángulo del norte en grados decimales.
     */
    fun setMapNorth(context: Context, deg: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat(KEY_MAP_NORTH, deg).apply()
    }
}
