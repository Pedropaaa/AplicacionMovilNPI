package com.example.aplicacionmovilnpi.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
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
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseBg
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

@Composable
fun GradesScreen(onBack: () -> Unit) {
    var selectedCourse by remember { mutableStateOf("4º") }
    val gradesList = SampleData.gradesMap[selectedCourse] ?: emptyList()
    val average = remember {
        val passed = SampleData.gradesMap.values.flatten().mapNotNull { it.grade.replace(",", ".").toDoubleOrNull() }
        "%.1f".format(passed.average()).replace(".", ",")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back Button Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⬅ Volver a Más",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UgrRed,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Expediente y Calificaciones",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = "${FacultyData.Student.degree} · ${FacultyData.Student.course}",
            fontSize = 12.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Average Grade Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ToneRoseBg,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "NOTA MEDIA ACUMULADA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ToneRoseText)
                    Text(text = "$average / 10", fontSize = 24.sp, fontWeight = FontWeight.Black, color = UgrRed)
                }

                Surface(
                    color = UgrRed,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "180 ECTS Aprobados",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Course Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("1º", "2º", "3º", "4º").forEach { course ->
                val selected = course == selectedCourse
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable { selectedCourse = course },
                    color = if (selected) UgrRed else Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Curso $course",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else Ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grades List
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                gradesList.forEachIndexed { index, grade ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = grade.subject, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.weight(1f))
                        Surface(
                            color = ToneRoseBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = grade.grade,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = ToneRoseText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (index < gradesList.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "📥 Descargar Resguardo en PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
