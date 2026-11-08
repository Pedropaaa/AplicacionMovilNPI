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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.GreenStatus
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import kotlinx.coroutines.launch
import kotlin.math.abs
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.io.File
import org.osmdroid.util.GeoPoint as OsmPoint
import org.osmdroid.events.MapListener
import androidx.compose.foundation.background
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import androidx.compose.ui.draw.clipToBounds

private const val ORIGIN_GPS = "gps"
private const val ORIGIN_MAP = "map"
private const val DEST_COMEDOR_CERCANO = "__dest_comedor_cercano__"
private const val DEST_BIBLIOTECA_CERCANA = "__dest_biblioteca_cercana__"

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
    onContinueIndoors: () -> Unit,
    initialDestinationId: String? = null,
    initialDestinationReady: Boolean = true,
    destinationRequestKey: Int = 0,
    showOutdoorFacilitiesUnavailableNotice: Boolean = false
) {
    val isEnglish = LocalIsEnglish.current
    val scope = rememberCoroutineScope()

    var handledComedorRequestKey by rememberSaveable {
        mutableStateOf(-1)
    }
    var handledBibliotecaRequestKey by rememberSaveable {
        mutableStateOf(-1)
    }
    var originId by rememberSaveable { mutableStateOf(if (hasLocationPermission) ORIGIN_GPS else "sciences") }
    var destId by rememberSaveable { mutableStateOf(CityPlaces.etsiit.id) }
    var mapOriginLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var mapOriginLon by rememberSaveable { mutableStateOf<Double?>(null) }
    var route by remember { mutableStateOf<WalkingRoute?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAllSteps by remember { mutableStateOf(false) }

    val destination = CityPlaces.byId(destId) ?: CityPlaces.etsiit

    // --- ESTADOS Y FILTRADO DE BÚSQUEDA DEL ORIGEN ---
    var originSearchQuery by rememberSaveable {
        mutableStateOf(
            when (originId) {
                ORIGIN_GPS -> "📍 Mi ubicación"
                ORIGIN_MAP -> "👆 Punto del mapa"
                else -> CityPlaces.byId(originId)?.name ?: ""
            }
        )
    }
    var isOriginSearchExpanded by remember { mutableStateOf(false) }

    val filteredOrigins = remember(originSearchQuery, destId) {
        val available = CityPlaces.all.filter { it.id != destId }
        if (originSearchQuery.isBlank() || originSearchQuery == "📍 Mi ubicación" || originSearchQuery == "👆 Punto del mapa") {
            available
        } else {
            available.filter { place ->
                place.name.contains(originSearchQuery, ignoreCase = true) ||
                        place.campus.contains(originSearchQuery, ignoreCase = true)
            }
        }
    }

    // --- ESTADOS Y FILTRADO DE BÚSQUEDA DEL DESTINO ---
    var destSearchQuery by rememberSaveable { mutableStateOf(destination.name) }
    var isDestSearchExpanded by remember { mutableStateOf(false) }

    val filteredDestinations = remember(destSearchQuery, originId) {
        val available = CityPlaces.all.filter { it.id != originId }
        if (destSearchQuery.isBlank()) {
            available
        } else {
            available.filter { place ->
                place.name.contains(destSearchQuery, ignoreCase = true) ||
                        place.campus.contains(destSearchQuery, ignoreCase = true)
            }
        }
    }

    val gpsPoint = gpsLocation?.toGeoPoint()
    val comedorCandidates = remember {
        listOf(
            "comedor_fuentenueva",
            "comedor_cartuja",
            "comedor_pts",
            "comedor_etsiit"
        ).mapNotNull { id ->
            CityPlaces.byId(id)
        }
    }
    // Todas las bibliotecas universitarias de Granada.
    val bibliotecaCandidates = remember {
        CityPlaces.bibliotecas
    }

    val mapOrigin = if (mapOriginLat != null && mapOriginLon != null) GeoPoint(mapOriginLat!!, mapOriginLon!!) else null
    val origin: GeoPoint? = when (originId) {
        ORIGIN_GPS -> gpsPoint
        ORIGIN_MAP -> mapOrigin
        else -> CityPlaces.byId(originId)?.point
    }

    // Si cambian origen o destino, la ruta anterior deja de valer
    LaunchedEffect(originId, destId, mapOriginLat, mapOriginLon) {
        route = null
        error = null
    }

    // ======================================================
// BÚSQUEDA AUTOMÁTICA DEL COMEDOR EXTERIOR MÁS CERCANO
// ======================================================

    LaunchedEffect(
        initialDestinationId,
        initialDestinationReady,
        destinationRequestKey,
        hasLocationPermission
    ) {

        // Solo actuamos cuando se solicita el comedor especial.
        if (initialDestinationId != DEST_COMEDOR_CERCANO) {
            return@LaunchedEffect
        }

        // Esperamos a que MapScreen haya terminado de determinar
        // si el usuario está dentro o fuera de la ETSIIT.
        if (!initialDestinationReady) {
            return@LaunchedEffect
        }

        // Evitamos procesar dos veces la misma petición.
        if (handledComedorRequestKey == destinationRequestKey) {
            return@LaunchedEffect
        }

        // Sin permiso GPS no podemos saber qué comedor está más cerca.
        if (!hasLocationPermission) {
            error = if (isEnglish) "Location is needed to find the nearest dining hall." else "Necesitamos tu ubicación para encontrar el comedor más cercano."
            return@LaunchedEffect
        }

        val location = gpsLocation

        if (location == null) {
            error = if (isEnglish) "Waiting for your GPS location..." else "Esperando a obtener tu ubicación GPS..."
            return@LaunchedEffect
        }

        // Utilizamos el mismo criterio de precisión que MapScreen.
        if (location.accuracy > 100f) {
            error = if (isEnglish) "The GPS signal is not accurate enough yet." else "La señal GPS todavía no es suficientemente precisa."
            return@LaunchedEffect
        }

        val currentPosition = gpsPoint ?: return@LaunchedEffect

        // Buscamos el comedor geográficamente más próximo.
        val nearestComedor = comedorCandidates.minByOrNull { comedor ->
            GeoUtils.distanceMeters(
                currentPosition,
                comedor.point
            )
        }

        if (nearestComedor == null) {
            error = if (isEnglish) "No university dining halls were found." else "No se han encontrado comedores universitarios."
            return@LaunchedEffect
        }

        // Marcamos la petición como procesada.
        handledComedorRequestKey = destinationRequestKey

        // Establecemos el GPS como origen.
        originId = ORIGIN_GPS
        originSearchQuery = "📍 Mi ubicación"

        // Establecemos el comedor encontrado como destino.
        destId = nearestComedor.id
        destSearchQuery = nearestComedor.name

        isOriginSearchExpanded = false
        isDestSearchExpanded = false

        error = null
        isLoading = true

        // Calculamos la ruta peatonal desde el GPS hasta el comedor.
        val result = WalkingRouteService.fetch(
            from = currentPosition,
            to = nearestComedor.point
        )

        result
            .onSuccess { walkingRoute ->
                route = walkingRoute
                showAllSteps = false
            }
            .onFailure {
                error = if (isEnglish) "Could not calculate a route to the dining hall. Check your internet connection." else "No se pudo calcular la ruta al comedor. Comprueba tu conexión a internet."
            }

        isLoading = false
    }
    // ======================================================
// BÚSQUEDA AUTOMÁTICA DE LA BIBLIOTECA MÁS CERCANA
// ======================================================

    LaunchedEffect(
        initialDestinationId,
        initialDestinationReady,
        destinationRequestKey,
        hasLocationPermission
    ) {

        // Solo actuamos cuando se solicita la biblioteca especial.
        if (initialDestinationId != DEST_BIBLIOTECA_CERCANA) {
            return@LaunchedEffect
        }

        // Esperamos a que MapScreen haya determinado el modo de mapa.
        if (!initialDestinationReady) {
            return@LaunchedEffect
        }

        // Evitamos procesar dos veces la misma petición.
        if (handledBibliotecaRequestKey == destinationRequestKey) {
            return@LaunchedEffect
        }

        // Necesitamos permiso GPS.
        if (!hasLocationPermission) {
            error = "Necesitamos tu ubicación para encontrar la biblioteca más cercana."
            return@LaunchedEffect
        }

        val location = gpsLocation

        if (location == null) {
            error = "Esperando a obtener tu ubicación GPS..."
            return@LaunchedEffect
        }

        // Exigimos una precisión razonable.
        if (location.accuracy > 100f) {
            error = "La señal GPS todavía no es suficientemente precisa."
            return@LaunchedEffect
        }

        val currentPosition = gpsPoint ?: return@LaunchedEffect

        // Buscamos la biblioteca geográficamente más próxima.
        val nearestBiblioteca = bibliotecaCandidates.minByOrNull { biblioteca ->
            GeoUtils.distanceMeters(
                currentPosition,
                biblioteca.point
            )
        }

        if (nearestBiblioteca == null) {
            error = "No se han encontrado bibliotecas universitarias."
            return@LaunchedEffect
        }

        // Marcamos la petición como procesada.
        handledBibliotecaRequestKey = destinationRequestKey

        // Establecemos el GPS como origen.
        originId = ORIGIN_GPS
        originSearchQuery = "📍 Mi ubicación"

        // Establecemos la biblioteca encontrada como destino.
        destId = nearestBiblioteca.id
        destSearchQuery = nearestBiblioteca.name

        isOriginSearchExpanded = false
        isDestSearchExpanded = false

        error = null
        isLoading = true

        // Calculamos la ruta peatonal desde el GPS hasta la biblioteca.
        val result = WalkingRouteService.fetch(
            from = currentPosition,
            to = nearestBiblioteca.point
        )

        result
            .onSuccess { walkingRoute ->
                route = walkingRoute
                showAllSteps = false
            }
            .onFailure {
                error = "No se pudo calcular la ruta a la biblioteca. Comprueba tu conexión a internet."
            }

        isLoading = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!hasLocationPermission) CityLocationPermissionButton(onRequestPermission)

        // Aviso cuando se buscan aseos o fuentes fuera de la ETSIIT.
        if (showOutdoorFacilitiesUnavailableNotice) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFFFFE0A3)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "ℹ️",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Búsqueda no disponible en esta zona",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Todavía no tenemos aseos ni fuentes registrados fuera de la ETSIIT. Puedes cambiar al mapa interior para usar esta función.",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }
            }
        }

        if (
            initialDestinationId == DEST_COMEDOR_CERCANO &&
            !initialDestinationReady
        ) {
            Text(
                text = if (isEnglish) "📍 Checking your location to find the nearest dining hall..." else "📍 Comprobando tu ubicación para encontrar el comedor más cercano...",
                fontSize = 11.sp,
                color = MutedText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        if (
            initialDestinationId == DEST_BIBLIOTECA_CERCANA &&
            !initialDestinationReady
        ) {
            Text(
                text = "📚 Comprobando tu ubicación para encontrar la biblioteca más cercana...",
                fontSize = 11.sp,
                color = MutedText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ORIGEN Y DESTINO CON RAÍL VISUAL
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Raíl visual igual que el menú interior
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

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        // CAMPO DE ORIGEN
                        androidx.compose.material3.OutlinedTextField(
                            value = originSearchQuery,
                            onValueChange = {
                                originSearchQuery = it
                                isOriginSearchExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 13.sp,
                                color = Ink
                            ),
                            leadingIcon = {
                                Text("🟢", fontSize = 13.sp)
                            },
                            placeholder = {
                                Text(if (isEnglish) "Select origin..." else "Seleccionar origen...", fontSize = 12.sp)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SoftBg,
                                unfocusedContainerColor = SoftBg,
                                focusedBorderColor = UgrRed,
                                unfocusedBorderColor = CardBorder,
                                cursorColor = UgrRed
                            )
                        )

                        // RESULTADOS DEL ORIGEN
                        if (isOriginSearchExpanded && filteredOrigins.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    CardBorder
                                ),
                                color = Color(0xFFFAFAFA)
                            ) {
                                androidx.compose.foundation.lazy.LazyColumn {
                                    items(filteredOrigins) { place ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    originId = place.id
                                                    originSearchQuery = place.name
                                                    isOriginSearchExpanded = false
                                                }
                                                .padding(
                                                    horizontal = 12.dp,
                                                    vertical = 10.dp
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = place.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Ink
                                                )
                                                Text(
                                                    text = "${if (isEnglish) "Campus" else "Campus"} ${place.campus}",
                                                    fontSize = 10.sp,
                                                    color = MutedText
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            color = CardBorder.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        // CAMPO DE DESTINO
                        androidx.compose.material3.OutlinedTextField(
                            value = destSearchQuery,
                            onValueChange = {
                                destSearchQuery = it
                                isDestSearchExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 13.sp,
                                color = Ink
                            ),
                            leadingIcon = {
                                Text("🔴", fontSize = 13.sp)
                            },
                            placeholder = {
                                Text(if (isEnglish) "Where are you going?" else "¿A dónde vas?", fontSize = 12.sp)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SoftBg,
                                unfocusedContainerColor = SoftBg,
                                focusedBorderColor = UgrRed,
                                unfocusedBorderColor = CardBorder,
                                cursorColor = UgrRed
                            )
                        )

                        // RESULTADOS DEL DESTINO
                        if (isDestSearchExpanded && filteredDestinations.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    CardBorder
                                ),
                                color = Color(0xFFFAFAFA)
                            ) {
                                androidx.compose.foundation.lazy.LazyColumn {
                                    items(filteredDestinations) { place ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    destId = place.id
                                                    destSearchQuery = place.name
                                                    isDestSearchExpanded = false

                                                    if (originId == place.id) {
                                                        originId =
                                                            if (hasLocationPermission) {
                                                                ORIGIN_GPS
                                                            } else {
                                                                ORIGIN_MAP
                                                            }

                                                        originSearchQuery =
                                                            if (hasLocationPermission) {
                                                                "📍 Mi ubicación"
                                                            } else {
                                                                "👆 Punto del mapa"
                                                            }
                                                    }
                                                }
                                                .padding(
                                                    horizontal = 12.dp,
                                                    vertical = 10.dp
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = place.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Ink
                                                )
                                                Text(
                                                    text = "${if (isEnglish) "Campus" else "Campus"} ${place.campus}",
                                                    fontSize = 10.sp,
                                                    color = MutedText
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            color = CardBorder.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // OPCIONES RÁPIDAS DE ORIGEN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlaceChip(
                        "📍 Mi ubicación",
                        selected = originId == ORIGIN_GPS
                    ) {
                        if (hasLocationPermission) {
                            originId = ORIGIN_GPS
                            originSearchQuery = "📍 Mi ubicación"
                            isOriginSearchExpanded = false
                        } else {
                            onRequestPermission()
                        }
                    }

                    PlaceChip(
                        "👆 Punto del mapa",
                        selected = originId == ORIGIN_MAP
                    ) {
                        if (mapOrigin != null) {
                            originId = ORIGIN_MAP
                            originSearchQuery = "👆 Punto del mapa"
                            isOriginSearchExpanded = false
                        } else {
                            error = if (isEnglish) "Tap the map to choose a starting point" else "Toca el mapa para elegir el punto de origen"
                        }
                    }
                }

                // ESTADO DEL GPS
                if (originId == ORIGIN_GPS && gpsPoint == null) {
                    Text(
                        text = if (hasLocationPermission) {
                            if (isEnglish) "Finding your GPS location..." else "Buscando tu ubicación GPS..."
                        } else {
                            if (isEnglish) "Allow location access to use it as the origin" else "Permite la ubicación para usarla como origen"
                        },
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }

                // CALCULAR RUTA
                Button(
                    onClick = {
                        val from = origin ?: return@Button

                        isLoading = true
                        error = null
                        isOriginSearchExpanded = false
                        isDestSearchExpanded = false

                        scope.launch {
                            WalkingRouteService.fetch(from, destination.point)
                                .onSuccess {
                                    route = it
                                    showAllSteps = false
                                }
                                .onFailure {
                                    error = if (isEnglish) "Could not calculate the route. Are you connected to the internet?" else "No se pudo calcular la ruta. ¿Tienes conexión a internet?"
                                }

                            isLoading = false
                        }
                    },
                    enabled = origin != null && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UgrRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isLoading) {
                            if (isEnglish) "Calculating route..." else "Calculando ruta..."
                        } else {
                            if (isEnglish) "🚶 Get walking directions" else "🚶 Calcular ruta a pie"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                error?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = UgrRed
                    )
                }
            }
        }

        OsmMap(
            gpsPoint = gpsPoint,
            origin = origin.takeIf { originId != ORIGIN_GPS },
            destination = destination.point,
            destinationId = destination.id,
            originPlaceId = originId,
            route = route,
            onPlaceTap = { id ->
                destId = id
                CityPlaces.byId(id)?.let { destSearchQuery = it.name }
                if (originId == id) {
                    originId = if (hasLocationPermission) ORIGIN_GPS else ORIGIN_MAP
                    originSearchQuery = if (hasLocationPermission) "📍 Mi ubicación" else "👆 Punto del mapa"
                }
            },
            onMapTap = { point ->
                mapOriginLat = point.lat
                mapOriginLon = point.lon
                originId = ORIGIN_MAP
                originSearchQuery = "👆 Punto del mapa"
                isOriginSearchExpanded = false
            },
            onLocateUnavailable = {
                if (hasLocationPermission) error = if (isEnglish) "No GPS signal yet. Turn on location services." else "Todavía no hay señal GPS. Activa la ubicación del móvil."
                else onRequestPermission()
            }
        )

        route?.let { r ->
            RouteInstructionsCard(
                route = r,
                destinationName = destination.name,
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
            Text(text = "${if (LocalIsEnglish.current) "WALKING ROUTE" else "RUTA A PIE"} · ${route.summary.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = UgrRed)
            Text(text = "${if (LocalIsEnglish.current) "To" else "Hasta"} $destinationName", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)

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
                    text = if (LocalIsEnglish.current) { if (showAll) "Show less ▲" else "Show all ${route.instructions.size} steps ▼" } else { if (showAll) "Ver menos ▲" else "Ver los ${route.instructions.size} pasos ▼" },
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
                        text = if (LocalIsEnglish.current) "🏢 I'm at ETSIIT ➔ Continue on the indoor map" else "🏢 Ya estoy en la ETSIIT ➔ Seguir en el plano interior",
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
    val isEnglish = LocalIsEnglish.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Modo seguimiento: el mapa se mueve con tu posición hasta que lo arrastras con el dedo
    val following = remember { mutableStateOf(false) }
    // Giro del mapa en grados (0 = norte arriba), para orientar el botón de la brújula
    val orientation = remember { mutableFloatStateOf(0f) }
    // Estado de zoom reactivo para actualizar marcadores al hacer pinza
    var currentZoom by remember { mutableDoubleStateOf(13.5) }

    val mapView = remember {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
        MapView(context).apply {
            setTileSource(OsmTiles)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            setScrollableAreaLimitDouble(
                BoundingBox(CityPlaces.GRANADA_NORTH, CityPlaces.GRANADA_EAST, CityPlaces.GRANADA_SOUTH, CityPlaces.GRANADA_WEST)
            )
            minZoomLevel = 12.5
            maxZoomLevel = 19.0
            controller.setZoom(13.5)
            controller.setCenter(
                OsmPoint(
                    CityPlaces.all.map { it.point.lat }.average(),
                    CityPlaces.all.map { it.point.lon }.average()
                )
            )

            // Escuchador de zoom para avisar a Compose al acercar/alejar la vista
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean = false
                override fun onZoom(event: ZoomEvent?): Boolean {
                    currentZoom = event?.zoomLevel ?: zoomLevelDouble
                    return true
                }
            })

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
                post {
                    orientation.floatValue = mapOrientation
                    currentZoom = zoomLevelDouble
                }
                false
            }
        }
    }
    val currentOnMapTap = rememberUpdatedState(onMapTap)
    val currentOnPlaceTap = rememberUpdatedState(onPlaceTap)

    val rotationOverlay = remember { RotationGestureOverlay(mapView).apply { isEnabled = true } }
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

    LaunchedEffect(gpsPoint) {
        if (following.value && gpsPoint != null) mapView.controller.animateTo(gpsPoint.toOsm())
    }

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
            .clipToBounds()
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            update = { map ->
                map.overlays.clear()
                map.overlays.add(rotationOverlay)
                map.overlays.add(tapOverlay)

                // 1. DIBUJAR RUTA PRIMERO (queda por debajo de los marcadores)
                route?.let { r ->
                    map.overlays.add(Polyline(map).apply {
                        setPoints(r.points.map { it.toOsm() })
                        outlinePaint.color = android.graphics.Color.rgb(0xD3, 0x2F, 0x2F)
                        outlinePaint.strokeWidth = 12f
                    })
                }

                // 2. DIBUJAR LUGARES SEGÚN ZOOM (Bucle único)
                val showAllDetails = currentZoom >= 16.2 // Nombres completos solo al estar muy cerca
                val showDots = currentZoom >= 14.0       // Puntos discretos a nivel medio

                CityPlaces.all.forEach { place ->
                    val isDestination = place.id == destinationId
                    val isOrigin = place.id == originPlaceId

                    val color = when {
                        isDestination -> android.graphics.Color.rgb(0xD3, 0x2F, 0x2F)
                        isOrigin -> android.graphics.Color.rgb(0x2E, 0x7D, 0x32)
                        else -> android.graphics.Color.rgb(0x75, 0x75, 0x75)
                    }

                    when {
                        // Origen y Destino SIEMPRE muestran etiqueta completa
                        isDestination || isOrigin -> {
                            map.overlays.add(campusMarker(map, place, color) { currentOnPlaceTap.value(place.id) })
                        }
                        // Zoom muy cercano (>= 16.2): Texto para el resto de facultades
                        showAllDetails -> {
                            map.overlays.add(campusMarker(map, place, color) { currentOnPlaceTap.value(place.id) })
                        }
                        // Zoom medio (14.0 a 16.1): Puntos sin texto
                        showDots -> {
                            map.overlays.add(simpleDotMarker(map, place, color) { currentOnPlaceTap.value(place.id) })
                        }
                        // Zoom alejado (< 14.0): No dibuja nada extra
                    }
                }

                // 3. MARCADORES ADICIONALES (Punto en mapa o GPS)
                if (originPlaceId == ORIGIN_MAP) {
                    origin?.let { map.overlays.add(placeMarker(map, it, if (isEnglish) "Origin" else "Origen", android.graphics.Color.rgb(0x2E, 0x7D, 0x32))) }
                }
                gpsPoint?.let { map.overlays.add(placeMarker(map, it, if (isEnglish) "Your location" else "Tu ubicación", android.graphics.Color.rgb(0x1E, 0x88, 0xE5))) }

                map.overlays.add(CopyrightOverlay(map.context))
                map.invalidate()
            }
        )

        // Botón para centrar en GPS
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

        // Brújula de orientación
        if (orientation.floatValue % 360f != 0f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 10.dp)
                    .size(44.dp)
                    .clickable {
                        mapView.controller.animateTo(mapView.mapCenter, mapView.zoomLevelDouble, 400L, 0f)
                        mapView.postDelayed({ orientation.floatValue = mapView.mapOrientation }, 450L)
                    },
                color = Color.White,
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.rotate(orientation.floatValue)) {
                    Text(text = "▲", fontSize = 14.sp, color = UgrRed, modifier = Modifier.padding(bottom = 14.dp))
                    Text(text = "N", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Ink, modifier = Modifier.padding(top = 10.dp))
                }
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
        infoWindow = null
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
        Text(text = if (LocalIsEnglish.current) "📍 Allow location to see where you are" else "📍 Permitir ubicación para ver dónde estás", fontSize = 12.sp, color = Ink)
    }
}

/** Marcador simplificado (solo un punto limpio) para cuando la vista está alejada */
private fun simpleDotMarker(map: MapView, place: CityPlace, color: Int, onClick: () -> Unit): Marker =
    Marker(map).apply {
        position = place.point.toOsm()
        title = place.name
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        val density = map.context.resources.displayMetrics.density
        icon = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke((2 * density).toInt(), android.graphics.Color.WHITE)
            setSize((12 * density).toInt(), (12 * density).toInt())
        }
        setOnMarkerClickListener { _, _ ->
            onClick()
            true
        }
    }