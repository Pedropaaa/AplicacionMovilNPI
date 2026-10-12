package com.example.aplicacionmovilnpi.navigation

import com.example.aplicacionmovilnpi.model.Edge
import com.example.aplicacionmovilnpi.model.EdgeType
import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest

object EtsiitData {

    // 1. Nodos físicos sobre el plano
    val nodes: Map<String, Node> = listOf(
        // --- PLANTA 0 ---
        Node("n_entrada", x = 120f, y = 720f, floor = 0),
        Node("n_patio", x = 320f, y = 620f, floor = 0),
        Node("n_hall", x = 380f, y = 470f, floor = 0),

        Node("n_corredor_central", x = 520f, y = 490f, floor = 0),
        Node("n_corredor_lateral", x = 500f, y = 330f, floor = 0),
        Node("n_escaleras_lateral_p0", x = 480f, y = 310f, floor = 0),

        Node("n_entrada_lateral_aulas", x = 560f, y = 280f, floor = 0),
        Node("n_entrada_central_aulas", x = 600f, y = 450f, floor = 0),

        // Pasillo de aulas P0 (0.1 a 0.8)
        Node("n_aula01", x = 540f, y = 220f, floor = 0),
        Node("n_aula02", x = 580f, y = 250f, floor = 0),
        Node("n_aula03", x = 620f, y = 290f, floor = 0),
        Node("n_aula04", x = 660f, y = 330f, floor = 0),
        Node("n_aula05", x = 700f, y = 370f, floor = 0),
        Node("n_aula06", x = 740f, y = 420f, floor = 0),
        Node("n_aula07", x = 780f, y = 460f, floor = 0),
        Node("n_aula08", x = 840f, y = 520f, floor = 0),

        // Cilindro P0
        Node("n_escaleras_p0", x = 680f, y = 490f, floor = 0),
        Node("n_ascensor_p0", x = 700f, y = 530f, floor = 0),
        Node("n_banos_p0", x = 680f, y = 550f, floor = 0),

        // --- PLANTA 1 ---
        Node("n_entrada_lateral_aulas_p1", x = 560f, y = 280f, floor = 1),
        Node("n_escaleras_lateral_p1", x = 480f, y = 310f, floor = 1),
        Node("n_aula11", x = 580f, y = 250f, floor = 1),
        Node("n_aula12", x = 620f, y = 290f, floor = 1),
        Node("n_aula13", x = 660f, y = 330f, floor = 1),
        Node("n_aula14", x = 700f, y = 370f, floor = 1),
        Node("n_aula15", x = 740f, y = 420f, floor = 1),
        Node("n_aula16", x = 780f, y = 460f, floor = 1),

        Node("n_escaleras_p1", x = 680f, y = 490f, floor = 1),
        Node("n_ascensor_p1", x = 700f, y = 530f, floor = 1),
        Node("n_banos_p1", x = 680f, y = 550f, floor = 1),

        // --- PLANTA 2 ---
        Node("n_entrada_lateral_aulas_p2", x = 560f, y = 280f, floor = 2),
        Node("n_escaleras_lateral_p2", x = 480f, y = 310f, floor = 2),
        Node("n_pasillo_p2", x = 740f, y = 420f, floor = 2),

        Node("n_escaleras_p2", x = 680f, y = 490f, floor = 2),
        Node("n_ascensor_p2", x = 700f, y = 530f, floor = 2),
        Node("n_banos_p2", x = 680f, y = 550f, floor = 2),

        // --- PLANTA 3 ---
        Node("n_entrada_lateral_aulas_p3", x = 560f, y = 280f, floor = 3),
        Node("n_escaleras_lateral_p3", x = 480f, y = 310f, floor = 3),
        Node("n_pasillo_p3", x = 740f, y = 420f, floor = 3),

        Node("n_escaleras_p3", x = 680f, y = 490f, floor = 3),
        Node("n_ascensor_p3", x = 700f, y = 530f, floor = 3),
        Node("n_banos_p3", x = 680f, y = 550f, floor = 3),

        // --- PLANTA 4 ---
        Node("n_delegacion_estudiantes", x = 700f, y = 510f, floor = 4),
        Node("n_escaleras_p4", x = 680f, y = 490f, floor = 4)
    ).associateBy { it.id }

    // 2. Conexiones transitables entre nodos
    val edges: List<Edge> = listOf(
        // --- CONEXIONES PLANTA 0 ---
        Edge(from = "n_entrada", to = "n_hall", weight = 12f),
        Edge(from = "n_entrada", to = "n_patio", weight = 10f),
        Edge(from = "n_patio", to = "n_corredor_central", weight = 10f),
        Edge(from = "n_corredor_central", to = "n_corredor_lateral", weight = 12f),
        Edge(from = "n_corredor_lateral", to = "n_escaleras_lateral_p0", weight = 4f, type = EdgeType.STAIRS),
        Edge(from = "n_corredor_central", to = "n_entrada_central_aulas", weight = 8f),
        Edge(from = "n_corredor_lateral", to = "n_entrada_lateral_aulas", weight = 8f),
        Edge(from = "n_entrada_lateral_aulas", to = "n_aula02", weight = 4f),
        Edge(from = "n_entrada_central_aulas", to = "n_aula06", weight = 4f),

        Edge(from = "n_entrada_central_aulas", to = "n_escaleras_p0", weight = 6f, type = EdgeType.STAIRS),
        Edge(from = "n_entrada_central_aulas", to = "n_ascensor_p0", weight = 7f, type = EdgeType.WALKWAY),
        Edge(from = "n_entrada_central_aulas", to = "n_banos_p0", weight = 7f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p0", to = "n_banos_p0", weight = 2f, type = EdgeType.WALKWAY),

        Edge(from = "n_aula01", to = "n_aula02", weight = 6f),
        Edge(from = "n_aula02", to = "n_aula03", weight = 6f),
        Edge(from = "n_aula03", to = "n_aula04", weight = 6f),
        Edge(from = "n_aula04", to = "n_aula05", weight = 6f),
        Edge(from = "n_aula05", to = "n_aula06", weight = 6f),
        Edge(from = "n_aula06", to = "n_aula07", weight = 6f),
        Edge(from = "n_aula07", to = "n_aula08", weight = 6f),

        // --- CONEXIONES VERTICALES (ASCENSOR P0 a P3) ---
        Edge(from = "n_ascensor_p0", to = "n_ascensor_p1", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p1", to = "n_ascensor_p2", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p2", to = "n_ascensor_p3", weight = 15f, type = EdgeType.WALKWAY),

        // --- CONEXIONES VERTICALES (ESCALERAS CENTRALES P0 a P4) ---
        Edge(from = "n_escaleras_p0", to = "n_escaleras_p1", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_p1", to = "n_escaleras_p2", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_p2", to = "n_escaleras_p3", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_p3", to = "n_escaleras_p4", weight = 15f, type = EdgeType.STAIRS),

        // --- CONEXIONES VERTICALES (ESCALERAS LATERALES P0 a P3) ---
        Edge(from = "n_escaleras_lateral_p0", to = "n_entrada_lateral_aulas_p1", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_lateral_p1", to = "n_entrada_lateral_aulas_p2", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_lateral_p2", to = "n_entrada_lateral_aulas_p3", weight = 15f, type = EdgeType.STAIRS),

        // --- PLANTA 1 ---
        Edge(from = "n_entrada_lateral_aulas_p1", to = "n_aula11", weight = 4f),
        Edge(from = "n_entrada_lateral_aulas_p1", to = "n_escaleras_lateral_p1", weight = 2f, type = EdgeType.STAIRS),
        Edge(from = "n_ascensor_p1", to = "n_aula15", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_escaleras_p1", to = "n_aula15", weight = 5f, type = EdgeType.STAIRS),
        Edge(from = "n_banos_p1", to = "n_aula15", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p1", to = "n_banos_p1", weight = 2f, type = EdgeType.WALKWAY),

        Edge(from = "n_aula11", to = "n_aula12", weight = 6f),
        Edge(from = "n_aula12", to = "n_aula13", weight = 6f),
        Edge(from = "n_aula13", to = "n_aula14", weight = 6f),
        Edge(from = "n_aula14", to = "n_aula15", weight = 6f),
        Edge(from = "n_aula15", to = "n_aula16", weight = 6f),

        // --- PLANTA 2 ---
        Edge(from = "n_entrada_lateral_aulas_p2", to = "n_pasillo_p2", weight = 10f),
        Edge(from = "n_entrada_lateral_aulas_p2", to = "n_escaleras_lateral_p2", weight = 2f, type = EdgeType.STAIRS),
        Edge(from = "n_ascensor_p2", to = "n_pasillo_p2", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_escaleras_p2", to = "n_pasillo_p2", weight = 5f, type = EdgeType.STAIRS),
        Edge(from = "n_banos_p2", to = "n_pasillo_p2", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p2", to = "n_banos_p2", weight = 2f, type = EdgeType.WALKWAY),

        // --- PLANTA 3 ---
        Edge(from = "n_entrada_lateral_aulas_p3", to = "n_pasillo_p3", weight = 10f),
        Edge(from = "n_entrada_lateral_aulas_p3", to = "n_escaleras_lateral_p3", weight = 2f, type = EdgeType.STAIRS),
        Edge(from = "n_ascensor_p3", to = "n_pasillo_p3", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_escaleras_p3", to = "n_pasillo_p3", weight = 5f, type = EdgeType.STAIRS),
        Edge(from = "n_banos_p3", to = "n_pasillo_p3", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p3", to = "n_banos_p3", weight = 2f, type = EdgeType.WALKWAY),

        // --- PLANTA 4 ---
        Edge(from = "n_escaleras_p4", to = "n_delegacion_estudiantes", weight = 3f, type = EdgeType.STAIRS)
    )

    // 3. Puntos de Interés seleccionables
    val pois: List<PointOfInterest> = listOf(
        // --- POIs PLANTA 0 ---
        PointOfInterest("poi_entrada", "Entrada Principal", "Main Entrance", "ACCESO", "n_entrada", "Entrada exterior a la ETSIIT"),
        PointOfInterest("poi_patio", "Patio Exterior", "Courtyard", "ZONA_COMUN", "n_patio", "Patio con mesas y bancos"),
        PointOfInterest("poi_hall", "Hall Central", "Central Hall", "ZONA_COMUN", "n_hall", "Distribuidor cilíndrico en el bloque principal"),
        PointOfInterest("poi_corredor_central", "Corredor Central", "Central Corridor", "TRANSITO", "n_corredor_central", "Paso intermedio al aulario"),
        PointOfInterest("poi_corredor_lateral", "Corredor Lateral", "Side Corridor", "TRANSITO", "n_corredor_lateral", "Paso superior al aulario"),
        PointOfInterest("poi_escaleras_lateral_p0", "Escaleras Corredor Lateral (P0)", "Side Stairs (Floor 0)", "SERVICIO", "n_escaleras_lateral_p0", "Escaleras laterales hacia plantas superiores"),
        PointOfInterest("poi_entrada_central_aulas", "Entrada Central Aulas (P0)", "Central Classrooms Entrance (P0)", "TRANSITO", "n_entrada_central_aulas", "Acceso al aulario junto al cilindro"),
        PointOfInterest("poi_entrada_lateral_aulas", "Entrada Lateral Aulas (P0)", "Side Classrooms Entrance (P0)", "TRANSITO", "n_entrada_lateral_aulas", "Acceso al aulario en la zona izquierda"),

        PointOfInterest("poi_escaleras_p0", "Escaleras Aulario (P0)", "Classrooms Stairs (Floor 0)", "SERVICIO", "n_escaleras_p0", "Escaleras centrales"),
        PointOfInterest("poi_ascensor_p0", "Ascensor Aulario (P0)", "Classrooms Elevator (Floor 0)", "ACCESIBILIDAD", "n_ascensor_p0", "Ascensor adaptado en Planta Baja"),
        PointOfInterest("poi_banos_p0", "Aseos Aulario (P0)", "Restrooms (Floor 0)", "SERVICIO", "n_banos_p0", "Baños en Planta Baja"),

        PointOfInterest("poi_aula01", "Aula 0.1", "Classroom 0.1", "AULA", "n_aula01"),
        PointOfInterest("poi_aula02", "Aula 0.2", "Classroom 0.2", "AULA", "n_aula02"),
        PointOfInterest("poi_aula03", "Aula 0.3", "Classroom 0.3", "AULA", "n_aula03"),
        PointOfInterest("poi_aula04", "Aula 0.4", "Classroom 0.4", "AULA", "n_aula04"),
        PointOfInterest("poi_aula05", "Aula 0.5", "Classroom 0.5", "AULA", "n_aula05"),
        PointOfInterest("poi_aula06", "Aula 0.6", "Classroom 0.6", "AULA", "n_aula06"),
        PointOfInterest("poi_aula07", "Aula 0.7", "Classroom 0.7", "AULA", "n_aula07"),
        PointOfInterest("poi_aula08", "Aula 0.8", "Classroom 0.8", "AULA", "n_aula08"),

        // --- POIs PLANTA 1 ---
        PointOfInterest("poi_entrada_lateral_aulas_p1", "Entrada Lateral Aulas (P1)", "Side Classrooms Entrance (P1)", "TRANSITO", "n_entrada_lateral_aulas_p1", "Acceso lateral Planta 1"),
        PointOfInterest("poi_escaleras_lateral_p1", "Escaleras Corredor Lateral (P1)", "Side Stairs (Floor 1)", "SERVICIO", "n_escaleras_lateral_p1", "Escaleras laterales hacia plantas superiores"),
        PointOfInterest("poi_escaleras_p1", "Escaleras Aulario (P1)", "Classrooms Stairs (Floor 1)", "SERVICIO", "n_escaleras_p1", "Escaleras centrales Planta 1"),
        PointOfInterest("poi_ascensor_p1", "Ascensor Aulario (P1)", "Classrooms Elevator (Floor 1)", "ACCESIBILIDAD", "n_ascensor_p1", "Ascensor adaptado en Planta 1"),
        PointOfInterest("poi_banos_p1", "Aseos Aulario (P1)", "Restrooms (Floor 1)", "SERVICIO", "n_banos_p1", "Baños en Planta 1"),

        PointOfInterest("poi_aula11", "Aula 1.1", "Classroom 1.1", "AULA", "n_aula11"),
        PointOfInterest("poi_aula12", "Aula 1.2", "Classroom 1.2", "AULA", "n_aula12"),
        PointOfInterest("poi_aula13", "Aula 1.3", "Classroom 1.3", "AULA", "n_aula13"),
        PointOfInterest("poi_aula14", "Aula 1.4", "Classroom 1.4", "AULA", "n_aula14"),
        PointOfInterest("poi_aula15", "Aula 1.5", "Classroom 1.5", "AULA", "n_aula15"),
        PointOfInterest("poi_aula16", "Aula 1.6", "Classroom 1.6", "AULA", "n_aula16"),

        // --- POIs PLANTA 2 ---
        PointOfInterest("poi_entrada_lateral_aulas_p2", "Entrada Lateral Aulas (P2)", "Side Classrooms Entrance (P2)", "TRANSITO", "n_entrada_lateral_aulas_p2", "Acceso lateral Planta 2"),
        PointOfInterest("poi_escaleras_lateral_p2", "Escaleras Corredor Lateral (P2)", "Side Stairs (Floor 2)", "SERVICIO", "n_escaleras_lateral_p2", "Escaleras laterales hacia plantas superiores"),
        PointOfInterest("poi_escaleras_p2", "Escaleras Aulario (P2)", "Classrooms Stairs (Floor 2)", "SERVICIO", "n_escaleras_p2", "Escaleras centrales Planta 2"),
        PointOfInterest("poi_ascensor_p2", "Ascensor Aulario (P2)", "Classrooms Elevator (Floor 2)", "ACCESIBILIDAD", "n_ascensor_p2", "Ascensor adaptado en Planta 2"),
        PointOfInterest("poi_banos_p2", "Aseos Aulario (P2)", "Restrooms (Floor 2)", "SERVICIO", "n_banos_p2", "Baños en Planta 2"),

        // --- POIs PLANTA 3 ---
        PointOfInterest("poi_entrada_lateral_aulas_p3", "Entrada Lateral Aulas (P3)", "Side Classrooms Entrance (P3)", "TRANSITO", "n_entrada_lateral_aulas_p3", "Acceso lateral Planta 3"),
        PointOfInterest("poi_escaleras_p3", "Escaleras Aulario (P3)", "Classrooms Stairs (Floor 3)", "SERVICIO", "n_escaleras_p3", "Escaleras centrales Planta 3"),
        PointOfInterest("poi_ascensor_p3", "Ascensor Aulario (P3)", "Classrooms Elevator (Floor 3)", "ACCESIBILIDAD", "n_ascensor_p3", "Ascensor adaptado en Planta 3"),
        PointOfInterest("poi_banos_p3", "Aseos Aulario (P3)", "Restrooms (Floor 3)", "SERVICIO", "n_banos_p3", "Baños en Planta 3"),

        // --- POIs PLANTA 4 ---
        PointOfInterest("poi_delegacion_estudiantes", "Delegación de Estudiantes", "Student Council Office", "DESPACHO", "n_delegacion_estudiantes", "Oficina de Delegación de Estudiantes en Planta 4"),
        PointOfInterest("poi_escaleras_p4", "Escaleras Aulario (P4)", "Classrooms Stairs (Floor 4)", "SERVICIO", "n_escaleras_p4", "Escaleras centrales Planta 4")
    )
}