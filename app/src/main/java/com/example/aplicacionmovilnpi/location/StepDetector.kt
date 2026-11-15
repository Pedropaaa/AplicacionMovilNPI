package com.example.aplicacionmovilnpi.location

import kotlin.math.sqrt

/**
 * Podómetro por software basado en el análisis de aceleración tridimensional.
 *
 * Detecta picos en el módulo de la aceleración ($|a| = \sqrt{x^2 + y^2 + z^2}$) ajustados dinámicamente
 * a la gravedad. Cuenta un paso cuando la aceleración supera el umbral [threshold] $m/s^2$ por encima
 * de la línea base y respeta un intervalo mínimo de tiempo [minIntervalMs] para evitar falsos positivos.
 *
 * @property threshold Umbral de aceleración pico por encima de la gravedad (por defecto 1.2 m/s²).
 * @property minIntervalMs Tiempo mínimo en milisegundos entre dos pasos consecutivos (por defecto 300 ms).
 */
class StepDetector(
    private val threshold: Float = 1.2f,
    private val minIntervalMs: Long = 300
) {
    private var baseline: Float? = null
    private var aboveThreshold = false
    private var lastStepMs = Long.MIN_VALUE / 2

    /**
     * Procesa una lectura de aceleración en los ejes X, Y, Z.
     *
     * @param x Aceleración en el eje X en m/s².
     * @param y Aceleración en el eje Y en m/s².
     * @param z Aceleración en el eje Z en m/s².
     * @param timestampMs Marca de tiempo en milisegundos de la lectura.
     * @return `true` si la lectura actual completa la detección de un paso válido.
     */
    fun onAcceleration(x: Float, y: Float, z: Float, timestampMs: Long): Boolean {
        // 1. Calcular el módulo de la aceleración tridimensional
        val magnitude = sqrt(x * x + y * y + z * z)
        
        // 2. Actualizar la línea base (filtro de media móvil suave para seguir a la gravedad)
        val base = baseline?.let { it + 0.02f * (magnitude - it) } ?: magnitude
        baseline = base
        val delta = magnitude - base

        // 3. Evaluar el pico superando el umbral y verificar la ventana temporal
        if (!aboveThreshold && delta > threshold) {
            aboveThreshold = true
            if (timestampMs - lastStepMs >= minIntervalMs) {
                lastStepMs = timestampMs
                return true
            }
        } else if (aboveThreshold && delta < 0f) {
            aboveThreshold = false
        }
        return false
    }
}

/**
 * Algoritmo de fusión que combina las lecturas del podómetro del sistema con el detector por acelerómetro.
 *
 * El detector del sistema es más preciso pero suele presentar retardos iniciales (5-10 pasos).
 * La fusión permite contar pasos inmediatamente usando el acelerómetro hasta que el sensor del sistema
 * responde, descartando duplicados mediante ventanas temporales.
 *
 * @property systemSilenceMs Tiempo de silencio del sensor del sistema antes de permitir al acelerómetro contar (2000 ms).
 * @property minGapMs Intervalo mínimo permitido entre dos pasos contados independientemente de la fuente (250 ms).
 */
class StepFusion(
    private val systemSilenceMs: Long = 2_000,
    private val minGapMs: Long = 250
) {
    private var lastSystemStepMs: Long? = null
    private var lastCountedMs = Long.MIN_VALUE / 2

    /**
     * Procesa un paso notificado por el sensor del sistema ([android.hardware.Sensor.TYPE_STEP_DETECTOR]).
     *
     * @param timestampMs Marca de tiempo del evento en milisegundos.
     * @return `true` si el paso debe contabilizarse.
     */
    fun onSystemStep(timestampMs: Long): Boolean {
        lastSystemStepMs = maxOf(lastSystemStepMs ?: timestampMs, timestampMs)
        if (timestampMs < lastCountedMs + minGapMs) return false
        lastCountedMs = timestampMs
        return true
    }

    /**
     * Procesa un paso detectado mediante software por [StepDetector].
     *
     * @param timestampMs Marca de tiempo del evento en milisegundos.
     * @return `true` si el paso debe contabilizarse.
     */
    fun onAccelerometerStep(timestampMs: Long): Boolean {
        val lastSystem = lastSystemStepMs
        // Si el sensor del sistema estuvo activo recientemente, ignorar la lectura del acelerómetro para no duplicar
        if (lastSystem != null && timestampMs - lastSystem <= systemSilenceMs) return false
        if (timestampMs < lastCountedMs + minGapMs) return false
        lastCountedMs = timestampMs
        return true
    }
}
