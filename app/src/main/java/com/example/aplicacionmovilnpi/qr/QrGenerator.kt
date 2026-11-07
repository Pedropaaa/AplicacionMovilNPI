package com.example.aplicacionmovilnpi.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generador de códigos QR compatible con la API antigua (Bitmap) y con Compose
 * (ImageBitmap), además de proporcionar el formato de contenido para rutas.
 */
object QrGenerator {

    /**
     * Genera el contenido que se guarda en un QR de ruta.
     * Formato esperado por el lector: ROUTE:<origen>:<destino>:<accesible>.
     */
    fun createRouteQrContent(
        startPoiId: String,
        destPoiId: String,
        isAccessible: Boolean = false
    ): String = "ROUTE:$startPoiId:$destPoiId:$isAccessible"

    /**
     * API conservada de HEAD: genera un QR como Bitmap de Android.
     */
    fun generateQRCode(
        text: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? = createQrBitmap(text, width, height)

    /**
     * API de rutasAutomaticas: genera un QR como ImageBitmap listo para Compose.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): ImageBitmap? = createQrBitmap(content, width, height)?.asImageBitmap()

    /**
     * Implementación compartida para evitar mantener dos generadores distintos.
     */
    private fun createQrBitmap(
        content: String,
        width: Int,
        height: Int
    ): Bitmap? {
        if (content.isBlank() || width <= 0 || height <= 0) return null

        return try {
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height
            )

            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val rowOffset = y * width
                for (x in 0 until width) {
                    pixels[rowOffset + x] =
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }

            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }
}
