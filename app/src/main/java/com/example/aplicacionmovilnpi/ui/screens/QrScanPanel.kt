package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Botón independiente para escanear QR sin modificar LocationStatusCard.
 */
@Composable
fun QrScanButton(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onScanClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = "📷 Escanear QR de ubicación",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}