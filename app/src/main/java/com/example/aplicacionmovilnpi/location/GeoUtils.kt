package com.example.aplicacionmovilnpi.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Coordenada GPS en grados decimales. */
data class GeoPoint(val lat: Double, val lon: Double)

object GeoUtils {

    /** Puerta principal de la ETSIIT (C/ Periodista Daniel Saucedo Aranda). */
    val ETSIIT = GeoPoint(37.19718, -3.62423)

    /** Radio a partir del cual consideramos que la persona ya está en el recinto de la ETSIIT. */
    const val ETSIIT_RADIUS_M = 120.0

    private const val EARTH_RADIUS_M = 6_371_000.0

    /** Distancia en metros entre dos puntos (fórmula del haversine). */
    fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val h = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_M * atan2(sqrt(h), sqrt(1 - h))
    }

    fun isAtEtsiit(point: GeoPoint): Boolean = distanceMeters(point, ETSIIT) <= ETSIIT_RADIUS_M

    /** "850 m" o "1,2 km", con el formato de las tarjetas de campus. */
    fun formatDistance(meters: Double): String =
        if (meters < 1000) "${(meters / 10).roundToInt() * 10} m"
        else "%.1f km".format(meters / 1000).replace(".", ",")
}
