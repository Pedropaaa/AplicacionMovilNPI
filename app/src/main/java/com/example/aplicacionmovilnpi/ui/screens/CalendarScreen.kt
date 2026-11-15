package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.CalendarEventItem
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.ToneOrangeBg
import com.example.aplicacionmovilnpi.ui.theme.ToneOrangeText
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseBg
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

/**
 * Pantalla que muestra la agenda de entregas, exámenes y eventos académicos programados.
 *
 * Renderiza la lista de hitos académicos con distinción visual entre entregas de prácticas
 * y examentes oficiales, ofreciendo localización bilingüe.
 *
 * @param onBack Callback de navegación para regresar a la vista de origen.
 */
@Composable
fun CalendarScreen(onBack: () -> Unit) {
    // 1. Obtener idioma del contexto de composición
    val isEnglish = LocalIsEnglish.current

    // 2. Contenedor principal con desplazamiento vertical
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 3. Botón de retroceso
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isEnglish) "⬅ Back to More" else "⬅ Volver a Más",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UgrRed,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Cabecera principal de la pantalla
        Text(
            text = if (isEnglish) "Calendar & Deadlines" else "Calendario y Entregas",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = if (isEnglish) "Assignments, exams and events" else "Entregas de trabajos, exámenes y eventos",
            fontSize = 12.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Renderizado secuencial de tarjetas de eventos agendados
        SampleData.initialEvents.forEach { event ->
            CalendarEventCard(event = event)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Tarjeta individual que muestra los detalles de un evento del calendario (Examen o Entrega).
 *
 * @param event Objeto de datos [CalendarEventItem] con la información del hito académico.
 */
@Composable
fun CalendarEventCard(event: CalendarEventItem) {
    // 1. Comprobar idioma para aplicar traducciones dinámicas
    val isEnglish = LocalIsEnglish.current

    // 2. Mapear título y descripción según el idioma seleccionado
    val englishTitle = when (event.id) {
        1L -> "NPI Lab 1 submission"
        2L -> "Lab 1 · Visión por Computador"
        3L -> "PL lexical analyzer"
        4L -> "Exam · Teoría de la Información y la Codificación"
        else -> event.title
    }
    val englishSubject = event.subject
    val englishDescription = when (event.id) {
        1L -> "Mobile app prototype and report submission on PRADO."
        2L -> "Filtering and edge detection. Submit on PRADO."
        3L -> "Language specification and lexical analyzer."
        4L -> "Regular exam session. Check the classroom on the ETSIIT website."
        else -> event.description
    }

    // 3. Determinar tipo de evento y aplicar la paleta temática adecuada (Examen vs Entrega)
    val isExam = event.type == "Examen" || event.type == "Exam"
    val (badgeBg, badgeText) = if (isExam) ToneOrangeBg to ToneOrangeText else ToneRoseBg to ToneRoseText

    // 4. Renderizar contenedor en superficie con bordes redondeados
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Insignia con el día del mes
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = event.date.takeLast(2),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = badgeText
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Información detallada de la asignatura, título y hora
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${if (isEnglish && isExam) "EXAM" else if (isEnglish) "ASSIGNMENT" else event.type.uppercase()} · ${if (isEnglish) englishSubject else event.subject}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeText
                )
                Text(
                    text = if (isEnglish) englishTitle else event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEnglish) "Time: ${event.time} · $englishDescription" else "Hora: ${event.time} · ${event.description}",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            }
        }
    }
}
