package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.CampusItem
import com.example.aplicacionmovilnpi.data.PointOfInterest
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.GreenStatus
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

@Composable
fun MapScreen() {
    var mapMode by remember { mutableStateOf("indoor") } // "city" or "indoor"
    var selectedFloor by remember { mutableStateOf("P1") }
    var destinationRoom by remember { mutableStateOf("Aula B-12") }
    var isRouteOpen by remember { mutableStateOf(false) }
    var isAccessibleRoute by remember { mutableStateOf(false) }

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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("P2", "P1", "PB", "S1").forEach { floor ->
                    val selected = floor == selectedFloor
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { selectedFloor = floor },
                        color = if (selected) UgrRed else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = floor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else Ink
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Floor Plan Layout Simulation
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
                        Text(
                            text = "Plano de la Planta $selectedFloor",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(text = "ETSIIT · Bloque A/B", fontSize = 10.sp, color = MutedText)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Floor Grid Boxes
                    val rooms = SampleData.floorRooms[selectedFloor] ?: emptyList()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rooms.forEach { room ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        destinationRoom = room
                                        isRouteOpen = true
                                    },
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🚪 $room", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                                    Text(text = "Guiarme ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UgrRed)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Points of Interest Section
            Text(
                text = "Puntos de Interés",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SampleData.pointsOfInterest) { poi ->
                    PoiChip(poi = poi) {
                        destinationRoom = poi.name
                        isRouteOpen = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Route Guidance Modal/Card if triggered
            if (isRouteOpen) {
                RouteGuidanceCard(
                    destination = destinationRoom,
                    isAccessible = isAccessibleRoute,
                    onAccessibleToggle = { isAccessibleRoute = it },
                    onClose = { isRouteOpen = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
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
            Text(text = poi.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "(${poi.detail})", fontSize = 9.sp, color = MutedText)
        }
    }
}

@Composable
fun RouteGuidanceCard(
    destination: String,
    isAccessible: Boolean,
    onAccessibleToggle: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    val steps = SampleData.getRouteSteps(isAccessible, destination)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, UgrRed)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RUTA ACTIVA PASO A PASO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgrRed
                    )
                    Text(
                        text = "Destino: $destination",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }

                Text(
                    text = "✖",
                    fontSize = 16.sp,
                    color = MutedText,
                    modifier = Modifier.clickable { onClose() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Accessible Route Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "♿ Ruta accesible (usar ascensor)",
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Steps List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                steps.forEach { step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SoftBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = step.icon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = step.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            if (step.distance.isNotEmpty()) {
                                Text(text = step.distance, fontSize = 10.sp, color = MutedText)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Finalizar guía de ruta", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
