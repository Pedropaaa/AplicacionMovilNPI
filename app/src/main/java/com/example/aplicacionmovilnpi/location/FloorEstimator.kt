package com.example.aplicacionmovilnpi.location

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Estima la planta en la que está el móvil a partir del barómetro.
 *
 * La presión absoluta depende del tiempo que haga, así que no sirve para saber la planta
 * directamente: se calibra con una planta conocida (la del origen de la ruta, o cuando la
 * persona pulsa «Estoy en esta planta») y a partir de ahí se mide cuánto se ha subido o bajado.
 * Una planta de la ETSIIT son unos 4 m, que equivalen a casi 0,5 hPa.
 */
class FloorEstimator(
    private val floorHeightMeters: Float = FLOOR_HEIGHT_M,
    /** Constante de tiempo del filtro: la lectura tarda ~1 s en reflejar un cambio de presión. */
    private val smoothingSeconds: Float = 1f,
    private val minFloor: Int = -1,
    private val maxFloor: Int = 4
) {
    var smoothedPressure: Float? = null
        private set
    var currentFloor: Int? = null
        private set

    private var referencePressure: Float? = null
    private var referenceFloor: Int = 0
    private var pendingCalibration: Int? = null
    private var lastTimestampMs: Long? = null

    /** Metros subidos (positivo) o bajados (negativo) desde la última calibración. */
    val relativeAltitudeMeters: Float?
        get() {
            val ref = referencePressure ?: return null
            val p = smoothedPressure ?: return null
            return altitudeDifference(ref, p)
        }

    /** Fija que, con la presión actual, el móvil está en [floor]. */
    fun calibrate(floor: Int) {
        if (smoothedPressure == null) {
            pendingCalibration = floor
            return
        }
        referencePressure = smoothedPressure
        referenceFloor = floor
        currentFloor = floor
    }

    /**
     * Procesa una lectura del barómetro (hPa) y devuelve la planta estimada, o null si no está calibrado.
     * El suavizado depende del tiempo entre lecturas y no de cuántas llegan, porque cada móvil
     * manda el barómetro a una frecuencia distinta (y algunos solo cuando cambia el valor).
     */
    fun onPressure(hPa: Float, timestampMs: Long): Int? {
        val dtSeconds = lastTimestampMs?.let { (timestampMs - it).coerceAtLeast(0) / 1000f }
        lastTimestampMs = timestampMs
        val alpha = if (dtSeconds == null) 1f else 1f - exp(-dtSeconds / smoothingSeconds)
        smoothedPressure = smoothedPressure?.let { it + alpha * (hPa - it) } ?: hPa
        pendingCalibration?.let {
            pendingCalibration = null
            calibrate(it)
        }

        val meters = relativeAltitudeMeters ?: return null
        val floors = meters / floorHeightMeters
        val nearest = floors.roundToInt()
        // Histéresis: solo cambiamos de planta cuando la altura está claramente cerca de la nueva
        if (abs(floors - nearest) < HYSTERESIS) {
            currentFloor = (referenceFloor + nearest).coerceIn(minFloor, maxFloor)
        }
        return currentFloor
    }

    companion object {
        const val FLOOR_HEIGHT_M = 4f
        const val HYSTERESIS = 0.3f

        /** Diferencia de altura (m) entre dos presiones, con la fórmula barométrica estándar. */
        fun altitudeDifference(referenceHPa: Float, currentHPa: Float): Float =
            44330f * (1f - (currentHPa / referenceHPa).pow(1f / 5.255f))
    }
}
