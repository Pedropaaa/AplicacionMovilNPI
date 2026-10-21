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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueBg
import com.example.aplicacionmovilnpi.ui.theme.ToneOrangeBg
import com.example.aplicacionmovilnpi.ui.theme.TonePurpleBg
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseBg

@Composable
fun MoreScreen(
    onNavigateToSubScreen: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Más opciones",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = "Expediente, entregas, mensajería y configuración",
            fontSize = 12.sp,
            color = MutedText,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Options List
        MoreOptionCard(
            title = "Expediente y Notas",
            subtitle = "Calificaciones y nota media",
            icon = "📊",
            bgTone = ToneRoseBg,
            onClick = { onNavigateToSubScreen("grades") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = "Calendario y Entregas",
            subtitle = "Exámenes, entregas y eventos",
            icon = "📅",
            bgTone = TonePurpleBg,
            onClick = { onNavigateToSubScreen("calendar") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = "Mensajes y Avisos",
            subtitle = "Buzón de correo institucional UGR",
            icon = "✉️",
            bgTone = ToneBlueBg,
            onClick = { onNavigateToSubScreen("messages") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = "Soporte y Ayuda",
            subtitle = "Preguntas frecuentes e incidencias",
            icon = "❓",
            bgTone = ToneOrangeBg,
            onClick = { onNavigateToSubScreen("help") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = "Accesibilidad",
            subtitle = "Texto grande, contraste y rutas adaptadas",
            icon = "♿",
            bgTone = SoftBg,
            onClick = { onNavigateToSubScreen("accessibility") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = "Mi Perfil",
            subtitle = "${FacultyData.Student.name} · ${FacultyData.Student.course}",
            icon = "👤",
            bgTone = SoftBg,
            onClick = { onNavigateToSubScreen("profile") }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun MoreOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    bgTone: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = androidx.compose.ui.graphics.Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = bgTone,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text(text = subtitle, fontSize = 11.sp, color = MutedText, modifier = Modifier.padding(top = 2.dp))
            }

            Text(text = "➔", fontSize = 14.sp, color = MutedText)
        }
    }
}
