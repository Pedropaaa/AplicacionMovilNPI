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
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueText
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.aplicacionmovilnpi.qr.QrLocationResolver
import com.example.aplicacionmovilnpi.qr.QrLocationScannerScreen
import com.example.aplicacionmovilnpi.qr.RouteQrScreen
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

@Composable
fun MapScreen(initialDestPoiId: String? = null) {
    val routeManager = remember { RouteManager() }
    val allPois = remember { EtsiitData.pois }

    // 1. DECLARACIONES DE ESTADO
    var mapMode by rememberSaveable { mutableStateOf("indoor") }
    var selectedFloor by rememberSaveable { mutableStateOf("P1") }

    // Routing State
    var startPoiId by rememberSaveable { mutableStateOf("poi_entrada") }
    var destPoiId by rememberSaveable(initialDestPoiId) { mutableStateOf(initialDestPoiId ?: "poi_aula12") }
    var isRouteOpen by rememberSaveable(initialDestPoiId) { mutableStateOf(initialDestPoiId != null) }
    var isAccessibleRoute by rememberSaveable { mutableStateOf(false) }

    // Estado para el escaner QR y generación de QR de ruta
    var isScanningQr by rememberSaveable { mutableStateOf(false) }
    var showRouteQr by rememberSaveable { mutableStateOf(false) }
    var routeTextForQr by rememberSaveable { mutableStateOf("") }

    // Declaración anticipada de la ruta y los segmentos para que activeSegmentIndex esté disponible en el escáner
    val calculatedRoute = remember(startPoiId, destPoiId, isAccessibleRoute, isRouteOpen) {
        if (isRouteOpen && startPoiId.isNotEmpty() && destPoiId.isNotEmpty()) {
            routeManager.calculateRoute(startPoiId, destPoiId, isAccessibleRoute)
        } else {
            emptyList()
        }
    }

    val routeSegments = remember(calculatedRoute) { parseRouteSegments(calculatedRoute) }
    var activeSegmentIndex by rememberSaveable(routeSegments) { mutableIntStateOf(0) }

    // 2. SENSORES DE LOCALIZACIÓN
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

    // Si el usuario presiona escanear QR, abrimos la vista de cámara
    if (isScanningQr) {
        QrLocationScannerScreen(
            onNodeDetected = { detectedNode ->
                val routeIds = QrLocationResolver.scannedRouteNodeIds
                if (routeIds.isNotEmpty()) {
                    val firstNodeId = routeIds.first()
                    val lastNodeId = routeIds.last()

                    val startPoi = allPois.find { it.nodeId == firstNodeId }
                    val destPoi = allPois.find { it.nodeId == lastNodeId }

                    if (startPoi != null) startPoiId = startPoi.id
                    if (destPoi != null) destPoiId = destPoi.id

                    isRouteOpen = true
                    activeSegmentIndex = 0
                } else {
                    val matchingPoi = allPois.find { it.nodeId == detectedNode.id }
                    if (matchingPoi != null) {
                        startPoiId = matchingPoi.id
                    }
                }
                selectedFloor = getFloorName(detectedNode.floor)
                isScanningQr = false
            },
            onCancel = {
                isScanningQr = false
            }
        )
        return
    }

    // Si se activa la pantalla del QR de la ruta generada
    if (showRouteQr) {
        RouteQrScreen(
            routeDetails = routeTextForQr,
            onClose = { showRouteQr = false }
        )
        return
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

    val startPoi = remember(startPoiId) { allPois.find { it.id == startPoiId } }
    val destPoi = remember(destPoiId) { allPois.find { it.id == destPoiId } }
    val activeSegment = routeSegments.getOrNull(activeSegmentIndex)

    StepEffect(active = indoorSensorsActive && isTracking && isRouteOpen) { tracker.onStep(azimuth) }
    val userPosition = tracker.position.takeIf { isTracking && isRouteOpen && activeSegment?.floorName == selectedFloor }
    val userHeadingMapDeg = azimuth?.let { it - LocationPrefs.mapNorth(context) }

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

        // La navegación interactiva solo aplica al plano interior
        if (mapMode == "indoor") {
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

            Spacer(modifier = Modifier.height(14.dp))
        }

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
                    // Planos apilados: cada planta lleva debajo las inferiores (ver scripts/apilar_plantas.py)
                    val inputStream = context.assets.open("Modelo 3D ETSIIT/Apiladas/$floorNumber.jpg")
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }.getOrNull()
            }

            Text(
                text = "Seleccionar Planta",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.onGloballyPositioned { floorSectionTop = it.positionInParent().y.toInt() }
            )

            Spacer(modifier = Modifier.height(10.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                // BOTÓN AÑADIDO: Generar QR con la ruta actual calculada[cite: 3]
                Button(
                    onClick = {
                        if (calculatedRoute.isNotEmpty()) {
                            routeTextForQr = calculatedRoute.joinToString(",") { it.id }
                            showRouteQr = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "📷 Generar Código QR de esta Ruta",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tu botón de escaneo QR
            QrScanButton(
                onScanClick = { isScanningQr = true }
            )

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

            Spacer(modifier = Modifier.height(20.dp))

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
                .height(350.dp)
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
    onStartPoiChange: (PointOfInterest) -> Unit,
    onDestPoiChange: (PointOfInterest) -> Unit,
    isAccessible: Boolean,
    onAccessibleToggle: (Boolean) -> Unit,
    onCalculateRoute: () -> Unit,
    onClearRoute: () -> Unit
) {
    var pickingStart by remember { mutableStateOf(false) }
    var pickingDest by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Raíl a la izquierda: punto de origen, línea de puntos y punto de destino
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
                    PoiSelectorField(
                        poi = startPoi,
                        placeholder = "Origen",
                        onClick = { pickingStart = true }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    PoiSelectorField(
                        poi = destPoi,
                        placeholder = "¿A dónde vas?",
                        onClick = { pickingDest = true }
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // Intercambiar origen y destino
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
                    Text(text = "⇅", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MutedText)
                }
            }

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

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de acciones: accesible · limpiar · ir
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
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(text = "Ir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
    placeholder: String,
    onClick: () -> Unit
) {
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
                text = poi?.let { "${poiCategoryIcon(it.category)} ${it.nameEs}" } ?: placeholder,
                fontSize = 13.sp,
                fontWeight = if (poi != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (poi != null) Ink else MutedText,
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