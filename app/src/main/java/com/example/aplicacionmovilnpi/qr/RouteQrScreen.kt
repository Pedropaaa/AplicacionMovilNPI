package com.example.aplicacionmovilnpi.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

/**
 * Pantalla que genera y muestra la imagen de un código QR que codifica los datos de una ruta.
 *
 * @param routeDetails Cadena de texto formateada con la información de la ruta compartida.
 * @param onClose Callback ejecutado para cerrar la vista del código QR.
 */
@Composable
fun RouteQrScreen(
    routeDetails: String,
    onClose: () -> Unit
) {
    // 1. Generar el código QR como Bitmap a partir del texto de la ruta
    val qrBitmap = remember(routeDetails) {
        QrGenerator.generateQRCode(routeDetails)
    }

    // 2. Renderizar contenedor principal centrado
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

            // 3. Renderizar la imagen del código QR o el mensaje de error si no pudo generarse
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR de la Ruta",
                    modifier = Modifier.size(250.dp)
                )
            } else {
                Text("Error al generar el código QR")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Botón de cierre
            Button(onClick = onClose) {
                Text("Volver / Cerrar")
            }
        }
    }
}
