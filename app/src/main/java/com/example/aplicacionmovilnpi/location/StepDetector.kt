package com.example.aplicacionmovilnpi.location

import kotlin.math.sqrt

/**
 * Podómetro a partir del acelerómetro, para móviles sin sensor de pasos o sin permiso de
 * actividad física. Cada paso produce un pico en el módulo de la aceleración: se cuenta un
 * paso cuando el módulo supera la gravedad en [threshold] m/s², y no se cuenta otro hasta
 * que vuelve a bajar y han pasado al menos [minIntervalMs].
 */
class StepDetector(
    private val threshold: Float = 1.6f,
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
