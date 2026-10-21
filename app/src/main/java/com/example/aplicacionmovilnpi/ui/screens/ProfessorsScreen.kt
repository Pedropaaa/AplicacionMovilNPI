package com.example.aplicacionmovilnpi.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.ActivityKind
import com.example.aplicacionmovilnpi.data.Moment
import com.example.aplicacionmovilnpi.data.Professor
import com.example.aplicacionmovilnpi.data.ProfessorLocator
import com.example.aplicacionmovilnpi.data.Whereabouts
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueText
import com.example.aplicacionmovilnpi.ui.theme.TonePurpleBg
import com.example.aplicacionmovilnpi.ui.theme.TonePurpleText
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseBg
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import kotlinx.coroutines.delay

@Composable
fun ProfessorsScreen(
    onNavigateToMap: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Se recalcula cada 30 s para que el estado ("en clase", "en tutoría"...) siga la hora real.
    var moment by remember { mutableStateOf(Moment.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            moment = Moment.now()
        }
    }

    val filteredProfessors = SampleData.professors.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.subjects.any { subject -> subject.contains(searchQuery, ignoreCase = true) } ||
                it.office.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Profesorado y Tutorías",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = "4º CSI · ${moment.semester}º semestre · dónde está cada profesor ahora",
            fontSize = 12.sp,
            color = MutedText,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(text = "Buscar profesor, asignatura o despacho...", fontSize = 12.sp, color = MutedText) },
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

        Spacer(modifier = Modifier.height(18.dp))

        if (filteredProfessors.isEmpty()) {
            Text(
                text = "No hay ningún profesor de 4º CSI que coincida con «$searchQuery».",
                fontSize = 12.sp,
                color = MutedText
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredProfessors) { professor ->
                ProfessorCard(
                    professor = professor,
                    whereabouts = ProfessorLocator.locate(professor, moment),
                    semester = moment.semester,
                    onNavigateToMap = onNavigateToMap
                )
            }
            item { Spacer(modifier = Modifier.height(6.dp)) }
        }
    }
}

@Composable
fun ProfessorCard(
    professor: Professor,
    whereabouts: Whereabouts,
    semester: Int,
    onNavigateToMap: (String) -> Unit
) {
    val context = LocalContext.current
    val (avatarBg, avatarText) = when (professor.tone) {
        "red" -> ToneRoseBg to ToneRoseText
        "blue" -> ToneBlueBg to ToneBlueText
        else -> TonePurpleBg to TonePurpleText
    }

    val currentSlot = (whereabouts as? Whereabouts.Located)?.slot
    val (statusLabel, statusBg, statusText) = when (currentSlot?.kind) {
        ActivityKind.CLASE -> Triple("En clase", ToneRoseBg, ToneRoseText)
        ActivityKind.TUTORIA -> Triple("En tutoría", ToneBlueBg, ToneBlueText)
        null -> Triple("Sin información", SoftBg, MutedText)
    }

    // A dónde guía el botón: primero donde está ahora; si no se sabe, a su despacho (si está en el plano).
    val routeTarget: Pair<String, String>? = when {
        currentSlot?.poiId != null -> "Guiarme a ${currentSlot.place}" to currentSlot.poiId
        currentSlot == null && professor.officePoiId != null -> "Ir a su despacho" to professor.officePoiId
        else -> null
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Initials Circle Avatar
                Surface(
                    color = avatarBg,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = professor.initials,
                            color = avatarText,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = professor.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = "${professor.role} · ${professor.department}",
                        fontSize = 10.sp,
                        color = MutedText
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ¿Dónde está ahora?
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = statusBg,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    when (whereabouts) {
                        is Whereabouts.Located -> {
                            val slot = whereabouts.slot
                            val activity = if (slot.kind == ActivityKind.CLASE) slot.title else "Tutoría"
                            Text(
                                text = "📍 Ahora: $activity en ${slot.place} (hasta las ${slot.end})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusText
                            )
                            if (slot.poiId == null) {
                                Text(
                                    text = "Este lugar todavía no está en el plano de la app, así que no podemos guiarte hasta allí.",
                                    fontSize = 10.sp,
                                    color = Ink,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        is Whereabouts.Unknown -> {
                            Text(
                                text = "ℹ️ Ahora mismo no sabemos dónde está",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = professor.note
                                    ?: "No tiene clase ni tutoría en la ETSIIT a esta hora. Puede estar en otro centro o fuera de su horario publicado.",
                                fontSize = 10.sp,
                                color = MutedText,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            whereabouts.next?.let { next ->
                                val activity = if (next.kind == ActivityKind.CLASE) next.title else "Tutoría"
                                Text(
                                    text = "Próximamente: ${ProfessorLocator.dayName(next.day)} ${next.start} · $activity · ${next.place}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details
            Text(
                text = "📚 Asignaturas: ${professor.subjects.joinToString(", ")}",
                fontSize = 11.sp,
                color = Ink
            )

            Text(
                text = "🚪 Despacho: ${professor.office}",
                fontSize = 11.sp,
                color = Ink,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "🕒 Tutorías (${semester}º sem.): ${ProfessorLocator.officeHoursText(professor, semester)}",
                fontSize = 11.sp,
                color = MutedText,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${professor.email}"))
                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            // Sin app de correo instalada: no hacemos nada.
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Text(text = "✉️ Enviar correo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
                }

                OutlinedButton(
                    onClick = { routeTarget?.let { onNavigateToMap(it.second) } },
                    enabled = routeTarget != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = routeTarget?.let { "🗺️ ${it.first}" } ?: "Sin ubicación en el plano",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (routeTarget != null) UgrRed else MutedText,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
