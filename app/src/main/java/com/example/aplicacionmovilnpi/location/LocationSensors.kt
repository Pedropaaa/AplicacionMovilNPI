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

/**
 * Utilidades para la gestión de permisos del sistema Android requeridos por el sistema de localización.
 *
 * Handles tanto los permisos de ubicación GPS fina/coarse como los de reconocimiento de actividad física (podómetro).
 *
 * @property location Array con los permisos de ubicación GPS fina y aproximada.
 * @property all Array completo de permisos necesarios según la versión de Android.
 */
object LocationPermissions {

    /** Array con los permisos de ubicación GPS fina y aproximada. */
    val location = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    /** Permisos solicitados conjuntamente al activar la localización según la versión del SDK. */
    val all: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) location + Manifest.permission.ACTIVITY_RECOGNITION
        else location

    /**
     * Comprueba si la aplicación dispone de permisos de localización concedidos.
     *
     * @param context Contexto de la aplicación.
     * @return `true` si al menos uno de los permisos de localización está concedido.
     */
    fun hasLocation(context: Context): Boolean = location.any { granted(context, it) }

    /**
     * Comprueba si la aplicación dispone de permiso para utilizar el sensor de reconocimiento de actividad del sistema.
     *
     * @param context Contexto de la aplicación.
     * @return `true` si el permiso está concedido o si la versión de Android es inferior a Android 10 (Q).
     */
    fun hasActivityRecognition(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || granted(context, Manifest.permission.ACTIVITY_RECOGNITION)

    /**
     * Comprueba si un permiso específico del sistema está concedido.
     *
     * @param context Contexto de la aplicación.
     * @param permission Nombre del permiso a comprobar (constante de [Manifest.permission]).
     * @return `true` si el permiso ha sido concedido.
     */
    private fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Comprueba la disponibilidad física de un tipo de sensor en el hardware del dispositivo.
 *
 * @param context Contexto de la aplicación.
 * @param type Constante entera que identifica el tipo de sensor en [SensorManager].
 * @return `true` si el dispositivo cuenta con el sensor solicitado.
 */
fun hasSensor(context: Context, type: Int): Boolean =
    (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(type) != null

/**
 * Composable que observa el ciclo de vida de la aplicación para determinar si está en primer plano.
 *
 * @return `true` si la actividad se encuentra al menos en estado [Lifecycle.State.STARTED].
 */
@Composable
fun rememberAppInForeground(): Boolean {
    val state by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    return state.isAtLeast(Lifecycle.State.STARTED)
}

/**
 * Composable que registra y observa la última posición física reportada por el GPS o red celular.
 *
 * Gestione la suscripción dinámica al servicio de localización [LocationManager] mientras [active] sea `true`.
 *
 * @param active Si es `true`, activa las lecturas de localización GPS/Red.
 * @return Instancia actual de [Location] con las coordenadas de la persona o `null` si no hay señal.
 */
@SuppressLint("MissingPermission")
@Composable
fun rememberGpsLocation(active: Boolean): Location? {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }

    DisposableEffect(active) {
        // 1. Verificar si la función está activa y se concedieron los permisos de localización
        if (!active || !LocationPermissions.hasLocation(context)) return@DisposableEffect onDispose { }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // 2. Obtener los proveedores habilitados (GPS y Red)
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

        // 3. Recuperar la última posición conocida más reciente entre los proveedores
        location = providers
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        // 4. Registrar escuchador de actualizaciones continuas (LocationListener)
        val listener = object : LocationListener {
            override fun onLocationChanged(newLocation: Location) {
                val current = location
                // Preferir fijado GPS más preciso y reciente frente a lecturas de red con mayor margen de error
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

/**
 * Función de utilidad interna para suscribirse a eventos de sensores en Compose respetando el ciclo de vida.
 *
 * @param active Controla si la escucha está activa.
 * @param sensorTypes Lista de constantes de tipos de sensores en [SensorManager].
 * @param delay Frecuencia del refresco del sensor (por defecto [SensorManager.SENSOR_DELAY_UI]).
 * @param onEvent Callback invocado cuando se produce un nuevo evento [SensorEvent].
 */
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

/**
 * Composable que calcula y retorna el azimut de la brújula en grados decimales (0° = Norte, 90° = Este).
 *
 * Utiliza el sensor de vector de rotación ([Sensor.TYPE_ROTATION_VECTOR]) si está disponible en el dispositivo;
 * de lo contrario, realiza la fusión de acelerómetro y magnetómetro. Aplica filtrado mediante media circular
 * para evitar discontinuidades al cruzar el límite de 359° a 0°.
 *
 * @param active Determina si se deben leer los sensores físicos.
 * @return Grados del rumbo respecto al Norte magnético [0..360), o `null` si los sensores no están disponibles.
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

    // Media circular (seno y coseno) para evitar saltos bruscos en el cruce de 359° a 0°
    fun update(newDeg: Float) {
        val previous = azimuth ?: run { azimuth = newDeg; return }
        val a = Math.toRadians(previous.toDouble())
        val b = Math.toRadians(newDeg.toDouble())
        val k = 0.1
        val s = (1 - k) * sin(a) + k * sin(b)
        val c = (1 - k) * cos(a) + k * cos(b)
        val smoothed = DeadReckoning.normalize(Math.toDegrees(atan2(s, c)).toFloat())
        
        // Actualizar el estado únicamente cuando el cambio supera 1° para minimizar recomposiciones
        if (abs(DeadReckoning.signedDifference(previous, smoothed)) >= 1f) azimuth = smoothed
    }

    if (useRotationVector) {
        // 1. Usar vector de rotación si existe en el hardware del teléfono
        SensorEffect(active, listOf(Sensor.TYPE_ROTATION_VECTOR)) { event ->
            SensorManager.getRotationMatrixFromVector(rotation, event.values)
            SensorManager.getOrientation(rotation, orientation)
            update(Math.toDegrees(orientation[0].toDouble()).toFloat())
        }
    } else {
        // 2. Fusión alternativa de acelerómetro + campo magnético
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
 * Escucha los eventos de pasos y ejecuta el callback [onStep] por cada paso detectado.
 *
 * Utiliza una estrategia híbrida con [StepFusion]: combina el sensor del sistema ([Sensor.TYPE_STEP_DETECTOR])
 * con la detección directa por acelerómetro ([StepDetector]), garantizando que los pasos se cuenten
 * inmediatamente y evitando duplicidades.
 *
 * @param active Controla la suscripción activa a los sensores.
 * @param onStep Lambda ejecutada cada vez que se detecta un paso válido del usuario.
 */
@Composable
fun StepEffect(active: Boolean, onStep: () -> Unit) {
    val context = LocalContext.current
    val canUseSystemDetector = hasSensor(context, Sensor.TYPE_STEP_DETECTOR) &&
            LocationPermissions.hasActivityRecognition(context)
    val detector = remember { StepDetector() }
    val fusion = remember { StepFusion() }

    // 1. Sensor de pasos del sistema (si está disponible y permitido)
    SensorEffect(active && canUseSystemDetector, listOf(Sensor.TYPE_STEP_DETECTOR)) { event ->
        if (fusion.onSystemStep(event.timestamp / 1_000_000)) onStep()
    }
    
    // 2. Sensor de acelerómetro como respaldo e inicio rápido
    SensorEffect(
        active,
        listOf(Sensor.TYPE_ACCELEROMETER),
        delay = SensorManager.SENSOR_DELAY_GAME
    ) { event ->
        val t = event.timestamp / 1_000_000
        if (detector.onAcceleration(event.values[0], event.values[1], event.values[2], t) &&
            fusion.onAccelerometerStep(t)
        ) {
            onStep()
        }
    }
}
