package com.example.aplicacionmovilnpi.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generador de códigos QR del módulo de navegación de la ETSIIT.
 *
 * Utiliza la librería ZXing para codificar cadenas de texto en matrices de píxeles (BitMatrix)
 * y generar mapas de bits utilizables en vistas Android tradicionales ([Bitmap]) o en componentes
 * de Jetpack Compose ([ImageBitmap]).
 */
object QrGenerator {

    /**
     * Genera la cadena de contenido formateada que representa una ruta compartida entre dos POIs.
     *
     * Formato estandarizado esperado por el lector de la aplicación:
     * `ROUTE:<startPoiId>:<destPoiId>:<isAccessible>`
     *
     * @param startPoiId Identificador único del POI de origen.
     * @param destPoiId Identificador único del POI de destino.
     * @param isAccessible Indica si la ruta generada debe ser accesible para personas con movilidad reducida (PMR).
     * @return Cadena de texto formateada para ser codificada dentro del código QR.
     */
    fun createRouteQrContent(
        startPoiId: String,
        destPoiId: String,
        isAccessible: Boolean = false
    ): String = "ROUTE:$startPoiId:$destPoiId:$isAccessible"

    /**
     * Genera un código QR formateado como un [Bitmap] nativo de Android.
     *
     * @param text Texto o datos a codificar dentro del código QR.
     * @param width Ancho del código QR en píxeles (por defecto 512px).
     * @param height Alto del código QR en píxeles (por defecto 512px).
     * @return Objeto [Bitmap] con la representación visual del QR, o `null` si falla la generación o el texto está vacío.
     */
    fun generateQRCode(
        text: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? = createQrBitmap(text, width, height)

    /**
     * Genera un código QR listo para ser renderizado directamente en composables de Jetpack Compose ([ImageBitmap]).
     *
     * @param content Texto o datos a codificar dentro del código QR.
     * @param width Ancho del código QR en píxeles (por defecto 512px).
     * @param height Alto del código QR en píxeles (por defecto 512px).
     * @return Objeto [ImageBitmap] listo para mostrar en un componente `Image`, o `null` si falla la generación.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): ImageBitmap? = createQrBitmap(content, width, height)?.asImageBitmap()

    /**
     * Implementación privada compartida para la codificación y generación de píxeles del código QR.
     *
     * @param content Cadena a codificar.
     * @param width Ancho deseado en píxeles.
     * @param height Alto deseado en píxeles.
     * @return Mapa de bits [Bitmap] renderizado en blanco y negro, o `null` si la entrada es inválida.
     */
    private fun createQrBitmap(
        content: String,
        width: Int,
        height: Int
    ): Bitmap? {
        // 1. Validar parámetros de entrada
        if (content.isBlank() || width <= 0 || height <= 0) return null

        return try {
            // 2. Codificar la cadena de texto en una matriz de bits (BitMatrix) usando ZXing
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height
            )

            // 3. Mapear la matriz de bits a un array plano de píxeles de color (Negro para bit activo, Blanco para inactivo)
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val rowOffset = y * width
                for (x in 0 until width) {
                    pixels[rowOffset + x] =
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }

            // 4. Crear y retornar el Bitmap de Android asignándole el array de píxeles
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }
}
