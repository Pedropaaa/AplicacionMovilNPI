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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Accessible
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.model.PointOfInterest
import com.example.aplicacionmovilnpi.model.getDescription
import com.example.aplicacionmovilnpi.model.getName
import com.example.aplicacionmovilnpi.navigation.EtsiitData
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import com.example.aplicacionmovilnpi.ui.theme.UgrRedSoft
import java.text.Normalizer

/** Grupos de categorías que se muestran como filtros en el selector. */
private enum class PoiFilter(val spanishLabel: String, val englishLabel: String, val icon: ImageVector, val categories: Set<String>?) {
    ALL("Todo", "All", Icons.Rounded.Star, null),
    AULAS("Aulas", "Classrooms", Icons.Rounded.School, setOf("AULA")),
    DESPACHOS("Despachos", "Offices", Icons.Rounded.Person, setOf("DESPACHO")),
    SERVICIOS("Servicios", "Services", Icons.Rounded.LocalCafe, setOf("SERVICIO", "ZONA_COMUN")),
    ACCESOS("Accesos", "Entrances", Icons.Rounded.DoorFront, setOf("ACCESO", "TRANSITO")),
    ACCESIBILIDAD("Accesibilidad", "Accessibility", Icons.AutoMirrored.Rounded.Accessible, setOf("ACCESIBILIDAD"))
}

fun poiCategoryIcon(category: String): ImageVector = when (category) {
    "AULA" -> Icons.Rounded.School
    "DESPACHO" -> Icons.Rounded.Person
    "SERVICIO" -> Icons.Rounded.LocalCafe
    "ZONA_COMUN" -> Icons.Rounded.Apartment
    "ACCESO" -> Icons.Rounded.DoorFront
    "TRANSITO" -> Icons.AutoMirrored.Rounded.DirectionsWalk
    "ACCESIBILIDAD" -> Icons.AutoMirrored.Rounded.Accessible
    else -> Icons.Rounded.Place
}

fun poiCategoryLabel(category: String, isEnglish: Boolean = false): String = when (category) {
    "AULA" -> if (isEnglish) "Classroom" else "Aula"
    "DESPACHO" -> if (isEnglish) "Office" else "Despacho"
    "SERVICIO" -> if (isEnglish) "Service" else "Servicio"
    "ZONA_COMUN" -> if (isEnglish) "Common area" else "Zona común"
    "ACCESO" -> if (isEnglish) "Entrance" else "Acceso"
    "TRANSITO" -> if (isEnglish) "Walkway" else "Tránsito"
    "ACCESIBILIDAD" -> if (isEnglish) "Accessibility" else "Accesibilidad"
    else -> category.lowercase().replaceFirstChar { it.uppercase() }
}

fun poiFloor(poi: PointOfInterest): Int? = EtsiitData.nodes[poi.nodeId]?.floor

@Composable
private fun floorTitle(floor: Int?): String {
    val isEnglish = LocalIsEnglish.current
    return when (floor) {
        null -> if (isEnglish) "Other locations" else "Otras ubicaciones"
        -1 -> if (isEnglish) "Basement (B1)" else "Planta sótano (S1)"
        0 -> if (isEnglish) "Ground floor (GF)" else "Planta baja (PB)"
        else -> if (isEnglish) "Floor $floor (F$floor)" else "Planta $floor (P$floor)"
    }
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
    allPois: List<PointOfInterest> = emptyList(),
    pois: List<PointOfInterest> = allPois,
    accent: Color = UgrRed,
    selectedPoiId: String?,
    excludedPoiId: String? = null,
    onSelect: (PointOfInterest) -> Unit,
    onDismiss: () -> Unit
) {
    val effectivePois = remember(allPois, pois, excludedPoiId) {
        val baseList = if (pois.isNotEmpty()) pois else allPois
        if (excludedPoiId != null) baseList.filter { it.id != excludedPoiId } else baseList
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(PoiFilter.ALL) }
    var floorFilter by remember { mutableStateOf<Int?>(null) }

    val availableFloors = remember(effectivePois) {
        effectivePois.mapNotNull { poiFloor(it) }.distinct().sorted()
    }

    val filtered = remember(query, filter, floorFilter, effectivePois) {
        val qNorm = query.trim().normalized()
        val categories = filter.categories
        effectivePois.filter { poi ->
            val matchesCategory = categories == null || categories.contains(poi.category)
            val matchesFloor = floorFilter == null || poiFloor(poi) == floorFilter
            val matchesText = qNorm.isEmpty() ||
                    poi.nameEs.normalized().contains(qNorm) ||
                    poi.nameEn.normalized().contains(qNorm) ||
                    poi.description.normalized().contains(qNorm) ||
                    poi.descriptionEn.normalized().contains(qNorm) ||
                    poi.id.normalized().contains(qNorm)
            matchesCategory && matchesFloor && matchesText
        }
    }

    val grouped = remember(filtered) {
        filtered.groupBy { poiFloor(it) }
            .entries
            .sortedBy { (floor, _) -> floor ?: 99 }
            .associate { it.key to it.value }
    }

    val isExpanded by remember {
        derivedStateOf { sheetState.currentValue == SheetValue.Expanded }
    }

    val blockUpwardOverscrollToSheet = remember(isExpanded) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isExpanded && available.y > 0f) {
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isExpanded && available.y > 0f) {
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (isExpanded && available.y > 0f) {
                    return Velocity(0f, available.y)
                }
                return Velocity.Zero
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(4.dp),
                color = MutedText.copy(alpha = 0.4f),
                shape = CircleShape
            ) {}
        },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Título
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                placeholder = { Text(if (LocalIsEnglish.current) "Search classroom, office, service…" else "Buscar aula, despacho, servicio…", fontSize = 13.sp) },
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
                        text = if (LocalIsEnglish.current) f.englishLabel else f.spanishLabel,
                        icon = f.icon,
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
                    FilterPill(
                        text = if (LocalIsEnglish.current) "All floors" else "Todas las plantas",
                        selected = floorFilter == null,
                        small = true
                    ) {
                        floorFilter = null
                    }
                }
                items(availableFloors) { floor ->
                    FilterPill(
                        text = getFloorName(floor),
                        selected = floorFilter == floor,
                        small = true
                    ) {
                        floorFilter = if (floorFilter == floor) null else floor
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (LocalIsEnglish.current) "${filtered.size} locations" else "${filtered.size} ubicaciones",
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
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (LocalIsEnglish.current) "No matching locations" else "No hay ubicaciones que coincidan", fontSize = 13.sp, color = MutedText)
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
    icon: ImageVector? = null,
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
        Row(
            modifier = Modifier.padding(
                horizontal = if (small) 12.dp else 14.dp,
                vertical = if (small) 6.dp else 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else Ink,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = if (small) 11.sp else 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else Ink
            )
        }
    }
}

@Composable
private fun PoiPickerRow(
    poi: PointOfInterest,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isEnglish = LocalIsEnglish.current

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
                Icon(
                    imageVector = poiCategoryIcon(poi.category),
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poi.getName(isEnglish),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = poi.getDescription(isEnglish).ifBlank { poiCategoryLabel(poi.category, isEnglish) },
                    fontSize = 11.sp,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = UgrRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
