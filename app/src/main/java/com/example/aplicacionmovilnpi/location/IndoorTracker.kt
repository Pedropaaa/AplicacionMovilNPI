package com.example.aplicacionmovilnpi.location

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.aplicacionmovilnpi.model.Node
import kotlin.math.abs

/**
 * Junta los sensores de interior para el plano: planta estimada por el barómetro y posición
 * por pasos + brújula sobre el tramo de ruta que se está siguiendo.
 * Las propiedades son estado de Compose, así que la UI se actualiza sola al cambiar.
 */
class IndoorTracker(initialMapNorthDeg: Float = DeadReckoning.DEFAULT_MAP_NORTH_DEG) {

    private val floorEstimator = FloorEstimator()
    private var path: List<MapPoint> = emptyList()

    var pressureHpa by mutableStateOf<Float?>(null)
        private set
    var relativeAltitudeM by mutableStateOf<Float?>(null)
        private set
    var estimatedFloor by mutableStateOf<Int?>(null)
        private set
    var steps by mutableIntStateOf(0)
        private set
    var progress by mutableStateOf<RouteProgress?>(null)
        private set
    var mapNorthDeg by mutableFloatStateOf(initialMapNorthDeg)
        private set

    val position: MapPoint? get() = progress?.position

    fun onPressure(hPa: Float, timestampMs: Long) {
        estimatedFloor = floorEstimator.onPressure(hPa, timestampMs)
        val smoothed = floorEstimator.smoothedPressure
        // Redondeamos para no recomponer la pantalla con cada variación mínima del sensor
        if (smoothed != null && abs(smoothed - (pressureHpa ?: 0f)) >= 0.01f) {
            pressureHpa = smoothed
            relativeAltitudeM = floorEstimator.relativeAltitudeMeters
        }
    }

    /** La persona confirma (o la ruta asume) que está en [floor]. */
    fun calibrateFloor(floor: Int) {
        floorEstimator.calibrate(floor)
        estimatedFloor = floorEstimator.currentFloor ?: floor
        relativeAltitudeM = floorEstimator.relativeAltitudeMeters
    }

    /** Empieza a seguir un tramo de ruta desde su primer nodo. */
    fun followPath(nodes: List<Node>) {
        path = nodes.map { MapPoint(it.x, it.y) }
        progress = path.firstOrNull()?.let { DeadReckoning.progressOnPath(it, path) }
    }

    fun resetSteps() {
        steps = 0
    }

    fun stopFollowing() {
        path = emptyList()
        progress = null
    }

    /** Un paso detectado: avanzamos una zancada en la dirección de la brújula y lo ajustamos a la ruta. */
    fun onStep(azimuthDeg: Float?) {
        steps++
        val from = position ?: return
        val heading = azimuthDeg ?: return
        val moved = DeadReckoning.step(from, heading, mapNorthDeg)
        progress = DeadReckoning.progressOnPath(moved, path)
    }

    /** Dirección del tramo de ruta en el que está la persona (en grados del plano). */
    fun currentLegMapAngle(): Float? {
        val p = progress ?: return null
        if (path.size < 2) return null
        val to = path[p.nextIndex.coerceAtMost(path.lastIndex)]
        // Si ya estamos encima del siguiente punto, usamos la dirección del tramo entero
        return if (p.metersToNext > 0.5f) DeadReckoning.mapAngle(p.position, to)
        else DeadReckoning.mapAngle(path[(p.nextIndex - 1).coerceAtLeast(0)], to)
    }

    /** Azimut hacia el que debería caminar la persona para seguir la ruta. */
    fun targetAzimuth(): Float? = currentLegMapAngle()?.let { DeadReckoning.mapAngleToAzimuth(it, mapNorthDeg) }

    /**
     * Calibra la brújula suponiendo que la persona está mirando en el sentido de la ruta.
     * Devuelve el nuevo norte del plano para poder guardarlo.
     */
    fun alignCompass(azimuthDeg: Float): Float? {
        val legAngle = currentLegMapAngle() ?: return null
        mapNorthDeg = DeadReckoning.calibrateMapNorth(azimuthDeg, legAngle)
        return mapNorthDeg
    }
}

/** Guarda la calibración de la brújula para no tener que repetirla cada vez. */
object LocationPrefs {
    private const val PREFS = "location"
    private const val KEY_MAP_NORTH = "map_north_deg"

    fun mapNorth(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_MAP_NORTH, DeadReckoning.DEFAULT_MAP_NORTH_DEG)

    fun setMapNorth(context: Context, deg: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat(KEY_MAP_NORTH, deg).apply()
    }
}
