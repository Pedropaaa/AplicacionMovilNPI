package com.example.aplicacionmovilnpi.model

/**
 * Tipo de conexión física entre dos nodos.
 * Permite distinguir pasillos de elementos verticales (escaleras, ascensores)
 * para generar las indicaciones de navegación ("coge el ascensor a planta 1").
 */
enum class EdgeType {
    WALKWAY,    // Pasillo, rampa o paso horizontal estándar
    STAIRS,     // Escaleras
    ELEVATOR    // Ascensor
}

/**
 * Representa un punto concreto en el mapa de la ETSIIT.
 *
 * @property id Identificador único del nodo.
 * @property x Coordenada horizontal sobre el plano de la planta (en píxeles o metros).
 * @property y Coordenada vertical sobre el plano de la planta.
 * @property floor Número de la planta
 */
data class Node(
    val id: String,
    val x: Float,
    val y: Float,
    val floor: Int = 0
)

/**
 * Conexión transitable entre dos nodos del edificio.
 *
 * @property from ID del nodo de origen.
 * @property to ID del nodo de destino.
 * @property weight Coste o distancia del tramo (en metros, pasos o segundos estimados).
 * @property type Tipo de vía (WALKWAY, STAIRS o ELEVATOR).
 * @property isAccessible Indica si una persona en silla de ruedas puede usar este tramo.
 *                         Si type == STAIRS siempre será false; si es ELEVATOR será true.
 */
data class Edge(
    val from: String,
    val to: String,
    val weight: Float,
    val type: EdgeType = EdgeType.WALKWAY,
    val isAccessible: Boolean = (type != EdgeType.STAIRS)
)

/**
 * Punto de interés (POI) visible y seleccionable por el usuario final.
 *
 * @property id Clave identificadora del POI (ej: "poi_aula_04").
 * @property nameEs Nombre visible en español (ej: "Aula 0.4").
 * @property nameEn Nombre visible en inglés (ej: "Classroom 0.4").
 * @property category Categoría para filtrar en la app (ej: "AULA", "DESPACHO", "SERVICIO", "CAFETERIA").
 * @property nodeId ID del nodo físico donde se ubica la puerta o acceso a este punto.
 * @property description Información adicional (horarios, equipamiento, capacidad).
 */
data class PointOfInterest(
    val id: String,
    val nameEs: String,
    val nameEn: String,
    val category: String,
    val nodeId: String,
    val description: String = ""
)