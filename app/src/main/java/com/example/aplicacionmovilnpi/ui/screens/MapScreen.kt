
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
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.TouchApp
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
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.localizedFloorName
import com.example.aplicacionmovilnpi.model.getName
import com.example.aplicacionmovilnpi.model.getDescription
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.location.IndoorTracker
import com.example.aplicacionmovilnpi.location.LocationPermissions
import com.example.aplicacionmovilnpi.location.LocationPrefs
import com.example.aplicacionmovilnpi.location.MapPoint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults

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
private const val DEST_BIBLIOTECA_CERCANA = "__dest_biblioteca_cercana__"
private const val DEST_ABRIR_ESCANER_QR = "__abrir_escaner_qr__"

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

/**
 * Busca el POI alcanzable cuya ruta acumulada tiene el menor coste.
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
        route.zipWithNext().sumOf { (from, to) ->
            EtsiitData.edges.firstOrNull { edge ->
                (edge.from == from.id && edge.to == to.id) ||
                    (edge.from == to.id && edge.to == from.id)
            }?.weight?.toDouble() ?: Double.POSITIVE_INFINITY
        }
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
    mapEntryKey: Int = 0,
    defaultAccessibleRoute: Boolean = false
) {
    val context = LocalContext.current
    val isEnglish = LocalIsEnglish.current

    val routeManager = remember { RouteManager() }
    val allPois = remember { EtsiitData.pois }

    // 1. DECLARACIONES DE ESTADO (Primero)
    val isComedorShortcut = initialDestPoiId == DEST_COMEDOR_CERCANO

    val isQrShortcut =
        initialDestPoiId == DEST_ABRIR_ESCANER_QR

    var mapMode by rememberSaveable(initialDestPoiId, mapEntryKey) {
        mutableStateOf(
            if (isQrShortcut) "indoor" else "city"
        )
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
            initialDestPoiId?.takeUnless {
                it == DEST_ABRIR_ESCANER_QR
            } ?: ""
        )
    }

    var isRouteOpen by rememberSaveable(initialDestPoiId, mapEntryKey) {
        mutableStateOf(
            initialDestPoiId != null &&
                    initialDestPoiId != DEST_ABRIR_ESCANER_QR
        )
    }

    var isAccessibleRoute by rememberSaveable(initialDestPoiId, mapEntryKey) {
        mutableStateOf(defaultAccessibleRoute)
    }

    // Estado para el escaner QR y diálogo de QR de Ruta
    var isScanningQr by rememberSaveable(initialDestPoiId, mapEntryKey) {
        mutableStateOf(
            initialDestPoiId == DEST_ABRIR_ESCANER_QR
        )
    }
    var showRouteQrDialog by rememberSaveable { mutableStateOf(false) }

    // 2. SENSORES DE LOCALIZACIÓN (Utilizan mapMode declarado arriba)
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

    // Si venimos del acceso directo del escáner QR,
    // abrimos directamente el mapa interior.
    if (initialDestPoiId == DEST_ABRIR_ESCANER_QR) {
        mapMode = "indoor"
        autoModeAppliedForEntry = mapEntryKey
        return@LaunchedEffect
    }

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
    val atEtsiit = when (initialDestPoiId) {

        DEST_COMEDOR_CERCANO,
        DEST_BIBLIOTECA_CERCANA,
        DEST_ASEO_CERCANO,
        DEST_FUENTE_CERCANA -> {
            GeoUtils.isAtEtsiit(currentPoint)
        }

        else -> {
            isNearEtsiit(location)
        }
    }

    mapMode = if (atEtsiit) "indoor" else "city"

    autoModeAppliedForEntry = mapEntryKey

}
    val tracker = remember { IndoorTracker(LocationPrefs.mapNorth(context)) }
    var isTracking by rememberSaveable { mutableStateOf(false) }
    val indoorSensorsActive = inForeground && mapMode == "indoor"
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
                if (startPoiId.isBlank()) {
                    DestinationResolution(poi = null)
                } else {
                    resolveNearestPoi(
                        startPoiId = startPoiId,
                        candidates = allPois.filter { it.isAseo() },
                        routeManager = routeManager,
                        isAccessibleRoute = isAccessibleRoute
                    )
                }
            }

            DEST_FUENTE_CERCANA -> {
                if (startPoiId.isBlank()) {
                    DestinationResolution(poi = null)
                } else {
                    val nearestFacilityNodeIds = setOf(
                        "n_banos_p0",
                        "n_banos_p1",
                        "n_banos_p2",
                        "n_banos_p3",
                        "n_escaleras_principal_p1",
                        "n_escaleras_principal_p2",
                        "n_escaleras_principal_p3",
                        "n_escaleras_principal_p4"
                    )
                    resolveNearestPoi(
                        startPoiId = startPoiId,
                        candidates = allPois.filter { it.nodeId in nearestFacilityNodeIds },
                        routeManager = routeManager,
                        isAccessibleRoute = isAccessibleRoute
                    )
                }
            }

            DEST_COMEDOR_CERCANO -> {
                DestinationResolution(
                    poi = allPois.find { it.id == "poi_comedor" }
                )
            }

            DEST_BIBLIOTECA_CERCANA -> {
                DestinationResolution(
                    poi = allPois.find { it.id == "poi_biblioteca" }
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
            destPoi?.let { if (isEnglish) "🚻 Nearest restroom · ${it.getName(true)}" else "🚻 Aseo más cercano · ${it.getName(false)}" }
                ?: if (isEnglish) "🚻 Nearest Restroom" else "🚻 Aseo más cercano"

        DEST_FUENTE_CERCANA ->
            destPoi?.let { if (isEnglish) "🚰 Nearest water fountain · ${it.getName(true)}" else "🚰 Fuente más cercana · ${it.getName(false)}" }
                ?: if (isEnglish) "🚰 Nearest Water Fountain" else "🚰 Fuente más cercana"

        DEST_COMEDOR_CERCANO ->
            destPoi?.let { "🍽️ ${it.getName(isEnglish)}" }
                ?: if (isEnglish) "🍽️ ETSIIT Dining Hall" else "🍽️ Comedor de la ETSIIT"

        DEST_BIBLIOTECA_CERCANA ->
            destPoi?.let { "📚 ${it.getName(isEnglish)}" }
                ?: if (isEnglish) "📚 ETSIIT Library" else "📚 Biblioteca de la ETSIIT"

        else ->
            destPoi?.getName(isEnglish)
                ?: if (isEnglish) "Select Destination" else "Seleccionar Destino"
    }

    val destinationError = when {
        destPoiId == DEST_ASEO_CERCANO && startPoi != null && destPoi == null ->
            if (isEnglish) "No restroom with an available route was found from this origin."
            else "No se ha encontrado un aseo con ruta disponible desde este origen."

        destPoiId == DEST_FUENTE_CERCANA && startPoi != null && destPoi == null ->
            if (isEnglish) "No water fountain with an available route was found from this origin."
            else "No se ha encontrado una fuente con ruta disponible desde este origen."

        destPoiId == DEST_COMEDOR_CERCANO && destPoi == null ->
            if (isEnglish) "ETSIIT dining hall was not found."
            else "No se ha encontrado el comedor de la ETSIIT."

        destPoiId == DEST_BIBLIOTECA_CERCANA && destPoi == null ->
            if (isEnglish) "ETSIIT library was not found."
            else "No se ha encontrado la biblioteca de la ETSIIT."

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

                            if (destPoiId.isNotBlank()) {
                                isRouteOpen = true
                                activeSegmentIndex = 0
                            }
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

    // Textos que muestra el buscador común cuando estamos en el mapa exterior.
    var cityOriginDisplayText by rememberSaveable(mapEntryKey) {
        mutableStateOf(
            if (isEnglish) "My location" else "Mi ubicación"
        )
    }

    var cityDestinationDisplayText by rememberSaveable(mapEntryKey) {
        mutableStateOf("")
    }

    // Peticiones para abrir los selectores exteriores.
    var requestCityOriginPicker by rememberSaveable {
        mutableStateOf(false)
    }

    var requestCityDestinationPicker by rememberSaveable {
        mutableStateOf(false)
    }

    var isCityMapOriginSelectionActive by rememberSaveable {
        mutableStateOf(false)
    }

    var requestCitySwap by rememberSaveable {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
                    text = if (isEnglish) "Map & Navigation" else "Mapa y Navegación",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )
                Text(
                    text = if (isEnglish) "Find the campus, buildings and classrooms" else "Ubica campus, edificios y aulas de la ETSIIT",
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
                        contentDescription = if (isEnglish) "Switch map type" else "Cambiar tipo de mapa",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val isSpecialDest = destPoiId in setOf(
            DEST_ASEO_CERCANO,
            DEST_FUENTE_CERCANA,
            DEST_COMEDOR_CERCANO,
            DEST_BIBLIOTECA_CERCANA
        )

        PoiRouteSelectorCard(
            pois = allPois,
            startPoi = startPoi,
            destPoi = destPoi,

            selectedDestLabel = selectedDestLabel,
            isSpecialDest = isSpecialDest,
            destinationError = destinationError,

            onStartPoiChange = { poi ->
                startPoiId = poi.id

                if (destPoiId.isNotBlank()) {
                    isRouteOpen = true
                    activeSegmentIndex = 0
                }
            },

            onDestPoiChange = { poi ->
                destPoiId = poi.id

                if (startPoiId.isNotBlank()) {
                    isRouteOpen = true
                    activeSegmentIndex = 0
                }
            },

            onSpecialDestChange = { specialId ->
                destPoiId = specialId

                if (startPoiId.isNotBlank()) {
                    isRouteOpen = true
                    activeSegmentIndex = 0
                }
            },

            isAccessible = isAccessibleRoute,
            onAccessibleToggle = { isAccessibleRoute = it },

            onScanQrClick = {
                isScanningQr = true
            },

            isCityMode = mapMode == "city",

            cityOriginDisplayText = cityOriginDisplayText,
            cityDestinationDisplayText = cityDestinationDisplayText,

            onCityOriginClick = {
                requestCityOriginPicker = true
            },

            isCityMapOriginSelectionActive = isCityMapOriginSelectionActive,

            onCityMapOriginClick = {
                isCityMapOriginSelectionActive =
                    !isCityMapOriginSelectionActive
            },

            onCityDestinationClick = {
                requestCityDestinationPicker = true
            },

            onCitySwapClick = {
                requestCitySwap = true
            }
        )

    if (mapMode == "city") {
        Spacer(modifier = Modifier.height(14.dp))

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

            initialDestinationId = initialDestPoiId.takeIf {
                it == DEST_COMEDOR_CERCANO ||
                        it == DEST_BIBLIOTECA_CERCANA
            },

            // Aviso cuando se buscan aseos o fuentes fuera de la ETSIIT.
            showOutdoorFacilitiesUnavailableNotice =
                (
                        initialDestPoiId == DEST_ASEO_CERCANO ||
                                initialDestPoiId == DEST_FUENTE_CERCANA
                        ) && autoModeAppliedForEntry == mapEntryKey,

            // Esperamos a que se haya determinado la ubicación.
            initialDestinationReady =
                autoModeAppliedForEntry == mapEntryKey ||
                        !hasLocationPermission,

            // Identificador único de cada entrada al mapa.
            destinationRequestKey = mapEntryKey,

            requestOriginPicker = requestCityOriginPicker,

            onOriginPickerRequestConsumed = {
                requestCityOriginPicker = false
            },

            isMapOriginSelectionActive = isCityMapOriginSelectionActive,

            onMapOriginSelected = {
                isCityMapOriginSelectionActive = false
            },

            requestDestinationPicker = requestCityDestinationPicker,

            onDestinationPickerRequestConsumed = {
                requestCityDestinationPicker = false
            },

            requestSwap = requestCitySwap,

            onSwapRequestConsumed = {
                requestCitySwap = false
            },

            onCitySelectorLabelsChange = {
                    originTitle,
                    originSubtitle,
                    destinationTitle,
                    destinationSubtitle ->

                cityOriginDisplayText =
                    if (originSubtitle.isNullOrBlank()) {
                        originTitle
                    } else {
                        "$originTitle · $originSubtitle"
                    }

                cityDestinationDisplayText =
                    if (destinationSubtitle.isNullOrBlank()) {
                        destinationTitle
                    } else {
                        "$destinationTitle · $destinationSubtitle"
                    }
            }
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Título de accesibilidad
            Text(
                text = if (isEnglish) "Accessible" else "PMR",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAccessibleRoute) ToneBlueText else MutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Título de las plantas
            Text(
                text = if (isEnglish) "Select Floor:" else "Seleccionar Planta:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.weight(6f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = SoftBg,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Casilla de accesibilidad
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable { isAccessibleRoute = !isAccessibleRoute },
                    color = if (isAccessibleRoute) ToneBlueBg else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "♿",
                            fontSize = 18.sp,
                            color = if (isAccessibleRoute) ToneBlueText else Ink
                        )
                    }
                }

                // Plantas
                listOf("P4", "P3", "P2", "P1", "PB", "S1").forEach { floor ->
                    val selected = floor == selectedFloor
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable {
                                selectedFloor = floor
                                if (routeSegments.isNotEmpty()) {
                                    val matchIdx = routeSegments.indexOfFirst { it.floorName == floor }
                                    if (matchIdx != -1) {
                                        activeSegmentIndex = matchIdx
                                    }
                                }
                            },
                        color = if (selected) UgrRed else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = localizedFloorName(floor, isEnglish),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else Ink
                            )
                        }
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
                userHeadingMapDeg = userHeadingMapDeg,

                // Calibración de sensores
                tracker = tracker,
                azimuthDeg = azimuth,
                isTracking = isTracking,

                showTrackingControl = isRouteOpen && routeSegments.isNotEmpty(),
                onTrackingChange = { isTracking = it },

                onCalibrateFloor = {
                    tracker.calibrateFloor(floorNumber)
                },

                onAlignCompass = {
                    azimuth?.let { az ->
                        tracker.alignCompass(az)?.let {
                            LocationPrefs.setMapNorth(context, it)
                        }
                    }
                },

                // Instrucciones de navegación
                showNavigationHint = userPosition != null && activeSegment != null,

                arrivalText = activeSegment?.let { segment ->
                    arrivalText(
                        segment,
                        destPoi?.getName(isEnglish)
                            ?: if (isEnglish) "your destination" else "tu destino",
                        isEnglish
                    )
                } ?: "",

                // Generación de QR
                showGenerateQrButton =
                    isRouteOpen &&
                            routeSegments.isNotEmpty() &&
                            startPoi != null &&
                            destPoi != null,

                onGenerateQrClick = {
                    showRouteQrDialog = true
                }
            )

            if (isRouteOpen && routeSegments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                RouteStepNavigationCard(
                    segments = routeSegments,
                    currentIndex = activeSegmentIndex,
                    startPoiName = startPoi?.getName(isEnglish) ?: if (isEnglish) "Origin" else "Origen",
                    destPoiName = destPoi?.getName(isEnglish) ?: if (isEnglish) "Destination" else "Destino",
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
                tracker = tracker,
                azimuthDeg = azimuth,
                isRouteOpen = isRouteOpen && routeSegments.isNotEmpty(),
                isTracking = isTracking,
                onUseEntranceAsStart = if (startPoiId != "poi_entrada") {
                    {
                        startPoiId = "poi_entrada"

                        if (destPoiId.isNotBlank()) {
                            isRouteOpen = true
                            activeSegmentIndex = 0
                        }
                    }
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
    userHeadingMapDeg: Float? = null,

    // Calibración de sensores
    tracker: IndoorTracker,
    azimuthDeg: Float?,
    isTracking: Boolean,
    showTrackingControl: Boolean,
    onTrackingChange: (Boolean) -> Unit,
    onCalibrateFloor: () -> Unit,
    onAlignCompass: () -> Unit,

    // Instrucciones
    showNavigationHint: Boolean,
    arrivalText: String,

    // QR
    showGenerateQrButton: Boolean,
    onGenerateQrClick: () -> Unit
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
                        contentDescription = if (LocalIsEnglish.current) "Floor plan ${localizedFloorName(selectedFloorName, true)}" else "Plano Planta $selectedFloorName",
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

            // ==========================================
            // FRANJA SUPERIOR:
            // - Izquierda: Indicaciones de pasos (ancho flexible)
            // - Derecha: Botón QR
            // ==========================================
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (showNavigationHint) {
                        NavigationHintStrip(
                            tracker = tracker,
                            azimuthDeg = azimuthDeg,
                            arrivalText = arrivalText
                        )
                    }
                }

                if (showGenerateQrButton) {
                    Surface(
                        modifier = Modifier
                            .height(36.dp)
                            .clickable {
                                onGenerateQrClick()
                            },
                        color = Color.White,
                        shape = RoundedCornerShape(9.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            UgrRed
                        ),
                        shadowElevation = 3.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▦ QR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = UgrRed
                            )
                        }
                    }
                }
            }

            // ==========================================
            // FRANJA INFERIOR:
            // - Izquierda: Activar sensores + Calibrar sensores
            // - Derecha: Controles de zoom
            // ==========================================
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interruptor de sensores (oculta el texto cuando está activo)
                    if (showTrackingControl) {
                        Surface(
                            modifier = Modifier.height(40.dp),
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                CardBorder
                            ),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    start = if (isTracking) 6.dp else 8.dp,
                                    end = 6.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (!isTracking) {
                                    Text(
                                        text = if (LocalIsEnglish.current)
                                            "Enable sensors"
                                        else
                                            "Activar sensores",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                Switch(
                                    checked = isTracking,
                                    onCheckedChange = onTrackingChange,
                                    enabled = azimuthDeg != null,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = UgrRed
                                    )
                                )
                            }
                        }
                    }

                    // Botón de calibrar brújula con texto
                    if (isTracking && azimuthDeg != null) {
                        Surface(
                            modifier = Modifier
                                .height(40.dp)
                                .clickable {
                                    onAlignCompass()
                                },
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                UgrRed
                            ),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🧭", fontSize = 15.sp)
                                Text(
                                    text = if (LocalIsEnglish.current)
                                        "Calibrate sensors"
                                    else
                                        "Calibrar sensores",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UgrRed,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Controles de zoom
                Surface(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ZoomIconButton("➕") { scale = (scale + 0.5f).coerceAtMost(5f) }
                        ZoomIconButton("➖") {
                            scale = (scale - 0.5f).coerceAtLeast(1f)
                            if (scale == 1f) offset = Offset.Zero
                        }
                        ZoomIconButton("⟲") {
                            scale = 1f
                            offset = Offset.Zero
                        }
                        ZoomIconButton("⤢") { isFullScreenOpen = true }
                    }
                }
            }

            // ==========================================
            // INFERIOR DERECHA: Controles de zoom
            // ==========================================
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                color = Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZoomIconButton("➕") { scale = (scale + 0.5f).coerceAtMost(5f) }
                    ZoomIconButton("➖") {
                        scale = (scale - 0.5f).coerceAtLeast(1f)
                        if (scale == 1f) offset = Offset.Zero
                    }
                    ZoomIconButton("⟲") {
                        scale = 1f
                        offset = Offset.Zero
                    }
                    ZoomIconButton("⤢") { isFullScreenOpen = true }
                }
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
                            text = if (LocalIsEnglish.current) "Floor plan · ${localizedFloorName(selectedFloorName, true)}" else "Plano de la Planta $selectedFloorName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (LocalIsEnglish.current) "Pinch to zoom and drag the map" else "Usa pellizco con dos dedos para zoom y arrastra el mapa",
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
                            text = if (LocalIsEnglish.current) "✖ Close" else "✖ Cerrar",
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
                            contentDescription = if (LocalIsEnglish.current) "Floor plan ${localizedFloorName(selectedFloorName, true)}" else "Plano Planta $selectedFloorName",
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
fun ZoomIconButton(icon: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(32.dp)
            .clickable { onClick() },
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = icon,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
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
    val isEnglish = LocalIsEnglish.current
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
                        text = if (isEnglish) "STEP ${currentIndex + 1} OF ${segments.size} · FLOOR ${currentSegment.floorName}" else "PASO ${currentIndex + 1} DE ${segments.size} · PLANTA ${currentSegment.floorName}",
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
                val actionName = if (isEnglish) { if (currentSegment.isElevator) "the elevator 🛗" else "the stairs 🪜" } else { if (currentSegment.isElevator) "el ascensor 🛗" else "las escaleras 🪜" }
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
                            text = if (isEnglish) "Head to $actionName on floor ${currentSegment.floorName}" else "Dirígete hacia $actionName en Planta ${currentSegment.floorName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = if (isEnglish) "Marked in orange 🟠 on the map" else "Marcado en naranja 🟠 en el plano",
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
                        text = if (isEnglish) "I've reached $actionName ➔ Go to floor $nextFloorName" else "He llegado a $actionName ➔ Ir a Planta $nextFloorName",
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
                            text = if (isEnglish) "Final stretch to $destPoiName" else "Tramo final hacia $destPoiName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = if (isEnglish) "Route drawn on floor ${currentSegment.floorName} from the intermediate node" else "Ruta dibujada en Planta ${currentSegment.floorName} desde el nodo intermedio",
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
                        text = if (isEnglish) "🏁 Destination reached! Finish guidance" else "🏁 ¡Llegada al Destino! Finalizar guía",
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
                            Text(text = if (isEnglish) "⬅ Floor ${segments[currentIndex - 1].floorName}" else "⬅ Planta ${segments[currentIndex - 1].floorName}", fontSize = 11.sp, color = Ink)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentIndex < segments.size - 1) {
                        OutlinedButton(
                            onClick = { onSegmentChange(currentIndex + 1) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "${if (isEnglish) "Floor" else "Planta"} ${segments[currentIndex + 1].floorName} ➔", fontSize = 11.sp, color = Ink)
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
    isSpecialDest: Boolean = false,
    destinationError: String?,
    onStartPoiChange: (PointOfInterest) -> Unit,
    onDestPoiChange: (PointOfInterest) -> Unit,
    onSpecialDestChange: (String) -> Unit,
    isAccessible: Boolean,
    onAccessibleToggle: (Boolean) -> Unit,
    onScanQrClick: () -> Unit,
    isCityMode: Boolean = false,
    cityOriginDisplayText: String? = null,
    cityDestinationDisplayText: String? = null,
    isCityMapOriginSelectionActive: Boolean = false,
    onCityOriginClick: () -> Unit = {},
    onCityDestinationClick: () -> Unit = {},
    onCityMapOriginClick: () -> Unit = {},
    onCitySwapClick: () -> Unit = {}
) {
    val isEnglish = LocalIsEnglish.current
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
                                poi = if (isCityMode) null else startPoi,
                                displayText = if (isCityMode) cityOriginDisplayText else null,
                                isSelected = if (isCityMode) {
                                    !cityOriginDisplayText.isNullOrBlank()
                                } else {
                                    startPoi != null
                                },
                                placeholder = if (isEnglish) "Origin" else "Origen",
                                onClick = {
                                    if (isCityMode) {
                                        onCityOriginClick()
                                    } else {
                                        pickingStart = true
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        if (isCityMode) {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        onCityMapOriginClick()
                                    },
                                color = if (isCityMapOriginSelectionActive) {
                                    UgrRed
                                } else {
                                    Color.White
                                },
                                shape = RoundedCornerShape(11.dp),
                                border = if (isCityMapOriginSelectionActive) {
                                    null
                                } else {
                                    androidx.compose.foundation.BorderStroke(1.dp, UgrRed)
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.TouchApp,
                                        contentDescription = null,
                                        tint = if (isCityMapOriginSelectionActive) Color.White else UgrRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onScanQrClick() },
                                color = UgrRed,
                                shape = RoundedCornerShape(11.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.QrCodeScanner,
                                        contentDescription = if (isEnglish) {
                                            "Scan a location QR code"
                                        } else {
                                            "Escanear QR de ubicación"
                                        },
                                        tint = Color.White,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
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
                                poi = if (isCityMode) null else destPoi,
                                displayText = if (isCityMode) {
                                    cityDestinationDisplayText?.takeIf { it != "ETSIIT" && it.isNotBlank() }
                                } else {
                                    selectedDestLabel
                                },
                                isSelected = if (isCityMode) {
                                    !cityDestinationDisplayText.isNullOrBlank() &&
                                            cityDestinationDisplayText != "Seleccionar Destino" &&
                                            cityDestinationDisplayText != "Select Destination" &&
                                            cityDestinationDisplayText != "ETSIIT"
                                } else {
                                    destPoi != null
                                },
                                showCategoryIcon = !isSpecialDest,
                                placeholder = if (isEnglish) "Select Destination" else "Seleccionar Destino",
                                onClick = {
                                    if (isCityMode) {
                                        onCityDestinationClick()
                                    } else {
                                        pickingDest = true
                                    }
                                }
                            )
                        }

                        val canSwap = if (isCityMode) {
                            !cityDestinationDisplayText.isNullOrBlank()
                        } else {
                            startPoi != null && destPoi != null
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(enabled = canSwap) {
                                    if (isCityMode) {
                                        onCitySwapClick()
                                    } else {
                                        if (startPoi != null && destPoi != null) {
                                            onStartPoiChange(destPoi)
                                            onDestPoiChange(startPoi)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = null,
                                tint = if (canSwap) UgrRed else MutedText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))
            }

            if (!isCityMode && pickingStart) {
                PoiPickerSheet(
                    title = if (isEnglish) "Choose origin" else "Elige el origen",
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

            if (!isCityMode && pickingDest) {
                PoiPickerSheet(
                    title = if (isEnglish) "Choose destination" else "Elige el destino",
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
    onClick: () -> Unit,
    isSelected: Boolean = poi != null,
    showCategoryIcon: Boolean = true
) {
    val isEnglish = LocalIsEnglish.current

    val label = displayText
        ?: poi?.getName(isEnglish)
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
            if (poi != null && showCategoryIcon) {
                Icon(
                    imageVector = poiCategoryIcon(poi.category),
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Ink else MutedText,
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
    val context = LocalContext.current
    val isEnglish = LocalIsEnglish.current

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
            Icon(
                imageVector = poiCategoryIcon(poi.category),
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = poi.getName(isEnglish), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
    }
}
