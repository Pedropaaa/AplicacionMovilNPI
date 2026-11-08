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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.ClassLesson
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.localizedCourseText
import com.example.aplicacionmovilnpi.ui.localizedDayName
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.data.Moment
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

@Composable
fun ScheduleScreen(
    onNavigateToMap: (String?) -> Unit
) {
    val isEnglish = LocalIsEnglish.current
    val weekSchedule = remember { SampleData.weekSchedule() }
    // Por defecto, el día de hoy (o el lunes si es fin de semana)
    var selectedDayIndex by remember { mutableIntStateOf(Moment.now().day.let { if (it > 5) 0 else it - 1 }) }
    val daySchedule = weekSchedule[selectedDayIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isEnglish) "Weekly Schedule" else "Horario semanal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Days Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            weekSchedule.forEachIndexed { index, day ->
                val selected = index == selectedDayIndex
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clickable { selectedDayIndex = index },
                    color = if (selected) UgrRed else Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isEnglish) listOf("M", "T", "W", "T", "F")[index] else day.short,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MutedText
                        )
                        Text(
                            text = day.date,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selected) Color.White else Ink,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Day Banner
        Text(
            text = if (isEnglish) "${localizedDayName(daySchedule.day, true)}, ${daySchedule.date} ${when (daySchedule.month.lowercase()) { "enero" -> "January"; "febrero" -> "February"; "marzo" -> "March"; "abril" -> "April"; "mayo" -> "May"; "junio" -> "June"; "julio" -> "July"; "agosto" -> "August"; "septiembre" -> "September"; "octubre" -> "October"; "noviembre" -> "November"; "diciembre" -> "December"; else -> daySchedule.month }}" else "${daySchedule.day}, ${daySchedule.date} de ${daySchedule.month}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (daySchedule.classes.isEmpty()) {
            Text(
                text = if (isEnglish) "No classes today 🎉" else "No tienes clases este día 🎉",
                fontSize = 12.sp,
                color = MutedText
            )
        }

        // Lessons List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(daySchedule.classes) { lesson ->
                LessonCard(lesson = lesson, onNavigateToMap = onNavigateToMap, isEnglish = isEnglish)
            }
        }
    }
}

@Composable
fun LessonCard(
    lesson: ClassLesson,
    onNavigateToMap: (String?) -> Unit,
    isEnglish: Boolean
) {
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
                Surface(
                    color = SoftBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🕒 ${lesson.time} - ${lesson.end}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "📍 ${localizedCourseText(lesson.room, LocalIsEnglish.current)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgrRed
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = lesson.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { onNavigateToMap(lesson.poiId) },
                enabled = lesson.poiId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = if (isEnglish) {
                        if (lesson.poiId != null) "Directions to ${lesson.room} ➔" else "${lesson.room} is not on the map yet"
                    } else {
                        if (lesson.poiId != null) "Cómo llegar al ${lesson.room} ➔" else "${lesson.room} aún no está en el plano"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }
        }
    }
}
