package com.example.aplicacionmovilnpi

import com.example.aplicacionmovilnpi.location.DeadReckoning
import com.example.aplicacionmovilnpi.location.GeoPoint
import com.example.aplicacionmovilnpi.location.GeoUtils
import com.example.aplicacionmovilnpi.location.IndoorTracker
import com.example.aplicacionmovilnpi.location.MapPoint
import com.example.aplicacionmovilnpi.location.StepDetector
import com.example.aplicacionmovilnpi.location.StepFusion
import com.example.aplicacionmovilnpi.model.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationTest {

    // --- GPS ---

    @Test
    fun distanciaEtsiitACienciasEsDeUnosDosKm() {
        val ciencias = GeoPoint(37.18047, -3.60945)
        val d = GeoUtils.distanceMeters(GeoUtils.ETSIIT, ciencias)
        assertTrue("d = $d", d in 2000.0..2600.0)
    }

    @Test
    fun puertaDeLaEtsiitCuentaComoDentro() {
        assertTrue(GeoUtils.isAtEtsiit(GeoPoint(37.19750, -3.62400)))
        assertFalse(GeoUtils.isAtEtsiit(GeoPoint(37.18047, -3.60945)))
    }

    @Test
    fun formatoDeDistancia() {
        assertEquals("850 m", GeoUtils.formatDistance(847.0))
        assertEquals("1,2 km", GeoUtils.formatDistance(1210.0))
    }

    // --- Podómetro ---

    @Test
    fun cuentaPicosDeAceleracionComoPasos() {
        val detector = StepDetector()
        var steps = 0
        var t = 0L
        repeat(10) {
            // ~0,5 s por paso: reposo, pico y vuelta al reposo
            repeat(10) { if (detector.onAcceleration(0f, 0f, 9.81f, t)) steps++; t += 20 }
            repeat(3) { if (detector.onAcceleration(0f, 0f, 13f, t)) steps++; t += 20 }
            repeat(12) { if (detector.onAcceleration(0f, 0f, 8f, t)) steps++; t += 20 }
        }
        assertEquals(10, steps)
    }

    @Test
    fun quietoNoCuentaPasos() {
        val detector = StepDetector()
        var steps = 0
        for (i in 0 until 500) if (detector.onAcceleration(0.1f, 0f, 9.81f + (i % 3) * 0.1f, i * 20L)) steps++
        assertEquals(0, steps)
    }

    @Test
    fun picosSuavesConElMovilEnLaManoTambienCuentan() {
        val detector = StepDetector()
        var steps = 0
        var t = 0L
        repeat(10) {
            repeat(10) { if (detector.onAcceleration(0f, 0f, 9.81f, t)) steps++; t += 20 }
            repeat(3) { if (detector.onAcceleration(0f, 0f, 11.3f, t)) steps++; t += 20 }
            repeat(12) { if (detector.onAcceleration(0f, 0f, 9.0f, t)) steps++; t += 20 }
        }
        assertEquals(10, steps)
    }

    // --- Combinación sensor de pasos del sistema + acelerómetro ---

    @Test
    fun sinSensorDelSistemaCuentaElAcelerometro() {
        val fusion = StepFusion()
        val counted = (0 until 5).count { fusion.onAccelerometerStep(it * 500L) }
        assertEquals(5, counted)
    }

    @Test
    fun losPrimerosPasosLosCuentaElAcelerometroYLuegoElSistema() {
        val fusion = StepFusion()
        var steps = 0
        // El sistema tarda en arrancar: los 4 primeros pasos solo los ve el acelerómetro
        for (i in 0 until 4) if (fusion.onAccelerometerStep(i * 500L)) steps++
        // Llega el sistema; desde entonces el acelerómetro no cuenta para no duplicar
        for (i in 4 until 10) {
            val t = i * 500L
            if (fusion.onSystemStep(t)) steps++
            if (fusion.onAccelerometerStep(t + 30)) steps++
        }
        assertEquals(10, steps)
    }

    @Test
    fun pasosDelSistemaQueLleganTardeNoSeCuentanDosVeces() {
        val fusion = StepFusion()
        var steps = 0
        for (i in 0 until 6) if (fusion.onAccelerometerStep(i * 500L)) steps++
        // El sistema entrega de golpe los mismos 6 pasos, con sus instantes originales
        for (i in 0 until 6) if (fusion.onSystemStep(i * 500L + 20)) steps++
        assertEquals(6, steps)
    }

    @Test
    fun siElSistemaDejaDeMandarPasosVuelveElAcelerometro() {
        val fusion = StepFusion()
        assertTrue(fusion.onSystemStep(0L))
        assertFalse(fusion.onAccelerometerStep(1_000L))
        assertTrue(fusion.onAccelerometerStep(3_000L))
    }

    // --- Pasos + brújula ---

    @Test
    fun unPasoHaciaElNorteDelPlanoSubeEnElPlano() {
        val p = DeadReckoning.step(MapPoint(500f, 500f), azimuthDeg = 0f, mapNorthDeg = 0f, stepLengthM = 1f)
        assertEquals(500f, p.x, 0.01f)
        assertEquals(500f - DeadReckoning.UNITS_PER_METER, p.y, 0.01f)
    }

    @Test
    fun elNorteDelPlanoGiraLaDireccion() {
        // Si el plano está girado 90° (arriba = este), caminar al este es subir en el plano
        val p = DeadReckoning.step(MapPoint(500f, 500f), azimuthDeg = 90f, mapNorthDeg = 90f, stepLengthM = 1f)
        assertEquals(500f, p.x, 0.01f)
        assertTrue(p.y < 500f)
    }

    @Test
    fun laPosicionSeAjustaALaRutaYCalculaLoQueFalta() {
        val path = listOf(MapPoint(0f, 0f), MapPoint(60f, 0f), MapPoint(60f, 60f))
        val progress = DeadReckoning.progressOnPath(MapPoint(30f, 8f), path)!!
        assertEquals(MapPoint(30f, 0f), progress.position)
        assertEquals(1, progress.nextIndex)
        assertEquals(5f, progress.metersToNext, 0.01f)  // 30 unidades / 6
        assertEquals(15f, progress.metersToEnd, 0.01f)  // (30 + 60) / 6
        assertFalse(progress.hasArrived)
    }

    @Test
    fun indicacionesDeGiro() {
        assertEquals("Sigue recto", DeadReckoning.turnInstruction(10f, 350f))
        assertEquals("Gira a la derecha", DeadReckoning.turnInstruction(0f, 90f))
        assertEquals("Gira a la izquierda", DeadReckoning.turnInstruction(0f, 270f))
        assertEquals("Date la vuelta", DeadReckoning.turnInstruction(0f, 180f))
    }

    @Test
    fun calibrarLaBrujulaAlineaElRumboConLaRuta() {
        // La ruta va hacia la derecha del plano (90°) y la brújula marca 30°: arriba del plano = 300°
        val north = DeadReckoning.calibrateMapNorth(currentAzimuthDeg = 30f, routeMapAngleDeg = 90f)
        assertEquals(300f, north, 0.01f)
        assertEquals(30f, DeadReckoning.mapAngleToAzimuth(90f, north), 0.01f)
    }

    @Test
    fun seguirUnTramoAPasosHastaLlegar() {
        val tracker = IndoorTracker(initialMapNorthDeg = 0f)
        // Pasillo de 12 m hacia la derecha del plano
        tracker.followPath(listOf(Node("a", 100f, 100f), Node("b", 172f, 100f)))
        assertEquals(12f, tracker.progress!!.metersToEnd, 0.01f)

        // Caminando hacia el este (derecha del plano) con algo de desvío
        repeat(17) { i -> tracker.onStep(if (i % 2 == 0) 80f else 100f) }
        assertEquals(17, tracker.steps)
        assertEquals(100f, tracker.position!!.y, 0.01f) // pegado al pasillo
        assertTrue(tracker.progress!!.hasArrived)
    }
}
