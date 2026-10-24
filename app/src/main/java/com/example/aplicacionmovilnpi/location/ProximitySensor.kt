package com.example.aplicacionmovilnpi.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.util.Log

class ProximitySensor(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null

    // Callback de cambio de estado: true = CERCA, false = LEJOS
    var onProximityChanged: ((Boolean) -> Unit)? = null

    private var lastStateIsNear: Boolean? = null

    fun start() {
        if (proximitySensor == null) {
            Log.e("PROXIMIDAD", "El dispositivo no cuenta con sensor de proximidad.")
            return
        }

        // 1. Intentar configurar WakeLock si el sistema lo admite (necesario en sensores virtuales)
        try {
            if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                if (wakeLock == null) {
                    @Suppress("DEPRECATION")
                    wakeLock = powerManager.newWakeLock(
                        PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                        "AplicacionMovilNPI:ProximitySensorWakeLock"
                    )
                }
                if (wakeLock?.isHeld == false) {
                    wakeLock?.acquire(10 * 60 * 1000L /* 10 minutos */)
                }
            }
        } catch (e: Exception) {
            Log.w("PROXIMIDAD", "No se pudo adquirir WakeLock de proximidad: ${e.message}")
        }

        // 2. Registro del sensor estándar de Android
        val registered = sensorManager.registerListener(
            this,
            proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        Log.d("PROXIMIDAD", "Registro de sensor de proximidad: $registered")
    }

    fun stop() {
        sensorManager.unregisterListener(this)

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
            }
        } catch (e: Exception) {
            Log.w("PROXIMIDAD", "Error liberando WakeLock: ${e.message}")
        }
        wakeLock = null
        lastStateIsNear = null

        Log.d("PROXIMIDAD", "Sensor de proximidad detenido")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return

        val distance = event.values[0]
        val maxRange = proximitySensor?.maximumRange ?: 5.0f

        // En el estándar de Android: si el valor devuelto es estrictamente menor al rango máximo,
        // o si equivale a 0, el objeto se considera CERCA.
        val isNear = distance < maxRange

        if (lastStateIsNear != isNear) {
            lastStateIsNear = isNear
            Log.d("PROXIMIDAD", "Distancia: $distance cm | Rango máx: $maxRange cm")
            Log.d("PROXIMIDAD", if (isNear) "CERCA" else "LEJOS")
            onProximityChanged?.invoke(isNear)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se requiere acción adicional
    }
}