package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.model.PointOfInterest
import com.example.aplicacionmovilnpi.navigation.EtsiitData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import com.example.aplicacionmovilnpi.ui.theme.UgrRedSoft
import java.text.Normalizer

/** Grupos de categorías que se muestran como filtros en el selector. */
private enum class PoiFilter(val label: String, val icon: String, val categories: Set<String>?) {
    ALL("Todo", "✨", null),
    AULAS("Aulas", "🎓", setOf("AULA")),
    DESPACHOS("Despachos", "👨‍🏫", setOf("DESPACHO")),
    SERVICIOS("Servicios", "☕", setOf("SERVICIO", "ZONA_COMUN")),
    ACCESOS("Accesos", "🚪", setOf("ACCESO", "TRANSITO")),
    ACCESIBILIDAD("Accesibilidad", "♿", setOf("ACCESIBILIDAD"))
}

fun poiCategoryIcon(category: String): String = when (category) {
    "AULA" -> "🎓"
    "DESPACHO" -> "👨‍🏫"
    "SERVICIO" -> "☕"
    "ZONA_COMUN" -> "🏛️"
    "ACCESO" -> "🚪"
    "TRANSITO" -> "🚶"
    "ACCESIBILIDAD" -> "♿"
    else -> "📍"
}

fun poiCategoryLabel(category: String): String = when (category) {
    "AULA" -> "Aula"
    "DESPACHO" -> "Despacho"
    "SERVICIO" -> "Servicio"
    "ZONA_COMUN" -> "Zona común"
    "ACCESO" -> "Acceso"
    "TRANSITO" -> "Tránsito"
    "ACCESIBILIDAD" -> "Accesibilidad"
    else -> category.lowercase().replaceFirstChar { it.uppercase() }
}

fun poiFloor(poi: PointOfInterest): Int? = EtsiitData.nodes[poi.nodeId]?.floor

private fun floorTitle(floor: Int?): String = when (floor) {
    null -> "Otras ubicaciones"
    -1 -> "Planta sótano (S1)"
    0 -> "Planta baja (PB)"
    else -> "Planta $floor (P$floor)"
}

/** Quita tildes y pasa a minúsculas para que "cafeteria" encuentre "Cafetería". */
private fun String.normalized(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase()

/**
 * Hoja inferior para elegir una ubicación: buscador, filtros por tipo y planta,
 * y la lista agrupada por planta.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PoiPickerSheet(
    title: String,
    accent: Color,
    pois: List<PointOfInterest>,
    selectedPoiId: String?,
    excludedPoiId: String?,
    onSelect: (PointOfInterest) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(PoiFilter.ALL) }
    var floorFilter by rememberSaveable { mutableStateOf<Int?>(null) }

    val floorsByPoi = remember(pois) { pois.associate { it.id to poiFloor(it) } }
    val availableFloors = remember(floorsByPoi) {
        floorsByPoi.values.filterNotNull().distinct().sorted()
    }

    val normalizedQuery = query.trim().normalized()
    val allowedCategories = filter.categories
    val filtered = pois.filter { poi ->
        poi.id != excludedPoiId &&
            (allowedCategories == null || poi.category in allowedCategories) &&
            (floorFilter == null || floorsByPoi[poi.id] == floorFilter) &&
            (normalizedQuery.isEmpty() ||
                poi.nameEs.normalized().contains(normalizedQuery) ||
                poi.nameEn.normalized().contains(normalizedQuery) ||
                poi.description.normalized().contains(normalizedQuery))
    }
    val grouped = filtered
        .groupBy { floorsByPoi[it.id] }
        .toSortedMap(compareBy(nullsLast()) { it })
        .mapValues { (_, list) -> list.sortedBy { it.nameEs.normalized() } }

    // Al llegar al final de la lista, el scroll sobrante hacia arriba se pasaba a la
    // hoja, que intentaba estirarse y rebotaba (efecto "tintineo"). Lo consumimos aquí.
    // El sobrante hacia abajo sí llega a la hoja, para poder cerrarla deslizando.
    val blockUpwardOverscrollToSheet = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (available.y < 0f) available else Offset.Zero

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                if (available.y < 0f) available else Velocity.Zero
        }
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
            // Cabecera
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
                Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buscador
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Buscar aula, despacho, servicio…", fontSize = 13.sp) },
                leadingIcon = { Text("🔍", fontSize = 16.sp) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        Text(
                            text = "✕",
                            fontSize = 16.sp,
                            color = MutedText,
                            modifier = Modifier
                                .clickable { query = "" }
                                .padding(8.dp)
                        )
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UgrRed,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = SoftBg,
                    unfocusedContainerColor = SoftBg,
                    cursorColor = UgrRed
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros por tipo
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PoiFilter.entries) { f ->
                    FilterPill(
                        text = "${f.icon} ${f.label}",
                        selected = filter == f,
                        onClick = { filter = f }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filtros por planta
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterPill(text = "Todas las plantas", selected = floorFilter == null, small = true) {
                        floorFilter = null
                    }
                }
                items(availableFloors) { floor ->
                    FilterPill(text = getFloorName(floor), selected = floorFilter == floor, small = true) {
                        floorFilter = if (floorFilter == floor) null else floor
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${filtered.size} ubicaciones",
                fontSize = 11.sp,
                color = MutedText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (filtered.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔎", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay ubicaciones que coincidan", fontSize = 13.sp, color = MutedText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(blockUpwardOverscrollToSheet),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp)
                ) {
                    grouped.forEach { (floor, floorPois) ->
                        stickyHeader(key = "floor_$floor") {
                            Text(
                                text = floorTitle(floor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = UgrRed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(top = 12.dp, bottom = 6.dp)
                            )
                        }
                        items(floorPois, key = { it.id }) { poi ->
                            PoiPickerRow(
                                poi = poi,
                                selected = poi.id == selectedPoiId,
                                onClick = { onSelect(poi) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    small: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) UgrRed else Color.White,
        shape = RoundedCornerShape(50),
        border = if (selected) null else BorderStroke(1.dp, CardBorder)
    ) {
        Text(
            text = text,
            fontSize = if (small) 11.sp else 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else Ink,
            modifier = Modifier.padding(
                horizontal = if (small) 12.dp else 14.dp,
                vertical = if (small) 6.dp else 8.dp
            )
        )
    }
}

@Composable
private fun PoiPickerRow(
    poi: PointOfInterest,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onClick() },
        color = if (selected) UgrRedSoft else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) UgrRed else CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SoftBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = poiCategoryIcon(poi.category), fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poi.nameEs,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = poi.description.ifBlank { poiCategoryLabel(poi.category) },
                    fontSize = 11.sp,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UgrRed)
            }
        }
    }
}
