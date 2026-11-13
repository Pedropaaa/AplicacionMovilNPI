package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wc
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.localizedCourseText
import com.example.aplicacionmovilnpi.ui.localizedDayName
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon

@Composable
fun HomeScreen(
    onNavigateToSchedule: () -> Unit,
    onNavigateToProfessors: () -> Unit,
    onNavigateToMap: (String?) -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    val isEnglish = LocalIsEnglish.current
    var searchQuery by remember { mutableStateOf("") }

    // Próxima clase (o la que está en curso) según el horario real de la alumna
    val moment = remember { Moment.now() }
    val nextSlot = remember(moment) { SampleData.nextLesson(moment) }
    val nextLesson = nextSlot?.let(SampleData::toLesson)
    val nextLabel = when {
        nextSlot == null -> if (isEnglish) "NO CLASSES SCHEDULED" else "SIN CLASES PROGRAMADAS"
        nextSlot.day == moment.day && moment.minutes >= nextSlot.startMinutes -> if (isEnglish) "IN PROGRESS · UNTIL ${nextSlot.end}" else "EN CURSO · HASTA LAS ${nextSlot.end}"
        nextSlot.day == moment.day -> {
            val minutes = nextSlot.startMinutes - moment.minutes
            (if (isEnglish) "NEXT CLASS · IN " else "PRÓXIMA CLASE · EN ") + if (minutes < 60) "$minutes ${if (isEnglish) "MIN" else "MIN"}" else "${minutes / 60} ${if (isEnglish) "H" else "H"} ${minutes % 60} MIN"
        }
        else -> "${if (isEnglish) "NEXT CLASS · " else "PRÓXIMA CLASE · "}${localizedDayName(ProfessorLocator.dayName(nextSlot.day), isEnglish).uppercase()}"
    }
    // Primera entrega (o examen) a partir de hoy
    val nextEvent = remember {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(java.util.Date())
        SampleData.initialEvents.sortedBy { it.date }.firstOrNull { it.date >= today }
    }
    val nextEventDate = nextEvent?.let { event ->
        val (_, month, day) = event.date.split("-").map { it.toInt() }
        val months = if (isEnglish) listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec") else listOf("ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sept.", "oct.", "nov.", "dic.")
        "$day ${months[month - 1]} · ${if (isEnglish) if (event.type == "Examen") "At" else "Due" else if (event.type == "Examen") "A las" else "Hasta las"} ${event.time}"
    }
    val nextFloor = nextLesson?.room?.removePrefix("Aula ")?.substringBefore(".")?.let {
        if (it == "0") if (isEnglish) "Ground floor" else "Planta baja" else "${if (isEnglish) "Floor" else "Planta"} $it"
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
            text = "${if (isEnglish) "Hello" else "¡Hola"}, ${FacultyData.Student.firstName}! 👋",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Ink
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
                        if (nextLabel.startsWith("EN CURSO") || nextLabel.startsWith("IN PROGRESS")) {
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
                                Text(text = if (isEnglish) "ROOM" else "AULA", color = Color(0xFFF5C6D0), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = nextLesson?.name ?: if (isEnglish) "No classes this semester" else "No tienes clases este semestre",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = nextLesson?.let { "📍 ${localizedCourseText(it.room, isEnglish)} · $nextFloor · ${it.time} - ${it.end}" } ?: if (isEnglish) "Check your schedule" else "Consulta tu horario",
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
                            text = nextLesson?.let { "${if (isEnglish) "Guide me to" else "Guiarme al"} ${localizedCourseText(it.room, isEnglish).lowercase()}  ➔" } ?: if (isEnglish) "No class to navigate to" else "Sin clase a la que guiarte",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
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
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = null,
                            tint = ToneRoseText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEnglish) { if (nextEvent?.type == "Examen") "UPCOMING EXAM" else "UPCOMING ASSIGNMENT" } else { if (nextEvent?.type == "Examen") "PRÓXIMO EXAMEN" else "PRÓXIMA ENTREGA" },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgrRed
                    )
                    Text(
                        text = nextEvent?.let { event ->
                            if (isEnglish) when (event.id) {
                                1L -> "NPI Lab 1 submission"
                                2L -> "Lab 1 · Visión por Computador"
                                3L -> "PL lexical analyzer"
                                4L -> "Exam · Teoría de la Información y la Codificación"
                                else -> event.title
                            } else event.title
                        } ?: if (isEnglish) "Nothing pending" else "Nada pendiente",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = nextEventDate ?: if (isEnglish) "No upcoming deadlines" else "No tienes entregas próximas",
                        fontSize = 10.sp,
                        color = MutedText
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Places Quick Categories Horizontal Row
        Text(
            text = if (isEnglish) "Explore the university" else "Explorar la universidad",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // FILA 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 105.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlaceCategoryCard(
                    place = PlaceCategory(
                        name = if (isEnglish) "Dining hall" else "Comedor",
                        detail = if (isEnglish) "Nearest" else "Más cercano",
                        icon = "utensils",
                        tone = "blue"
                    ),
                    onClick = {
                        onNavigateToMap("__dest_comedor_cercano__")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                PlaceCategoryCard(
                    place = PlaceCategory(
                        name = if (isEnglish) "Library" else "Biblioteca",
                        detail = if (isEnglish) "Nearest" else "Más cercana",
                        icon = "book",
                        tone = "rose"
                    ),
                    onClick = {
                        onNavigateToMap("__dest_biblioteca_cercana__")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            // FILA 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 115.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TARJETA IZQUIERDA: ESCÁNER QR
                PlaceCategoryCard(
                    place = PlaceCategory(
                        name = if (isEnglish) "QR Scanner" else "Escáner QR",
                        detail = if (isEnglish) "Scan a code" else "Escanea un código",
                        icon = "qr",
                        tone = "black"
                    ),
                    onClick = {
                        onNavigateToMap("__abrir_escaner_qr__")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // COLUMNA DERECHA: ASEOS Y FUENTES
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlaceCategoryCard(
                        place = PlaceCategory(
                            name = if (isEnglish) "Restrooms" else "Aseos",
                            detail = if (isEnglish) "Nearest" else "Más cercanos",
                            icon = "toilet",
                            tone = "blue"
                        ),
                        onClick = {
                            onNavigateToMap("__dest_aseo_cercano__")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    PlaceCategoryCard(
                        place = PlaceCategory(
                            name = if (isEnglish) "Water fountain" else "Fuente",
                            detail = if (isEnglish) "Nearest" else "Más cercana",
                            icon = "water",
                            tone = "blue"
                        ),
                        onClick = {
                            onNavigateToMap("__dest_fuente_cercana__")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

    }
}


@Composable
fun PlaceCategoryCard(
    place: PlaceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (place.tone) {
        "rose" -> ToneRoseBg to ToneRoseText
        "purple" -> TonePurpleBg to TonePurpleText
        "blue" -> ToneBlueBg to ToneBlueText
        "black" -> Color(0xFFF2F4F7) to Ink
        else -> ToneOrangeBg to ToneOrangeText
    }

    val vectorIcon = when (place.icon) {
        "building" -> Icons.Rounded.Apartment
        "person" -> Icons.Rounded.Person
        "book" -> Icons.AutoMirrored.Rounded.MenuBook
        "toilet" -> Icons.Rounded.Wc
        "water" -> Icons.Rounded.WaterDrop
        "car" -> Icons.Rounded.DirectionsCar
        "utensils" -> Icons.Rounded.Restaurant
        "qr" -> Icons.Rounded.QrCodeScanner
        else -> Icons.Rounded.LocalCafe
    }

    val isHorizontal = place.icon == "toilet" || place.icon == "water"

    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        if (isHorizontal) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = place.name,
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = place.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Text(
                        text = place.detail,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedText,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = place.name,
                            tint = textColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = place.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = place.detail,
                    fontSize = 10.sp,
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
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
