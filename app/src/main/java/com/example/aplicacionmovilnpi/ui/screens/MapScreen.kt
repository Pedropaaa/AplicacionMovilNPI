
package com.example.aplicacionmovilnpi.ui.screens

import android.graphics.BitmapFactory
import android.hardware.Sensor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aplicacionmovilnpi.data.CampusItem
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.location.IndoorTracker
import com.example.aplicacionmovilnpi.location.LocationPermissions
import com.example.aplicacionmovilnpi.location.LocationPrefs
import com.example.aplicacionmovilnpi.location.MapPoint
import com.example.aplicacionmovilnpi.location.PressureEffect
import com.example.aplicacionmovilnpi.location.StepEffect
import com.example.aplicacionmovilnpi.location.hasSensor
import com.example.aplicacionmovilnpi.location.rememberAppInForeground
import com.example.aplicacionmovilnpi.location.rememberCompassAzimuth
import com.example.aplicacionmovilnpi.location.rememberGpsLocation
import com.example.aplicacionmovilnpi.model.EdgeType
import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest
import com.example.aplicacionmovilnpi.navigation.EtsiitData
import com.example.aplicacionmovilnpi.navigation.RouteManager
import com.example.aplicacionmovilnpi.qr.QrLocationScannerScreen
import com.example.aplicacionmovilnpi.qr.QrScanResult
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.GreenStatus
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import kotlinx.coroutines.launch
import android.location.Location
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.CameraAlt
import com.example.aplicacionmovilnpi.location.GeoPoint
import com.example.aplicacionmovilnpi.location.GeoUtils

fun getFloorName(floor: Int): String = when (floor) {
    4 -> "P4"
    3 -> "P3"
    2 -> "P2"
    1 -> "P1"
    0 -> "PB"
    -1 -> "S1"
    else -> "P$floor"
}

private const val ETSIIT_LAT = 37.19722
private const val ETSIIT_LON = -3.62389

// Radio de detección: 250 metros
private const val ETSIIT_RADIUS = 250f

private fun isNearEtsiit(location: Location): Boolean {
    val distance = FloatArray(1)

    Location.distanceBetween(
        location.latitude,
        location.longitude,
        ETSIIT_LAT,
        ETSIIT_LON,
        distance
    )

    return distance[0] <= ETSIIT_RADIUS
}

data class RouteFloorSegment(
    val floorNumber: Int,
    val floorName: String,
    val segmentNodes: List<Node>,
    val transitionNode: Node?,
    val nextFloorNumber: Int?,
    val nextFloorName: String?,
    val isElevator: Boolean
)

fun parseRouteSegments(route: List<Node>): List<RouteFloorSegment> {
    if (route.isEmpty()) return emptyList()

    val segments = mutableListOf<RouteFloorSegment>()
    var currentFloorNodes = mutableListOf<Node>()
    var currentFloor = route.first().floor

    for (i in route.indices) {
        val currNode = route[i]

        if (currNode.floor == currentFloor) {
            currentFloorNodes.add(currNode)
        } else {
            val transitionNode = currentFloorNodes.lastOrNull()
            val nextFloor = currNode.floor

            val edge = EtsiitData.edges.find {
                (it.from == transitionNode?.id && it.to == currNode.id) ||
                        (it.from == currNode.id && it.to == transitionNode?.id)
            }
            val isElevator = (edge?.type == EdgeType.ELEVATOR) ||
                    (transitionNode?.id?.contains("ascensor", ignoreCase = true) == true) ||
                    (currNode.id.contains("ascensor", ignoreCase = true))

            segments.add(
                RouteFloorSegment(
                    floorNumber = currentFloor,
                    floorName = getFloorName(currentFloor),
                    segmentNodes = ArrayList(currentFloorNodes),
                    transitionNode = transitionNode,
                    nextFloorNumber = nextFloor,
                    nextFloorName = getFloorName(nextFloor),
                    isElevator = isElevator
                )
            )

            currentFloor = currNode.floor
            currentFloorNodes = mutableListOf(currNode)
        }
    }

    if (currentFloorNodes.isNotEmpty()) {
        segments.add(
            RouteFloorSegment(
                floorNumber = currentFloor,
                floorName = getFloorName(currentFloor),
                segmentNodes = ArrayList(currentFloorNodes),
                transitionNode = null,
                nextFloorNumber = null,
                nextFloorName = null,
                isElevator = false
            )
        )
    }

    return segments
}

private const val DEST_ASEO_CERCANO = "__dest_aseo_cercano__"
private const val DEST_FUENTE_CERCANA = "__dest_fuente_cercana__"
private const val DEST_COMEDOR_CERCANO = "__dest_comedor_cercano__"

private data class DestinationResolution(
    val poi: PointOfInterest?,
    val route: List<Node>? = null
)

private fun normalizePoiText(value: String): String =
    value.lowercase(java.util.Locale.ROOT)
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace("ü", "u")

private fun PointOfInterest.isAseo(): Boolean {
    val text = normalizePoiText("$nameEs $category")

    return listOf(
        "aseo", "bano", "wc", "toilet", "restroom"
    ).any { text.contains(it) }
}

private fun PointOfInterest.isFuente(): Boolean {
    val text = normalizePoiText("$nameEs $category")

    return listOf(
        "fuente", "fontana", "water fountain"
    ).any { text.contains(it) }
}

/**
 * Busca el POI alcanzable cuya ruta tiene menos nodos.
 */
private fun resolveNearestPoi(
    startPoiId: String,
    candidates: List<PointOfInterest>,
    routeManager: RouteManager,
    isAccessibleRoute: Boolean
): DestinationResolution {

    val possibleRoutes = candidates.mapNotNull { poi ->

        val route = if (poi.id == startPoiId) {
            EtsiitData.nodes[poi.nodeId]?.let { listOf(it) } ?: emptyList()
        } else {
            routeManager.calculateRoute(
                startPoiId,
                poi.id,
                isAccessibleRoute
            )
        }

        if (route.isNotEmpty()) {
            poi to route
        } else {
            null
        }
    }

    val nearest = possibleRoutes.minByOrNull { (_, route) ->
        route.size
    }

    return nearest?.let { (poi, route) ->
        DestinationResolution(
            poi = poi,
            route = route
        )
    } ?: DestinationResolution(poi = null)
}

@Composable
fun MapScreen(
    initialDestPoiId: String? = null,
    mapEntryKey: Int = 0
) {
    val routeManager = remember { RouteManager() }
    val allPois = remember { EtsiitData.pois }

    // 1. DECLARACIONES DE ESTADO (Primero)
    val isComedorShortcut = initialDestPoiId == DEST_COMEDOR_CERCANO

    var mapMode by rememberSaveable {
        mutableStateOf("city")
    }

    var selectedFloor by rememberSaveable(initialDestPoiId, mapEntryKey) {
        mutableStateOf(
            if (isComedorShortcut) "S1" else "P1"
        )
    }

// Routing State
    var startPoiId by rememberSaveable(initialDestPoiId, mapEntryKey) {
        mutableStateOf("")
    }

    var destPoiId by rememberSaveable(initialDestPoiId, mapEntryKey) {
        mutableStateOf(
            if (isComedorShortcut) "poi_comedor"
            else initialDestPoiId ?: ""
        )
    }

    var isRouteOpen by rememberSaveable(initialDestPoiId, mapEntryKey) {
        mutableStateOf(
            initialDestPoiId != null && !isComedorShortcut
        )
    }

    var isAccessibleRoute by rememberSaveable { mutableStateOf(false) }

    // Estado para el escaner QR y diálogo de QR de Ruta
    var isScanningQr by rememberSaveable { mutableStateOf(false) }
    var showRouteQrDialog by rememberSaveable { mutableStateOf(false) }

    // 2. SENSORES DE LOCALIZACIÓN (Utilizan mapMode declarado arriba)
    val context = LocalContext.current
    val inForeground = rememberAppInForeground()
    var hasLocationPermission by remember { mutableStateOf(LocationPermissions.hasLocation(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasLocationPermission = LocationPermissions.hasLocation(context)
    }
    val gpsLocation = rememberGpsLocation(active = inForeground && hasLocationPermission)
    var autoModeAppliedForEntry by rememberSaveable {
        mutableIntStateOf(-1)
    }
    var permissionRequestedForEntry by rememberSaveable {
        mutableIntStateOf(-1)
    }

LaunchedEffect(
mapEntryKey,
gpsLocation,
hasLocationPermission,
initialDestPoiId
) {
    if (autoModeAppliedForEntry == mapEntryKey) return@LaunchedEffect

    if (!hasLocationPermission) {
        if (permissionRequestedForEntry != mapEntryKey) {
            permissionRequestedForEntry = mapEntryKey
            permissionLauncher.launch(LocationPermissions.all)
        }
        return@LaunchedEffect
    }

    val location = gpsLocation ?: return@LaunchedEffect

    // Esperamos una lectura GPS suficientemente precisa.
    if (location.accuracy > 100f) return@LaunchedEffect

    val currentPoint = GeoPoint(
        location.latitude,
        location.longitude
    )

    // Para el comedor utilizamos el radio específico de GeoUtils (120 m).
    // Para el resto de accesos conservamos el comportamiento actual.
    val atEtsiit = if (initialDestPoiId == DEST_COMEDOR_CERCANO) {
        GeoUtils.isAtEtsiit(currentPoint)
    } else {
        isNearEtsiit(location)
    }

    mapMode = if (atEtsiit) "indoor" else "city"

    autoModeAppliedForEntry = mapEntryKey
}
    val hasBarometer = remember { hasSensor(context, Sensor.TYPE_PRESSURE) }
    val tracker = remember { IndoorTracker(LocationPrefs.mapNorth(context)) }
    var isTracking by rememberSaveable { mutableStateOf(false) }
    val indoorSensorsActive = inForeground && mapMode == "indoor"
    PressureEffect(active = indoorSensorsActive) { hPa, timestampMs -> tracker.onPressure(hPa, timestampMs) }
    val azimuth = rememberCompassAzimuth(active = indoorSensorsActive)

// Si es un destino especial, buscamos el POI real más cercano.
val destinationResolution = remember(
    startPoiId,
    destPoiId,
    isAccessibleRoute,
    allPois
) {
    when (destPoiId) {

        DEST_ASEO_CERCANO -> {
            resolveNearestPoi(
                startPoiId = startPoiId,
                candidates = allPois.filter { it.isAseo() },
                routeManager = routeManager,
                isAccessibleRoute = isAccessibleRoute
            )
        }

        DEST_FUENTE_CERCANA -> {
            resolveNearestPoi(
                startPoiId = startPoiId,
                candidates = allPois.filter { it.isFuente() },
                routeManager = routeManager,
                isAccessibleRoute = isAccessibleRoute
            )
        }

        DEST_COMEDOR_CERCANO -> {
            DestinationResolution(
                poi = allPois.find { it.id == "poi_comedor" }
            )
        }

        else -> {
            DestinationResolution(
                poi = allPois.find { it.id == destPoiId }
            )
        }
    }
}

// Calculamos la ruta utilizando el POI real resuelto.
    val calculatedRoute = remember(
        startPoiId,
        destPoiId,
        destinationResolution.poi?.id,
        destinationResolution.route,
        isAccessibleRoute,
        isRouteOpen
    ) {
        val resolvedDest = destinationResolution.poi

        if (isRouteOpen && startPoiId.isNotEmpty() && resolvedDest != null) {

            // Si es un destino especial, reutilizamos la ruta que ya hemos calculado.
            destinationResolution.route
                ?: routeManager.calculateRoute(
                    startPoiId,
                    resolvedDest.id,
                    isAccessibleRoute
                )

        } else {
            emptyList()
        }
    }

    val routeSegments = remember(calculatedRoute) { parseRouteSegments(calculatedRoute) }
    var activeSegmentIndex by rememberSaveable(routeSegments) { mutableIntStateOf(0) }

    val startPoi = remember(startPoiId) {
        allPois.find { it.id == startPoiId }
    }
    val destPoi = destinationResolution.poi

    val selectedDestLabel = when (destPoiId) {

        DEST_ASEO_CERCANO ->
            destPoi?.let { "🚻 Aseo más cercano · ${it.nameEs}" }
                ?: "🚻 Aseo más cercano"

        DEST_FUENTE_CERCANA ->
            destPoi?.let { "🚰 Fuente más cercana · ${it.nameEs}" }
                ?: "🚰 Fuente más cercana"

        DEST_COMEDOR_CERCANO ->
            destPoi?.let { "🍽️ ${it.nameEs}" }
                ?: "🍽️ Comedor de la ETSIIT"

        else -> destPoi?.nameEs ?: "Seleccionar Destino"
    }

    val destinationError = when {
        destPoiId == DEST_ASEO_CERCANO && destPoi == null ->
            "No se ha encontrado un aseo con ruta disponible desde este origen."

        destPoiId == DEST_FUENTE_CERCANA && destPoi == null ->
            "No se ha encontrado una fuente con ruta disponible desde este origen."

        destPoiId == DEST_COMEDOR_CERCANO && destPoi == null ->
            "No se ha encontrado el comedor de la ETSIIT."

        else -> null
    }
    val activeSegment = routeSegments.getOrNull(activeSegmentIndex)

    // Si el usuario presiona escanear QR, abrimos la vista de cámara
    if (isScanningQr) {
        QrLocationScannerScreen(
            onResultDetected = { result ->
                when (result) {
                    is QrScanResult.Location -> {
                        val matchingPoi = allPois.find { it.nodeId == result.node.id }
                        if (matchingPoi != null) {
                            startPoiId = matchingPoi.id
                        }
                        selectedFloor = getFloorName(result.node.floor)
                    }
                    is QrScanResult.Route -> {
                        startPoiId = result.startPoi.id
                        destPoiId = result.destPoi.id
                        isAccessibleRoute = result.isAccessible
                        isRouteOpen = true
                        activeSegmentIndex = 0
                        EtsiitData.nodes[result.startPoi.nodeId]?.let { node ->
                            selectedFloor = getFloorName(node.floor)
                        }
                    }
                }
                isScanningQr = false
            },
            onCancel = {
                isScanningQr = false
            }
        )
        return
    }

    if (showRouteQrDialog && startPoi != null && destPoi != null) {
        RouteQrDialog(
            startPoi = startPoi,
            destPoi = destPoi,
            isAccessible = isAccessibleRoute,
            onDismiss = { showRouteQrDialog = false }
        )
    }

    LaunchedEffect(activeSegmentIndex, routeSegments) {
        if (routeSegments.isNotEmpty() && activeSegmentIndex in routeSegments.indices) {
            val segment = routeSegments[activeSegmentIndex]
            selectedFloor = segment.floorName
            tracker.calibrateFloor(segment.floorNumber)
            tracker.followPath(segment.segmentNodes)
        } else {
            tracker.stopFollowing()
        }
    }
    LaunchedEffect(routeSegments) { tracker.resetSteps() }

    LaunchedEffect(tracker.estimatedFloor) {
        val floor = tracker.estimatedFloor ?: return@LaunchedEffect
        val segment = routeSegments.getOrNull(activeSegmentIndex)
        if (isRouteOpen && segment?.nextFloorNumber == floor) {
            activeSegmentIndex += 1
        } else if (!isRouteOpen) {
            selectedFloor = getFloorName(floor)
        }
    }

    StepEffect(active = indoorSensorsActive && isTracking && isRouteOpen) { tracker.onStep(azimuth) }
    val userPosition = tracker.position.takeIf { isTracking && isRouteOpen && activeSegment?.floorName == selectedFloor }
    val userHeadingMapDeg = azimuth?.let { it - tracker.mapNorthDeg }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var floorSectionTop by remember { mutableIntStateOf(0) }
    LaunchedEffect(isRouteOpen, routeSegments, floorSectionTop) {
        if (isRouteOpen && routeSegments.isNotEmpty() && floorSectionTop > 0) {
            scrollState.animateScrollTo(floorSectionTop)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Mapa y Navegación",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )
                Text(
                    text = "Ubica campus, edificios y aulas de la ETSIIT",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(42.dp)
                    .clickable {
                        mapMode = if (mapMode == "indoor") "city" else "indoor"
                    },
                color = UgrRed,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (mapMode == "indoor") {
                            Icons.Filled.Map
                        } else {
                            Icons.Filled.Apartment
                        },
                        contentDescription = "Cambiar tipo de mapa",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

    if (mapMode == "city") {
        CityRouteSection(
            gpsLocation = gpsLocation,
            hasLocationPermission = hasLocationPermission,

            onRequestPermission = {
                permissionLauncher.launch(LocationPermissions.all)
            },

            onContinueIndoors = {
                mapMode = "indoor"
                scope.launch {
                    scrollState.scrollTo(0)
                }
            },

            // Petición especial del comedor más cercano.
            initialDestinationId = initialDestPoiId.takeIf {
                it == DEST_COMEDOR_CERCANO
            },

            // Esperamos a que se haya determinado la ubicación.
            initialDestinationReady =
                autoModeAppliedForEntry == mapEntryKey ||
                        !hasLocationPermission,

            // Identificador único de cada entrada al mapa.
            destinationRequestKey = mapEntryKey
        )
    } else {
            val floorNumber = when (selectedFloor) {
                "P4" -> 4
                "P3" -> 3
                "P2" -> 2
                "P1" -> 1
                "PB" -> 0
                "S1" -> -1
                else -> 0
            }

            val floorImageBitmap = remember(floorNumber) {
                runCatching {
                    val inputStream = context.assets.open("Modelo 3D ETSIIT/ConSuelo/$floorNumber.jpg")
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }.getOrNull()
            }

            Spacer(modifier = Modifier.height(10.dp))

            PoiRouteSelectorCard(
                pois = allPois,
                startPoi = startPoi,
                destPoi = destPoi,

                selectedDestLabel = selectedDestLabel,
                destinationError = destinationError,

                onStartPoiChange = { startPoiId = it.id },
                onDestPoiChange = { destPoiId = it.id },

                // Opciones especiales exclusivamente para el destino.
                onSpecialDestChange = { destPoiId = it },

                isAccessible = isAccessibleRoute,
                onAccessibleToggle = { isAccessibleRoute = it },

                onCalculateRoute = {
                    isRouteOpen = true
                    activeSegmentIndex = 0
                },

                onScanQrClick = {
                    isScanningQr = true
                },

                onGenerateQrClick = { showRouteQrDialog = true },
                isRouteActive = isRouteOpen && routeSegments.isNotEmpty(),
                onClearRoute = { isRouteOpen = false }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Seleccionar Planta",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .onGloballyPositioned {
                        floorSectionTop = it.positionInParent().y.toInt()
                    }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("P4", "P3", "P2", "P1", "PB", "S1").forEach { floor ->
                    val selected = floor == selectedFloor
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable {
                                selectedFloor = floor
                                if (routeSegments.isNotEmpty()) {
                                    val matchIdx = routeSegments.indexOfFirst { it.floorName == floor }
                                    if (matchIdx != -1) {
                                        activeSegmentIndex = matchIdx
                                    }
                                }
                            },
                        color = if (selected) UgrRed else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = floor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else Ink
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // PLANO INTERIOR A ANCHO COMPLETO
            FloorPlanCanvasOverlay(
                bitmap = floorImageBitmap,
                selectedFloorName = selectedFloor,
                activeSegment = activeSegment,
                fullRoute = calculatedRoute,
                isFirstSegment = activeSegmentIndex == 0,
                isLastSegment = activeSegmentIndex == routeSegments.size - 1,
                userPosition = userPosition,
                userHeadingMapDeg = userHeadingMapDeg
            )

            // Indicaciones de navegación (separadas del plano)
            if (userPosition != null && activeSegment != null) {
                Spacer(modifier = Modifier.height(8.dp))

                NavigationHintStrip(
                    tracker = tracker,
                    azimuthDeg = azimuth,
                    arrivalText = arrivalText(
                        activeSegment,
                        destPoi?.nameEs ?: "tu destino"
                    )
                )
            }

            if (isRouteOpen && routeSegments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                RouteStepNavigationCard(
                    segments = routeSegments,
                    currentIndex = activeSegmentIndex,
                    startPoiName = startPoi?.nameEs ?: "Origen",
                    destPoiName = destPoi?.nameEs ?: "Destino",
                    onSegmentChange = { activeSegmentIndex = it },
                    onGenerateQrClick = { showRouteQrDialog = true },
                    onClose = { isRouteOpen = false }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta original de tus compañeros
            LocationStatusCard(
                hasLocationPermission = hasLocationPermission,
                onRequestPermission = { permissionLauncher.launch(LocationPermissions.all) },
                gpsLocation = gpsLocation,
                hasBarometer = hasBarometer,
                tracker = tracker,
                azimuthDeg = azimuth,
                selectedFloorName = selectedFloor,
                onCalibrateFloor = { tracker.calibrateFloor(floorNumber) },
                isRouteOpen = isRouteOpen && routeSegments.isNotEmpty(),
                isTracking = isTracking,
                onTrackingChange = { isTracking = it },
                onAlignCompass = {
                    azimuth?.let { az ->
                        tracker.alignCompass(az)?.let { LocationPrefs.setMapNorth(context, it) }
                    }
                },
                onUseEntranceAsStart = if (startPoiId != "poi_entrada") {
                    { startPoiId = "poi_entrada" }
                } else null
            )
        }
    }
}

@Composable
fun FloorPlanCanvasOverlay(
    bitmap: ImageBitmap?,
    selectedFloorName: String,
    activeSegment: RouteFloorSegment?,
    fullRoute: List<Node>,
    isFirstSegment: Boolean,
    isLastSegment: Boolean,
    userPosition: MapPoint? = null,
    userHeadingMapDeg: Float? = null
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isFullScreenOpen by remember { mutableStateOf(false) }

    val nodesToDraw = activeSegment?.segmentNodes ?: emptyList()

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                //.clip(RoundedCornerShape(14.dp))
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale == 1f) Offset.Zero else offset + pan
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Plano Planta $selectedFloorName",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (nodesToDraw.isEmpty()) return@Canvas

                    val refWidth = 1000f
                    val refHeight = 1000f
                    val scaleX = size.width / refWidth
                    val scaleY = size.height / refHeight

                    for (i in 0 until (nodesToDraw.size - 1)) {
                        val n1 = nodesToDraw[i]
                        val n2 = nodesToDraw[i + 1]

                        drawLine(
                            color = Color(0xFFD32F2F),
                            start = Offset(n1.x * scaleX, n1.y * scaleY),
                            end = Offset(n2.x * scaleX, n2.y * scaleY),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    nodesToDraw.forEach { node ->
                        val px = node.x * scaleX
                        val py = node.y * scaleY

                        val isStartNode = isFirstSegment && (node.id == fullRoute.firstOrNull()?.id)
                        val isEndNode = isLastSegment && (node.id == fullRoute.lastOrNull()?.id)
                        val isTransitionNode = (node == activeSegment?.transitionNode) ||
                                (!isFirstSegment && node == nodesToDraw.firstOrNull())

                        when {
                            isStartNode -> {
                                drawCircle(
                                    color = Color(0xFF2E7D32),
                                    radius = 10.dp.toPx(),
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                            isEndNode -> {
                                drawCircle(
                                    color = Color(0xFFD32F2F),
                                    radius = 10.dp.toPx(),
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                            isTransitionNode -> {
                                drawCircle(
                                    color = Color(0xFFE65100),
                                    radius = 11.dp.toPx(),
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                            else -> {
                                drawCircle(
                                    color = Color(0xFFD32F2F),
                                    radius = 4.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }

                    userPosition?.let { drawUserMarker(it, userHeadingMapDeg, scaleX, scaleY) }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ZoomButton("➕") { scale = (scale + 0.5f).coerceAtMost(5f) }
                ZoomButton("➖") {
                    scale = (scale - 0.5f).coerceAtLeast(1f)
                    if (scale == 1f) offset = Offset.Zero
                }
                ZoomButton("↺") {
                    scale = 1f
                    offset = Offset.Zero
                }
                ZoomButton("🔍 Pantalla Completa") { isFullScreenOpen = true }
            }
        }
    }

    if (isFullScreenOpen) {
        ZoomableMapDialog(
            bitmap = bitmap,
            selectedFloorName = selectedFloorName,
            activeSegment = activeSegment,
            fullRoute = fullRoute,
            isFirstSegment = isFirstSegment,
            isLastSegment = isLastSegment,
            userPosition = userPosition,
            userHeadingMapDeg = userHeadingMapDeg,
            onDismiss = { isFullScreenOpen = false }
        )
    }
}

@Composable
fun ZoomableMapDialog(
    bitmap: ImageBitmap?,
    selectedFloorName: String,
    activeSegment: RouteFloorSegment?,
    fullRoute: List<Node>,
    isFirstSegment: Boolean,
    isLastSegment: Boolean,
    userPosition: MapPoint? = null,
    userHeadingMapDeg: Float? = null,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1.8f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val nodesToDraw = activeSegment?.segmentNodes ?: emptyList()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.95f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Plano de la Planta $selectedFloorName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Usa pellizco con dos dedos para zoom y arrastra el mapa",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }

                    Surface(
                        modifier = Modifier.clickable { onDismiss() },
                        color = UgrRed,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "✖ Cerrar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, bottom = 70.dp, start = 8.dp, end = 8.dp)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 6f)
                                offset = if (scale == 1f) Offset.Zero else offset + pan
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Plano Planta $selectedFloorName",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (nodesToDraw.isEmpty()) return@Canvas

                        val refWidth = 1000f
                        val refHeight = 1000f
                        val scaleX = size.width / refWidth
                        val scaleY = size.height / refHeight

                        for (i in 0 until (nodesToDraw.size - 1)) {
                            val n1 = nodesToDraw[i]
                            val n2 = nodesToDraw[i + 1]

                            drawLine(
                                color = Color(0xFFD32F2F),
                                start = Offset(n1.x * scaleX, n1.y * scaleY),
                                end = Offset(n2.x * scaleX, n2.y * scaleY),
                                strokeWidth = 6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        nodesToDraw.forEach { node ->
                            val px = node.x * scaleX
                            val py = node.y * scaleY

                            val isStartNode = isFirstSegment && (node.id == fullRoute.firstOrNull()?.id)
                            val isEndNode = isLastSegment && (node.id == fullRoute.lastOrNull()?.id)
                            val isTransitionNode = (node == activeSegment?.transitionNode) ||
                                    (!isFirstSegment && node == nodesToDraw.firstOrNull())

                            when {
                                isStartNode -> {
                                    drawCircle(
                                        color = Color(0xFF2E7D32),
                                        radius = 10.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 5.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }
                                isEndNode -> {
                                    drawCircle(
                                        color = Color(0xFFD32F2F),
                                        radius = 10.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 5.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }
                                isTransitionNode -> {
                                    drawCircle(
                                        color = Color(0xFFE65100),
                                        radius = 11.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }
                                else -> {
                                    drawCircle(
                                        color = Color(0xFFD32F2F),
                                        radius = 4.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }
                            }
                        }

                        userPosition?.let { drawUserMarker(it, userHeadingMapDeg, scaleX, scaleY) }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 30.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ZoomButton("➕ Acercar") { scale = (scale + 0.5f).coerceAtMost(6f) }
                    ZoomButton("➖ Alejar") {
                        scale = (scale - 0.5f).coerceAtLeast(1f)
                        if (scale == 1f) offset = Offset.Zero
                    }
                    ZoomButton("↺ Restablecer") {
                        scale = 1f
                        offset = Offset.Zero
                    }
                }
            }
        }
    }
}

@Composable
fun RouteStepNavigationCard(
    segments: List<RouteFloorSegment>,
    currentIndex: Int,
    startPoiName: String,
    destPoiName: String,
    onSegmentChange: (Int) -> Unit,
    onGenerateQrClick: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    val currentSegment = segments.getOrNull(currentIndex) ?: return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, UgrRed)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PASO ${currentIndex + 1} DE ${segments.size} · PLANTA ${currentSegment.floorName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgrRed
                    )
                    Text(
                        text = "$startPoiName ➔ $destPoiName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }

                Text(
                    text = "✖",
                    fontSize = 16.sp,
                    color = MutedText,
                    modifier = Modifier
                        .clickable { onClose() }
                        .padding(start = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            if (currentSegment.transitionNode != null) {
                val actionName = if (currentSegment.isElevator) "el ascensor 🛗" else "las escaleras 🪜"
                val nextFloorName = currentSegment.nextFloorName ?: ""

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = ToneBlueBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = if (currentSegment.isElevator) "🛗" else "🪜",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dirígete hacia $actionName en Planta ${currentSegment.floorName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "Marcado en naranja 🟠 en el plano",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (currentIndex < segments.size - 1) {
                            onSegmentChange(currentIndex + 1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "He llegado a $actionName ➔ Ir a Planta $nextFloorName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = ToneBlueBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = "🏁",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tramo final hacia $destPoiName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "Ruta dibujada en Planta ${currentSegment.floorName} desde el nodo intermedio",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenStatus),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🏁 ¡Llegada al Destino! Finalizar guía",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (segments.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentIndex > 0) {
                        OutlinedButton(
                            onClick = { onSegmentChange(currentIndex - 1) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "⬅ Planta ${segments[currentIndex - 1].floorName}", fontSize = 11.sp, color = Ink)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentIndex < segments.size - 1) {
                        OutlinedButton(
                            onClick = { onSegmentChange(currentIndex + 1) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Planta ${segments[currentIndex + 1].floorName} ➔", fontSize = 11.sp, color = Ink)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun PoiRouteSelectorCard(
    pois: List<PointOfInterest>,
    startPoi: PointOfInterest?,
    destPoi: PointOfInterest?,
    selectedDestLabel: String,
    destinationError: String?,
    onStartPoiChange: (PointOfInterest) -> Unit,
    onDestPoiChange: (PointOfInterest) -> Unit,
    onSpecialDestChange: (String) -> Unit,
    isAccessible: Boolean,
    onAccessibleToggle: (Boolean) -> Unit,
    onCalculateRoute: () -> Unit,
    onGenerateQrClick: (() -> Unit)? = null,
    isRouteActive: Boolean = false,
    onClearRoute: () -> Unit,
    onScanQrClick: () -> Unit
) {
    var pickingStart by remember { mutableStateOf(false) }
    var pickingDest by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Raíl visual de origen y destino.
                Column(
                    modifier = Modifier.width(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(GreenStatus, RoundedCornerShape(50))
                    )
                    repeat(3) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(CardBorder, RoundedCornerShape(50))
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(UgrRed, RoundedCornerShape(50))
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {

                    // FILA 1: Origen + Cámara QR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PoiSelectorField(
                                poi = startPoi,
                                placeholder = "Origen",
                                onClick = { pickingStart = true }
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onScanQrClick() },
                            color = UgrRed,
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Escanear QR de ubicación",
                                    tint = Color.White,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // FILA 2: Destino + Intercambiador
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PoiSelectorField(
                                poi = destPoi,
                                displayText = selectedDestLabel,
                                placeholder = "¿A dónde vas?",
                                onClick = { pickingDest = true }
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(enabled = startPoi != null && destPoi != null) {
                                    if (startPoi != null && destPoi != null) {
                                        onStartPoiChange(destPoi)
                                        onDestPoiChange(startPoi)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⇅",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))
            }

            // Se conserva el selector en hoja, que permite buscar POI por texto.
            if (pickingStart) {
                PoiPickerSheet(
                    title = "Elige el origen",
                    accent = GreenStatus,
                    pois = pois,
                    selectedPoiId = startPoi?.id,
                    excludedPoiId = destPoi?.id,
                    onSelect = {
                        onStartPoiChange(it)
                        pickingStart = false
                    },
                    onDismiss = { pickingStart = false }
                )
            }

            if (pickingDest) {
                PoiPickerSheet(
                    title = "Elige el destino",
                    accent = UgrRed,
                    pois = pois,
                    selectedPoiId = destPoi?.id,
                    excludedPoiId = startPoi?.id,
                    onSelect = {
                        onDestPoiChange(it)
                        pickingDest = false
                    },
                    onDismiss = { pickingDest = false }
                )
            }

            if (destinationError != null) {
                Text(
                    text = destinationError,
                    fontSize = 10.sp,
                    color = UgrRed,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onAccessibleToggle(!isAccessible) },
                    color = if (isAccessible) ToneBlueBg else Color.White,
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAccessible) ToneBlueText else CardBorder
                    )
                ) {
                    Text(
                        text = if (isAccessible) "♿ Sin escaleras ✓" else "♿ Sin escaleras",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAccessible) ToneBlueText else Ink,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Limpiar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClearRoute() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = onCalculateRoute,
                    enabled = startPoi != null && destPoi != null,
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                    shape = RoundedCornerShape(50),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                ) {
                    Text(text = "Ir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (onGenerateQrClick != null && isRouteActive) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onGenerateQrClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "📱 Generar QR de esta ruta",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
            }
        }
    }
}

@Composable
fun CampusCard(campus: CampusItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campus.campus,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgrRed
                )
                Text(
                    text = "📍 ${campus.distance}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = campus.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Text(
                text = campus.full,
                fontSize = 11.sp,
                color = MutedText,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "🕒 ${campus.hours}",
                fontSize = 11.sp,
                color = GreenStatus,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PoiSelectorField(
    poi: PointOfInterest?,
    displayText: String? = null,
    placeholder: String,
    onClick: () -> Unit
) {
    val label = displayText
        ?: poi?.let { "${poiCategoryIcon(it.category)} ${it.nameEs}" }
        ?: placeholder

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = SoftBg,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (poi != null || displayText != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (poi != null || displayText != null) Ink else MutedText,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            poi?.let { poiFloor(it) }?.let { floor ->
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = getFloorName(floor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText
                )
            }
        }
    }
}

@Composable
fun PoiChip(poi: PointOfInterest, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = poiCategoryIcon(poi.category), fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = poi.nameEs, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
    }
}
