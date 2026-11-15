package com.example.aplicacionmovilnpi

import com.example.aplicacionmovilnpi.core.location.indoor.DeadReckoning
import com.example.aplicacionmovilnpi.core.location.outdoor.GeoPoint
import com.example.aplicacionmovilnpi.core.location.outdoor.GeoUtils
import com.example.aplicacionmovilnpi.core.location.indoor.IndoorTracker
import com.example.aplicacionmovilnpi.core.location.indoor.MapPoint
import com.example.aplicacionmovilnpi.core.location.indoor.StepDetector
import com.example.aplicacionmovilnpi.core.location.indoor.StepFusion
import com.example.aplicacionmovilnpi.core.navigation.model.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Suite de pruebas unitarias para los componentes del sistema de localización, sensores y navegación indoor/exterior.
 *
 * Evalúa las funciones de cálculo de distancia GPS (Haversine), detección de pasos por acelerómetro ([StepDetector]),
 * algoritmo de fusión de sensores ([StepFusion]), navegación por estima PDR ([DeadReckoning]) y seguimiento indoor ([IndoorTracker]).
 */
class LocationTest {

    // ======================================================
    // PRUEBAS DE GEOLOCALIZACIÓN Y DISTANCIA GPS
    // ======================================================

    /**
     * Valida que la distancia calculada entre la ETSIIT y la Facultad de Ciencias sea de aproximadamente 2,3 km.
     */
    @Test
    fun distanciaEtsiitACienciasEsDeUnosDosKm() {
        val ciencias = GeoPoint(37.18047, -3.60945)
        val d = GeoUtils.distanceMeters(GeoUtils.ETSIIT, ciencias)
        assertTrue("d = $d", d in 2000.0..2600.0)
    }

    /**
     * Comprueba que la coordenada de la entrada principal de la ETSIIT se identifique dentro del recinto y Ciencias fuera.
     */
    @Test
    fun puertaDeLaEtsiitCuentaComoDentro() {
        assertTrue(GeoUtils.isAtEtsiit(GeoPoint(37.19750, -3.62400)))
        assertFalse(GeoUtils.isAtEtsiit(GeoPoint(37.18047, -3.60945)))
    }

    /**
     * Verifica la correcta conversión del formato de distancias (metros a m/km).
     */
    @Test
    fun formatoDeDistancia() {
        assertEquals("850 m", GeoUtils.formatDistance(847.0))
        assertEquals("1,2 km", GeoUtils.formatDistance(1210.0))
    }

    // ======================================================
    // PRUEBAS DEL PODÓMETRO POR ACELERÓMETRO
    // ======================================================

    /**
     * Valida que el algoritmo [StepDetector] identifique 10 picos de aceleración como 10 pasos exactos.
     */
    @Test
    fun cuentaPicosDeAceleracionComoPasos() {
        val detector = StepDetector()
        var steps = 0
        var t = 0L
        repeat(10) {
            // 1. Simular un paso completo (~0,5 s): estado de reposo, pico de aceleración y vuelta al reposo
            repeat(10) { if (detector.onAcceleration(0f, 0f, 9.81f, t)) steps++; t += 20 }
            repeat(3) { if (detector.onAcceleration(0f, 0f, 13f, t)) steps++; t += 20 }
            repeat(12) { if (detector.onAcceleration(0f, 0f, 8f, t)) steps++; t += 20 }
        }
        assertEquals(10, steps)
    }

    /**
     * Comprueba que estando el teléfono en reposo sin variaciones de aceleración no se contabilicen pasos.
     */
    @Test
    fun quietoNoCuentaPasos() {
        val detector = StepDetector()
        var steps = 0
        for (i in 0 until 500) if (detector.onAcceleration(0.1f, 0f, 9.81f + (i % 3) * 0.1f, i * 20L)) steps++
        assertEquals(0, steps)
    }

    /**
     * Valida que picos más suaves (móvil en la mano al caminar) superen el umbral y se cuenten como pasos.
     */
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

    // ======================================================
    // PRUEBAS DE FUSIÓN DE SENSORES DE PASOS
    // ======================================================

    /**
     * Comprueba que a falta del sensor de pasos del sistema, el acelerómetro se encarga de contar los pasos.
     */
    @Test
    fun sinSensorDelSistemaCuentaElAcelerometro() {
        val fusion = StepFusion()
        val counted = (0 until 5).count { fusion.onAccelerometerStep(it * 500L) }
        assertEquals(5, counted)
    }

    /**
     * Valida que la fusión cuente los pasos iniciales con el acelerómetro y luego transfiera el conteo al sistema sin duplicar.
     */
    @Test
    fun losPrimerosPasosLosCuentaElAcelerometroYLuegoElSistema() {
        val fusion = StepFusion()
        var steps = 0
        // 1. Los 4 primeros pasos son capturados por el acelerómetro mientras el sistema arranca
        for (i in 0 until 4) if (fusion.onAccelerometerStep(i * 500L)) steps++
        // 2. El sensor del sistema empieza a responder; el acelerómetro deja de contar
        for (i in 4 until 10) {
            val t = i * 500L
            if (fusion.onSystemStep(t)) steps++
            if (fusion.onAccelerometerStep(t + 30)) steps++
        }
        assertEquals(10, steps)
    }

    /**
     * Comprueba que eventos atrasados entregados en bloque por el sensor del sistema no se vuelvan a contar si el acelerómetro ya los registró.
     */
    @Test
    fun pasosDelSistemaQueLleganTardeNoSeCuentanDosVeces() {
        val fusion = StepFusion()
        var steps = 0
        for (i in 0 until 6) if (fusion.onAccelerometerStep(i * 500L)) steps++
        for (i in 0 until 6) if (fusion.onSystemStep(i * 500L + 20)) steps++
        assertEquals(6, steps)
    }

    /**
     * Comprueba que si el sensor del sistema se interrumpe durante más del umbral de silencio, el acelerómetro vuelve a tomar el control.
     */
    @Test
    fun siElSistemaDejaDeMandarPasosVuelveElAcelerometro() {
        val fusion = StepFusion()
        assertTrue(fusion.onSystemStep(0L))
        assertFalse(fusion.onAccelerometerStep(1_000L))
        assertTrue(fusion.onAccelerometerStep(3_000L))
    }

    // ======================================================
    // PRUEBAS DE DEAD RECKONING Y SEGUIMIENTO INDOOR
    // ======================================================

    /**
     * Valida que dar un paso en rumbo Norte (0°) desplace las coordenadas hacia arriba en el eje Y del plano.
     */
    @Test
    fun unPasoHaciaElNorteDelPlanoSubeEnElPlano() {
        val p = DeadReckoning.step(MapPoint(500f, 500f), azimuthDeg = 0f, mapNorthDeg = 0f, stepLengthM = 1f)
        assertEquals(500f, p.x, 0.01f)
        assertEquals(500f - DeadReckoning.UNITS_PER_METER, p.y, 0.01f)
    }

    /**
     * Valida que la calibración del norte del plano ajuste la dirección del avance adecuadamente.
     */
    @Test
    fun elNorteDelPlanoGiraLaDireccion() {
        val p = DeadReckoning.step(MapPoint(500f, 500f), azimuthDeg = 90f, mapNorthDeg = 90f, stepLengthM = 1f)
        assertEquals(500f, p.x, 0.01f)
        assertTrue(p.y < 500f)
    }

    /**
     * Verifica la proyección perpendicular sobre el segmento de ruta y el cálculo de metros restantes.
     */
    @Test
    fun laPosicionSeAjustaALaRutaYCalculaLoQueFalta() {
        val path = listOf(MapPoint(0f, 0f), MapPoint(60f, 0f), MapPoint(60f, 60f))
        val progress = DeadReckoning.progressOnPath(MapPoint(30f, 8f), path)!!
        assertEquals(MapPoint(30f, 0f), progress.position)
        assertEquals(1, progress.nextIndex)
        assertEquals(5f, progress.metersToNext, 0.01f)
        assertEquals(15f, progress.metersToEnd, 0.01f)
        assertFalse(progress.hasArrived)
    }

    /**
     * Comprueba la generación de instrucciones habladas/textuales de giro según las diferencias angulares.
     */
    @Test
    fun indicacionesDeGiro() {
        assertEquals("Sigue recto", DeadReckoning.turnInstruction(10f, 350f))
        assertEquals("Gira a la derecha", DeadReckoning.turnInstruction(0f, 90f))
        assertEquals("Gira a la izquierda", DeadReckoning.turnInstruction(0f, 270f))
        assertEquals("Date la vuelta", DeadReckoning.turnInstruction(0f, 180f))
    }

    /**
     * Valida la calibración de la brújula asumiendo que el usuario está caminando sobre el sentido del tramo.
     */
    @Test
    fun calibrarLaBrujulaAlineaElRumboConLaRuta() {
        val north = DeadReckoning.calibrateMapNorth(currentAzimuthDeg = 30f, routeMapAngleDeg = 90f)
        assertEquals(300f, north, 0.01f)
        assertEquals(30f, DeadReckoning.mapAngleToAzimuth(90f, north), 0.01f)
    }

    /**
     * Comprueba el seguimiento dinámico de un trayecto indoor paso a paso mediante [IndoorTracker].
     */
    @Test
    fun seguirUnTramoAPasosHastaLlegar() {
        val tracker = IndoorTracker(initialMapNorthDeg = 0f)
        tracker.followPath(listOf(Node("a", 100f, 100f), Node("b", 172f, 100f)))
        assertEquals(12f, tracker.progress!!.metersToEnd, 0.01f)

        repeat(17) { i -> tracker.onStep(if (i % 2 == 0) 80f else 100f) }
        assertEquals(17, tracker.steps)
        assertEquals(100f, tracker.position!!.y, 0.01f)
        assertTrue(tracker.progress!!.hasArrived)
    }
}
