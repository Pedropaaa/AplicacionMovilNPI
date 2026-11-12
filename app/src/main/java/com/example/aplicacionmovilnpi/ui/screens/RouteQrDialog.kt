package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Accessible
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aplicacionmovilnpi.model.PointOfInterest
import com.example.aplicacionmovilnpi.qr.QrGenerator
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

@Composable
fun RouteQrDialog(
    startPoi: PointOfInterest?,
    destPoi: PointOfInterest?,
    isAccessible: Boolean,
    onDismiss: () -> Unit
) {
    val isEnglish = LocalIsEnglish.current
    val startPoiId = startPoi?.id ?: ""
    val destPoiId = destPoi?.id ?: ""
    val startName = startPoi?.let { if (isEnglish && it.nameEn.isNotBlank()) it.nameEn else it.nameEs } ?: if (isEnglish) "Origin" else "Origen"
    val destName = destPoi?.let { if (isEnglish && it.nameEn.isNotBlank()) it.nameEn else it.nameEs } ?: if (isEnglish) "Destination" else "Destino"

    val qrContent = remember(startPoiId, destPoiId, isAccessible) {
        QrGenerator.createRouteQrContent(startPoiId, destPoiId, isAccessible)
    }

    val qrBitmap = remember(qrContent) {
        QrGenerator.generateQrBitmap(qrContent, width = 512, height = 512)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.QrCode,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "Route QR Code" else "QR de la Ruta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$startName ➔ $destName",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UgrRed,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = ToneBlueBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAccessible) Icons.AutoMirrored.Rounded.Accessible else Icons.AutoMirrored.Rounded.DirectionsWalk,
                            contentDescription = null,
                            tint = Ink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) { if (isAccessible) "Accessible Route" else "Standard Route" } else { if (isAccessible) "Ruta accesible PMR" else "Ruta Estándar" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = if (isEnglish) "Route QR code" else "Código QR de la Ruta",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = if (isEnglish) "Error generating QR code" else "Error al generar QR",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isEnglish) "Scan this code with the app on another device to load this route automatically." else "Escanea este código con el escáner de la app en otro dispositivo para cargar automáticamente esta ruta.",
                    fontSize = 11.sp,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isEnglish) "Close" else "Cerrar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
