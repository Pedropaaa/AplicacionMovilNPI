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

    /** Lista completa de ubicaciones universitarias de la UGR. */
    val all: List<CityPlace> = listOf(
        // ==========================================
        // CAMPUS AYNADAMAR
        // ==========================================
        CityPlace("etsiit", "ETSIIT - Informática y Telecomunicación", "Aynadamar", GeoPoint(37.197076, -3.624243)),
        CityPlace("bellas_artes", "Facultad de Bellas Artes", "Aynadamar", GeoPoint(37.193240, -3.626210)),
        CityPlace("comedor_etsiit", "Comedor Universitario ETSIIT", "Aynadamar", GeoPoint(37.197076, -3.624243)),

        // ==========================================
        // CAMPUS FUENTENUEVA
        // ==========================================
        CityPlace("ciencias", "Facultad de Ciencias", "Fuentenueva", GeoPoint(37.182470, -3.608300)),
        CityPlace("caminos", "ETSI de Caminos, Canales y Puertos", "Fuentenueva", GeoPoint(37.183561, -3.608677)),
        CityPlace("aparejadores", "ETSI de Edificación (Aparejadores)", "Fuentenueva", GeoPoint(37.181313, -3.609462)),
        CityPlace("comedor_fuentenueva", "Comedor Universitario Fuentenueva", "Fuentenueva", GeoPoint(37.182054, -3.607062)),
        CityPlace("paseillos", "Paseo Profesor Juan Ossorio", "Fuentenueva", GeoPoint(37.182496, -3.607212)),

        // ==========================================
        // CAMPUS CARTUJA
        // ==========================================
        CityPlace("farmacia", "Facultad de Farmacia", "Cartuja", GeoPoint(37.188612, -3.597920)),
        CityPlace("economicas", "Facultad de Ciencias Económicas y Empresariales", "Cartuja", GeoPoint(37.188175, -3.599723)),
        CityPlace("filosofia", "Facultad de Filosofía y Letras", "Cartuja", GeoPoint(37.190240, -3.598218)),
        CityPlace("psicologia", "Facultad de Psicología", "Cartuja", GeoPoint(37.192534, -3.598285)),
        CityPlace("educacion", "Facultad de Ciencias de la Educación", "Cartuja", GeoPoint(37.191763, -3.599298)),
        CityPlace("odontologia", "Facultad de Odontología", "Cartuja", GeoPoint(37.187654, -3.596637)),
        CityPlace("comunicacion", "Facultad de Comunicación y Documentación", "Cartuja", GeoPoint(37.189658, -3.598910)),
        CityPlace("inef", "Facultad de Ciencias del Deporte (INEF)", "Cartuja", GeoPoint(37.2050953, -3.5979293)),
        CityPlace("comedor_cartuja", "Comedor Universitario Cartuja", "Cartuja", GeoPoint(37.191297, -3.599427)),

        // ==========================================
        // CAMPUS CENTRO
        // ==========================================
        CityPlace("derecho", "Facultad de Derecho", "Centro", GeoPoint(37.178877, -3.601552)),
        CityPlace("politicas", "Facultad de Ciencias Políticas y Sociología", "Centro", GeoPoint(37.178553, -3.601831)),
        CityPlace("trabajo", "Facultad de Ciencias del Trabajo", "Centro", GeoPoint(37.178864, -3.600985)),
        CityPlace("trabajo_social", "Facultad de Trabajo Social", "Centro", GeoPoint(37.177011, -3.605338)),
        CityPlace("traduccion", "Facultad de Traducción e Interpretación", "Centro", GeoPoint(37.175116, -3.602283)),
        CityPlace("arquitectura", "ETS de Arquitectura", "Centro", GeoPoint(37.172823, -3.593792)),

        // ==========================================
        // CAMPUS PTS
        // ==========================================
        CityPlace("medicina", "Facultad de Medicina", "PTS", GeoPoint(37.147775, -3.606778)),
        CityPlace("ciencias_salud", "Facultad de Ciencias de la Salud", "PTS", GeoPoint(37.147137, -3.607212)),
        CityPlace("comedor_pts", "Comedor Universitario PTS", "PTS", GeoPoint(37.147571, -3.607077)),

        // ==========================================
        // EDIFICIOS ESPECIALES Y RECTORADO
        // ==========================================
        CityPlace("v_centenario", "Espacio V Centenario", "Otros", GeoPoint(37.185366, -3.601676)),
        CityPlace("hospital_real", "Hospital Real (Rectorado UGR)", "Otros", GeoPoint(37.184323, -3.599723)),
        CityPlace("triunfo", "Complejo Administrativo Triunfo", "Otros", GeoPoint(37.184400, -3.601000)),
        CityPlace("clm", "Centro de Lenguas Modernas (CLM)", "Otros", GeoPoint(37.173874, -3.592735)),
        CityPlace("carmen_victoria", "Carmen de la Victoria", "Otros", GeoPoint(37.178848, -3.587932)),
        CityPlace("corrala_santiago", "Corrala de Santiago", "Otros", GeoPoint(37.173434, -3.594793)),

        // ==========================================
        // BIBLIOTECAS UNIVERSITARIAS UGR
        // ==========================================

        // ------------------------------------------
        // CAMPUS AYNADAMAR
        // ------------------------------------------
        CityPlace("biblioteca_etsiit", "Biblioteca de Informática y Telecomunicación", "Aynadamar", GeoPoint(37.1968511, -3.6250039)),
        CityPlace("biblioteca_bellas_artes", "Biblioteca de Bellas Artes", "Aynadamar", GeoPoint(37.1955078, -3.6266987)),

        // ------------------------------------------
        // CAMPUS FUENTENUEVA
        // ------------------------------------------
        CityPlace("biblioteca_ciencias", "Biblioteca de Ciencias", "Fuentenueva", GeoPoint(37.182470, -3.608300)),
        CityPlace("biblioteca_politecnica", "Biblioteca Politécnica", "Fuentenueva", GeoPoint(37.183561, -3.608677)),

        // ------------------------------------------
        // ÁREA DE NUEVA GRANADA
        // ------------------------------------------
        CityPlace("biblioteca_deportes", "Biblioteca de Ciencias del Deporte", "Nueva Granada", GeoPoint(37.2050953, -3.5979293)),

        // ------------------------------------------
        // CAMPUS CARTUJA
        // ------------------------------------------
        CityPlace("biblioteca_colegio_maximo", "Biblioteca del Colegio Máximo", "Cartuja", GeoPoint(37.1932947, -3.5965153)),
        CityPlace("biblioteca_educacion", "Biblioteca de Ciencias de la Educación", "Cartuja", GeoPoint(37.1927120, -3.5995049)),
        CityPlace("biblioteca_economicas", "Biblioteca de Económicas y Empresariales", "Cartuja", GeoPoint(37.1924201, -3.5947257)),
        CityPlace("biblioteca_farmacia", "Biblioteca de Farmacia", "Cartuja", GeoPoint(37.1945146, -3.5965296)),
        CityPlace("biblioteca_filosofia", "Biblioteca de Filosofía y Letras", "Cartuja", GeoPoint(37.1912879, -3.5952891)),
        CityPlace("biblioteca_psicologia", "Biblioteca de Psicología", "Cartuja", GeoPoint(37.1944643, -3.5943505)),

        // ------------------------------------------
        // CAMPUS CENTRO
        // ------------------------------------------
        CityPlace("biblioteca_derecho", "Biblioteca de Derecho", "Centro", GeoPoint(37.1787723, -3.6030586)),
        CityPlace("biblioteca_hospital_real", "Biblioteca del Hospital Real", "Centro", GeoPoint(37.1846347, -3.6008714)),
        CityPlace("biblioteca_institutos", "Biblioteca de Institutos y Recursos Especializados", "Centro", GeoPoint(37.1810261, -3.6044263)),
        CityPlace("biblioteca_politicas", "Biblioteca de Ciencias Políticas y Sociología", "Centro", GeoPoint(37.1807027, -3.6045967)),
        CityPlace("biblioteca_san_jeronimo", "Biblioteca del Edificio San Jerónimo", "Centro", GeoPoint(37.1804206, -3.6042372)),
        CityPlace("biblioteca_traduccion", "Biblioteca de Traducción e Interpretación", "Centro", GeoPoint(37.1754771, -3.603615)),

        // ------------------------------------------
        // ÁREA DEL REALEJO
        // ------------------------------------------
        CityPlace("biblioteca_arquitectura", "Biblioteca de Arquitectura", "Realejo", GeoPoint(37.1729047, -3.5912768)),

        // ------------------------------------------
        // CAMPUS PTS
        // ------------------------------------------
        CityPlace("biblioteca_biosanitaria", "Biblioteca de Medicina y Ciencias de la Salud", "PTS", GeoPoint(37.1485325, -3.6029231))
    )

    /** Referencia directa al destino por defecto. */
    val etsiit: CityPlace = all.first { it.id == "etsiit" }

    /**
     * Todas las bibliotecas de la UGR incluidas en el mapa.
     */
    val bibliotecas: List<CityPlace> by lazy {
        all.filter { it.id.startsWith("biblioteca_") }
    }

    /** Índice en memoria para consultas instantáneas O(1) por ID. */
    private val mapById: Map<String, CityPlace> by lazy {
        all.associateBy { it.id }
    }

    fun byId(id: String): CityPlace? = mapById[id]
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
