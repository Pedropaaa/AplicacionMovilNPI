package com.example.aplicacionmovilnpi.navigation

import com.example.aplicacionmovilnpi.model.Edge
import com.example.aplicacionmovilnpi.model.EdgeType
import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest

object EtsiitData {

    // 1. Nodos físicos sobre el plano
    val nodes: Map<String, Node> = listOf(
        // --- PLANTA -1 (SÓTANO Y PATIO INFERIOR EXTERIOR) ---
        Node("n_escaleras_entrada_p-1", x = 130f, y = 710f, floor = 0),
        Node("n_pasillo_abierto_p-1", x = 140f, y = 700f, floor = -1),
        Node("n_entrada_edificio_principal_p-1", x = 200f, y = 680f, floor = -1),
        Node("n_escaleras_internas_p-1_a_p0", x = 220f, y = 660f, floor = -1),
        Node("n_ascensor_principal_p-1", x = 230f, y = 660f, floor = -1),

        // Distribuidor interno P-1
        Node("n_distribuidor_p-1", x = 250f, y = 650f, floor = -1),
        Node("n_cafeteria", x = 220f, y = 620f, floor = -1),
        Node("n_salon_actos", x = 250f, y = 580f, floor = -1),
        Node("n_comedor", x = 290f, y = 620f, floor = -1),

        // Patio abierto inferior
        Node("n_patio_inferior_p-1", x = 360f, y = 600f, floor = -1),

        // --- PLANTA 0 ---
        Node("n_entrada", x = 120f, y = 720f, floor = 0), // Recinto exterior abierto
        Node("n_patio", x = 320f, y = 620f, floor = 0),
        Node("n_hall", x = 380f, y = 470f, floor = 0),

        // Edificio Principal P0
        Node("n_distribuidor_entrada_principal_p0", x = 200f, y = 680f, floor = 0), // Entrada General
        Node("n_entrada_edificio_principal_p0", x = 280f, y = 650f, floor = 0), // Entrada desde el Patio
        Node("n_ascensor_principal_p0", x = 230f, y = 660f, floor = 0),
        Node("n_escaleras_principal_p0", x = 240f, y = 660f, floor = 0),
        Node("n_banos_edificio_principal_p0", x = 210f, y = 660f, floor = 0),

        Node("n_corredor_central", x = 520f, y = 490f, floor = 0),
        Node("n_corredor_lateral", x = 500f, y = 330f, floor = 0),

        Node("n_escaleras_lateral_p0", x = 480f, y = 310f, floor = 0),
        Node("n_escaleras_patio_central_p0", x = 540f, y = 470f, floor = 0),

        Node("n_entrada_lateral_aulas", x = 560f, y = 280f, floor = 0),
        Node("n_entrada_central_aulas", x = 600f, y = 450f, floor = 0),

        // Pasillo de aulas P0
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
        Node("n_ascensor_principal_p1", x = 230f, y = 660f, floor = 1),
        Node("n_escaleras_principal_p1", x = 240f, y = 660f, floor = 1),
        Node("n_biblioteca", x = 260f, y = 660f, floor = 1),

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
        Node("n_ascensor_principal_p2", x = 230f, y = 660f, floor = 2),
        Node("n_escaleras_principal_p2", x = 240f, y = 660f, floor = 2),
        Node("n_despachos_p2", x = 260f, y = 660f, floor = 2),

        Node("n_entrada_lateral_aulas_p2", x = 560f, y = 280f, floor = 2),
        Node("n_escaleras_lateral_p2", x = 480f, y = 310f, floor = 2),
        Node("n_aula21", x = 560f, y = 230f, floor = 2),
        Node("n_aula22", x = 590f, y = 260f, floor = 2),
        Node("n_aula23", x = 620f, y = 290f, floor = 2),
        Node("n_aula24", x = 650f, y = 320f, floor = 2),
        Node("n_aula25", x = 680f, y = 350f, floor = 2),
        Node("n_aula26", x = 710f, y = 380f, floor = 2),
        Node("n_aula27", x = 740f, y = 410f, floor = 2),
        Node("n_aula28", x = 770f, y = 440f, floor = 2),
        Node("n_aula29", x = 800f, y = 470f, floor = 2),
        Node("n_aula210", x = 830f, y = 500f, floor = 2),

        Node("n_escaleras_p2", x = 680f, y = 490f, floor = 2),
        Node("n_ascensor_p2", x = 700f, y = 530f, floor = 2),
        Node("n_banos_p2", x = 680f, y = 550f, floor = 2),

        // --- PLANTA 3 ---
        Node("n_ascensor_principal_p3", x = 230f, y = 660f, floor = 3),
        Node("n_escaleras_principal_p3", x = 240f, y = 660f, floor = 3),
        Node("n_despachos_p3", x = 260f, y = 660f, floor = 3),

        Node("n_entrada_lateral_aulas_p3", x = 560f, y = 280f, floor = 3),
        Node("n_escaleras_lateral_p3", x = 480f, y = 310f, floor = 3),
        Node("n_aula31", x = 550f, y = 220f, floor = 3),
        Node("n_aula32", x = 580f, y = 250f, floor = 3),
        Node("n_aula33", x = 610f, y = 280f, floor = 3),
        Node("n_aula34", x = 640f, y = 310f, floor = 3),
        Node("n_aula35", x = 670f, y = 340f, floor = 3),
        Node("n_aula36", x = 700f, y = 370f, floor = 3),
        Node("n_aula37", x = 730f, y = 400f, floor = 3),
        Node("n_aula38", x = 760f, y = 430f, floor = 3),
        Node("n_aula39", x = 790f, y = 460f, floor = 3),
        Node("n_aula310", x = 820f, y = 490f, floor = 3),
        Node("n_aula311", x = 850f, y = 520f, floor = 3),

        Node("n_escaleras_p3", x = 680f, y = 490f, floor = 3),
        Node("n_ascensor_p3", x = 700f, y = 530f, floor = 3),
        Node("n_banos_p3", x = 680f, y = 550f, floor = 3),

        // --- PLANTA 4 ---
        Node("n_ascensor_principal_p4", x = 230f, y = 660f, floor = 4),
        Node("n_escaleras_principal_p4", x = 240f, y = 660f, floor = 4),
        Node("n_despachos_p4", x = 260f, y = 660f, floor = 4),

        Node("n_delegacion_estudiantes", x = 700f, y = 510f, floor = 4),
        Node("n_escaleras_p4", x = 680f, y = 490f, floor = 4),
        Node("n_ascensor_p4", x = 700f, y = 530f, floor = 4)
    ).associateBy { it.id }

    // 2. Conexiones transitables entre nodos
    val edges: List<Edge> = listOf(
        // --- CONEXIONES PLANTA -1 ---
        Edge(from = "n_entrada", to = "n_escaleras_entrada_p-1", weight = 2f),
        Edge(from = "n_escaleras_entrada_p-1", to = "n_pasillo_abierto_p-1", weight = 3f, type = EdgeType.STAIRS),
        Edge(from = "n_pasillo_abierto_p-1", to = "n_entrada_edificio_principal_p-1", weight = 5f),

        // Escaleras internas conectadas exclusivamente a la entrada desde el patio en P0
        Edge(from = "n_entrada_edificio_principal_p0", to = "n_escaleras_internas_p-1_a_p0", weight = 4f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_internas_p-1_a_p0", to = "n_distribuidor_p-1", weight = 3f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_internas_p-1_a_p0", to = "n_entrada_edificio_principal_p-1", weight = 3f, type = EdgeType.STAIRS),

        Edge(from = "n_distribuidor_p-1", to = "n_ascensor_principal_p-1", weight = 2f, type = EdgeType.WALKWAY),

        Edge(from = "n_entrada_edificio_principal_p-1", to = "n_distribuidor_p-1", weight = 4f),
        Edge(from = "n_distribuidor_p-1", to = "n_cafeteria", weight = 4f),
        Edge(from = "n_distribuidor_p-1", to = "n_salon_actos", weight = 5f),
        Edge(from = "n_distribuidor_p-1", to = "n_comedor", weight = 4f),

        Edge(from = "n_cafeteria", to = "n_patio_inferior_p-1", weight = 5f),

        Edge(from = "n_corredor_central", to = "n_escaleras_patio_central_p0", weight = 4f),
        Edge(from = "n_escaleras_patio_central_p0", to = "n_patio_inferior_p-1", weight = 8f, type = EdgeType.STAIRS),

        // --- CONEXIONES PLANTA 0 ---
        Edge(from = "n_entrada", to = "n_patio", weight = 8f),
        Edge(from = "n_entrada", to = "n_distribuidor_entrada_principal_p0", weight = 4f),

        // Patio conecta con la entrada lateral del Edificio Principal
        Edge(from = "n_patio", to = "n_entrada_edificio_principal_p0", weight = 4f),
        Edge(from = "n_entrada_edificio_principal_p0", to = "n_hall", weight = 5f),

        // Hall comunica la zona central con la entrada general
        Edge(from = "n_hall", to = "n_distribuidor_entrada_principal_p0", weight = 5f),

        // Distribuidor de la Entrada General conecta con el núcleo de ascensores/escaleras principales y baños
        Edge(from = "n_distribuidor_entrada_principal_p0", to = "n_ascensor_principal_p0", weight = 2f, type = EdgeType.WALKWAY),
        Edge(from = "n_distribuidor_entrada_principal_p0", to = "n_escaleras_principal_p0", weight = 2f, type = EdgeType.STAIRS),
        Edge(from = "n_distribuidor_entrada_principal_p0", to = "n_banos_edificio_principal_p0", weight = 2f, type = EdgeType.WALKWAY),

        Edge(from = "n_patio", to = "n_corredor_central", weight = 10f),
        Edge(from = "n_corredor_central", to = "n_corredor_lateral", weight = 12f),
        Edge(from = "n_corredor_lateral", to = "n_escaleras_lateral_p0", weight = 4f),
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

        // --- CONEXIONES VERTICALES (ESCALERAS PRINCIPALES P0 a P4) ---
        Edge(from = "n_escaleras_principal_p0", to = "n_escaleras_principal_p1", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_principal_p1", to = "n_escaleras_principal_p2", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_principal_p2", to = "n_escaleras_principal_p3", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_principal_p3", to = "n_escaleras_principal_p4", weight = 15f, type = EdgeType.STAIRS),

        // --- CONEXIONES VERTICALES (ASCENSOR PRINCIPAL P-1 a P4) ---
        Edge(from = "n_ascensor_principal_p-1", to = "n_ascensor_principal_p0", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_principal_p0", to = "n_ascensor_principal_p1", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_principal_p1", to = "n_ascensor_principal_p2", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_principal_p2", to = "n_ascensor_principal_p3", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_principal_p3", to = "n_ascensor_principal_p4", weight = 15f, type = EdgeType.WALKWAY),

        // --- CONEXIONES PLANTA 1 ---
        Edge(from = "n_escaleras_principal_p1", to = "n_biblioteca", weight = 3f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_principal_p1", to = "n_biblioteca", weight = 3f, type = EdgeType.WALKWAY),

        Edge(from = "n_entrada_lateral_aulas_p1", to = "n_aula11", weight = 4f),
        Edge(from = "n_ascensor_p1", to = "n_aula15", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_escaleras_p1", to = "n_aula15", weight = 5f, type = EdgeType.STAIRS),
        Edge(from = "n_banos_p1", to = "n_aula15", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p1", to = "n_banos_p1", weight = 2f, type = EdgeType.WALKWAY),

        Edge(from = "n_aula11", to = "n_aula12", weight = 6f),
        Edge(from = "n_aula12", to = "n_aula13", weight = 6f),
        Edge(from = "n_aula13", to = "n_aula14", weight = 6f),
        Edge(from = "n_aula14", to = "n_aula15", weight = 6f),
        Edge(from = "n_aula15", to = "n_aula16", weight = 6f),

        // --- CONEXIONES PLANTA 2 ---
        Edge(from = "n_escaleras_principal_p2", to = "n_despachos_p2", weight = 3f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_principal_p2", to = "n_despachos_p2", weight = 3f, type = EdgeType.WALKWAY),

        Edge(from = "n_entrada_lateral_aulas_p2", to = "n_aula21", weight = 4f),
        Edge(from = "n_ascensor_p2", to = "n_aula27", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_escaleras_p2", to = "n_aula27", weight = 5f, type = EdgeType.STAIRS),
        Edge(from = "n_banos_p2", to = "n_aula27", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p2", to = "n_banos_p2", weight = 2f, type = EdgeType.WALKWAY),

        Edge(from = "n_aula21", to = "n_aula22", weight = 5f),
        Edge(from = "n_aula22", to = "n_aula23", weight = 5f),
        Edge(from = "n_aula23", to = "n_aula24", weight = 5f),
        Edge(from = "n_aula24", to = "n_aula25", weight = 5f),
        Edge(from = "n_aula25", to = "n_aula26", weight = 5f),
        Edge(from = "n_aula26", to = "n_aula27", weight = 5f),
        Edge(from = "n_aula27", to = "n_aula28", weight = 5f),
        Edge(from = "n_aula28", to = "n_aula29", weight = 5f),
        Edge(from = "n_aula29", to = "n_aula210", weight = 5f),

        // --- CONEXIONES PLANTA 3 ---
        Edge(from = "n_escaleras_principal_p3", to = "n_despachos_p3", weight = 3f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_principal_p3", to = "n_despachos_p3", weight = 3f, type = EdgeType.WALKWAY),

        Edge(from = "n_entrada_lateral_aulas_p3", to = "n_aula31", weight = 4f),
        Edge(from = "n_ascensor_p3", to = "n_aula38", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_escaleras_p3", to = "n_aula38", weight = 5f, type = EdgeType.STAIRS),
        Edge(from = "n_banos_p3", to = "n_aula38", weight = 5f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p3", to = "n_banos_p3", weight = 2f, type = EdgeType.WALKWAY),

        Edge(from = "n_aula31", to = "n_aula32", weight = 5f),
        Edge(from = "n_aula32", to = "n_aula33", weight = 5f),
        Edge(from = "n_aula33", to = "n_aula34", weight = 5f),
        Edge(from = "n_aula34", to = "n_aula35", weight = 5f),
        Edge(from = "n_aula35", to = "n_aula36", weight = 5f),
        Edge(from = "n_aula36", to = "n_aula37", weight = 5f),
        Edge(from = "n_aula37", to = "n_aula38", weight = 5f),
        Edge(from = "n_aula38", to = "n_aula39", weight = 5f),
        Edge(from = "n_aula39", to = "n_aula310", weight = 5f),
        Edge(from = "n_aula310", to = "n_aula311", weight = 5f),

        // --- CONEXIONES PLANTA 4 ---
        Edge(from = "n_escaleras_principal_p4", to = "n_despachos_p4", weight = 3f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_principal_p4", to = "n_despachos_p4", weight = 3f, type = EdgeType.WALKWAY),

        // Aulario P4
        Edge(from = "n_ascensor_p4", to = "n_delegacion_estudiantes", weight = 3f, type = EdgeType.WALKWAY),
        Edge(from = "n_escaleras_p4", to = "n_delegacion_estudiantes", weight = 3f, type = EdgeType.STAIRS),

        // --- CONEXIONES VERTICALES (ASCENSOR AULARIO P0 a P4) ---
        Edge(from = "n_ascensor_p0", to = "n_ascensor_p1", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p1", to = "n_ascensor_p2", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p2", to = "n_ascensor_p3", weight = 15f, type = EdgeType.WALKWAY),
        Edge(from = "n_ascensor_p3", to = "n_ascensor_p4", weight = 15f, type = EdgeType.WALKWAY),

        // --- CONEXIONES VERTICALES (ESCALERAS CENTRALES AULARIO P0 a P4) ---
        Edge(from = "n_escaleras_p0", to = "n_escaleras_p1", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_p1", to = "n_escaleras_p2", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_p2", to = "n_escaleras_p3", weight = 15f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_p3", to = "n_escaleras_p4", weight = 15f, type = EdgeType.STAIRS),

        // --- CONEXIONES VERTICALES (ESCALERAS LATERALES AULARIO P3 -> P2 -> P1 -> P0 -> P-1) ---
        Edge(from = "n_escaleras_lateral_p3", to = "n_escaleras_lateral_p2", weight = 10f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_lateral_p2", to = "n_escaleras_lateral_p1", weight = 10f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_lateral_p1", to = "n_escaleras_lateral_p0", weight = 10f, type = EdgeType.STAIRS),
        Edge(from = "n_escaleras_lateral_p0", to = "n_patio_inferior_p-1", weight = 10f, type = EdgeType.STAIRS),

        Edge(from = "n_entrada_lateral_aulas_p3", to = "n_escaleras_lateral_p3", weight = 2f),
        Edge(from = "n_entrada_lateral_aulas_p2", to = "n_escaleras_lateral_p2", weight = 2f),
        Edge(from = "n_entrada_lateral_aulas_p1", to = "n_escaleras_lateral_p1", weight = 2f),
        Edge(from = "n_entrada_lateral_aulas", to = "n_escaleras_lateral_p0", weight = 2f)
    )

    // 3. Puntos de Interés seleccionables
    val pois: List<PointOfInterest> = listOf(
        // --- POIs PLANTA -1 ---
        PointOfInterest("poi_cafeteria", "Cafetería", "Cafeteria", "SERVICIO", "n_cafeteria", "Cafetería principal en Planta Sótano conectada con el patio inferior"),
        PointOfInterest("poi_salon_actos", "Salón de Actos", "Assembly Hall", "ZONA_COMUN", "n_salon_actos", "Salón de Actos principal en Planta Sótano"),
        PointOfInterest("poi_comedor", "Comedor", "Dining Hall", "SERVICIO", "n_comedor", "Comedor universitario en Planta Sótano"),
        PointOfInterest("poi_patio_inferior_p-1", "Patio Cero / Zona Abierta (P-1)", "Lower Courtyard (Floor -1)", "ZONA_COMUN", "n_patio_inferior_p-1", "Patio inferior exterior para eventos"),
        PointOfInterest("poi_ascensor_principal_p-1", "Ascensor Principal (P-1)", "Main Elevator (Floor -1)", "ACCESIBILIDAD", "n_ascensor_principal_p-1", "Ascensor principal en Planta Sótano"),

        // --- POIs PLANTA 0 ---
        PointOfInterest("poi_entrada", "Entrada Principal (Recinto Abierto)", "Main Entrance (Open Enclosure)", "ACCESO", "n_entrada", "Recinto abierto exterior de la entrada principal"),
        PointOfInterest("poi_patio", "Patio Exterior", "Courtyard", "ZONA_COMUN", "n_patio", "Patio con mesas y bancos"),
        PointOfInterest("poi_hall", "Hall Central", "Central Hall", "ZONA_COMUN", "n_hall", "Distribuidor cilíndrico en el bloque principal"),
        PointOfInterest("poi_distribuidor_entrada_principal_p0", "Entrada General Edificio Principal", "Main Building Main Entrance", "ACCESO", "n_distribuidor_entrada_principal_p0", "Distribuidor interno de la entrada general con acceso a escaleras y ascensor principal"),
        PointOfInterest("poi_entrada_edificio_principal_p0", "Entrada Patio Edificio Principal (P0)", "Patio Building Entrance (P0)", "ACCESO", "n_entrada_edificio_principal_p0", "Puerta de acceso al edificio principal desde el patio exterior"),
        PointOfInterest("poi_ascensor_principal_p0", "Ascensor Principal (P0)", "Main Elevator (Floor 0)", "ACCESIBILIDAD", "n_ascensor_principal_p0", "Ascensor principal en Planta Baja"),
        PointOfInterest("poi_escaleras_principal_p0", "Escaleras Edificio Principal (P0)", "Main Stairs (Floor 0)", "SERVICIO", "n_escaleras_principal_p0", "Escaleras principales hacia plantas superiores"),
        PointOfInterest("poi_banos_edificio_principal_p0", "Aseos Edificio Principal (P0)", "Main Building Restrooms (Floor 0)", "SERVICIO", "n_banos_edificio_principal_p0", "Aseos en Planta Baja del edificio principal"),
        PointOfInterest("poi_corredor_central", "Corredor Central", "Central Corridor", "TRANSITO", "n_corredor_central", "Paso intermedio al aulario"),
        PointOfInterest("poi_corredor_lateral", "Corredor Lateral", "Side Corridor", "TRANSITO", "n_corredor_lateral", "Paso superior al aulario"),
        PointOfInterest("poi_escaleras_lateral_p0", "Escaleras Corredor Lateral (P0)", "Side Stairs (Floor 0)", "SERVICIO", "n_escaleras_lateral_p0", "Escaleras exteriores hacia plantas superiores e inferiores"),
        PointOfInterest("poi_escaleras_patio_central_p0", "Escaleras Patio Central a P-1", "Central Patio Stairs to P-1", "SERVICIO", "n_escaleras_patio_central_p0", "Escaleras junto al corredor central que bajan al patio inferior"),
        PointOfInterest("poi_escaleras_entrada_p-1", "Escaleras Entrada a P-1", "Entrance Stairs to P-1", "SERVICIO", "n_escaleras_entrada_p-1", "Escaleras exteriores de la entrada principal hacia el pasillo abierto P-1"),
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
        PointOfInterest("poi_biblioteca", "Biblioteca", "Library", "SERVICIO", "n_biblioteca", "Biblioteca universitaria en Planta 1"),
        PointOfInterest("poi_ascensor_principal_p1", "Ascensor Principal (P1)", "Main Elevator (Floor 1)", "ACCESIBILIDAD", "n_ascensor_principal_p1", "Ascensor principal en Planta 1"),
        PointOfInterest("poi_escaleras_principal_p1", "Escaleras Edificio Principal (P1)", "Main Stairs (Floor 1)", "SERVICIO", "n_escaleras_principal_p1", "Escaleras principales Planta 1"),
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
        PointOfInterest("poi_despachos_p2", "Despachos Planta 2", "Offices Floor 2", "DESPACHO", "n_despachos_p2", "Pasillo principal de despachos del profesorado en Planta 2"),
        PointOfInterest("poi_ascensor_principal_p2", "Ascensor Principal (P2)", "Main Elevator (Floor 2)", "ACCESIBILIDAD", "n_ascensor_principal_p2", "Ascensor principal en Planta 2"),
        PointOfInterest("poi_escaleras_principal_p2", "Escaleras Edificio Principal (P2)", "Main Stairs (Floor 2)", "SERVICIO", "n_escaleras_principal_p2", "Escaleras principales Planta 2"),
        PointOfInterest("poi_entrada_lateral_aulas_p2", "Entrada Lateral Aulas (P2)", "Side Classrooms Entrance (P2)", "TRANSITO", "n_entrada_lateral_aulas_p2", "Acceso lateral Planta 2"),
        PointOfInterest("poi_escaleras_lateral_p2", "Escaleras Corredor Lateral (P2)", "Side Stairs (Floor 2)", "SERVICIO", "n_escaleras_lateral_p2", "Escaleras laterales hacia plantas superiores"),
        PointOfInterest("poi_escaleras_p2", "Escaleras Aulario (P2)", "Classrooms Stairs (Floor 2)", "SERVICIO", "n_escaleras_p2", "Escaleras centrales Planta 2"),
        PointOfInterest("poi_ascensor_p2", "Ascensor Aulario (P2)", "Classrooms Elevator (Floor 2)", "ACCESIBILIDAD", "n_ascensor_p2", "Ascensor adaptado en Planta 2"),
        PointOfInterest("poi_banos_p2", "Aseos Aulario (P2)", "Restrooms (Floor 2)", "SERVICIO", "n_banos_p2", "Baños en Planta 2"),

        PointOfInterest("poi_aula21", "Aula 2.1", "Classroom 2.1", "AULA", "n_aula21"),
        PointOfInterest("poi_aula22", "Aula 2.2", "Classroom 2.2", "AULA", "n_aula22"),
        PointOfInterest("poi_aula23", "Aula 2.3", "Classroom 2.3", "AULA", "n_aula23"),
        PointOfInterest("poi_aula24", "Aula 2.4", "Classroom 2.4", "AULA", "n_aula24"),
        PointOfInterest("poi_aula25", "Aula 2.5", "Classroom 2.5", "AULA", "n_aula25"),
        PointOfInterest("poi_aula26", "Aula 2.6", "Classroom 2.6", "AULA", "n_aula26"),
        PointOfInterest("poi_aula27", "Aula 2.7", "Classroom 2.7", "AULA", "n_aula27"),
        PointOfInterest("poi_aula28", "Aula 2.8", "Classroom 2.8", "AULA", "n_aula28"),
        PointOfInterest("poi_aula29", "Aula 2.9", "Classroom 2.9", "AULA", "n_aula29"),
        PointOfInterest("poi_aula210", "Aula 2.10", "Classroom 2.10", "AULA", "n_aula210"),

        // --- POIs PLANTA 3 ---
        PointOfInterest("poi_despachos_p3", "Despachos Planta 3", "Offices Floor 3", "DESPACHO", "n_despachos_p3", "Pasillo principal de despachos del profesorado en Planta 3"),
        PointOfInterest("poi_ascensor_principal_p3", "Ascensor Principal (P3)", "Main Elevator (Floor 3)", "ACCESIBILIDAD", "n_ascensor_principal_p3", "Ascensor principal en Planta 3"),
        PointOfInterest("poi_escaleras_principal_p3", "Escaleras Edificio Principal (P3)", "Main Stairs (Floor 3)", "SERVICIO", "n_escaleras_principal_p3", "Escaleras principales Planta 3"),
        PointOfInterest("poi_entrada_lateral_aulas_p3", "Entrada Lateral Aulas (P3)", "Side Classrooms Entrance (P3)", "TRANSITO", "n_entrada_lateral_aulas_p3", "Acceso lateral Planta 3"),
        PointOfInterest("poi_escaleras_p3", "Escaleras Aulario (P3)", "Classrooms Stairs (Floor 3)", "SERVICIO", "n_escaleras_p3", "Escaleras centrales Planta 3"),
        PointOfInterest("poi_ascensor_p3", "Ascensor Aulario (P3)", "Classrooms Elevator (Floor 3)", "ACCESIBILIDAD", "n_ascensor_p3", "Ascensor adaptado en Planta 3"),
        PointOfInterest("poi_banos_p3", "Aseos Aulario (P3)", "Restrooms (Floor 3)", "SERVICIO", "n_banos_p3", "Baños en Planta 3"),

        PointOfInterest("poi_aula31", "Aula 3.1", "Classroom 3.1", "AULA", "n_aula31"),
        PointOfInterest("poi_aula32", "Aula 3.2", "Classroom 3.2", "AULA", "n_aula32"),
        PointOfInterest("poi_aula33", "Aula 3.3", "Classroom 3.3", "AULA", "n_aula33"),
        PointOfInterest("poi_aula34", "Aula 3.4", "Classroom 3.4", "AULA", "n_aula34"),
        PointOfInterest("poi_aula35", "Aula 3.5", "Classroom 3.5", "AULA", "n_aula35"),
        PointOfInterest("poi_aula36", "Aula 3.6", "Classroom 3.6", "AULA", "n_aula36"),
        PointOfInterest("poi_aula37", "Aula 3.7", "Classroom 3.7", "AULA", "n_aula37"),
        PointOfInterest("poi_aula38", "Aula 3.8", "Classroom 3.8", "AULA", "n_aula38"),
        PointOfInterest("poi_aula39", "Aula 3.9", "Classroom 3.9", "AULA", "n_aula39"),
        PointOfInterest("poi_aula310", "Aula 3.10", "Classroom 3.10", "AULA", "n_aula310"),
        PointOfInterest("poi_aula311", "Aula 3.11", "Classroom 3.11", "AULA", "n_aula311"),

        // --- POIs PLANTA 4 ---
        PointOfInterest("poi_despachos_p4", "Despachos Planta 4", "Offices Floor 4", "DESPACHO", "n_despachos_p4", "Pasillo principal de despachos del profesorado en Planta 4"),
        PointOfInterest("poi_ascensor_principal_p4", "Ascensor Principal (P4)", "Main Elevator (Floor 4)", "ACCESIBILIDAD", "n_ascensor_principal_p4", "Ascensor principal en Planta 4"),
        PointOfInterest("poi_escaleras_principal_p4", "Escaleras Edificio Principal (P4)", "Main Stairs (Floor 4)", "SERVICIO", "n_escaleras_principal_p4", "Escaleras principales Planta 4"),
        PointOfInterest("poi_delegacion_estudiantes", "Delegación de Estudiantes", "Student Council Office", "DESPACHO", "n_delegacion_estudiantes", "Oficina de Delegación de Estudiantes en Planta 4"),
        PointOfInterest("poi_escaleras_p4", "Escaleras Aulario (P4)", "Classrooms Stairs (Floor 4)", "SERVICIO", "n_escaleras_p4", "Escaleras centrales Planta 4"),
        PointOfInterest("poi_ascensor_p4", "Ascensor Aulario (P4)", "Classrooms Elevator (Floor 4)", "ACCESIBILIDAD", "n_ascensor_p4", "Ascensor adaptado en Planta 4")
    )
}