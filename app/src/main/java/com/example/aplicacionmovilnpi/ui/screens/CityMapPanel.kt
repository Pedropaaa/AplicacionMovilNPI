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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.input.ImeAction

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
    showOutdoorFacilitiesUnavailableNotice: Boolean = false,

    // Solicitudes del buscador común de MapScreen.
    requestOriginPicker: Boolean = false,
    onOriginPickerRequestConsumed: () -> Unit = {},

    isMapOriginSelectionActive: Boolean = false,
    onMapOriginSelected: () -> Unit = {},

    requestSwap: Boolean = false,
    onSwapRequestConsumed: () -> Unit = {},

    requestDestinationPicker: Boolean = false,
    onDestinationPickerRequestConsumed: () -> Unit = {},

    // Información que devolvemos al buscador común.
    onCitySelectorLabelsChange: (
        String,
        String?,
        String,
        String?
    ) -> Unit = { _, _, _, _ -> }
) {
    val isEnglish = LocalIsEnglish.current
    val scope = rememberCoroutineScope()

    var handledComedorRequestKey by rememberSaveable {
        mutableStateOf(-1)
    }
    var handledBibliotecaRequestKey by rememberSaveable {
        mutableStateOf(-1)
    }
    // Indica si todavía estamos resolviendo un destino especial inicial.
    var initialDestinationResolutionPending by rememberSaveable(destinationRequestKey) {
        mutableStateOf(
            initialDestinationId == DEST_COMEDOR_CERCANO ||
                    initialDestinationId == DEST_BIBLIOTECA_CERCANA
        )
    }
    var originId by rememberSaveable {
        mutableStateOf(if (hasLocationPermission) ORIGIN_GPS else "ciencias")
    }
    var destId by rememberSaveable { mutableStateOf("") }
    var mapOriginLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var mapOriginLon by rememberSaveable { mutableStateOf<Double?>(null) }
    var route by remember { mutableStateOf<WalkingRoute?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAllSteps by remember { mutableStateOf(false) }

    val destination = CityPlaces.byId(destId)

    val selectedOriginPlace = CityPlaces.byId(originId)

    val originTitle = when (originId) {
        ORIGIN_GPS ->
            if (isEnglish) "My location" else "Mi ubicación"

        ORIGIN_MAP ->
            if (isEnglish) "Map point" else "Punto del mapa"

        else ->
            selectedOriginPlace?.name ?: ""
    }

    val originSubtitle = when (originId) {
        ORIGIN_GPS ->
            if (isEnglish) "GPS location" else "Ubicación GPS"

        ORIGIN_MAP ->
            if (isEnglish) "Selected on map" else "Seleccionado en el mapa"

        else ->
            selectedOriginPlace?.campus
    }

    var pickingOrigin by rememberSaveable {
        mutableStateOf(false)
    }

    var pickingDestination by rememberSaveable {
        mutableStateOf(false)
    }

    // Abrir el selector exterior de origen desde el buscador común.
    LaunchedEffect(requestOriginPicker) {
        if (requestOriginPicker) {
            pickingOrigin = true
            pickingDestination = false
            onOriginPickerRequestConsumed()
        }
    }

    // Abrir el selector exterior de destino desde el buscador común.
    LaunchedEffect(requestDestinationPicker) {
        if (requestDestinationPicker) {
            pickingDestination = true
            pickingOrigin = false
            onDestinationPickerRequestConsumed()
        }
    }

    // Intercambiar origen y destino desde el buscador común.
    LaunchedEffect(requestSwap) {
        if (requestSwap) {

            val destinationCanBeSwapped =
                destId == ORIGIN_GPS ||
                        destId == ORIGIN_MAP ||
                        destination != null

            if (!initialDestinationResolutionPending && destinationCanBeSwapped) {
                val previousOriginId = originId

                originId = destId
                destId = previousOriginId

                // Cerramos cualquier selector que estuviera abierto.
                pickingOrigin = false
                pickingDestination = false

                // Limpiamos el estado de la ruta anterior.
                error = null
                showAllSteps = false
            }

            onSwapRequestConsumed()
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

    // El destino también puede ser una ubicación GPS o un punto del mapa.
    val destinationPoint: GeoPoint? = when (destId) {
        ORIGIN_GPS -> gpsPoint
        ORIGIN_MAP -> mapOrigin
        else -> destination?.point
    }

    // Nombre que se muestra en el buscador para el destino.
    val destinationTitle = when (destId) {
        ORIGIN_GPS ->
            if (isEnglish) "My location" else "Mi ubicación"

        ORIGIN_MAP ->
            if (isEnglish) "Map point" else "Punto del mapa"

        else ->
            destination?.name ?: ""
    }

    // Subtítulo del destino.
    val destinationSubtitle = when (destId) {
        ORIGIN_GPS ->
            if (isEnglish) "GPS location" else "Ubicación GPS"

        ORIGIN_MAP ->
            if (isEnglish) "Selected on map" else "Seleccionado en el mapa"

        else ->
            destination?.campus
    }

    // Mantenemos actualizados los textos del buscador común.
    LaunchedEffect(
        originTitle,
        originSubtitle,
        destinationTitle,
        destinationSubtitle,
        isEnglish
    ) {
        onCitySelectorLabelsChange(
            originTitle,
            originSubtitle,
            destinationTitle,
            destinationSubtitle
        )
    }

    // ======================================================
    // CÁLCULO AUTOMÁTICO DE LA RUTA EXTERIOR
    // ======================================================

    LaunchedEffect(
        originId,
        destId,
        mapOriginLat,
        mapOriginLon,
        gpsPoint != null,
        initialDestinationResolutionPending
    ) {
        // La ruta anterior deja de ser válida.
        route = null
        error = null

        // Esperamos a que se resuelva el comedor o biblioteca
        // solicitados mediante un acceso directo.
        if (initialDestinationResolutionPending) {
            return@LaunchedEffect
        }

        // Comprobamos que tenemos un origen válido.
        val from = origin
        val to = destinationPoint

        if (from == null || to == null) {
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true

        try {
            WalkingRouteService.fetch(
                from = from,
                to = to
            ).onSuccess { walkingRoute ->
                route = walkingRoute
                showAllSteps = false
            }.onFailure {
                error = if (isEnglish) {
                    "Could not calculate the route. Are you connected to the internet?"
                } else {
                    "No se pudo calcular la ruta. ¿Tienes conexión a internet?"
                }
            }
        } finally {
            isLoading = false
        }
    }

    // ======================================================
// BÚSQUEDA AUTOMÁTICA DEL COMEDOR EXTERIOR MÁS CERCANO
// ======================================================

    LaunchedEffect(
        initialDestinationId,
        initialDestinationReady,
        destinationRequestKey,
        hasLocationPermission,
        gpsLocation
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

        // Establecemos el GPS como origen y el comedor como destino.
        originId = ORIGIN_GPS
        destId = nearestComedor.id

        initialDestinationResolutionPending = false

        pickingOrigin = false
        pickingDestination = false

        error = null
    }
    // ======================================================
// BÚSQUEDA AUTOMÁTICA DE LA BIBLIOTECA MÁS CERCANA
// ======================================================

    LaunchedEffect(
        initialDestinationId,
        initialDestinationReady,
        destinationRequestKey,
        hasLocationPermission,
        gpsLocation
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

        // Establecemos el GPS como origen y la biblioteca como destino.
        originId = ORIGIN_GPS
        destId = nearestBiblioteca.id

        initialDestinationResolutionPending = false

        pickingOrigin = false
        pickingDestination = false

        error = null
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

                error?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = UgrRed
                    )
                }
            }
        }

        // SELECTOR DE ORIGEN
        if (pickingOrigin) {
            CityPlacePickerSheet(
                title = if (isEnglish) "Choose origin" else "Elige el origen",
                accent = GreenStatus,
                places = CityPlaces.all,
                selectedPlaceId = originId.takeUnless {
                    it == ORIGIN_GPS || it == ORIGIN_MAP
                },
                excludedPlaceId = destId,
                onSelect = { place ->
                    initialDestinationResolutionPending = false
                    originId = place.id
                    pickingOrigin = false
                },
                isMyLocationSelected = originId == ORIGIN_GPS,

                onSelectMyLocation = if (destId != ORIGIN_GPS) {
                    {
                        if (hasLocationPermission) {
                            initialDestinationResolutionPending = false
                            originId = ORIGIN_GPS
                            pickingOrigin = false
                            error = null
                        } else {
                            onRequestPermission()
                        }
                    }
                } else {
                    null
                },
                onDismiss = {
                    pickingOrigin = false
                }
            )
        }

        // SELECTOR DE DESTINO
        if (pickingDestination) {
            CityPlacePickerSheet(
                title = if (isEnglish) "Choose destination" else "Elige el destino",
                accent = UgrRed,
                places = CityPlaces.all,
                selectedPlaceId = destId,
                excludedPlaceId = originId.takeUnless {
                    it == ORIGIN_GPS || it == ORIGIN_MAP
                },
                onSelect = { place ->
                    initialDestinationResolutionPending = false
                    destId = place.id
                    pickingDestination = false
                },
                onDismiss = {
                    pickingDestination = false
                }
            )
        }

        OsmMap(
            gpsPoint = gpsPoint,
            origin = origin.takeIf { originId != ORIGIN_GPS },
            destination = destinationPoint,
            destinationId = destination?.id,
            originPlaceId = originId,
            route = route,
            onPlaceTap = { id ->
                initialDestinationResolutionPending = false
                destId = id
                pickingDestination = false

                if (originId == id) {
                    originId = if (hasLocationPermission) {
                        ORIGIN_GPS
                    } else {
                        ORIGIN_MAP
                    }
                }
            },
            onMapTap = { point ->
                if (isMapOriginSelectionActive) {
                    initialDestinationResolutionPending = false

                    mapOriginLat = point.lat
                    mapOriginLon = point.lon
                    originId = ORIGIN_MAP

                    pickingOrigin = false
                    error = null

                    // Desactivamos el botón después de seleccionar el punto.
                    onMapOriginSelected()
                }
            },
            onLocateUnavailable = {
                if (hasLocationPermission) error = if (isEnglish) "No GPS signal yet. Turn on location services." else "Todavía no hay señal GPS. Activa la ubicación del móvil."
                else onRequestPermission()
            }
        )

        route?.let { r ->
            RouteInstructionsCard(
                route = r,
                destinationName = destinationTitle,
                showAll = showAllSteps,
                onToggleShowAll = { showAllSteps = !showAllSteps },
                onContinueIndoors = onContinueIndoors.takeIf { destId == CityPlaces.etsiit.id }
            )
        }
    }
}

private enum class CityPlaceKind {
    FACULTY,
    LIBRARY,
    DINING,
    OTHER
}

private enum class CityPlaceFilter(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val spanishLabel: String,
    val englishLabel: String,
    val kind: CityPlaceKind?
) {
    ALL(Icons.Rounded.Star, "Todos", "All", null),
    FACULTIES(Icons.Rounded.Apartment, "Facultades", "Faculties", CityPlaceKind.FACULTY),
    LIBRARIES(Icons.AutoMirrored.Rounded.MenuBook, "Bibliotecas", "Libraries", CityPlaceKind.LIBRARY),
    DINING(Icons.Rounded.Restaurant, "Comedores", "Dining halls", CityPlaceKind.DINING),
    OTHER(Icons.Rounded.Place, "Otros", "Other", CityPlaceKind.OTHER)
}

private fun cityPlaceKind(place: CityPlace): CityPlaceKind {
    return when {
        place.id.startsWith("biblioteca_") ->
            CityPlaceKind.LIBRARY

        place.id.startsWith("comedor_") ->
            CityPlaceKind.DINING

        place.name.startsWith("Facultad", ignoreCase = true) ||
                place.name.startsWith("ETSI", ignoreCase = true) ||
                place.name.startsWith("ETS ", ignoreCase = true) ->
            CityPlaceKind.FACULTY

        else ->
            CityPlaceKind.OTHER
    }
}

private fun normalizeCitySearch(value: String): String {
    return java.text.Normalizer
        .normalize(
            value.lowercase(java.util.Locale.ROOT),
            java.text.Normalizer.Form.NFD
        )
        .replace(Regex("\\p{Mn}+"), "")
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
private fun CityPlacePickerSheet(
    title: String,
    accent: Color,
    places: List<CityPlace>,
    selectedPlaceId: String?,
    excludedPlaceId: String?,
    onSelect: (CityPlace) -> Unit,
    onDismiss: () -> Unit,
    isMyLocationSelected: Boolean = false,
    onSelectMyLocation: (() -> Unit)? = null
) {
    val isEnglish = LocalIsEnglish.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var query by rememberSaveable {
        mutableStateOf("")
    }

    var filter by rememberSaveable {
        mutableStateOf(CityPlaceFilter.ALL)
    }

    var campusFilter by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val campuses = remember(places) {
        places.map { it.campus }
            .distinct()
            .sorted()
    }

    val normalizedQuery = remember(query) {
        normalizeCitySearch(query.trim())
    }

    val filteredPlaces = remember(
        places,
        normalizedQuery,
        filter,
        campusFilter,
        excludedPlaceId
    ) {
        places.filter { place ->

            val matchesType =
                filter.kind == null ||
                        cityPlaceKind(place) == filter.kind

            val matchesCampus =
                campusFilter == null ||
                        place.campus == campusFilter

            val matchesQuery =
                normalizedQuery.isEmpty() ||
                        normalizeCitySearch(place.name).contains(normalizedQuery) ||
                        normalizeCitySearch(place.campus).contains(normalizedQuery)

            place.id != excludedPlaceId &&
                    matchesType &&
                    matchesCampus &&
                    matchesQuery
        }
    }

    val groupedPlaces = remember(filteredPlaces) {
        filteredPlaces
            .groupBy { it.campus }
            .toSortedMap()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {

            // CABECERA
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BUSCADOR
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = {
                    Text(
                        text = if (isEnglish) {
                            "Search faculty, library, dining hall..."
                        } else {
                            "Buscar facultad, biblioteca, comedor..."
                        },
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Limpiar",
                            tint = MutedText,
                            modifier = Modifier
                                .clickable { query = "" }
                                .padding(8.dp)
                                .size(18.dp)
                        )
                    }
                } else {
                    null
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = SoftBg,
                    unfocusedContainerColor = SoftBg,
                    cursorColor = accent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // FILTROS POR TIPO
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CityPlaceFilter.entries) { item ->
                    CityPlaceFilterPill(
                        text = if (isEnglish) item.englishLabel else item.spanishLabel,
                        icon = item.icon,
                        selected = filter == item,
                        accent = accent
                    ) {
                        filter = item
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FILTROS POR CAMPUS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CityPlaceFilterPill(
                        text = if (isEnglish) "All campuses" else "Todos los campus",
                        selected = campusFilter == null,
                        accent = accent,
                        small = true
                    ) {
                        campusFilter = null
                    }
                }

                items(campuses) { campus ->
                    CityPlaceFilterPill(
                        text = campus,
                        selected = campusFilter == campus,
                        accent = accent,
                        small = true
                    ) {
                        campusFilter =
                            if (campusFilter == campus) null else campus
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CONTADOR
            Text(
                text = if (isEnglish) {
                    "${filteredPlaces.size} locations"
                } else {
                    "${filteredPlaces.size} ubicaciones"
                },
                fontSize = 11.sp,
                color = MutedText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // RESULTADOS
            if (filteredPlaces.isEmpty() && onSelectMyLocation == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔎", fontSize = 32.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isEnglish) {
                                "No matching locations"
                            } else {
                                "No hay ubicaciones que coincidan"
                            },
                            fontSize = 13.sp,
                            color = MutedText
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 4.dp,
                        bottom = 32.dp
                    )
                ) {

                    // OPCIÓN ESPECIAL: MI UBICACIÓN
                    if (onSelectMyLocation != null) {
                        item(key = "my_location_option") {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectMyLocation()
                                        }
                                        .padding(
                                            horizontal = 4.dp,
                                            vertical = 12.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MyLocation,
                                        contentDescription = null,
                                        tint = UgrRed,
                                        modifier = Modifier.size(22.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (isEnglish) "My location" else "Mi ubicación",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Ink
                                        )

                                        Text(
                                            text = if (isEnglish) "GPS location" else "Ubicación GPS",
                                            fontSize = 10.sp,
                                            color = MutedText
                                        )
                                    }

                                    if (isMyLocationSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = GreenStatus,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    color = CardBorder.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // UBICACIONES AGRUPADAS POR CAMPUS
                    groupedPlaces.forEach { (campus, campusPlaces) ->

                        stickyHeader(key = "campus_$campus") {
                            Text(
                                text = campus.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(
                                        top = 12.dp,
                                        bottom = 6.dp
                                    )
                            )
                        }

                        items(
                            items = campusPlaces,
                            key = { it.id }
                        ) { place ->

                            val placeIcon = when (cityPlaceKind(place)) {
                                CityPlaceKind.LIBRARY -> "📚"
                                CityPlaceKind.DINING -> "🍽️"
                                CityPlaceKind.FACULTY -> "🏛️"
                                CityPlaceKind.OTHER -> "📍"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(place)
                                    }
                                    .padding(
                                        horizontal = 4.dp,
                                        vertical = 12.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = placeIcon,
                                    fontSize = 21.sp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = place.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    )

                                    Text(
                                        text = place.campus,
                                        fontSize = 10.sp,
                                        color = MutedText
                                    )
                                }

                                if (place.id == selectedPlaceId) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(20.dp)
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
}

@Composable
private fun CityPlaceFilterPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    selected: Boolean,
    accent: Color,
    small: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) accent.copy(alpha = 0.12f)
                else SoftBg
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    accent.copy(alpha = 0.5f)
                } else {
                    CardBorder
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = if (small) 7.dp else 9.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) accent else Ink,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = if (small) 10.sp else 11.sp,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
                color = if (selected) accent else Ink
            )
        }
    }
}

@Composable
private fun CityPlaceSelectorField(
    title: String,
    subtitle: String?,
    placeholder: String,
    icon: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = SoftBg,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            CardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title.ifBlank { placeholder },
                    fontSize = 13.sp,
                    fontWeight = if (title.isBlank()) {
                        FontWeight.Normal
                    } else {
                        FontWeight.SemiBold
                    },
                    color = if (title.isBlank()) MutedText else Ink,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = MutedText,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "⌄",
                fontSize = 20.sp,
                color = MutedText
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
    destination: GeoPoint?,
    destinationId: String?,
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
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = null,
                    tint = if (following.value) Color.White else Ink,
                    modifier = Modifier.size(22.dp)
                )
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (LocalIsEnglish.current) "Allow location to see where you are" else "Permitir ubicación para ver dónde estás",
                fontSize = 12.sp,
                color = Ink
            )
        }
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