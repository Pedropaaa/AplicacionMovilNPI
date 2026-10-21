package com.example.aplicacionmovilnpi.ui.screens

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.location.DeadReckoning
import com.example.aplicacionmovilnpi.location.GeoPoint
import com.example.aplicacionmovilnpi.location.GeoUtils
import com.example.aplicacionmovilnpi.location.IndoorTracker
import com.example.aplicacionmovilnpi.location.MapPoint
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.GreenStatus
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import kotlin.math.roundToInt

private val UserBlue = Color(0xFF1E88E5)

fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

/**
 * Tarjeta con lo que dicen los sensores: GPS (distancia a la ETSIIT), barómetro (planta),
 * brújula y pasos. Desde aquí se dan los permisos, se calibra y se activa el seguimiento.
 */
@Composable
fun LocationStatusCard(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    gpsLocation: Location?,
    hasBarometer: Boolean,
    tracker: IndoorTracker,
    azimuthDeg: Float?,
    selectedFloorName: String,
    onCalibrateFloor: () -> Unit,
    isRouteOpen: Boolean,
    isTracking: Boolean,
    onTrackingChange: (Boolean) -> Unit,
    onAlignCompass: () -> Unit,
    onUseEntranceAsStart: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "📡 Tu ubicación", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)

            // --- GPS ---
            when {
                !hasLocationPermission -> {
                    SensorLine("📍", "Activa la ubicación para saber a qué distancia estás de la ETSIIT y contar tus pasos.")
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Permitir ubicación", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                gpsLocation == null -> SensorLine("📍", "Buscando señal GPS… (activa la ubicación del móvil)")
                else -> {
                    val here = gpsLocation.toGeoPoint()
                    val accuracy = "±${gpsLocation.accuracy.roundToInt()} m"
                    if (GeoUtils.isAtEtsiit(here)) {
                        SensorLine("📍", "Estás en la ETSIIT ($accuracy). Dentro del edificio te guían el barómetro y los pasos.")
                        if (onUseEntranceAsStart != null) {
                            SmallAction("Empezar la ruta en la entrada principal", onUseEntranceAsStart)
                        }
                    } else {
                        val distance = GeoUtils.formatDistance(GeoUtils.distanceMeters(here, GeoUtils.ETSIIT))
                        SensorLine("📍", "Estás a $distance de la ETSIIT ($accuracy)")
                    }
                }
            }

            // --- Barómetro ---
            if (hasBarometer) {
                val floor = tracker.estimatedFloor
                val pressure = tracker.pressureHpa?.let { "%.2f hPa".format(it).replace(".", ",") } ?: "…"
                val altitude = tracker.relativeAltitudeM?.let { " · %+.1f m".format(it).replace(".", ",") } ?: ""
                SensorLine(
                    "🏢",
                    if (floor != null) "Planta estimada: ${getFloorName(floor)} ($pressure$altitude)"
                    else "Barómetro listo ($pressure). Indica en qué planta estás para calibrarlo."
                )
                SmallAction("Estoy en la planta $selectedFloorName", onCalibrateFloor)
            } else {
                SensorLine("🏢", "Este móvil no tiene barómetro: cambia de planta con los botones del plano.")
            }

            // --- Brújula y pasos ---
            SensorLine("🧭", azimuthDeg?.let { "Rumbo: ${DeadReckoning.compassLabel(it)}" } ?: "Brújula no disponible")

            if (isRouteOpen) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Seguir mis pasos en el plano", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(
                            text = if (isTracking) "👣 ${tracker.steps} pasos desde el inicio" else "Usa el podómetro y la brújula",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                    Switch(
                        checked = isTracking,
                        onCheckedChange = onTrackingChange,
                        enabled = azimuthDeg != null,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = UgrRed)
                    )
                }
                if (isTracking) {
                    SmallAction("🧭 Calibrar brújula (mira hacia donde va la ruta)", onAlignCompass)
                }
            } else {
                SensorLine("👣", "Calcula una ruta para seguir tus pasos en el plano.")
            }
        }
    }
}

/** Indicación de giro y distancia, para mostrar justo debajo del plano mientras se sigue la ruta. */
@Composable
fun NavigationHintStrip(tracker: IndoorTracker, azimuthDeg: Float?, arrivalText: String) {
    val progress = tracker.progress ?: return
    val target = tracker.targetAzimuth()

    val (icon, text) = when {
        progress.hasArrived -> "✅" to arrivalText
        azimuthDeg == null || target == null -> "👣" to "Faltan ${progress.metersToEnd.roundToInt()} m"
        else -> "🧭" to "${DeadReckoning.turnInstruction(azimuthDeg, target)} · faltan ${progress.metersToEnd.roundToInt()} m"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (progress.hasArrived) GreenStatus.copy(alpha = 0.12f) else Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
    }
}

@Composable
private fun SensorLine(icon: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = icon, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = Ink)
    }
}

@Composable
private fun SmallAction(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = Ink)
    }
}

/**
 * Punto azul con un cono hacia donde mira el móvil.
 * [headingMapDeg] es la dirección en el plano (0 = arriba); null si no hay brújula.
 */
fun DrawScope.drawUserMarker(position: MapPoint, headingMapDeg: Float?, scaleX: Float, scaleY: Float) {
    val center = Offset(position.x * scaleX, position.y * scaleY)
    if (headingMapDeg != null) {
        rotate(degrees = headingMapDeg, pivot = center) {
            val cone = Path().apply {
                moveTo(center.x, center.y - 24.dp.toPx())
                lineTo(center.x - 10.dp.toPx(), center.y - 4.dp.toPx())
                lineTo(center.x + 10.dp.toPx(), center.y - 4.dp.toPx())
                close()
            }
            drawPath(cone, color = UserBlue.copy(alpha = 0.45f))
        }
    }
    drawCircle(color = UserBlue.copy(alpha = 0.2f), radius = 16.dp.toPx(), center = center)
    drawCircle(color = Color.White, radius = 8.dp.toPx(), center = center)
    drawCircle(color = UserBlue, radius = 6.dp.toPx(), center = center)
}

/** Texto de llegada al final de un tramo: escaleras/ascensor o destino final. */
fun arrivalText(segment: RouteFloorSegment, destName: String): String =
    if (segment.nextFloorName != null) {
        val via = if (segment.isElevator) "al ascensor" else "a las escaleras"
        "Has llegado $via: ve a la planta ${segment.nextFloorName}"
    } else {
        "Has llegado a $destName"
    }
