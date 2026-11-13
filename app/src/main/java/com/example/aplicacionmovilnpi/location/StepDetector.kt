package com.example.aplicacionmovilnpi.location

import kotlin.math.sqrt

/**
 * Podómetro a partir del acelerómetro. Cada paso produce un pico en el módulo de la
 * aceleración: se cuenta un paso cuando el módulo supera la gravedad en [threshold] m/s², y
 * no se cuenta otro hasta que vuelve a bajar y han pasado al menos [minIntervalMs].
 * El umbral es bajo para que también cuente con el móvil en la mano (picos más suaves que en
 * el bolsillo).
 */
class StepDetector(
    private val threshold: Float = 1.2f,
    private val minIntervalMs: Long = 300
) {
    private var baseline: Float? = null
    private var aboveThreshold = false
    private var lastStepMs = Long.MIN_VALUE / 2

    /** Devuelve true si esta muestra completa un paso. */
    fun onAcceleration(x: Float, y: Float, z: Float, timestampMs: Long): Boolean {
        val magnitude = sqrt(x * x + y * y + z * z)
        // La línea base sigue muy despacio a la gravedad, para que los picos de cada paso destaquen
        val base = baseline?.let { it + 0.02f * (magnitude - it) } ?: magnitude
        baseline = base
        val delta = magnitude - base

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
 * Combina el detector de pasos del sistema con el del acelerómetro sin contar dos veces.
 *
 * El del sistema es más fiable, pero muchos móviles no dan el primer paso hasta confirmar que
 * la persona camina (5–10 pasos), los entregan en bloque con retraso, o directamente no
 * mandan nada aunque anuncien el sensor. Por eso:
 * - El acelerómetro cuenta mientras el sistema lleve más de [systemSilenceMs] sin dar pasos,
 *   así los primeros pasos se cuentan al instante y, si el sistema no responde, sigue contando.
 * - Un paso del sistema solo cuenta si es posterior al último paso ya contado; los que llegan
 *   tarde, de un periodo que ya contó el acelerómetro, se descartan.
 *
 * Los instantes son los del sensor (`event.timestamp` en ms); ambos sensores usan el mismo reloj.
 */
class StepFusion(
    private val systemSilenceMs: Long = 2_000,
    private val minGapMs: Long = 250
) {
    private var lastSystemStepMs: Long? = null
    private var lastCountedMs = Long.MIN_VALUE / 2

    /** Paso del detector del sistema. Devuelve true si hay que contarlo. */
    fun onSystemStep(timestampMs: Long): Boolean {
        lastSystemStepMs = maxOf(lastSystemStepMs ?: timestampMs, timestampMs)
        if (timestampMs < lastCountedMs + minGapMs) return false
        lastCountedMs = timestampMs
        return true
    }

    /** Paso detectado con el acelerómetro. Devuelve true si hay que contarlo. */
    fun onAccelerometerStep(timestampMs: Long): Boolean {
        val lastSystem = lastSystemStepMs
        if (lastSystem != null && timestampMs - lastSystem <= systemSilenceMs) return false
        if (timestampMs < lastCountedMs + minGapMs) return false
        lastCountedMs = timestampMs
        return true
    }
}
