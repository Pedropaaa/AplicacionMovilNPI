package com.example.aplicacionmovilnpi.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrGenerator {

    /**
     * Genera la cadena codificada para una ruta entre dos POIs.
     */
    fun createRouteQrContent(startPoiId: String, destPoiId: String, isAccessible: Boolean = false): String {
        return "ROUTE:$startPoiId:$destPoiId:$isAccessible"
    }

    /**
     * Convierte cualquier cadena en una imagen QR tipo [ImageBitmap] lista para Compose.
     */
    fun generateQrBitmap(content: String, width: Int = 512, height: Int = 512): ImageBitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
