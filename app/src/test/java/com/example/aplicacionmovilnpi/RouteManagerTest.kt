package com.example.aplicacionmovilnpi

import com.example.aplicacionmovilnpi.navigation.RouteManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteManagerTest {

    private val routeManager = RouteManager()

    @Test
    fun testRutaEntradaHaciaAula08() {
        val route = routeManager.calculateRoute("poi_entrada", "poi_aula08")
        assertTrue(route.isNotEmpty())
        assertEquals("n_entrada", route.first().id)
        assertEquals("n_aula08", route.last().id)
    }

    @Test
    fun testRutaPmrHaciaAscensorYBanos() {
        // En modo accesible debe llegar al ascensor y a los baños sin problemas
        val rutaAscensor = routeManager.calculateRoute("poi_entrada", "poi_ascensor_p0", onlyAccessible = true)
        assertTrue(rutaAscensor.isNotEmpty())
        assertEquals("n_ascensor_p0", rutaAscensor.last().id)

        val rutaBanos = routeManager.calculateRoute("poi_entrada", "poi_banos_p0", onlyAccessible = true)
        assertTrue(rutaBanos.isNotEmpty())
        assertEquals("n_banos_p0", rutaBanos.last().id)
    }

    @Test
    fun testRutaPmrBloqueadaHaciaEscaleras() {
        // El tramo de escaleras tiene EdgeType.STAIRS, por lo que debe quedar bloqueado en modo PMR
        val route = routeManager.calculateRoute("poi_entrada", "poi_escaleras_p0", onlyAccessible = true)
        assertTrue(route.isEmpty())
    }

    @Test
    fun testRutaPatioHaciaDespachosP4Directa() {
        val route = routeManager.calculateRoute("poi_patio", "poi_despachos_p4", onlyAccessible = true)
        assertTrue(route.isNotEmpty())
        assertEquals("n_patio", route.first().id)
        assertEquals("n_despachos_p4", route.last().id)
        
        // Debe entrar directamente por la puerta del patio del edificio principal, NO volver a la entrada exterior
        val nodeIds = route.map { it.id }
        assertTrue("La ruta debe entrar directamente por la puerta del patio", nodeIds.contains("n_entrada_edificio_principal_p0"))
        assertTrue("La ruta debe pasar por el hall central", nodeIds.contains("n_hall"))
        assertTrue("La ruta debe usar el ascensor principal de P0", nodeIds.contains("n_ascensor_principal_p0"))
    }

    @Test
    fun testEscalerasEnNormalYAscensorEnPmr() {
        // Modo Normal: debe usar escaleras
        val rutaNormal = routeManager.calculateRoute("poi_entrada", "poi_aula12", onlyAccessible = false)
        assertTrue(rutaNormal.isNotEmpty())
        val nodeIdsNormal = rutaNormal.map { it.id }
        assertTrue(
            "En modo normal se eligen las escaleras",
            nodeIdsNormal.any { it.contains("escaleras", ignoreCase = true) }
        )

        // Modo PMR / Accesible: las escaleras se filtran y debe usar el ascensor
        val rutaPmr = routeManager.calculateRoute("poi_entrada", "poi_aula12", onlyAccessible = true)
        assertTrue(rutaPmr.isNotEmpty())
        val nodeIdsPmr = rutaPmr.map { it.id }
        assertTrue(
            "En modo PMR se eligen los ascensores",
            nodeIdsPmr.any { it.contains("ascensor", ignoreCase = true) }
        )
    }

    @Test
    fun imprimirRutaManual() {
        val origen = "poi_entrada"
        val destino = "poi_aula12"

        println("\n==============================================")
        println("CALCULANDO RUTA DESDE $origen HASTA $destino")
        println("==============================================")

        val ruta = routeManager.calculateRoute(origen, destino, onlyAccessible = true)

        if (ruta.isEmpty()) {
            println("❌ No se encontró ruta.")
        } else {
            println("✅ Ruta encontrada (${ruta.size} nodos):")
            ruta.forEachIndexed { i, nodo ->
                println("   ${i + 1}. [${nodo.id}] -> x: ${nodo.x}, y: ${nodo.y}, planta: ${nodo.floor}")
            }
        }
        println("==============================================\n")
    }
}