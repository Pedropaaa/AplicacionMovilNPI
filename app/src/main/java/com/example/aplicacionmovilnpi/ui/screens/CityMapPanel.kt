package com.example.aplicacionmovilnpi.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.view.MotionEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aplicacionmovilnpi.location.CityPlace
import com.example.aplicacionmovilnpi.location.CityPlaces
import com.example.aplicacionmovilnpi.location.GeoPoint
import com.example.aplicacionmovilnpi.location.GeoUtils
import com.example.aplicacionmovilnpi.location.WalkingRoute
import com.example.aplicacionmovilnpi.location.WalkingRouteService
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.GreenStatus
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import kotlinx.coroutines.launch
import kotlin.math.abs
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import org.osmdroid.util.GeoPoint as OsmPoint

private const val ORIGIN_GPS = "gps"
private const val ORIGIN_MAP = "map"

private fun GeoPoint.toOsm() = OsmPoint(lat, lon)

/**
 * Teselas de OpenStreetMap servidas por FOSSGIS (los mismos que el servidor de rutas).
 * Usamos este servidor y no tile.openstreetmap.org porque el certificado de este último
 * fallaba en el emulador de Android 17 ("Chain validation failed").
 */
private val OsmTiles = XYTileSource(
    "OpenStreetMap.de", 0, 19, 256, ".png",
    arrayOf("https://tile.openstreetmap.de/"),
    "© OpenStreetMap contributors"
)

/**
 * Mapa de Granada (OpenStreetMap) con ruta a pie entre un origen y un destino.
 * El origen puede ser la ubicación GPS, un punto tocado en el mapa o uno de los campus.
 */
@Composable
fun CityRouteSection(
    gpsLocation: Location?,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onContinueIndoors: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var originId by rememberSaveable { mutableStateOf(if (hasLocationPermission) ORIGIN_GPS else "sciences") }
    var destId by rememberSaveable { mutableStateOf(CityPlaces.etsiit.id) }
    var mapOriginLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var mapOriginLon by rememberSaveable { mutableStateOf<Double?>(null) }
    var route by remember { mutableStateOf<WalkingRoute?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAllSteps by remember { mutableStateOf(false) }

    val gpsPoint = gpsLocation?.toGeoPoint()
    val mapOrigin = if (mapOriginLat != null && mapOriginLon != null) GeoPoint(mapOriginLat!!, mapOriginLon!!) else null
    val origin: GeoPoint? = when (originId) {
        ORIGIN_GPS -> gpsPoint
        ORIGIN_MAP -> mapOrigin
        else -> CityPlaces.byId(originId)?.point
    }
    val destination = CityPlaces.byId(destId) ?: CityPlaces.etsiit

    // Si cambian origen o destino, la ruta anterior deja de valer
    LaunchedEffect(originId, destId, mapOriginLat, mapOriginLon) {
        route = null
        error = null
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!hasLocationPermission) CityLocationPermissionButton(onRequestPermission)

        OsmMap(
            gpsPoint = gpsPoint,
            origin = origin.takeIf { originId != ORIGIN_GPS },
            destination = destination.point,
            destinationId = destination.id,
            originPlaceId = originId,
            route = route,
            onPlaceTap = { id ->
                destId = id
                if (originId == id) originId = if (hasLocationPermission) ORIGIN_GPS else ORIGIN_MAP
            },
            onMapTap = { point ->
                mapOriginLat = point.lat
                mapOriginLon = point.lon
                originId = ORIGIN_MAP
            },
            onLocateUnavailable = {
                if (hasLocationPermission) error = "Todavía no hay señal GPS. Activa la ubicación del móvil."
                else onRequestPermission()
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "🟢 Origen", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        PlaceChip("📍 Mi ubicación", selected = originId == ORIGIN_GPS) {
                            if (hasLocationPermission) originId = ORIGIN_GPS else onRequestPermission()
                        }
                    }
                    item {
                        PlaceChip("👆 Punto del mapa", selected = originId == ORIGIN_MAP) {
                            if (mapOrigin != null) originId = ORIGIN_MAP
                            else error = "Toca el mapa para elegir el punto de origen"
                        }
                    }
                    items(CityPlaces.all.filter { it.id != destId }) { place ->
                        PlaceChip(place.label, selected = originId == place.id) { originId = place.id }
                    }
                }

                Text(text = "🔴 Destino", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CityPlaces.all) { place ->
                        PlaceChip(place.label, selected = destId == place.id) {
                            destId = place.id
                            if (originId == place.id) originId = if (hasLocationPermission) ORIGIN_GPS else ORIGIN_MAP
                        }
                    }
                }

                if (originId == ORIGIN_GPS && gpsPoint == null) {
                    Text(
                        text = if (hasLocationPermission) "Buscando tu ubicación GPS…" else "Permite la ubicación para usarla como origen",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }

                Button(
                    onClick = {
                        val from = origin ?: return@Button
                        isLoading = true
                        error = null
                        scope.launch {
                            WalkingRouteService.fetch(from, destination.point)
                                .onSuccess { route = it; showAllSteps = false }
                                .onFailure { error = "No se pudo calcular la ruta. ¿Tienes conexión a internet?" }
                            isLoading = false
                        }
                    },
                    enabled = origin != null && !isLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isLoading) "Calculando ruta…" else "🚶 Calcular ruta a pie",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                error?.let { Text(text = it, fontSize = 11.sp, color = UgrRed) }
            }
        }

        route?.let { r ->
            RouteInstructionsCard(
                route = r,
                destinationName = destination.label,
                showAll = showAllSteps,
                onToggleShowAll = { showAllSteps = !showAllSteps },
                onContinueIndoors = onContinueIndoors.takeIf { destId == CityPlaces.etsiit.id }
            )
        }
    }
}

@Composable
private fun RouteInstructionsCard(
    route: WalkingRoute,
    destinationName: String,
    showAll: Boolean,
    onToggleShowAll: () -> Unit,
    onContinueIndoors: (() -> Unit)?
) {
    val visible = if (showAll) route.instructions else route.instructions.take(4)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, UgrRed)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "RUTA A PIE · ${route.summary.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = UgrRed)
            Text(text = "Hasta $destinationName", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            visible.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Text(text = "${index + 1}.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UgrRed, modifier = Modifier.width(24.dp))
                    Text(text = step.text, fontSize = 12.sp, color = Ink, modifier = Modifier.weight(1f))
                    if (step.distanceM >= 1) {
                        Text(text = GeoUtils.formatDistance(step.distanceM), fontSize = 11.sp, color = MutedText)
                    }
                }
            }

            if (route.instructions.size > 4) {
                Text(
                    text = if (showAll) "Ver menos ▲" else "Ver los ${route.instructions.size} pasos ▼",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgrRed,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable { onToggleShowAll() }
                )
            }

            if (onContinueIndoors != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onContinueIndoors,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenStatus),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🏢 Ya estoy en la ETSIIT ➔ Seguir en el plano interior",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) UgrRed else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Ink,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/** Vista de osmdroid dentro de Compose, con marcadores de origen, destino, tu posición y la ruta. */
@SuppressLint("ClickableViewAccessibility")
@Composable
private fun OsmMap(
    gpsPoint: GeoPoint?,
    origin: GeoPoint?,
    destination: GeoPoint,
    destinationId: String,
    originPlaceId: String,
    route: WalkingRoute?,
    onPlaceTap: (String) -> Unit,
    onMapTap: (GeoPoint) -> Unit,
    onLocateUnavailable: () -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Modo seguimiento: el mapa se mueve con tu posición hasta que lo arrastras con el dedo
    val following = remember { mutableStateOf(false) }

    val mapView = remember {
        Configuration.getInstance().apply {
            // La política de uso de los servidores de OSM exige identificar la app
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
        MapView(context).apply {
            setTileSource(OsmTiles)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            // Solo Granada: no se puede salir de la ciudad ni alejar más de lo que ocupa
            setScrollableAreaLimitDouble(
                BoundingBox(CityPlaces.GRANADA_NORTH, CityPlaces.GRANADA_EAST, CityPlaces.GRANADA_SOUTH, CityPlaces.GRANADA_WEST)
            )
            minZoomLevel = 12.5
            maxZoomLevel = 19.0
            controller.setZoom(13.5)
            // Centrado entre los campus, para que se vean todos al abrir el mapa
            controller.setCenter(
                OsmPoint(
                    CityPlaces.all.map { it.point.lat }.average(),
                    CityPlaces.all.map { it.point.lon }.average()
                )
            )
            // Que arrastrar el mapa mueva el mapa y no el scroll de la pantalla.
            // Arrastrar con un dedo desactiva el seguimiento; el zoom con dos dedos no.
            val touchSlop = android.view.ViewConfiguration.get(context).scaledTouchSlop
            var downX = 0f
            var downY = 0f
            setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        downX = event.x
                        downY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> if (
                        following.value && event.pointerCount == 1 &&
                        (abs(event.x - downX) > touchSlop || abs(event.y - downY) > touchSlop)
                    ) {
                        following.value = false
                    }
                }
                false
            }
        }
    }
    val currentOnMapTap = rememberUpdatedState(onMapTap)
    val currentOnPlaceTap = rememberUpdatedState(onPlaceTap)
    val tapOverlay = remember {
        MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: OsmPoint): Boolean {
                currentOnMapTap.value(GeoPoint(p.latitude, p.longitude))
                return true
            }

            override fun longPressHelper(p: OsmPoint): Boolean = false
        })
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Mientras se sigue, cada nueva posición GPS recoloca el mapa (sin tocar el zoom).
    // Solo depende de la posición: al pulsar el botón ya centra él mismo, con su zoom.
    LaunchedEffect(gpsPoint) {
        if (following.value && gpsPoint != null) mapView.controller.animateTo(gpsPoint.toOsm())
    }

    // Al llegar una ruta nueva, encuadramos el mapa para que se vea entera
    LaunchedEffect(route) {
        val points = route?.points?.map { it.toOsm() } ?: return@LaunchedEffect
        following.value = false
        if (points.size >= 2) {
            mapView.post { mapView.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(points), true, 80) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.overlays.clear()
                map.overlays.add(tapOverlay)

                route?.let { r ->
                    map.overlays.add(Polyline(map).apply {
                        setPoints(r.points.map { it.toOsm() })
                        outlinePaint.color = android.graphics.Color.rgb(0xD3, 0x2F, 0x2F)
                        outlinePaint.strokeWidth = 12f
                    })
                }
                // Todos los campus, siempre visibles con su nombre; tocar uno lo elige como destino
                CityPlaces.all.forEach { place ->
                    val color = when (place.id) {
                        destinationId -> android.graphics.Color.rgb(0xD3, 0x2F, 0x2F)
                        originPlaceId -> android.graphics.Color.rgb(0x2E, 0x7D, 0x32)
                        else -> android.graphics.Color.rgb(0x5E, 0x38, 0x50)
                    }
                    map.overlays.add(campusMarker(map, place, color) { currentOnPlaceTap.value(place.id) })
                }
                // Origen tocado en el mapa (los campus ya están marcados arriba)
                if (originPlaceId == ORIGIN_MAP) {
                    origin?.let { map.overlays.add(placeMarker(map, it, "Origen", android.graphics.Color.rgb(0x2E, 0x7D, 0x32))) }
                }
                gpsPoint?.let { map.overlays.add(placeMarker(map, it, "Tu ubicación", android.graphics.Color.rgb(0x1E, 0x88, 0xE5))) }

                map.overlays.add(CopyrightOverlay(map.context))
                map.invalidate()
            }
        )

        // Botón para centrar el mapa en tu posición y seguirla (azul mientras sigue)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(44.dp)
                .clickable {
                    if (gpsPoint != null) {
                        mapView.controller.animateTo(gpsPoint.toOsm(), 16.5, 600L)
                        following.value = true
                    } else {
                        onLocateUnavailable()
                    }
                },
            color = if (following.value) Color(0xFF1E88E5) else Color.White,
            shape = CircleShape,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "📍", fontSize = 20.sp)
            }
        }
    }
}

/** Marcador de campus: punto de color con el nombre escrito debajo, para leerlo sin tocarlo. */
private fun campusMarker(map: MapView, place: CityPlace, color: Int, onClick: () -> Unit): Marker =
    Marker(map).apply {
        position = place.point.toOsm()
        title = place.name
        snippet = place.campus
        val density = map.context.resources.displayMetrics.density
        icon = BitmapDrawable(map.context.resources, labeledDot(place.name, color, density))
        // El punto queda arriba del dibujo, con el texto debajo
        setAnchor(Marker.ANCHOR_CENTER, 12f * density / icon.intrinsicHeight)
        setOnMarkerClickListener { marker, _ ->
            onClick()
            marker.showInfoWindow()
            true
        }
    }

private fun labeledDot(text: String, color: Int, density: Float): Bitmap {
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.rgb(0x1F, 0x1A, 0x1D)
        textSize = 13 * density
        typeface = Typeface.DEFAULT_BOLD
    }
    val textWidth = textPaint.measureText(text)
    val padding = 6 * density
    val width = (textWidth + 2 * padding).toInt().coerceAtLeast((24 * density).toInt())
    val height = (24 * density + 20 * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = width / 2f
    val cy = 12 * density
    canvas.drawCircle(cx, cy, 11 * density, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE })
    canvas.drawCircle(cx, cy, 8 * density, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    // Fondo blanco semitransparente para que el nombre se lea sobre las calles
    val labelTop = 25 * density
    canvas.drawRoundRect(
        RectF(cx - textWidth / 2 - padding, labelTop, cx + textWidth / 2 + padding, labelTop + 18 * density),
        6 * density, 6 * density,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.argb(220, 255, 255, 255) }
    )
    canvas.drawText(text, cx - textWidth / 2, labelTop + 13.5f * density, textPaint)
    return bitmap
}

/** Marcador redondo de color con borde blanco (sin recursos gráficos aparte). */
private fun placeMarker(map: MapView, point: GeoPoint, title: String, color: Int): Marker =
    Marker(map).apply {
        position = point.toOsm()
        this.title = title
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        val density = map.context.resources.displayMetrics.density
        icon = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke((3 * density).toInt(), android.graphics.Color.WHITE)
            setSize((20 * density).toInt(), (20 * density).toInt())
        }
    }

/** Botón para pedir la ubicación desde el mapa de la ciudad. */
@Composable
private fun CityLocationPermissionButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = "📍 Permitir ubicación para ver dónde estás", fontSize = 12.sp, color = Ink)
    }
}
