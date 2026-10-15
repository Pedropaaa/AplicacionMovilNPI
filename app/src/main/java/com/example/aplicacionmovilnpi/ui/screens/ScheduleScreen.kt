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
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

@Composable
fun ScheduleScreen(
    onNavigateToMap: () -> Unit
) {
    var selectedDayIndex by remember { mutableIntStateOf(2) } // Default Miércoles (X 16)
    val daySchedule = SampleData.weekSchedule[selectedDayIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Horario semanal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = "Grado en Ingeniería Informática · ETSIIT",
            fontSize = 12.sp,
            color = MutedText,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Days Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SampleData.weekSchedule.forEachIndexed { index, day ->
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
                            text = day.short,
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
            text = "${daySchedule.day}, ${daySchedule.date} de octubre",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lessons List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(daySchedule.classes) { lesson ->
                LessonCard(lesson = lesson, onNavigateToMap = onNavigateToMap)
            }
        }
    }
}

@Composable
fun LessonCard(
    lesson: ClassLesson,
    onNavigateToMap: () -> Unit
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
                    text = "📍 ${lesson.room}",
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
                onClick = onNavigateToMap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = "Cómo llegar al ${lesson.room} ➔",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }
        }
    }
}
