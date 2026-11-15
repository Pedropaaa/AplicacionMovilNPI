package com.example.aplicacionmovilnpi.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Representa una coordenada geográfica GPS compuesta por latitud y longitud en grados decimales.
 *
 * @property lat Latitud en grados decimales.
 * @property lon Longitud en grados decimales.
 */
data class GeoPoint(val lat: Double, val lon: Double)

/**
 * Utilidades para cálculos geográficos, distancias GPS y formato de medidas en el mapa exterior de la ciudad.
 */
object GeoUtils {

    /** Coordenadas GPS fijas de la puerta principal de la ETSIIT (C/ Periodista Daniel Saucedo Aranda). */
    val ETSIIT = GeoPoint(37.19718, -3.62423)

    /** Radio de tolerancia en metros (120 m) para considerar que el usuario se encuentra dentro del recinto de la ETSIIT. */
    const val ETSIIT_RADIUS_M = 120.0

    /** Radio medio de la Tierra en metros para el cálculo ortodrómico. */
    private const val EARTH_RADIUS_M = 6_371_000.0

    /**
     * Calcula la distancia en metros entre dos coordenadas geográficas mediante la fórmula del Haversine.
     *
     * @param a Punto inicial de coordenadas.
     * @param b Punto final de coordenadas.
     * @return Distancia ortodrómica en metros entre ambos puntos.
     */
    fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        // 1. Convertir la diferencia de latitudes y longitudes a radianes
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)

        // 2. Aplicar la fórmula del Haversine para el cálculo en la esfera terrestre
        val h = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLon / 2) * sin(dLon / 2)

        // 3. Retornar el resultado en metros
        return 2 * EARTH_RADIUS_M * atan2(sqrt(h), sqrt(1 - h))
    }

    /**
     * Evalúa si una coordenada determinada se encuentra dentro del recinto de la ETSIIT.
     *
     * @param point Coordenada a verificar.
     * @return `true` si la distancia a la ETSIIT es inferior o igual a [ETSIIT_RADIUS_M].
     */
    fun isAtEtsiit(point: GeoPoint): Boolean = distanceMeters(point, ETSIIT) <= ETSIIT_RADIUS_M

    /**
     * Formatea una distancia en metros a una cadena legible para el usuario (ej: "850 m" o "1,2 km").
     *
     * @param meters Distancia en metros a formatear.
     * @return Cadena formateada.
     */
    fun formatDistance(meters: Double): String =
        if (meters < 1000) "${(meters / 10).roundToInt() * 10} m"
        else "%.1f km".format(meters / 1000).replace(".", ",")
}
