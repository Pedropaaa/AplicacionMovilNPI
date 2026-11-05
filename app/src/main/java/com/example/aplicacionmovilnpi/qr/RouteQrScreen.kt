package com.example.aplicacionmovilnpi.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun RouteQrScreen(
    routeDetails: String, // Texto o identificadores de la ruta que se van a codificar
    onClose: () -> Unit   // Acción para cerrar o salir de la pantalla del QR
) {
    // Genera el código QR en un Bitmap usando el generador de la carpeta qr
    val qrBitmap = remember(routeDetails) {
        QrGenerator.generateQRCode(routeDetails)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("¡Código QR de tu ruta generada!")
            Spacer(modifier = Modifier.height(16.dp))

            if (qrBitmap != null) {
                // Muestra la imagen del QR en pantalla convertida a ImageBitmap
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR de la Ruta",
                    modifier = Modifier.size(250.dp)
                )
            } else {
                Text("Error al generar el código QR")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onClose) {
                Text("Volver / Cerrar")
            }
        }
    }
}