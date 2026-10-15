package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

@Composable
fun AccessibilityScreen(onBack: () -> Unit) {
    var largeText by remember { mutableStateOf(false) }
    var highContrast by remember { mutableStateOf(false) }
    var reduceMotion by remember { mutableStateOf(false) }
    var defaultAccessibleRoute by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
            text = "Accesibilidad",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = "Ajusta la navegación visual y física para la UGR",
            fontSize = 12.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AccessibilityToggleRow(
                    title = "Texto más grande",
                    subtitle = "Aumenta el tamaño de la tipografía",
                    checked = largeText,
                    onCheckedChange = { largeText = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                AccessibilityToggleRow(
                    title = "Alto contraste",
                    subtitle = "Mejora la legibilidad de elementos",
                    checked = highContrast,
                    onCheckedChange = { highContrast = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                AccessibilityToggleRow(
                    title = "Reducir movimiento",
                    subtitle = "Desactiva animaciones no esenciales",
                    checked = reduceMotion,
                    onCheckedChange = { reduceMotion = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                AccessibilityToggleRow(
                    title = "Ruta accesible por defecto",
                    subtitle = "Priorizar siempre ascensores y rampas sin escaleras",
                    checked = defaultAccessibleRoute,
                    onCheckedChange = { defaultAccessibleRoute = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AccessibilityToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(text = subtitle, fontSize = 11.sp, color = MutedText, modifier = Modifier.padding(top = 2.dp))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = UgrRed)
        )
    }
}
