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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.GreenStatus
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import kotlinx.coroutines.launch

fun getFloorName(floor: Int): String = when (floor) {
    4 -> "P4"
    3 -> "P3"
    2 -> "P2"
    1 -> "P1"
    0 -> "PB"
    -1 -> "S1"
    else -> "P$floor"
}

/**
 * Representa el tramo de ruta correspondiente a una sola planta.
 *
 * @property floorNumber Número de la planta (-1, 0, 1, 2, 3, 4)
 * @property floorName Nombre legible de la planta ("S1", "PB", "P1", etc.)
 * @property segmentNodes Lista ordenada de nodos de la ruta devuelta por RouteManager que pertenecen a esta planta
 * @property transitionNode Nodo intermedio (escalera o ascensor) donde se realiza el cambio de planta
 * @property nextFloorNumber Planta a la que se asciende o desciende
 * @property nextFloorName Nombre legible de la planta de destino
 * @property isElevator Indica si la transición entre plantas es por ascensor (true) o escaleras (false)
 */
data class RouteFloorSegment(
    val floorNumber: Int,
    val floorName: String,
    val segmentNodes: List<Node>,
    val transitionNode: Node?,
    val nextFloorNumber: Int?,
    val nextFloorName: String?,
    val isElevator: Boolean
)

/**
 * Parsea estrictamente la ruta devuelta por RouteManager.calculateRoute,
 * identificando los nodos intermedios (escaleras/ascensores) y agrupando los nodos por planta.
 */
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

/**
 * @param initialDestPoiId Si llega un POI (desde Horario, Profesorado o Inicio), se abre directamente la ruta hasta él.
 */
@Composable
fun MapScreen(initialDestPoiId: String? = null) {
    val routeManager = remember { RouteManager() }
    val allPois = remember { EtsiitData.pois }

    var mapMode by rememberSaveable { mutableStateOf("indoor") } // "city" or "indoor"
    var selectedFloor by rememberSaveable { mutableStateOf("P1") }

    // Routing State
    var startPoiId by rememberSaveable { mutableStateOf("poi_entrada") }
    var destPoiId by rememberSaveable(initialDestPoiId) { mutableStateOf(initialDestPoiId ?: "poi_aula12") }
    var isRouteOpen by rememberSaveable(initialDestPoiId) { mutableStateOf(initialDestPoiId != null) }
    var isAccessibleRoute by rememberSaveable { mutableStateOf(false) }

    // Sensores de localización: GPS fuera del edificio; barómetro, brújula y pasos dentro
    val context = LocalContext.current
    val inForeground = rememberAppInForeground()
    var hasLocationPermission by remember { mutableStateOf(LocationPermissions.hasLocation(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasLocationPermission = LocationPermissions.hasLocation(context)
    }
    val gpsLocation = rememberGpsLocation(active = inForeground && hasLocationPermission)
    val hasBarometer = remember { hasSensor(context, Sensor.TYPE_PRESSURE) }
    val tracker = remember { IndoorTracker(LocationPrefs.mapNorth(context)) }
    var isTracking by rememberSaveable { mutableStateOf(false) }
    val indoorSensorsActive = inForeground && mapMode == "indoor"
    PressureEffect(active = indoorSensorsActive) { hPa, timestampMs -> tracker.onPressure(hPa, timestampMs) }
    val azimuth = rememberCompassAzimuth(active = indoorSensorsActive)

    // 1. Obtener la ruta calculada por Dijkstra en RouteManager.calculateRoute
    val calculatedRoute = remember(startPoiId, destPoiId, isAccessibleRoute, isRouteOpen) {
        if (isRouteOpen && startPoiId.isNotEmpty() && destPoiId.isNotEmpty()) {
            routeManager.calculateRoute(startPoiId, destPoiId, isAccessibleRoute)
        } else {
            emptyList()
        }
    }

    // 2. Parsear estrictamente la ruta calculada en tramos por planta según los nodos intermedios de EtsiitData
    val routeSegments = remember(calculatedRoute) { parseRouteSegments(calculatedRoute) }
    var activeSegmentIndex by rememberSaveable(routeSegments) { mutableIntStateOf(0) }

    // 3. Sincronizar selectedFloor con la planta del tramo activo.
    // Al empezar cada tramo suponemos que la persona está en su primer nodo: ahí se calibran
    // el barómetro (planta conocida) y la posición de partida de los pasos.
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

    // 4. Si el barómetro detecta que ya estamos en la planta siguiente, pasamos solos al siguiente tramo
    LaunchedEffect(tracker.estimatedFloor) {
        val floor = tracker.estimatedFloor ?: return@LaunchedEffect
        val segment = routeSegments.getOrNull(activeSegmentIndex)
        if (isRouteOpen && segment?.nextFloorNumber == floor) {
            activeSegmentIndex += 1
        } else if (!isRouteOpen) {
            selectedFloor = getFloorName(floor)
        }
    }

    val startPoi = remember(startPoiId) { allPois.find { it.id == startPoiId } }
    val destPoi = remember(destPoiId) { allPois.find { it.id == destPoiId } }
    val activeSegment = routeSegments.getOrNull(activeSegmentIndex)

    StepEffect(active = indoorSensorsActive && isTracking && isRouteOpen) { tracker.onStep(azimuth) }
    // El punto azul solo tiene sentido en la planta del tramo que se está siguiendo
    val userPosition = tracker.position.takeIf { isTracking && isRouteOpen && activeSegment?.floorName == selectedFloor }
    val userHeadingMapDeg = azimuth?.let { it - tracker.mapNorthDeg }

    // Al abrir una ruta, bajamos hasta el selector de plantas para ver a la vez el plano y los pasos
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
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(18.dp))

        // Toggle Switch: City vs Indoor
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable { mapMode = "indoor" },
                    color = if (mapMode == "indoor") UgrRed else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🏢 Plano Interior (ETSIIT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mapMode == "indoor") Color.White else Ink
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable { mapMode = "city" },
                    color = if (mapMode == "city") UgrRed else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🗺️ Mapa Ciudad",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mapMode == "city") Color.White else Ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (mapMode == "city") {
            // Mapa de Granada (OpenStreetMap) con ruta a pie
            Text(
                text = "Cómo llegar a pie",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                text = "Toca el mapa o elige un origen, y el destino",
                fontSize = 11.sp,
                color = MutedText,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            CityRouteSection(
                gpsLocation = gpsLocation,
                hasLocationPermission = hasLocationPermission,
                onRequestPermission = { permissionLauncher.launch(LocationPermissions.all) },
                onContinueIndoors = {
                    mapMode = "indoor"
                    scope.launch { scrollState.scrollTo(0) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // City Map View
            Text(
                text = "Campus Universitarios UGR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Spacer(modifier = Modifier.height(12.dp))

            SampleData.campuses.forEach { campus ->
                CampusCard(campus = campus)
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            // Indoor Building Plan View
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

            // Floor Selector Section
            Text(
                text = "Seleccionar Planta",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.onGloballyPositioned { floorSectionTop = it.positionInParent().y.toInt() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Floors Row
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            // Floor Plan View with Active Route Segment Overlay
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ToneBlueBg,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Plano de la Planta $selectedFloor",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            if (isRouteOpen && activeSegment != null) {
                                Text(
                                    text = "🔴 Tramo en Planta ${activeSegment.floorName} (Paso ${activeSegmentIndex + 1}/${routeSegments.size})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UgrRed
                                )
                            }
                        }
                        Text(text = "ETSIIT · Modelo 3D", fontSize = 10.sp, color = MutedText)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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

                    if (userPosition != null && activeSegment != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        NavigationHintStrip(
                            tracker = tracker,
                            azimuthDeg = azimuth,
                            arrivalText = arrivalText(activeSegment, destPoi?.nameEs ?: "tu destino")
                        )
                    }
                }
            }

            // Pasos de la ruta justo debajo del plano, para seguirlos sin perder el mapa de vista
            if (isRouteOpen && routeSegments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                RouteStepNavigationCard(
                    segments = routeSegments,
                    currentIndex = activeSegmentIndex,
                    startPoiName = startPoi?.nameEs ?: "Origen",
                    destPoiName = destPoi?.nameEs ?: "Destino",
                    onSegmentChange = { activeSegmentIndex = it },
                    onClose = { isRouteOpen = false }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // Selector de Origen y Destino para la Ruta
            Text(
                text = "Navegación Interactiva",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Spacer(modifier = Modifier.height(10.dp))

            PoiRouteSelectorCard(
                pois = allPois,
                startPoi = startPoi,
                destPoi = destPoi,
                onStartPoiChange = { startPoiId = it.id },
                onDestPoiChange = { destPoiId = it.id },
                isAccessible = isAccessibleRoute,
                onAccessibleToggle = { isAccessibleRoute = it },
                onCalculateRoute = {
                    isRouteOpen = true
                    activeSegmentIndex = 0
                },
                onClearRoute = { isRouteOpen = false }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Points of Interest Quick Selector
            Text(
                text = "Puntos de Interés Rápidos",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(allPois.take(8)) { poi ->
                    PoiChip(poi = poi) {
                        destPoiId = poi.id
                        isRouteOpen = true
                        activeSegmentIndex = 0
                    }
                }
            }


        }

        Spacer(modifier = Modifier.height(30.dp))
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
                .height(280.dp)
                .clip(RoundedCornerShape(14.dp))
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
                        contentScale = ContentScale.Fit
                    )
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (nodesToDraw.isEmpty()) return@Canvas

                    val refWidth = 1000f
                    val refHeight = 1000f
                    val scaleX = size.width / refWidth
                    val scaleY = size.height / refHeight

                    // 1. Dibujar líneas estrictamente conectando los nodos del tramo actual devueltos por RouteManager
                    for (i in 0 until (nodesToDraw.size - 1)) {
                        val n1 = nodesToDraw[i]
                        val n2 = nodesToDraw[i + 1]

                        drawLine(
                            color = Color(0xFFD32F2F), // UgrRed
                            start = Offset(n1.x * scaleX, n1.y * scaleY),
                            end = Offset(n2.x * scaleX, n2.y * scaleY),
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 2. Dibujar nodos sobre el mapa
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
                                    color = Color(0xFF2E7D32), // Verde Origen
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
                                    color = Color(0xFFD32F2F), // Rojo Destino
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
                                    color = Color(0xFFE65100), // Naranja Nodo Intermedio Escalera/Ascensor
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

            // Quick Zoom & Fullscreen Action Bar
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
                // Top Header Bar
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

                // Interactive Zoomable Map Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 100.dp)
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
                            contentScale = ContentScale.Fit
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

                // Floating Zoom Controls Bar
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
            // Header
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

            // Instructions for transition node or destination node
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

                // Action button when arrived at stairs/elevator
                Button(
                    onClick = {
                        if (currentIndex < segments.size - 1) {
                            onSegmentChange(currentIndex + 1)
                        }
                    },
                    // Altura mínima, no fija: si el texto ocupa dos líneas el botón crece
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
                // Final Segment
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

            // Manual segment switcher controls
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
    onStartPoiChange: (PointOfInterest) -> Unit,
    onDestPoiChange: (PointOfInterest) -> Unit,
    isAccessible: Boolean,
    onAccessibleToggle: (Boolean) -> Unit,
    onCalculateRoute: () -> Unit,
    onClearRoute: () -> Unit
) {
    var expandedStart by remember { mutableStateOf(false) }
    var expandedDest by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Origen Selector
            Text(text = "🟢 Punto de Origen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(modifier = Modifier.height(4.dp))
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedStart = true },
                    color = SoftBg,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = startPoi?.nameEs ?: "Seleccionar Origen",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                DropdownMenu(
                    expanded = expandedStart,
                    onDismissRequest = { expandedStart = false }
                ) {
                    pois.forEach { poi ->
                        DropdownMenuItem(
                            text = { Text("${poi.nameEs} (${poi.category})") },
                            onClick = {
                                onStartPoiChange(poi)
                                expandedStart = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Destino Selector
            Text(text = "🔴 Punto de Destino", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(modifier = Modifier.height(4.dp))
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDest = true },
                    color = SoftBg,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = destPoi?.nameEs ?: "Seleccionar Destino",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                DropdownMenu(
                    expanded = expandedDest,
                    onDismissRequest = { expandedDest = false }
                ) {
                    pois.forEach { poi ->
                        DropdownMenuItem(
                            text = { Text("${poi.nameEs} (${poi.category})") },
                            onClick = {
                                onDestPoiChange(poi)
                                expandedDest = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switch PMR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "♿ Ruta accesible PMR (evitar escaleras)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Switch(
                    checked = isAccessible,
                    onCheckedChange = onAccessibleToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = UgrRed)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onCalculateRoute,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "📍 Dibujar Ruta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onClearRoute,
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Limpiar", fontSize = 12.sp, color = Ink)
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
            Text(text = poi.nameEs, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "(${poi.category})", fontSize = 9.sp, color = MutedText)
        }
    }
}
