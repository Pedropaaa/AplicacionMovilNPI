package com.example.aplicacionmovilnpi.navigation

import com.example.aplicacionmovilnpi.model.Edge
import com.example.aplicacionmovilnpi.model.EdgeType
import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest

object EtsiitData {

    // 1. Nodos físicos sobre el plano
    val nodes: Map<String, Node> = listOf(
        // Acceso exterior y Hall
        Node("n_entrada", x = 120f, y = 720f, floor = 0),
        Node("n_patio", x = 320f, y = 620f, floor = 0),
        Node("n_hall", x = 380f, y = 470f, floor = 0),

        // Corredores hacia el aulario
        Node("n_corredor_central", x = 520f, y = 490f, floor = 0),
        Node("n_corredor_lateral", x = 500f, y = 330f, floor = 0),

        // Accesos / Puertas al bloque del aulario
        Node("n_entrada_lateral_aulas", x = 560f, y = 280f, floor = 0),
        Node("n_entrada_central_aulas", x = 600f, y = 450f, floor = 0),

        // Pasillo lineal continuo de aulas: 0.1 a la izquierda -> 0.8 a la derecha
        Node("n_aula01", x = 540f, y = 220f, floor = 0), // Izquierda del todo
        Node("n_aula02", x = 580f, y = 250f, floor = 0),
        Node("n_aula03", x = 620f, y = 290f, floor = 0),
        Node("n_aula04", x = 660f, y = 330f, floor = 0),
        Node("n_aula05", x = 700f, y = 370f, floor = 0),
        Node("n_aula06", x = 740f, y = 420f, floor = 0),
        Node("n_aula07", x = 780f, y = 460f, floor = 0),
        Node("n_aula08", x = 840f, y = 520f, floor = 0), // Derecha del todo

        // Cilindro anexo (Planta 0)
        Node("n_escaleras_p0", x = 680f, y = 490f, floor = 0),
        Node("n_ascensor_p0", x = 700f, y = 530f, floor = 0),
        Node("n_banos_p0", x = 680f, y = 550f, floor = 0)
    ).associateBy { it.id }

    // 2. Conexiones transitables entre nodos
    val edges: List<Edge> = listOf(
        // Exterior y Hall
        Edge(from = "n_entrada", to = "n_hall", weight = 12f),
        Edge(from = "n_entrada", to = "n_patio", weight = 10f),
        Edge(from = "n_patio", to = "n_corredor_central", weight = 10f),

        // Conexión directa entre corredores
        Edge(from = "n_corredor_central", to = "n_corredor_lateral", weight = 12f),

        // Caminos a las entradas del aulario
        Edge(from = "n_corredor_central", to = "n_entrada_central_aulas", weight = 8f),
        Edge(from = "n_corredor_lateral", to = "n_entrada_lateral_aulas", weight = 8f),

        // ENTRADA LATERAL: Desemboca en la zona izquierda del pasillo (cerca de aula 0.2)
        Edge(from = "n_entrada_lateral_aulas", to = "n_aula02", weight = 4f),

        // ENTRADA CENTRAL: Desemboca en la zona derecha/cilindro (cerca de aula 0.6)
        Edge(from = "n_entrada_central_aulas", to = "n_aula06", weight = 4f),

        // Conexiones desde la Entrada Central hacia el cilindro
        Edge(from = "n_entrada_central_aulas", to = "n_escaleras_p0", weight = 6f, type = EdgeType.STAIRS),
        Edge(from = "n_entrada_central_aulas", to = "n_ascensor_p0", weight = 7f, type = EdgeType.WALKWAY),
        Edge(from = "n_entrada_central_aulas", to = "n_banos_p0", weight = 7f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p0", to = "n_banos_p0", weight = 2f, type = EdgeType.WALKWAY),

        // PASILLO CONTINUO DE IZQUIERDA A DERECHA (0.1 a 0.8)
        Edge(from = "n_aula01", to = "n_aula02", weight = 6f),
        Edge(from = "n_aula02", to = "n_aula03", weight = 6f),
        Edge(from = "n_aula03", to = "n_aula04", weight = 6f),
        Edge(from = "n_aula04", to = "n_aula05", weight = 6f),
        Edge(from = "n_aula05", to = "n_aula06", weight = 6f),
        Edge(from = "n_aula06", to = "n_aula07", weight = 6f),
        Edge(from = "n_aula07", to = "n_aula08", weight = 6f)
    )

    // 3. Puntos de Interés seleccionables
    val pois: List<PointOfInterest> = listOf(
        PointOfInterest("poi_entrada", "Entrada Principal", "Main Entrance", "ACCESO", "n_entrada", "Entrada exterior a la ETSIIT"),
        PointOfInterest("poi_patio", "Patio Exterior", "Courtyard", "ZONA_COMUN", "n_patio", "Patio con mesas y bancos"),
        PointOfInterest("poi_hall", "Hall Central", "Central Hall", "ZONA_COMUN", "n_hall", "Distribuidor cilíndrico en el bloque principal"),
        PointOfInterest("poi_corredor_central", "Corredor Central", "Central Corridor", "TRANSITO", "n_corredor_central", "Paso intermedio al aulario"),
        PointOfInterest("poi_corredor_lateral", "Corredor Lateral", "Side Corridor", "TRANSITO", "n_corredor_lateral", "Paso superior al aulario"),
        PointOfInterest("poi_entrada_central_aulas", "Entrada Central Aulas", "Central Classrooms Entrance", "TRANSITO", "n_entrada_central_aulas", "Acceso al aulario junto al cilindro"),
        PointOfInterest("poi_entrada_lateral_aulas", "Entrada Lateral Aulas", "Side Classrooms Entrance", "TRANSITO", "n_entrada_lateral_aulas", "Acceso al aulario en la zona izquierda"),

        // Cilindro Planta 0
        PointOfInterest("poi_escaleras_p0", "Escaleras Aulario (P0)", "Classrooms Stairs (Floor 0)", "SERVICIO", "n_escaleras_p0", "Escaleras hacia planta superior"),
        PointOfInterest("poi_ascensor_p0", "Ascensor Aulario (P0)", "Classrooms Elevator (Floor 0)", "ACCESIBILIDAD", "n_ascensor_p0", "Ascensor adaptado en Planta Baja"),
        PointOfInterest("poi_banos_p0", "Aseos Aulario (P0)", "Restrooms (Floor 0)", "SERVICIO", "n_banos_p0", "Baños en Planta Baja"),

        // Aulas ordenadas 0.1 a 0.8
        PointOfInterest("poi_aula01", "Aula 0.1", "Classroom 0.1", "AULA", "n_aula01"),
        PointOfInterest("poi_aula02", "Aula 0.2", "Classroom 0.2", "AULA", "n_aula02"),
        PointOfInterest("poi_aula03", "Aula 0.3", "Classroom 0.3", "AULA", "n_aula03"),
        PointOfInterest("poi_aula04", "Aula 0.4", "Classroom 0.4", "AULA", "n_aula04"),
        PointOfInterest("poi_aula05", "Aula 0.5", "Classroom 0.5", "AULA", "n_aula05"),
        PointOfInterest("poi_aula06", "Aula 0.6", "Classroom 0.6", "AULA", "n_aula06"),
        PointOfInterest("poi_aula07", "Aula 0.7", "Classroom 0.7", "AULA", "n_aula07"),
        PointOfInterest("poi_aula08", "Aula 0.8", "Classroom 0.8", "AULA", "n_aula08")
    )
}