package com.example.aplicacionmovilnpi

import com.example.aplicacionmovilnpi.qr.QrGenerator
import com.example.aplicacionmovilnpi.qr.QrLocationResolver
import com.example.aplicacionmovilnpi.qr.QrScanResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QrRouteTest {

    @Test
    fun testCreateAndResolveRouteQr() {
        val startId = "poi_entrada"
        val destId = "poi_aula12"
        val isAccessible = true

        val qrContent = QrGenerator.createRouteQrContent(startId, destId, isAccessible)
        assertEquals("ROUTE:poi_entrada:poi_aula12:true", qrContent)

        val result = QrLocationResolver.resolveQrCodeResult(qrContent)
        assertNotNull(result)
        assertTrue(result is QrScanResult.Route)

        val routeResult = result as QrScanResult.Route
        assertEquals("poi_entrada", routeResult.startPoi.id)
        assertEquals("poi_aula12", routeResult.destPoi.id)
        assertEquals(true, routeResult.isAccessible)
    }

    @Test
    fun testResolveRouteQrStandard() {
        val qrContent = "ROUTE:poi_entrada:poi_aula08:false"
        val result = QrLocationResolver.resolveQrCodeResult(qrContent)

        assertNotNull(result)
        assertTrue(result is QrScanResult.Route)

        val routeResult = result as QrScanResult.Route
        assertEquals("poi_entrada", routeResult.startPoi.id)
        assertEquals("poi_aula08", routeResult.destPoi.id)
        assertEquals(false, routeResult.isAccessible)
    }

    @Test
    fun testResolveSingleLocationNodeQr() {
        val qrContent = "n_entrada"
        val result = QrLocationResolver.resolveQrCodeResult(qrContent)

        assertNotNull(result)
        assertTrue(result is QrScanResult.Location)

        val locResult = result as QrScanResult.Location
        assertEquals("n_entrada", locResult.node.id)
    }

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

    @Test
    fun testResolveInvalidQrReturnsNull() {
        val qrContent = "CODIGO_INVALIDO_INEXISTENTE"
        val result = QrLocationResolver.resolveQrCodeResult(qrContent)
        assertNull(result)
    }
}
