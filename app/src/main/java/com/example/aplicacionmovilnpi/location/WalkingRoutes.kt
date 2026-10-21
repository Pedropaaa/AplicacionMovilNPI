package com.example.aplicacionmovilnpi.location

import com.example.aplicacionmovilnpi.data.SampleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

/** Campus de la UGR marcado en el mapa de Granada; se puede elegir como origen o destino. */
data class CityPlace(val id: String, val name: String, val campus: String, val point: GeoPoint) {
    /** "ETSIIT · Campus Aynadamar" */
    val label: String get() = "$name · $campus"
}

object CityPlaces {

    /** Zona de Granada a la que se limita el mapa (norte, este, sur, oeste). */
    const val GRANADA_NORTH = 37.235
    const val GRANADA_EAST = -3.53
    const val GRANADA_SOUTH = 37.12
    const val GRANADA_WEST = -3.68

    /** Coordenadas de cada campus de las tarjetas de SampleData.campuses (por su clave). */
    private val coordinates = mapOf(
        "etsiit" to GeoUtils.ETSIIT,
        "sciences" to GeoPoint(37.18047, -3.60945),
        "cartuja" to GeoPoint(37.19140, -3.59780)
    )

    /** Los mismos campus que las tarjetas de «Mapa Ciudad», con sus nombres. */
    val all: List<CityPlace> = SampleData.campuses.mapNotNull { card ->
        coordinates[card.key]?.let { point ->
            CityPlace(
                id = card.key,
                name = card.name,
                campus = card.campus.lowercase().split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } },
                point = point
            )
        }
    }

    val etsiit: CityPlace = all.first { it.id == "etsiit" }

    fun byId(id: String): CityPlace? = all.find { it.id == id }
}

/** Una indicación de la ruta a pie: "Gira a la izquierda por Calle Periodista…", con su distancia. */
data class RouteInstruction(val text: String, val distanceM: Double, val location: GeoPoint)

data class WalkingRoute(
    val points: List<GeoPoint>,
    val distanceM: Double,
    val durationS: Double,
    val instructions: List<RouteInstruction>
) {
    val summary: String
        get() = "${GeoUtils.formatDistance(distanceM)} · ${formatDuration(durationS)} a pie"

    companion object {
        fun formatDuration(seconds: Double): String {
            val minutes = (seconds / 60).roundToInt().coerceAtLeast(1)
            return if (minutes < 60) "$minutes min" else "${minutes / 60} h ${minutes % 60} min"
        }
    }
}

/**
 * Rutas a pie sobre OpenStreetMap con el servidor OSRM público de FOSSGIS
 * (routing.openstreetmap.de, perfil peatonal). No usa ningún servicio de Google.
 */
object WalkingRouteService {

    private const val BASE_URL = "https://routing.openstreetmap.de/routed-foot/route/v1/driving"

    suspend fun fetch(from: GeoPoint, to: GeoPoint): Result<WalkingRoute> = withContext(Dispatchers.IO) {
        runCatching {
            // OSRM recibe las coordenadas como lon,lat
            val url = URL("$BASE_URL/${from.lon},${from.lat};${to.lon},${to.lat}?overview=full&geometries=geojson&steps=true")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.setRequestProperty("User-Agent", "AplicacionMovilNPI (proyecto de clase ETSIIT-UGR)")
                check(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode}" }
                parse(connection.inputStream.bufferedReader().use { it.readText() })
            } finally {
                connection.disconnect()
            }
        }
    }

    /** Convierte la respuesta JSON de OSRM en una [WalkingRoute]. */
    fun parse(json: String): WalkingRoute {
        val root = JSONObject(json)
        check(root.optString("code") == "Ok") { "No hay ruta: ${root.optString("code")}" }
        val route = root.getJSONArray("routes").getJSONObject(0)

        val coordinates = route.getJSONObject("geometry").getJSONArray("coordinates")
        val points = (0 until coordinates.length()).map {
            val c = coordinates.getJSONArray(it)
            GeoPoint(lat = c.getDouble(1), lon = c.getDouble(0))
        }

        val instructions = mutableListOf<RouteInstruction>()
        val legs = route.getJSONArray("legs")
        for (l in 0 until legs.length()) {
            val steps = legs.getJSONObject(l).getJSONArray("steps")
            for (s in 0 until steps.length()) {
                val step = steps.getJSONObject(s)
                val maneuver = step.getJSONObject("maneuver")
                val location = maneuver.getJSONArray("location")
                instructions += RouteInstruction(
                    text = instructionText(
                        type = maneuver.optString("type"),
                        modifier = maneuver.optString("modifier").ifEmpty { null },
                        streetName = step.optString("name"),
                        exit = maneuver.optInt("exit", 0)
                    ),
                    distanceM = step.optDouble("distance", 0.0),
                    location = GeoPoint(lat = location.getDouble(1), lon = location.getDouble(0))
                )
            }
        }

        return WalkingRoute(
            points = points,
            distanceM = route.getDouble("distance"),
            durationS = route.getDouble("duration"),
            instructions = instructions
        )
    }

    /** Traduce una maniobra de OSRM a una indicación en español. */
    fun instructionText(type: String, modifier: String?, streetName: String, exit: Int = 0): String {
        val street = if (streetName.isNotBlank()) " por $streetName" else ""
        val direction = when (modifier) {
            "left" -> "gira a la izquierda"
            "right" -> "gira a la derecha"
            "slight left" -> "gira ligeramente a la izquierda"
            "slight right" -> "gira ligeramente a la derecha"
            "sharp left" -> "gira completamente a la izquierda"
            "sharp right" -> "gira completamente a la derecha"
            "uturn" -> "da la vuelta"
            else -> "sigue recto"
        }
        val text = when (type) {
            "depart" -> "Empieza a caminar$street"
            "arrive" -> "Has llegado a tu destino"
            "end of road" -> "Al final de la calle, $direction$street"
            "fork" -> when {
                modifier?.contains("left") == true -> "En la bifurcación, ve por la izquierda$street"
                modifier?.contains("right") == true -> "En la bifurcación, ve por la derecha$street"
                else -> "En la bifurcación, sigue recto$street"
            }
            "roundabout", "rotary" ->
                if (exit > 0) "En la rotonda, toma la ${exit}ª salida$street" else "Entra en la rotonda$street"
            else -> "${direction.replaceFirstChar { it.uppercase() }}$street"
        }
        return text
    }
}
