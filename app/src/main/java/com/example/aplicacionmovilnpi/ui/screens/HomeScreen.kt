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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.data.Moment
import com.example.aplicacionmovilnpi.data.PlaceCategory
import com.example.aplicacionmovilnpi.data.ProfessorLocator
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueText
import com.example.aplicacionmovilnpi.ui.theme.ToneOrangeBg
import com.example.aplicacionmovilnpi.ui.theme.ToneOrangeText
import com.example.aplicacionmovilnpi.ui.theme.TonePurpleBg
import com.example.aplicacionmovilnpi.ui.theme.TonePurpleText
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseBg
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import com.example.aplicacionmovilnpi.ui.theme.UgrRedDark

@Composable
fun HomeScreen(
    onNavigateToSchedule: () -> Unit,
    onNavigateToProfessors: () -> Unit,
    onNavigateToMap: (String?) -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Próxima clase (o la que está en curso) según el horario real de la alumna
    val moment = remember { Moment.now() }
    val nextSlot = remember(moment) { SampleData.nextLesson(moment) }
    val nextLesson = nextSlot?.let(SampleData::toLesson)
    val nextLabel = when {
        nextSlot == null -> "SIN CLASES PROGRAMADAS"
        nextSlot.day == moment.day && moment.minutes >= nextSlot.startMinutes -> "EN CURSO · HASTA LAS ${nextSlot.end}"
        nextSlot.day == moment.day -> {
            val minutes = nextSlot.startMinutes - moment.minutes
            "PRÓXIMA CLASE · EN " + if (minutes < 60) "$minutes MIN" else "${minutes / 60} H ${minutes % 60} MIN"
        }
        else -> "PRÓXIMA CLASE · ${ProfessorLocator.dayName(nextSlot.day).uppercase()}"
    }
    // Primera entrega (o examen) a partir de hoy
    val nextEvent = remember {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(java.util.Date())
        SampleData.initialEvents.sortedBy { it.date }.firstOrNull { it.date >= today }
    }
    val nextEventDate = nextEvent?.let { event ->
        val (_, month, day) = event.date.split("-").map { it.toInt() }
        val months = listOf("ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sept.", "oct.", "nov.", "dic.")
        "$day ${months[month - 1]} · ${if (event.type == "Examen") "A las" else "Hasta las"} ${event.time}"
    }
    val nextFloor = nextLesson?.room?.removePrefix("Aula ")?.substringBefore(".")?.let {
        if (it == "0") "Planta baja" else "Planta $it"
    }

    val nextClassGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF951734),
                Color(0xFFBE2949)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Intro Greeting
        Text(
            text = "¡Hola, ${FacultyData.Student.firstName}! 👋",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = "${FacultyData.Student.course} · ETSIIT",
            fontSize = 12.sp,
            color = MutedText,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(text = "¿Qué clase o despacho buscas?", fontSize = 12.sp, color = MutedText) },
            leadingIcon = { Text(text = "🔍", fontSize = 16.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFDFE2E7),
                focusedBorderColor = UgrRed,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Next Class Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .background(nextClassGradient)
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nextLabel,
                            color = Color(0xFFF7C9D3),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        if (nextLabel.startsWith("EN CURSO")) {
                            Text(text = "🟢 Live", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = nextLesson?.time ?: "--:--", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "AULA", color = Color(0xFFF5C6D0), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = nextLesson?.name ?: "No tienes clases este semestre",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = nextLesson?.let { "📍 ${it.room} · $nextFloor · ${it.time} - ${it.end}" } ?: "Consulta tu horario",
                                color = Color(0xFFF4C9D2),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigateToMap(nextLesson?.poiId) },
                        enabled = nextLesson?.poiId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = UgrRedDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = nextLesson?.let { "Guiarme al ${it.room.lowercase()}  ➔" } ?: "Sin clase a la que guiarte",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Places Quick Categories Horizontal Row
        Text(
            text = "Explorar la facultad",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(SampleData.places) { place ->
                PlaceCategoryCard(place = place, onClick = { onNavigateToMap(null) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Shortcuts Grid
        Text(
            text = "Accesos rápidos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShortcutCard(
                title = "Horario semanal",
                subtitle = "Ver clases",
                icon = "📅",
                bgTone = TonePurpleBg,
                iconTone = TonePurpleText,
                onClick = onNavigateToSchedule,
                modifier = Modifier.weight(1f)
            )

            ShortcutCard(
                title = "Profesorado",
                subtitle = "Tutorías y despachos",
                icon = "👨‍🏫",
                bgTone = ToneRoseBg,
                iconTone = ToneRoseText,
                onClick = onNavigateToProfessors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Next Delivery Banner Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToCalendar() },
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ToneRoseBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "📌", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (nextEvent?.type == "Examen") "PRÓXIMO EXAMEN" else "PRÓXIMA ENTREGA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgrRed
                    )
                    Text(
                        text = nextEvent?.title ?: "Nada pendiente",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = nextEventDate ?: "No tienes entregas próximas",
                        fontSize = 10.sp,
                        color = MutedText
                    )
                }

                Text(text = "➔", fontSize = 14.sp, color = MutedText)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun PlaceCategoryCard(place: PlaceCategory, onClick: () -> Unit) {
    val (bgColor, textColor) = when (place.tone) {
        "rose" -> ToneRoseBg to ToneRoseText
        "purple" -> TonePurpleBg to TonePurpleText
        "blue" -> ToneBlueBg to ToneBlueText
        else -> ToneOrangeBg to ToneOrangeText
    }

    val iconText = when (place.icon) {
        "building" -> "🏢"
        "person" -> "👤"
        "book" -> "📚"
        else -> "☕"
    }

    Surface(
        modifier = Modifier
            .width(100.dp)
            .height(115.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = bgColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = iconText, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = place.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(text = place.detail, fontSize = 9.sp, color = MutedText)
        }
    }
}

@Composable
fun ShortcutCard(
    title: String,
    subtitle: String,
    icon: String,
    bgTone: Color,
    iconTone: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = bgTone,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text(text = subtitle, fontSize = 9.sp, color = MutedText, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}
