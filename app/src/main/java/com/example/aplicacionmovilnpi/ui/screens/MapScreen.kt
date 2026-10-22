package com.example.aplicacionmovilnpi.ui.screens

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aplicacionmovilnpi.data.CampusItem
import com.example.aplicacionmovilnpi.data.SampleData
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
import kotlin.math.hypot

data class ImagePlacement(
    val offsetX: Float,
    val offsetY: Float,
    val drawnWidth: Float,
    val drawnHeight: Float
) {
    fun mapToScreen(x: Float, y: Float): Offset {
        val px = offsetX + (x / 1000f) * drawnWidth
        val py = offsetY + (y / 1000f) * drawnHeight
        return Offset(px, py)
    }

    fun mapToNormalized(screenX: Float, screenY: Float): Pair<Float, Float> {
        val mapX = (((screenX - offsetX) / drawnWidth) * 1000f).coerceIn(0f, 1000f)
        val mapY = (((screenY - offsetY) / drawnHeight) * 1000f).coerceIn(0f, 1000f)
        return Pair(mapX, mapY)
    }
}

fun calculateImagePlacement(
    canvasWidth: Float,
    canvasHeight: Float,
    bitmap: ImageBitmap?
): ImagePlacement {
    if (canvasWidth <= 0f || canvasHeight <= 0f) {
        return ImagePlacement(0f, 0f, 1000f, 1000f)
    }
    val bmpWidth = bitmap?.width?.toFloat() ?: 1000f
    val bmpHeight = bitmap?.height?.toFloat() ?: 1000f
    val imageAspect = bmpWidth / bmpHeight
    val canvasAspect = canvasWidth / canvasHeight

    return if (imageAspect > canvasAspect) {
        val drawnWidth = canvasWidth
        val drawnHeight = canvasWidth / imageAspect
        val offsetX = 0f
        val offsetY = (canvasHeight - drawnHeight) / 2f
        ImagePlacement(offsetX, offsetY, drawnWidth, drawnHeight)
    } else {
        val drawnHeight = canvasHeight
        val drawnWidth = canvasHeight * imageAspect
        val offsetX = (canvasWidth - drawnWidth) / 2f
        val offsetY = 0f
        ImagePlacement(offsetX, offsetY, drawnWidth, drawnHeight)
    }
}

fun getFloorName(floor: Int): String = when (floor) {
    4 -> "P4"
    3 -> "P3"
    2 -> "P2"
    1 -> "P1"
    0 -> "PB"
    -1 -> "S1"
    else -> "P$floor"
}

fun getNodeDisplayName(nodeId: String?, pois: List<PointOfInterest>): String {
    if (nodeId == null) return ""
    val poi = pois.find { it.nodeId == nodeId }
    if (poi != null) return poi.nameEs

    return nodeId.removePrefix("n_")
        .replace("_p0", " (PB)")
        .replace("_p1", " (P1)")
        .replace("_p2", " (P2)")
        .replace("_p3", " (P3)")
        .replace("_p4", " (P4)")
        .replace("_p-1", " (S1)")
        .replace("_", " ")
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

/**
 * Representa el tramo de ruta correspondiente a una sola planta.
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
 * Parsea la ruta devuelta por RouteManager.calculateRoute agrupando por plantas.
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

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val routeManager = remember { RouteManager() }
    val allPois = remember { EtsiitData.pois }

    // Map Modes
    var mapMode by remember { mutableStateOf("indoor") } // "city" or "indoor"
    var selectedFloor by remember { mutableStateOf("P1") }

    // Node Calibrator / Inspector State
    var customNodesMap by remember { mutableStateOf(EtsiitData.nodes.toMutableMap()) }
    var isCalibrationMode by remember { mutableStateOf(false) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var lastTappedCoord by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    // Routing State
    var startPoiId by remember { mutableStateOf("poi_entrada") }
    var destPoiId by remember { mutableStateOf("poi_aula12") }
    var isRouteOpen by remember { mutableStateOf(false) }
    var isAccessibleRoute by remember { mutableStateOf(false) }

    // 1. Obtener la ruta calculada por Dijkstra en RouteManager.calculateRoute
    val calculatedRoute = remember(startPoiId, destPoiId, isAccessibleRoute, isRouteOpen, customNodesMap) {
        if (isRouteOpen && startPoiId.isNotEmpty() && destPoiId.isNotEmpty()) {
            routeManager.calculateRoute(startPoiId, destPoiId, isAccessibleRoute)
                .map { customNodesMap[it.id] ?: it }
        } else {
            emptyList()
        }
    }

    // 2. Estado de guiado PASO A PASO (nodo por nodo)
    var currentStepIndex by remember(calculatedRoute) { mutableIntStateOf(0) }

    // 3. Sincronización automática de planta cuando avanza el nodo actual
    val currentRouteNode = calculatedRoute.getOrNull(currentStepIndex)
    LaunchedEffect(currentStepIndex, calculatedRoute) {
        if (calculatedRoute.isNotEmpty() && currentRouteNode != null) {
            selectedFloor = getFloorName(currentRouteNode.floor)
        }
    }

    val routeSegments = remember(calculatedRoute) { parseRouteSegments(calculatedRoute) }
    val activeSegmentIndex = remember(selectedFloor, routeSegments) {
        val idx = routeSegments.indexOfFirst { it.floorName == selectedFloor }
        if (idx != -1) idx else 0
    }

    val startPoi = remember(startPoiId) { allPois.find { it.id == startPoiId } }
    val destPoi = remember(destPoiId) { allPois.find { it.id == destPoiId } }
    val activeSegment = routeSegments.getOrNull(activeSegmentIndex)

    val floorNumber = when (selectedFloor) {
        "P4" -> 4
        "P3" -> 3
        "P2" -> 2
        "P1" -> 1
        "PB" -> 0
        "S1" -> -1
        else -> 0
    }

    val allFloorNodes = remember(customNodesMap, floorNumber) {
        customNodesMap.values.filter { it.floor == floorNumber }
    }

    val allFloorEdges = remember(customNodesMap, floorNumber) {
        EtsiitData.edges.filter { edge ->
            val n1 = customNodesMap[edge.from]
            val n2 = customNodesMap[edge.to]
            n1?.floor == floorNumber && n2?.floor == floorNumber
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
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
            var useCompletaImageManual by remember { mutableStateOf(false) }
            val outdoorCompletaNodes = remember {
                setOf(
                    "n_entrada",
                    "n_patio"
                )
            }

            val isCompletaImage = useCompletaImageManual ||
                    (currentRouteNode?.id in outdoorCompletaNodes) ||
                    (selectedNodeId in outdoorCompletaNodes)

            val imagePath = if (isCompletaImage) {
                "Modelo 3D ETSIIT/ConSuelo/completa.jpg"
            } else {
                "Modelo 3D ETSIIT/ConSuelo/$floorNumber.jpg"
            }

            val floorImageBitmap = remember(floorNumber, isCompletaImage) {
                runCatching {
                    val inputStream = context.assets.open(imagePath)
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }.getOrNull()
            }

            // Calibration Mode Switch Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isCalibrationMode) Color(0xFFFFF8E1) else Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCalibrationMode) Color(0xFFFFB300) else CardBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛠️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Modo Inspector / Calibrador de Nodos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = "Ajuste preciso de coordenadas con cruceta de flechas",
                                fontSize = 10.sp,
                                color = MutedText
                            )
                        }
                    }

                    Switch(
                        checked = isCalibrationMode,
                        onCheckedChange = { isCalibrationMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFF8F00),
                            checkedTrackColor = Color(0xFFFFECB3)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Floor Selector Section
            Text(
                text = "Seleccionar Planta",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
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
                                selectedNodeId = null
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

            // Calibration Control Panel (Visible when Calibration Mode is ON)
            if (isCalibrationMode) {
                CalibrationControlPanel(
                    selectedFloorName = selectedFloor,
                    floorNodes = allFloorNodes,
                    selectedNodeId = selectedNodeId,
                    lastTappedCoord = lastTappedCoord,
                    onSelectNode = { selectedNodeId = it },
                    onNudgeNode = { dx, dy ->
                        selectedNodeId?.let { id ->
                            val current = customNodesMap[id] ?: return@let
                            val updated = current.copy(
                                x = (current.x + dx).coerceIn(0f, 1000f),
                                y = (current.y + dy).coerceIn(0f, 1000f)
                            )
                            customNodesMap = customNodesMap.toMutableMap().apply { put(id, updated) }
                        }
                    },
                    onResetNodes = {
                        customNodesMap = EtsiitData.nodes.toMutableMap()
                        selectedNodeId = null
                        lastTappedCoord = null
                        Toast.makeText(context, "Coordenadas restablecidas a valores iniciales", Toast.LENGTH_SHORT).show()
                    },
                    onCopyKotlinCode = {
                        val kotlinCode = StringBuilder().apply {
                            appendLine("// --- PLANTA $selectedFloor ---")
                            allFloorNodes.forEach { node ->
                                appendLine("Node(\"${node.id}\", x = ${node.x.toInt()}f, y = ${node.y.toInt()}f, floor = ${node.floor}),")
                            }
                        }.toString()

                        clipboardManager.setText(AnnotatedString(kotlinCode))
                        Log.d("NPI_NODES_CALIBRATOR", kotlinCode)
                        Toast.makeText(
                            context,
                            "¡Código de Planta $selectedFloor (${allFloorNodes.size} nodos) copiado!",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onCopyAllKotlinCode = {
                        val kotlinCode = StringBuilder().apply {
                            appendLine("// --- TODOS LOS NODOS ACTUALIZADOS PARA EtsiitData.kt ---")
                            listOf(-1, 0, 1, 2, 3, 4).forEach { f ->
                                val fName = getFloorName(f)
                                appendLine("// --- PLANTA $fName ---")
                                customNodesMap.values.filter { it.floor == f }.forEach { node ->
                                    appendLine("Node(\"${node.id}\", x = ${node.x.toInt()}f, y = ${node.y.toInt()}f, floor = ${node.floor}),")
                                }
                            }
                        }.toString()

                        clipboardManager.setText(AnnotatedString(kotlinCode))
                        Log.d("NPI_NODES_CALIBRATOR", kotlinCode)
                        Toast.makeText(
                            context,
                            "¡Código de TODOS los nodos (${customNodesMap.size}) copiado al portapapeles!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Floor Plan View with Active Route Segment or Calibration Overlay
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isCompletaImage) "Plano Completo (Entrada / Patio / Exterior)" else "Plano de la Planta $selectedFloor",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            if (isCalibrationMode) {
                                Text(
                                    text = "🛠️ MODO CALIBRADOR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            } else if (isRouteOpen && calculatedRoute.isNotEmpty()) {
                                Text(
                                    text = "🔴 Guiado Paso ${currentStepIndex + 1}/${calculatedRoute.size} en Planta $selectedFloor",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UgrRed
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.clickable { useCompletaImageManual = !useCompletaImageManual },
                            color = if (isCompletaImage) Color(0xFFE8F5E9) else Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = if (isCompletaImage) "🖼️ Ver PB" else "🖼️ Ver Completo",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FloorPlanCanvasOverlay(
                        bitmap = floorImageBitmap,
                        selectedFloorName = selectedFloor,
                        activeSegment = activeSegment,
                        fullRoute = calculatedRoute,
                        currentStepIndex = currentStepIndex,
                        isFirstSegment = activeSegmentIndex == 0,
                        isLastSegment = activeSegmentIndex == routeSegments.size - 1,
                        isCalibrationMode = isCalibrationMode,
                        allFloorNodes = allFloorNodes,
                        allFloorEdges = allFloorEdges,
                        customNodesMap = customNodesMap,
                        selectedNodeId = selectedNodeId,
                        lastTappedCoord = lastTappedCoord,
                        onSelectNode = { selectedNodeId = it },
                        onNudgeNode = { dx, dy ->
                            selectedNodeId?.let { id ->
                                val current = customNodesMap[id] ?: return@FloorPlanCanvasOverlay
                                val updated = current.copy(
                                    x = (current.x + dx).coerceIn(0f, 1000f),
                                    y = (current.y + dy).coerceIn(0f, 1000f)
                                )
                                customNodesMap = customNodesMap.toMutableMap().apply { put(id, updated) }
                            }
                        },
                        onTapMapCoordinate = { mapX, mapY ->
                            lastTappedCoord = Pair(mapX, mapY)
                        }
                    )
                }
            }

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
                    currentStepIndex = 0
                },
                onClearRoute = {
                    isRouteOpen = false
                    currentStepIndex = 0
                }
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
                        currentStepIndex = 0
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step-by-Step Node-by-Node Navigation Guidance Card
            if (isRouteOpen && calculatedRoute.isNotEmpty()) {
                RouteStepNavigationCard(
                    calculatedRoute = calculatedRoute,
                    currentStepIndex = currentStepIndex,
                    allPois = allPois,
                    startPoiName = startPoi?.nameEs ?: "Origen",
                    destPoiName = destPoi?.nameEs ?: "Destino",
                    onStepIndexChange = { newIdx ->
                        currentStepIndex = newIdx.coerceIn(0, calculatedRoute.size - 1)
                    },
                    onClose = {
                        isRouteOpen = false
                        currentStepIndex = 0
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun CalibrationControlPanel(
    selectedFloorName: String,
    floorNodes: List<Node>,
    selectedNodeId: String?,
    lastTappedCoord: Pair<Float, Float>?,
    onSelectNode: (String?) -> Unit,
    onNudgeNode: (dx: Float, dy: Float) -> Unit,
    onResetNodes: () -> Unit,
    onCopyKotlinCode: () -> Unit,
    onCopyAllKotlinCode: () -> Unit
) {
    val selectedNode = floorNodes.find { it.id == selectedNodeId }
    var stepSize by remember { mutableStateOf(5f) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛠️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Panel de Calibración de Nodos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }

                Surface(
                    color = Color(0xFFFFE082),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Planta $selectedFloorName (${floorNodes.size} nodos)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📌 Selecciona un nodo en la lista o mapa y usa la cruceta de flechas para desplazarlo con precisión.",
                fontSize = 11.sp,
                color = Ink.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Node Selector Chips
            Text(
                text = "Lista de nodos en Planta $selectedFloorName:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(floorNodes) { node ->
                    val isSel = node.id == selectedNodeId
                    Surface(
                        modifier = Modifier.clickable { onSelectNode(if (isSel) null else node.id) },
                        color = if (isSel) UgrRed else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = if (isSel) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = node.id.removePrefix("n_"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else Ink,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // D-Pad Arrow Controls for Active Node
            if (selectedNode != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFC107))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 Nodo activo: ${selectedNode.id}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = UgrRed
                            )
                            Text(
                                text = "✖ Deseleccionar",
                                fontSize = 11.sp,
                                color = MutedText,
                                modifier = Modifier.clickable { onSelectNode(null) }
                            )
                        }

                        Text(
                            text = "Coordenadas: x = ${selectedNode.x.toInt()}f, y = ${selectedNode.y.toInt()}f",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Step Size Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "Paso:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                            listOf(1f, 5f, 10f, 25f).forEach { size ->
                                val active = stepSize == size
                                Surface(
                                    modifier = Modifier.clickable { stepSize = size },
                                    color = if (active) Color(0xFFFFC107) else SoftBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${size.toInt()}px",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // D-Pad Cross Layout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Arriba
                            ArrowPadButton("⬆️ ARRIBA") { onNudgeNode(0f, -stepSize) }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Izquierda
                                ArrowPadButton("⬅️ IZQUIERDA") { onNudgeNode(-stepSize, 0f) }

                                // Derecha
                                ArrowPadButton("➡️ DERECHA") { onNudgeNode(stepSize, 0f) }
                            }

                            // Abajo
                            ArrowPadButton("⬇️ ABAJO") { onNudgeNode(0f, stepSize) }
                        }
                    }
                }
            } else if (lastTappedCoord != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = "📍 Coordenada tocada: x = ${lastTappedCoord.first.toInt()}f, y = ${lastTappedCoord.second.toInt()}f\n(Selecciona un nodo arriba para moverlo con la cruceta de flechas)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCopyKotlinCode,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "📋 Copiar Planta $selectedFloorName", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onResetNodes,
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "↺ Restablecer", fontSize = 11.sp, color = Ink)
                    }
                }

                Button(
                    onClick = onCopyAllKotlinCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "📋 Copiar TODOS los Nodos para EtsiitData.kt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ArrowPadButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = Color(0xFFECEFF1),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB0BEC5))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FloorPlanCanvasOverlay(
    bitmap: ImageBitmap?,
    selectedFloorName: String,
    activeSegment: RouteFloorSegment?,
    fullRoute: List<Node>,
    currentStepIndex: Int = 0,
    isFirstSegment: Boolean,
    isLastSegment: Boolean,
    isCalibrationMode: Boolean = false,
    allFloorNodes: List<Node> = emptyList(),
    allFloorEdges: List<com.example.aplicacionmovilnpi.model.Edge> = emptyList(),
    customNodesMap: Map<String, Node> = emptyMap(),
    selectedNodeId: String? = null,
    lastTappedCoord: Pair<Float, Float>? = null,
    onSelectNode: (String?) -> Unit = {},
    onNudgeNode: (Float, Float) -> Unit = { _, _ -> },
    onTapMapCoordinate: (Float, Float) -> Unit = { _, _ -> }
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isFullScreenOpen by remember { mutableStateOf(false) }

    val floorNumber = when (selectedFloorName) {
        "P4" -> 4
        "P3" -> 3
        "P2" -> 2
        "P1" -> 1
        "PB" -> 0
        "S1" -> -1
        else -> 0
    }

    val nodesToDraw = fullRoute.filter { it.floor == floorNumber }

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
                            scale = (scale * zoom).coerceIn(1f, 6f)
                            offset += pan
                        }
                    }
                    .pointerInput(isCalibrationMode, allFloorNodes, scale, offset, bitmap) {
                        if (isCalibrationMode) {
                            detectTapGestures { touchOffset ->
                                val canvasWidth = size.width.toFloat()
                                val canvasHeight = size.height.toFloat()

                                if (canvasWidth > 0 && canvasHeight > 0) {
                                    val centerX = canvasWidth / 2f
                                    val centerY = canvasHeight / 2f
                                    val localX = centerX + (touchOffset.x - offset.x - centerX) / scale
                                    val localY = centerY + (touchOffset.y - offset.y - centerY) / scale

                                    val placement = calculateImagePlacement(canvasWidth, canvasHeight, bitmap)
                                    val (mapX, mapY) = placement.mapToNormalized(localX, localY)
                                    val thresholdPx = 30.dp.toPx()

                                    val clickedNode = allFloorNodes.find { n ->
                                        val p = placement.mapToScreen(n.x, n.y)
                                        hypot(localX - p.x, localY - p.y) < thresholdPx
                                    }

                                    if (clickedNode != null) {
                                        onSelectNode(clickedNode.id)
                                    } else {
                                        onTapMapCoordinate(mapX, mapY)
                                    }
                                }
                            }
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
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚠️ No se encontró la imagen del plano\nPlanta $selectedFloorName",
                            fontSize = 11.sp,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val placement = calculateImagePlacement(size.width, size.height, bitmap)

                    if (isCalibrationMode) {
                        // 1. Dibujar todas las conexiones (edges) de esta planta
                        allFloorEdges.forEach { edge ->
                            val n1 = customNodesMap[edge.from]
                            val n2 = customNodesMap[edge.to]
                            if (n1 != null && n2 != null) {
                                val p1 = placement.mapToScreen(n1.x, n1.y)
                                val p2 = placement.mapToScreen(n2.x, n2.y)
                                drawLine(
                                    color = Color(0x990288D1),
                                    start = p1,
                                    end = p2,
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 8.dp.toPx()
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        val textBgPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            alpha = 210
                        }

                        // 2. Dibujar todos los nodos de esta planta
                        allFloorNodes.forEach { node ->
                            val p = placement.mapToScreen(node.x, node.y)
                            val px = p.x
                            val py = p.y
                            val isSelected = (node.id == selectedNodeId)

                            if (isSelected) {
                                drawCircle(
                                    color = Color(0xFFFFD600),
                                    radius = 12.dp.toPx(),
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = Color(0xFFD32F2F),
                                    radius = 7.dp.toPx(),
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            } else {
                                drawCircle(
                                    color = Color(0xFF0288D1),
                                    radius = 6.dp.toPx(),
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }

                            val shortLabel = node.id.removePrefix("n_")
                            val textWidth = textPaint.measureText(shortLabel)
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawRect(
                                    px + 6.dp.toPx(),
                                    py - 8.dp.toPx(),
                                    px + 10.dp.toPx() + textWidth,
                                    py + 4.dp.toPx(),
                                    textBgPaint
                                )
                                canvas.nativeCanvas.drawText(
                                    shortLabel,
                                    px + 8.dp.toPx(),
                                    py + 2.dp.toPx(),
                                    textPaint
                                )
                            }
                        }

                        // 3. Dibujar retícula de mira en la última coordenada tocada
                        lastTappedCoord?.let { (tx, ty) ->
                            val p = placement.mapToScreen(tx, ty)
                            val px = p.x
                            val py = p.y

                            drawLine(
                                color = Color.Red,
                                start = Offset(px - 12.dp.toPx(), py),
                                end = Offset(px + 12.dp.toPx(), py),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawLine(
                                color = Color.Red,
                                start = Offset(px, py - 12.dp.toPx()),
                                end = Offset(px, py + 12.dp.toPx()),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    } else {
                        // MODO RUTA NORMAL - PASO A PASO
                        if (nodesToDraw.isEmpty()) return@Canvas

                        val currentRouteNode = fullRoute.getOrNull(currentStepIndex)

                        // 1. Dibujar líneas conectando los nodos del tramo actual
                        for (i in 0 until (nodesToDraw.size - 1)) {
                            val n1 = nodesToDraw[i]
                            val n2 = nodesToDraw[i + 1]
                            val p1 = placement.mapToScreen(n1.x, n1.y)
                            val p2 = placement.mapToScreen(n2.x, n2.y)

                            val idx1 = fullRoute.indexOfFirst { it.id == n1.id }
                            val isStepSegment = (idx1 == currentStepIndex)

                            drawLine(
                                color = if (isStepSegment) Color(0xFFD32F2F) else Color(0xAAEF5350),
                                start = p1,
                                end = p2,
                                strokeWidth = if (isStepSegment) 8.dp.toPx() else 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // 2. Dibujar nodos sobre el mapa destacando el PASO ACTUAL
                        nodesToDraw.forEach { node ->
                            val p = placement.mapToScreen(node.x, node.y)
                            val px = p.x
                            val py = p.y

                            val nodeIdxInFullRoute = fullRoute.indexOfFirst { it.id == node.id }
                            val isCurrentStepNode = (node.id == currentRouteNode?.id)
                            val isVisitedNode = (nodeIdxInFullRoute in 0 until currentStepIndex)
                            val isStartNode = (node.id == fullRoute.firstOrNull()?.id)
                            val isEndNode = (node.id == fullRoute.lastOrNull()?.id)

                            when {
                                isCurrentStepNode -> {
                                    // Target Node Resaltado en Verde/Amarillo Vibrante
                                    drawCircle(
                                        color = Color(0xFFFFD600),
                                        radius = 15.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color(0xFF2E7D32),
                                        radius = 9.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 4.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }
                                isVisitedNode -> {
                                    drawCircle(
                                        color = Color(0xFF81C784),
                                        radius = 5.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }
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
                                else -> {
                                    drawCircle(
                                        color = Color(0xFFD32F2F),
                                        radius = 5.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }
                            }
                        }
                    }
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
            currentStepIndex = currentStepIndex,
            isFirstSegment = isFirstSegment,
            isLastSegment = isLastSegment,
            isCalibrationMode = isCalibrationMode,
            allFloorNodes = allFloorNodes,
            allFloorEdges = allFloorEdges,
            customNodesMap = customNodesMap,
            selectedNodeId = selectedNodeId,
            lastTappedCoord = lastTappedCoord,
            onSelectNode = onSelectNode,
            onNudgeNode = onNudgeNode,
            onTapMapCoordinate = onTapMapCoordinate,
            onDismiss = { isFullScreenOpen = false }
        )
    }
}

@Composable
fun FloatingCalibrationOverlay(
    floorNodes: List<Node>,
    selectedNodeId: String?,
    onSelectNode: (String?) -> Unit,
    onNudgeNode: (dx: Float, dy: Float) -> Unit
) {
    val selectedNode = floorNodes.find { it.id == selectedNodeId }
    var stepSize by remember { mutableStateOf(5f) }

    Surface(
        color = Color.Black.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedNode != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.width(280.dp)
                ) {
                    Text(
                        text = "🎯 ${selectedNode.id.removePrefix("n_")} (${selectedNode.x.toInt()}, ${selectedNode.y.toInt()})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD600)
                    )

                    Surface(
                        modifier = Modifier.clickable { onSelectNode(null) },
                        color = Color.Red.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "✖ Deseleccionar",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "Paso:", fontSize = 10.sp, color = Color.LightGray)
                    listOf(1f, 5f, 10f, 25f).forEach { size ->
                        val active = stepSize == size
                        Surface(
                            modifier = Modifier.clickable { stepSize = size },
                            color = if (active) Color(0xFFFFB300) else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${size.toInt()}px",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FloatingPadButton("⬆️") { onNudgeNode(0f, -stepSize) }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingPadButton("⬅️") { onNudgeNode(-stepSize, 0f) }
                        FloatingPadButton("➡️") { onNudgeNode(stepSize, 0f) }
                    }

                    FloatingPadButton("⬇️") { onNudgeNode(0f, stepSize) }
                }
            } else {
                Text(
                    text = "💡 Toca un nodo en el plano o elígelo para moverlo con flechas:",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.width(280.dp)
                ) {
                    items(floorNodes) { node ->
                        Surface(
                            modifier = Modifier.clickable { onSelectNode(node.id) },
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = node.id.removePrefix("n_"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingPadButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = Color.White.copy(alpha = 0.25f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ZoomableMapDialog(
    bitmap: ImageBitmap?,
    selectedFloorName: String,
    activeSegment: RouteFloorSegment?,
    fullRoute: List<Node>,
    currentStepIndex: Int = 0,
    isFirstSegment: Boolean,
    isLastSegment: Boolean,
    isCalibrationMode: Boolean = false,
    allFloorNodes: List<Node> = emptyList(),
    allFloorEdges: List<com.example.aplicacionmovilnpi.model.Edge> = emptyList(),
    customNodesMap: Map<String, Node> = emptyMap(),
    selectedNodeId: String? = null,
    lastTappedCoord: Pair<Float, Float>? = null,
    onSelectNode: (String?) -> Unit = {},
    onNudgeNode: (Float, Float) -> Unit = { _, _ -> },
    onTapMapCoordinate: (Float, Float) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1.8f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val floorNumber = when (selectedFloorName) {
        "P4" -> 4
        "P3" -> 3
        "P2" -> 2
        "P1" -> 1
        "PB" -> 0
        "S1" -> -1
        else -> 0
    }

    val nodesToDraw = fullRoute.filter { it.floor == floorNumber }

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
                            text = if (isCalibrationMode) "Modo Calibración activo · Arrastra para mover el mapa" else "Usa un dedo para mover el mapa y dos dedos para zoom",
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
                        .padding(vertical = 90.dp)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 6f)
                                offset += pan
                            }
                        }
                        .pointerInput(isCalibrationMode, allFloorNodes, scale, offset, bitmap) {
                            if (isCalibrationMode) {
                                detectTapGestures { touchOffset ->
                                    val canvasWidth = size.width.toFloat()
                                    val canvasHeight = size.height.toFloat()

                                    if (canvasWidth > 0 && canvasHeight > 0) {
                                        val centerX = canvasWidth / 2f
                                        val centerY = canvasHeight / 2f
                                        val localX = centerX + (touchOffset.x - offset.x - centerX) / scale
                                        val localY = centerY + (touchOffset.y - offset.y - centerY) / scale

                                        val placement = calculateImagePlacement(canvasWidth, canvasHeight, bitmap)
                                        val (mapX, mapY) = placement.mapToNormalized(localX, localY)
                                        val thresholdPx = 30.dp.toPx()

                                        val clickedNode = allFloorNodes.find { n ->
                                            val p = placement.mapToScreen(n.x, n.y)
                                            hypot(localX - p.x, localY - p.y) < thresholdPx
                                        }

                                        if (clickedNode != null) {
                                            onSelectNode(clickedNode.id)
                                        } else {
                                            onTapMapCoordinate(mapX, mapY)
                                        }
                                    }
                                }
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
                    } else {
                        Text(
                            text = "⚠️ No se encontró la imagen del plano para Planta $selectedFloorName",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val placement = calculateImagePlacement(size.width, size.height, bitmap)

                        if (isCalibrationMode) {
                            allFloorEdges.forEach { edge ->
                                val n1 = customNodesMap[edge.from]
                                val n2 = customNodesMap[edge.to]
                                if (n1 != null && n2 != null) {
                                    val p1 = placement.mapToScreen(n1.x, n1.y)
                                    val p2 = placement.mapToScreen(n2.x, n2.y)
                                    drawLine(
                                        color = Color(0x990288D1),
                                        start = p1,
                                        end = p2,
                                        strokeWidth = 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 8.dp.toPx()
                                isAntiAlias = true
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                            val textBgPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                alpha = 210
                            }

                            allFloorNodes.forEach { node ->
                                val p = placement.mapToScreen(node.x, node.y)
                                val px = p.x
                                val py = p.y
                                val isSelected = (node.id == selectedNodeId)

                                if (isSelected) {
                                    drawCircle(
                                        color = Color(0xFFFFD600),
                                        radius = 12.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color(0xFFD32F2F),
                                        radius = 7.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 3.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                } else {
                                    drawCircle(
                                        color = Color(0xFF0288D1),
                                        radius = 6.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }

                                val shortLabel = node.id.removePrefix("n_")
                                val textWidth = textPaint.measureText(shortLabel)
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawRect(
                                        px + 6.dp.toPx(),
                                        py - 8.dp.toPx(),
                                        px + 10.dp.toPx() + textWidth,
                                        py + 4.dp.toPx(),
                                        textBgPaint
                                    )
                                    canvas.nativeCanvas.drawText(
                                        shortLabel,
                                        px + 8.dp.toPx(),
                                        py + 2.dp.toPx(),
                                        textPaint
                                    )
                                }
                            }

                            lastTappedCoord?.let { (tx, ty) ->
                                val p = placement.mapToScreen(tx, ty)
                                val px = p.x
                                val py = p.y

                                drawLine(
                                    color = Color.Red,
                                    start = Offset(px - 12.dp.toPx(), py),
                                    end = Offset(px + 12.dp.toPx(), py),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(px, py - 12.dp.toPx()),
                                    end = Offset(px, py + 12.dp.toPx()),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                        } else {
                            if (nodesToDraw.isEmpty()) return@Canvas

                            val currentRouteNode = fullRoute.getOrNull(currentStepIndex)

                            for (i in 0 until (nodesToDraw.size - 1)) {
                                val n1 = nodesToDraw[i]
                                val n2 = nodesToDraw[i + 1]
                                val p1 = placement.mapToScreen(n1.x, n1.y)
                                val p2 = placement.mapToScreen(n2.x, n2.y)

                                val idx1 = fullRoute.indexOfFirst { it.id == n1.id }
                                val isStepSegment = (idx1 == currentStepIndex)

                                drawLine(
                                    color = if (isStepSegment) Color(0xFFD32F2F) else Color(0xAAEF5350),
                                    start = p1,
                                    end = p2,
                                    strokeWidth = if (isStepSegment) 8.dp.toPx() else 4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            nodesToDraw.forEach { node ->
                                val p = placement.mapToScreen(node.x, node.y)
                                val px = p.x
                                val py = p.y

                                val nodeIdxInFullRoute = fullRoute.indexOfFirst { it.id == node.id }
                                val isCurrentStepNode = (node.id == currentRouteNode?.id)
                                val isVisitedNode = (nodeIdxInFullRoute in 0 until currentStepIndex)
                                val isStartNode = (node.id == fullRoute.firstOrNull()?.id)
                                val isEndNode = (node.id == fullRoute.lastOrNull()?.id)

                                when {
                                    isCurrentStepNode -> {
                                        drawCircle(
                                            color = Color(0xFFFFD600),
                                            radius = 15.dp.toPx(),
                                            center = Offset(px, py)
                                        )
                                        drawCircle(
                                            color = Color(0xFF2E7D32),
                                            radius = 9.dp.toPx(),
                                            center = Offset(px, py)
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 4.dp.toPx(),
                                            center = Offset(px, py)
                                        )
                                    }
                                    isVisitedNode -> {
                                        drawCircle(
                                            color = Color(0xFF81C784),
                                            radius = 5.dp.toPx(),
                                            center = Offset(px, py)
                                        )
                                    }
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
                                    else -> {
                                        drawCircle(
                                            color = Color(0xFFD32F2F),
                                            radius = 5.dp.toPx(),
                                            center = Offset(px, py)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating Action Controls Bar (Bottom Center)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isCalibrationMode) {
                        FloatingCalibrationOverlay(
                            floorNodes = allFloorNodes,
                            selectedNodeId = selectedNodeId,
                            onSelectNode = onSelectNode,
                            onNudgeNode = onNudgeNode
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ZoomButton("➕ Acercar") { scale = (scale + 0.5f).coerceAtMost(6f) }
                        ZoomButton("➖ Alejar") {
                            scale = (scale - 0.5f).coerceAtLeast(1f)
                            if (scale == 1f) offset = Offset.Zero
                        }
                        ZoomButton("↺ Restablecer") {
                            scale = 1.8f
                            offset = Offset.Zero
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteStepNavigationCard(
    calculatedRoute: List<Node>,
    currentStepIndex: Int,
    allPois: List<PointOfInterest>,
    startPoiName: String,
    destPoiName: String,
    onStepIndexChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    if (calculatedRoute.isEmpty()) return

    val totalSteps = calculatedRoute.size
    val currentNode = calculatedRoute.getOrNull(currentStepIndex) ?: return
    val nextNode = calculatedRoute.getOrNull(currentStepIndex + 1)

    val currentNodeName = getNodeDisplayName(currentNode.id, allPois)
    val nextNodeName = getNodeDisplayName(nextNode?.id, allPois)

    val isLastStep = (currentStepIndex == totalSteps - 1)
    val isFloorTransition = (nextNode != null && nextNode.floor != currentNode.floor)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, UgrRed)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PASO ${currentStepIndex + 1} DE $totalSteps · PLANTA ${getFloorName(currentNode.floor)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgrRed
                    )
                    Text(
                        text = "$startPoiName ➔ $destPoiName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }

                Text(
                    text = "✖ Cerrar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    modifier = Modifier.clickable { onClose() }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Step Instruction Body
            when {
                isLastStep -> {
                    // Reached Destination!
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "🏁",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Has llegado a tu destino!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenStatus
                            )
                            Text(
                                text = "Ubicación actual: $currentNodeName",
                                fontSize = 12.sp,
                                color = Ink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenStatus),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🏁 Finalizar Navegación",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                isFloorTransition -> {
                    // Change Floor!
                    val isElevator = currentNode.id.contains("ascensor", ignoreCase = true) ||
                            (nextNode?.id?.contains("ascensor", ignoreCase = true) == true)
                    val icon = if (isElevator) "🛗" else "🪜"
                    val actionName = if (isElevator) "el ascensor" else "las escaleras"
                    val targetFloorName = getFloorName(nextNode?.floor ?: currentNode.floor)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ToneBlueBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = icon,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cambio de Planta: Dirígete a $actionName",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = "Estás en $currentNodeName. Usa $actionName para cambiar a Planta $targetFloorName ($nextNodeName).",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onStepIndexChange(currentStepIndex + 1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$icon Cambiar a Planta $targetFloorName ➔",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                else -> {
                    // Normal turn-by-turn node step
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ToneBlueBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "📍",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Paso actual: $currentNodeName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "👉 Ahora ve hacia: $nextNodeName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onStepIndexChange(currentStepIndex + 1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✅ He llegado a $nextNodeName ➔",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Prev / Next Step Switcher Bar
            if (totalSteps > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { onStepIndexChange(currentStepIndex - 1) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "⬅ Anterior", fontSize = 11.sp, color = Ink)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStepIndex < totalSteps - 1) {
                        OutlinedButton(
                            onClick = { onStepIndexChange(currentStepIndex + 1) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Siguiente ➡️", fontSize = 11.sp, color = Ink)
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
