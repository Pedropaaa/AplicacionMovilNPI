package com.example.aplicacionmovilnpi

import com.example.aplicacionmovilnpi.location.GeoPoint
import com.example.aplicacionmovilnpi.location.WalkingRoute
import com.example.aplicacionmovilnpi.location.WalkingRouteService
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Suite de pruebas unitarias para el parseo, cálculo y formateo del servicio de rutas peatonales exteriores ([WalkingRouteService]).
 *
 * Revisa el procesamiento de respuestas JSON producidas por el servidor OSRM (Open Source Routing Machine),
 * la conversión a objetos del dominio [WalkingRoute], la traducción de maniobras peatonales a instrucciones en español
 * y la presentación legible de distancias y tiempos estimados de caminata.
 */
class WalkingRouteTest {

    /** Respuesta JSON de muestra recortada emitida por el servidor OSRM (perfil peatonal). */
    private val sample = """
        {"code":"Ok","routes":[{
          "distance":3568.4,"duration":2854.1,
          "geometry":{"type":"LineString","coordinates":[[-3.598617,37.177318],[-3.598513,37.177379],[-3.62423,37.19718]]},
          "legs":[{"steps":[
            {"name":"Calle Cárcel Baja","distance":11.5,"maneuver":{"type":"depart","location":[-3.598617,37.177318]}},
            {"name":"","distance":712.0,"maneuver":{"type":"turn","modifier":"left","location":[-3.598513,37.177379]}},
            {"name":"Avenida de la Constitución","distance":235.0,"maneuver":{"type":"end of road","modifier":"right","location":[-3.6,37.18]}},
            {"name":"","distance":0,"maneuver":{"type":"arrive","location":[-3.62423,37.19718]}}
          ]}]
        }]}
    """.trimIndent()

    /**
     * Valida el parseo correcto de una respuesta JSON de OSRM extrayendo puntos de la polilínea, distancia y resumen.
     */
    @Test
    fun leeLaRutaDeOsrm() {
        val route = WalkingRouteService.parse(sample)
        assertEquals(3, route.points.size)
        assertEquals(GeoPoint(37.177318, -3.598617), route.points.first())
        assertEquals(3568.4, route.distanceM, 0.01)
        assertEquals(
            listOf(
                "Empieza a caminar por Calle Cárcel Baja",
                "Gira a la izquierda",
                "Al final de la calle, gira a la derecha por Avenida de la Constitución",
                "Has llegado a tu destino"
            ),
            route.instructions.map { it.text }
        )
        assertEquals("3,6 km · 48 min a pie", route.summary)
    }

    /**
     * Valida que una respuesta sin ruta calculada ("code": "NoRoute") devuelva una excepción [IllegalStateException].
     */
    @Test(expected = IllegalStateException::class)
    fun sinRutaDaError() {
        WalkingRouteService.parse("""{"code":"NoRoute","routes":[]}""")
    }

    /**
     * Valida el formateo textual de maniobras peatonales especiales (rotondas, bifurcaciones y cambios de nombre).
     */
    @Test
    fun indicacionesEspeciales() {
        assertEquals("En la rotonda, toma la 2ª salida por Calle Real",
            WalkingRouteService.instructionText("roundabout", "right", "Calle Real", exit = 2))
        assertEquals("En la bifurcación, ve por la izquierda",
            WalkingRouteService.instructionText("fork", "slight left", ""))
        assertEquals("Sigue recto por Gran Vía",
            WalkingRouteService.instructionText("new name", "straight", "Gran Vía"))
    }

    /**
     * Valida la conversión de segundos a formato legible en minutos u horas (ej: "1 min", "1 h 15 min").
     */
    @Test
    fun duracionLegible() {
        assertEquals("1 min", WalkingRoute.formatDuration(20.0))
        assertEquals("1 h 15 min", WalkingRoute.formatDuration(4500.0))
    }
}
