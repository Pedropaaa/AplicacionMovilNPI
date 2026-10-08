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
    fun imprimirRutaManual() {
        val origen = "poi_entrada"
        val destino = "poi_aula01"

        println("\n==============================================")
        println("CALCULANDO RUTA DESDE $origen HASTA $destino")
        println("==============================================")

        val ruta = routeManager.calculateRoute(origen, destino, onlyAccessible = false)

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