package com.example.aplicacionmovilnpi

import com.example.aplicacionmovilnpi.qr.QrGenerator
import com.example.aplicacionmovilnpi.qr.QrLocationResolver
import com.example.aplicacionmovilnpi.qr.QrScanResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Suite de pruebas unitarias para la generación, formateo y resolución de códigos QR en la aplicación.
 *
 * Verifica la correcta serialización de rutas compartidas mediante [QrGenerator] y la decodificación
 * e interpretación tipada a través de [QrLocationResolver] para los diferentes formatos (rutas accesibles,
 * rutas estándar, nodos individuales, POIs y manejo de códigos inválidos).
 */
class QrRouteTest {

    /**
     * Valida la creación de la cadena formateada de una ruta accesible (PMR) y su posterior decodificación.
     *
     * Regla de negocio comprobada:
     * - `QrGenerator.createRouteQrContent` debe producir la estructura `"ROUTE:<origen>:<destino>:true"`.
     * - `QrLocationResolver.resolveQrCodeResult` debe retornar un [QrScanResult.Route] preservando el flag de accesibilidad.
     */
    @Test
    fun testCreateAndResolveRouteQr() {
        // 1. Definir los datos de entrada de la ruta de prueba
        val startId = "poi_entrada"
        val destId = "poi_aula12"
        val isAccessible = true

        // 2. Generar el contenido del QR de ruta compartida
        val qrContent = QrGenerator.createRouteQrContent(startId, destId, isAccessible)
        assertEquals("ROUTE:poi_entrada:poi_aula12:true", qrContent)

        // 3. Decodificar la cadena generada y verificar el resultado tipado
        val result = QrLocationResolver.resolveQrCodeResult(qrContent)
        assertNotNull(result)
        assertTrue(result is QrScanResult.Route)

        val routeResult = result as QrScanResult.Route
        assertEquals("poi_entrada", routeResult.startPoi.id)
        assertEquals("poi_aula12", routeResult.destPoi.id)
        assertEquals(true, routeResult.isAccessible)
    }

    /**
     * Valida la decodificación de una ruta estándar (no PMR, `isAccessible = false`).
     *
     * Regla de negocio comprobada:
     * - El decodificador identifica correctamente las rutas estándar con flag `false` y extrae los POIs asociados.
     */
    @Test
    fun testResolveRouteQrStandard() {
        // 1. Formatear la cadena de prueba para una ruta estándar
        val qrContent = "ROUTE:poi_entrada:poi_aula08:false"

        // 2. Resolver la ruta y comprobar la correspondencia de POIs
        val result = QrLocationResolver.resolveQrCodeResult(qrContent)

        assertNotNull(result)
        assertTrue(result is QrScanResult.Route)

        val routeResult = result as QrScanResult.Route
        assertEquals("poi_entrada", routeResult.startPoi.id)
        assertEquals("poi_aula08", routeResult.destPoi.id)
        assertEquals(false, routeResult.isAccessible)
    }

    /**
     * Valida la resolución de un QR correspondiente a un ID de nodo individual (ej: `"n_entrada"`).
     *
     * Regla de negocio comprobada:
     * - Debe interpretarse como un resultado de tipo [QrScanResult.Location] vinculando el nodo exacto del edificio.
     */
    @Test
    fun testResolveSingleLocationNodeQr() {
        val qrContent = "n_entrada"
        val result = QrLocationResolver.resolveQrCodeResult(qrContent)

        assertNotNull(result)
        assertTrue(result is QrScanResult.Location)

        val locResult = result as QrScanResult.Location
        assertEquals("n_entrada", locResult.node.id)
    }

    /**
     * Valida la resolución de un QR correspondiente al ID directo de un POI (ej: `"poi_entrada"`).
     *
     * Regla de negocio comprobada:
     * - Debe resolver tanto el POI como su nodo físico asociado en el mapa.
     */
    @Test
    fun testResolveSingleLocationPoiQr() {
        val qrContent = "poi_entrada"
        val result = QrLocationResolver.resolveQrCodeResult(qrContent)

        assertNotNull(result)
        assertTrue(result is QrScanResult.Location)

        val locResult = result as QrScanResult.Location
        assertEquals("n_entrada", locResult.node.id)
        assertEquals("poi_entrada", locResult.poi?.id)
    }

    /**
     * Valida que un código QR no reconocido o con sintaxis inválida devuelva `null` sin lanzar excepciones.
     */
    @Test
    fun testResolveInvalidQrReturnsNull() {
        val qrContent = "CODIGO_INVALIDO_INEXISTENTE"
        val result = QrLocationResolver.resolveQrCodeResult(qrContent)
        assertNull(result)
    }
}
