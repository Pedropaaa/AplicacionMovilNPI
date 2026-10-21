package com.example.aplicacionmovilnpi.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/*
 * Acceso a los sensores desde Compose. Cada función registra su listener solo mientras
 * `active` es true, y se da de baja al salir de la pantalla, para no gastar batería.
 */

object LocationPermissions {

    val location = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    /** Permisos que pedimos juntos al activar la localización. */
    val all: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) location + Manifest.permission.ACTIVITY_RECOGNITION
        else location

    fun hasLocation(context: Context): Boolean = location.any { granted(context, it) }

    /** El sensor de pasos del sistema necesita el permiso de actividad física desde Android 10. */
    fun hasActivityRecognition(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || granted(context, Manifest.permission.ACTIVITY_RECOGNITION)

    private fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun hasSensor(context: Context, type: Int): Boolean =
    (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(type) != null

/** true mientras la app está en primer plano. */
@Composable
fun rememberAppInForeground(): Boolean {
    val state by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    return state.isAtLeast(Lifecycle.State.STARTED)
}

/** Última ubicación del GPS (o de la red, si el GPS aún no tiene señal). */
@SuppressLint("MissingPermission")
@Composable
fun rememberGpsLocation(active: Boolean): Location? {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }

    DisposableEffect(active) {
        if (!active || !LocationPermissions.hasLocation(context)) return@DisposableEffect onDispose { }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

        location = providers
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        // Implementación completa (no lambda): en Android < 11 los otros métodos son abstractos
        val listener = object : LocationListener {
            override fun onLocationChanged(newLocation: Location) {
                val current = location
                // Preferimos el GPS: no sustituimos un fijo preciso reciente por uno de red peor
                if (current == null || newLocation.accuracy <= current.accuracy ||
                    newLocation.time - current.time > 10_000
                ) {
                    location = newLocation
                }
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        providers.forEach {
            runCatching { manager.requestLocationUpdates(it, 2000L, 1f, listener, Looper.getMainLooper()) }
        }
        onDispose { manager.removeUpdates(listener) }
    }
    return location
}

/** Registra un listener de un tipo de sensor mientras [active] sea true. */
@Composable
private fun SensorEffect(
    active: Boolean,
    sensorTypes: List<Int>,
    delay: Int = SensorManager.SENSOR_DELAY_UI,
    onEvent: (SensorEvent) -> Unit
) {
    val context = LocalContext.current
    val currentOnEvent by rememberUpdatedState(onEvent)

    DisposableEffect(active, sensorTypes) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorTypes.mapNotNull { manager.getDefaultSensor(it) }
        if (!active || sensors.size != sensorTypes.size) return@DisposableEffect onDispose { }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) = currentOnEvent(event)
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensors.forEach { manager.registerListener(listener, it, delay) }
        onDispose { manager.unregisterListener(listener) }
    }
}

/** Lecturas del barómetro: presión en hPa e instante en ms. No hace nada si el móvil no tiene barómetro. */
@Composable
fun PressureEffect(active: Boolean, onPressure: (hPa: Float, timestampMs: Long) -> Unit) {
    SensorEffect(active, listOf(Sensor.TYPE_PRESSURE)) { onPressure(it.values[0], it.timestamp / 1_000_000) }
}

/**
 * Brújula: azimut en grados (0 = norte, 90 = este) con el móvil en vertical.
 * Usa el vector de rotación si existe; si no, acelerómetro + magnetómetro.
 */
@Composable
fun rememberCompassAzimuth(active: Boolean): Float? {
    val context = LocalContext.current
    val useRotationVector = remember { hasSensor(context, Sensor.TYPE_ROTATION_VECTOR) }
    var azimuth by remember { mutableStateOf<Float?>(null) }

    val rotation = remember { FloatArray(9) }
    val orientation = remember { FloatArray(3) }
    val gravity = remember { FloatArray(3) }
    val geomagnetic = remember { FloatArray(3) }
    val hasReadings = remember { BooleanArray(2) } // [acelerómetro, magnetómetro]

    // Media circular (seno y coseno) para que el valor no salte al pasar de 359° a 0°
    fun update(newDeg: Float) {
        val previous = azimuth ?: run { azimuth = newDeg; return }
        val a = Math.toRadians(previous.toDouble())
        val b = Math.toRadians(newDeg.toDouble())
        val k = 0.25
        val s = (1 - k) * sin(a) + k * sin(b)
        val c = (1 - k) * cos(a) + k * cos(b)
        val smoothed = DeadReckoning.normalize(Math.toDegrees(atan2(s, c)).toFloat())
        // Solo actualizamos el estado (y recomponemos) cuando el cambio se nota
        if (abs(DeadReckoning.signedDifference(previous, smoothed)) >= 1f) azimuth = smoothed
    }

    if (useRotationVector) {
        SensorEffect(active, listOf(Sensor.TYPE_ROTATION_VECTOR)) { event ->
            SensorManager.getRotationMatrixFromVector(rotation, event.values)
            SensorManager.getOrientation(rotation, orientation)
            update(Math.toDegrees(orientation[0].toDouble()).toFloat())
        }
    } else {
        SensorEffect(active, listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD)) { event ->
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> { event.values.copyInto(gravity, endIndex = 3); hasReadings[0] = true }
                Sensor.TYPE_MAGNETIC_FIELD -> { event.values.copyInto(geomagnetic, endIndex = 3); hasReadings[1] = true }
            }
            if (hasReadings[0] && hasReadings[1] &&
                SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic)
            ) {
                SensorManager.getOrientation(rotation, orientation)
                update(Math.toDegrees(orientation[0].toDouble()).toFloat())
            }
        }
    }
    return azimuth
}

/**
 * Llama a [onStep] por cada paso. Usa el sensor de pasos del sistema si existe y hay permiso
 * de actividad física; si no, detecta los pasos con el acelerómetro ([StepDetector]).
 */
@Composable
fun StepEffect(active: Boolean, onStep: () -> Unit) {
    val context = LocalContext.current
    val useSystemDetector = hasSensor(context, Sensor.TYPE_STEP_DETECTOR) &&
            LocationPermissions.hasActivityRecognition(context)
    val detector = remember { StepDetector() }

    SensorEffect(active && useSystemDetector, listOf(Sensor.TYPE_STEP_DETECTOR)) { onStep() }
    SensorEffect(
        active && !useSystemDetector,
        listOf(Sensor.TYPE_ACCELEROMETER),
        delay = SensorManager.SENSOR_DELAY_GAME
    ) { event ->
        if (detector.onAcceleration(event.values[0], event.values[1], event.values[2], event.timestamp / 1_000_000)) {
            onStep()
        }
    }
}
