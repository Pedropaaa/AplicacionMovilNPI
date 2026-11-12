package com.example.aplicacionmovilnpi.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class ProximitySensor(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    // Callback de cambio de estado: true = CERCA, false = LEJOS
    var onProximityChanged: ((Boolean) -> Unit)? = null

    private var lastStateIsNear: Boolean? = null

    fun start() {
        if (proximitySensor == null) {
            Log.e("PROXIMIDAD", "El dispositivo no cuenta con sensor de proximidad.")
            return
        }

        // Registro del sensor estándar de Android (escucha eventos sin apagar la pantalla)
        val registered = sensorManager.registerListener(
            this,
            proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        Log.d("PROXIMIDAD", "Registro de sensor de proximidad: $registered")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        lastStateIsNear = null
        Log.d("PROXIMIDAD", "Sensor de proximidad detenido")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_PROXIMITY) return

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
