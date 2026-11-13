package com.example.aplicacionmovilnpi

import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.navigation.EtsiitData
import com.example.aplicacionmovilnpi.navigation.RouteManager
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.acos
import kotlin.math.hypot

/**
 * Recorre las rutas entre todos los pares de puntos de interés (modo normal y accesible) y
 * busca «idas y vueltas»: pasar por un nodo y volver casi en sentido contrario dentro de la
 * misma planta (p. ej. aula 1.1 → aula 1.2 → volver a la entrada de la escalera que estaba
 * entre las dos). Suelen indicar que falta una arista en el grafo.
 */
class RouteBacktrackTest {

    private val routeManager = RouteManager()

    /** Giro (en grados) a partir del cual se considera que la ruta da media vuelta. */
    private val maxTurnDeg = 135.0

    /** Nodos donde dar media vuelta es correcto (p. ej. un callejón sin salida). */
    private val allowedTurnNodes = setOf(
        // Vestíbulo de la planta baja del edificio principal: por decisión de diseño todas las
        // rutas del edificio pasan por él, aunque el destino (ascensor, escaleras…) quede detrás.
        "n_vestibulo_principal_p0",
    )

    /**
     * Giros fuertes revisados que se dan por buenos (en los dos sentidos). Pendientes de confirmar
     * en el edificio; si resultan ser un error, se quita de aquí y se corrige el grafo.
     */
    private val allowedTurns = setOf(
        // P-1: el ascensor principal se abre hacia el distribuidor, no hacia el comedor
        Triple("n_comedor", "n_distribuidor_p-1", "n_ascensor_principal_p-1"),
    )

    private fun isAllowed(a: String, b: String, c: String) =
        Triple(a, b, c) in allowedTurns || Triple(c, b, a) in allowedTurns

    data class Backtrack(val before: String, val at: String, val after: String, val turnDeg: Int)

    private fun turnDeg(a: Node, b: Node, c: Node): Double? {
        val (x1, y1) = (b.x - a.x).toDouble() to (b.y - a.y).toDouble()
        val (x2, y2) = (c.x - b.x).toDouble() to (c.y - b.y).toDouble()
        val l1 = hypot(x1, y1)
        val l2 = hypot(x2, y2)
        if (l1 < 1e-6 || l2 < 1e-6) return null
        val cos = ((x1 * x2 + y1 * y2) / (l1 * l2)).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cos))
    }

    private fun backtracksIn(route: List<Node>): List<Backtrack> =
        route.windowed(3).mapNotNull { (a, b, c) ->
            if (a.floor != b.floor || b.floor != c.floor) return@mapNotNull null
            if (b.id in allowedTurnNodes || isAllowed(a.id, b.id, c.id)) return@mapNotNull null
            val turn = turnDeg(a, b, c) ?: return@mapNotNull null
            if (turn >= maxTurnDeg) Backtrack(a.id, b.id, c.id, turn.toInt()) else null
        }

    @Test
    fun ningunaRutaVaYVuelve() {
        val pois = EtsiitData.pois
        // Tramo con media vuelta -> ejemplo de ruta donde aparece
        val found = linkedMapOf<Backtrack, String>()
        for (accessible in listOf(false, true)) {
            for (from in pois) for (to in pois) {
                if (from.id == to.id) continue
                // Una ruta accesible que empieza o acaba en unas escaleras no tiene sentido
                if (accessible && (from.id.startsWith("poi_escaleras") || to.id.startsWith("poi_escaleras"))) continue
                val route = routeManager.calculateRoute(from.id, to.id, onlyAccessible = accessible)
                for (b in backtracksIn(route)) {
                    found.putIfAbsent(b, "${from.id} → ${to.id}${if (accessible) " (accesible)" else ""}")
                }
            }
        }

        val report = found.entries.joinToString("\n") { (b, example) ->
            "  ${b.before} → ${b.at} → ${b.after} (giro ${b.turnDeg}°), p. ej. $example"
        }
        if (found.isNotEmpty()) println("Idas y vueltas encontradas (${found.size}):\n$report")
        assertTrue("Rutas que van y vuelven:\n$report", found.isEmpty())
    }
}
