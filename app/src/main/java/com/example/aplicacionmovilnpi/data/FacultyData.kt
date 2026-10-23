package com.example.aplicacionmovilnpi.data

import com.example.aplicacionmovilnpi.navigation.EtsiitData

/**
 * Profesorado real de 4º CSI (curso 2026/2027) y horario de la alumna.
 * Fuente: docs/datos/profesores/profesores_CSI_4.md (guías docentes y horarios oficiales de la ETSIIT).
 */
object FacultyData {

    private const val LUN = 1
    private const val MAR = 2
    private const val MIE = 3
    private const val JUE = 4
    private const val VIE = 5

    private const val DESPACHOS_P3 = "poi_despachos_p3"

    /** POI del aula con código "X.Y" si está digitalizada en el plano (ej: "2.10" -> "poi_aula210"). */
    fun roomPoiId(code: String): String? {
        val id = "poi_aula" + code.replace(".", "")
        return id.takeIf { EtsiitData.pois.any { poi -> poi.id == id } }
    }

    private fun clase(semester: Int, day: Int, start: String, end: String, title: String, room: String) =
        WeeklySlot(semester, day, start, end, ActivityKind.CLASE, title, "Aula $room", roomPoiId(room))

    // Laboratorios: su numeración no coincide con la de las aulas del plano, así que no se enlazan.
    private fun lab(semester: Int, day: Int, start: String, end: String, title: String, room: String) =
        WeeklySlot(semester, day, start, end, ActivityKind.CLASE, title, "Lab. $room")

    private fun tutoria(semester: Int, day: Int, start: String, end: String, place: String, poiId: String? = null) =
        WeeklySlot(semester, day, start, end, ActivityKind.TUTORIA, "Tutoría", place, poiId)

    val professors = listOf(
        Professor(
            id = 1,
            initials = "MC",
            name = "Marcelino José Cabrera Cuevas",
            role = "Profesor Colaborador Indefinido",
            department = "Lenguajes y Sistemas Informáticos",
            email = "mcabrera@ugr.es",
            phone = "958 248 420",
            office = "Despacho 25 · planta 3",
            officePoiId = DESPACHOS_P3,
            subjects = listOf("Nuevos Paradigmas de Interacción", "Programación de Dispositivos Móviles"),
            schedule = listOf(
                clase(1, JUE, "8:30", "10:30", "NPI · Teoría", "0.5"),
                clase(1, JUE, "10:30", "12:30", "NPI · Prácticas A2", "2.7"),
                clase(1, VIE, "8:30", "10:30", "NPI · Prácticas A3", "2.7"),
                clase(1, VIE, "12:30", "14:30", "NPI · Prácticas A1", "2.7"),
                clase(2, VIE, "10:30", "12:30", "Prog. Dispositivos Móviles · Prácticas", "2.8"),
                clase(2, VIE, "12:30", "14:30", "Prog. Dispositivos Móviles · Teoría", "2.8"),
                tutoria(1, LUN, "11:30", "13:30", "Despacho 25 · planta 3", DESPACHOS_P3),
                tutoria(1, JUE, "12:30", "14:30", "Despacho 25 · planta 3", DESPACHOS_P3),
                tutoria(1, VIE, "10:30", "12:30", "Despacho 25 · planta 3", DESPACHOS_P3),
                tutoria(2, MAR, "9:30", "14:30", "Despacho 25 · planta 3", DESPACHOS_P3),
                tutoria(2, VIE, "9:30", "10:30", "Despacho 25 · planta 3", DESPACHOS_P3)
            ),
            tone = "red"
        ),
        Professor(
            id = 2,
            initials = "RL",
            name = "Ramón López-Cózar Delgado",
            role = "Profesor Titular",
            department = "Lenguajes y Sistemas Informáticos",
            email = "rlopezc@ugr.es",
            phone = "958 240 579",
            office = "Despacho 26 · planta 3",
            officePoiId = DESPACHOS_P3,
            subjects = listOf("Procesadores de Lenguajes"),
            schedule = listOf(
                clase(1, VIE, "10:30", "12:30", "PL · Teoría", "0.5"),
                clase(1, VIE, "12:30", "14:30", "PL · Prácticas A2", "2.6"),
                tutoria(1, MAR, "10:30", "13:30", "Despacho 26 · planta 3", DESPACHOS_P3),
                tutoria(1, MIE, "10:30", "13:30", "Despacho 26 · planta 3", DESPACHOS_P3),
                tutoria(2, MAR, "10:30", "13:30", "Aula Inf. 1 · Facultad de Bellas Artes"),
                tutoria(2, MIE, "10:30", "13:30", "Despacho 26 · planta 3", DESPACHOS_P3)
            ),
            tone = "blue"
        ),
        Professor(
            id = 3,
            initials = "JR",
            name = "Jorge Revelles Moreno",
            role = "Profesor Colaborador Indefinido",
            department = "Lenguajes y Sistemas Informáticos",
            email = "jrevelle@ugr.es",
            phone = "958 240 575",
            office = "Despacho 4 · planta 3",
            officePoiId = DESPACHOS_P3,
            subjects = listOf("Procesadores de Lenguajes", "Sistemas Cooperativos y Gestión de Contenidos"),
            schedule = listOf(
                clase(1, JUE, "10:30", "12:30", "PL · Prácticas A3", "2.8"),
                clase(1, VIE, "8:30", "10:30", "PL · Prácticas A1", "2.8"),
                clase(2, LUN, "9:30", "11:30", "Sist. Cooperativos · Teoría", "3.4"),
                clase(2, LUN, "11:30", "13:30", "Sist. Cooperativos · Prácticas", "3.4"),
                tutoria(1, LUN, "9:30", "10:30", "Facultad de Filosofía y Letras"),
                tutoria(1, LUN, "17:30", "18:30", "Facultad de Filosofía y Letras"),
                tutoria(1, MAR, "9:30", "12:30", "Despacho 4 · planta 3", DESPACHOS_P3),
                tutoria(1, JUE, "9:30", "10:30", "Despacho 4 · planta 3", DESPACHOS_P3),
                tutoria(2, MAR, "11:30", "12:30", "Facultad de Filosofía y Letras"),
                tutoria(2, MIE, "9:30", "13:30", "Despacho 4 · planta 3", DESPACHOS_P3),
                tutoria(2, JUE, "11:30", "12:30", "Facultad de Filosofía y Letras")
            ),
            tone = "purple"
        ),
        Professor(
            id = 4,
            initials = "PM",
            name = "Pablo Mesejo Santiago",
            role = "Profesor Titular",
            department = "Ciencias de la Computación e IA (DECSAI)",
            email = "pmesejo@ugr.es",
            phone = "958 248 555",
            office = "Despacho no publicado (ETSIIT)",
            officePoiId = null,
            subjects = listOf("Visión por Computador", "Técnicas de los Sistemas Inteligentes"),
            schedule = listOf(
                clase(1, JUE, "12:30", "14:30", "VC · Teoría", "0.5"),
                clase(1, VIE, "8:30", "10:30", "VC · Prácticas A2", "2.6"),
                clase(1, VIE, "12:30", "14:30", "VC · Prácticas A3", "3.2"),
                lab(2, MIE, "9:30", "11:30", "TSI · Prácticas (3º CSI-A1)", "0.2"),
                lab(2, JUE, "9:30", "11:30", "TSI · Prácticas (3º CSI-A3)", "1.1"),
                tutoria(1, MAR, "10:00", "13:00", "ETSIIT (despacho no publicado)"),
                tutoria(1, MIE, "10:00", "13:00", "ETSIIT (despacho no publicado)"),
                tutoria(2, MAR, "10:00", "13:00", "ETSIIT (despacho no publicado)"),
                tutoria(2, MIE, "10:00", "13:00", "ETSIIT (despacho no publicado)")
            ),
            tone = "red"
        ),
        Professor(
            id = 5,
            initials = "ST",
            name = "Siham Tabik Ouled Hrour",
            role = "Catedrática de Universidad",
            department = "Ciencias de la Computación e IA (DECSAI)",
            email = "siham@ugr.es",
            phone = "",
            office = "Despacho D16 · ETSIIT",
            officePoiId = null,
            subjects = listOf("Visión por Computador"),
            schedule = listOf(
                clase(1, JUE, "10:30", "12:30", "VC · Prácticas A1", "3.8"),
                tutoria(1, LUN, "14:30", "15:30", "Despacho D16"),
                tutoria(1, MIE, "14:00", "15:30", "Despacho D16"),
                tutoria(1, JUE, "8:00", "10:30", "Despacho D16"),
                tutoria(1, JUE, "14:30", "15:30", "Despacho D16"),
                tutoria(2, LUN, "15:30", "17:30", "ETSIIT"),
                tutoria(2, MIE, "15:30", "17:30", "ETSIIT"),
                tutoria(2, JUE, "15:30", "17:30", "ETSIIT")
            ),
            tone = "blue"
        ),
        Professor(
            id = 6,
            initials = "JC",
            name = "José Enrique Cano Ocaña",
            role = "Profesor Titular",
            department = "Ciencias de la Computación e IA (DECSAI)",
            email = "eco@ugr.es",
            phone = "958 242 808",
            office = "Despacho D09 · ETSIIT",
            officePoiId = null,
            subjects = listOf("Programación Técnica y Científica"),
            schedule = listOf(
                clase(1, LUN, "9:30", "11:30", "PTC · Teoría", "1.8"),
                clase(1, MIE, "11:30", "13:30", "PTC · Prácticas", "3.5"),
                tutoria(1, MIE, "8:30", "11:30", "Despacho D09"),
                tutoria(1, MIE, "13:30", "14:30", "Despacho D09"),
                tutoria(1, JUE, "10:30", "12:30", "D68-4P · ETS de Ingeniería de Caminos"),
                tutoria(2, LUN, "11:30", "14:30", "ETSIIT"),
                tutoria(2, MAR, "11:30", "14:30", "ETSIIT")
            ),
            tone = "purple"
        ),
        Professor(
            id = 7,
            initials = "LC",
            name = "Luis Miguel Campos Ibáñez",
            role = "Catedrático de Universidad",
            department = "Ciencias de la Computación e IA (DECSAI)",
            email = "lmcampos@ugr.es",
            phone = "958 243 199",
            office = "Despacho D10 · ETSIIT",
            officePoiId = null,
            subjects = listOf("Simulación de Sistemas", "Algorítmica (GIIM)"),
            schedule = listOf(
                clase(1, LUN, "11:30", "13:30", "SS · Prácticas", "2.7"),
                clase(1, MAR, "11:30", "13:30", "SS · Teoría", "0.1"),
                clase(2, MIE, "8:30", "9:30", "Algorítmica (GIIM) · Teoría", "1.7"),
                clase(2, MIE, "11:30", "12:30", "Algorítmica (GIIM) · Prácticas A2", "2.4"),
                clase(2, JUE, "8:30", "10:30", "Algorítmica (GIIM) · Teoría", "1.7"),
                clase(2, JUE, "13:30", "14:30", "Algorítmica (GIIM) · Prácticas A1", "2.7"),
                tutoria(1, LUN, "9:30", "11:30", "Despacho D10"),
                tutoria(1, LUN, "13:30", "14:30", "Despacho D10"),
                tutoria(1, MAR, "9:30", "11:30", "Despacho D10"),
                tutoria(1, MAR, "13:30", "14:30", "Despacho D10"),
                tutoria(2, MIE, "9:30", "11:30", "Despacho D10"),
                tutoria(2, MIE, "12:30", "13:30", "Despacho D10"),
                tutoria(2, JUE, "10:30", "13:30", "Despacho D10")
            ),
            tone = "red"
        ),
        Professor(
            id = 8,
            initials = "MP",
            name = "Manuel Pegalajar Cuéllar",
            role = "Catedrático de Universidad",
            department = "Ciencias de la Computación e IA (DECSAI)",
            email = "manupc@ugr.es",
            phone = "958 244 235",
            office = "Despacho D19 · ETSIIT",
            officePoiId = null,
            subjects = listOf("Teoría de la Información y la Codificación", "Aprendizaje Automático"),
            schedule = listOf(
                clase(1, MAR, "9:30", "11:30", "TIC · Prácticas", "2.3"),
                clase(1, MIE, "9:30", "11:30", "TIC · Teoría", "1.1"),
                lab(2, MAR, "9:30", "11:30", "AA · Prácticas (3º CSI-A3)", "1.1"),
                lab(2, JUE, "9:30", "11:30", "AA · Prácticas (3º CSI-A2)", "0.3"),
                tutoria(1, LUN, "11:30", "13:30", "Despacho D19"),
                tutoria(1, LUN, "15:30", "17:30", "Despacho D19"),
                tutoria(1, MAR, "11:30", "13:30", "Despacho D19"),
                tutoria(2, LUN, "11:30", "13:30", "Despacho D19"),
                tutoria(2, LUN, "15:30", "17:30", "Despacho D1"),
                tutoria(2, MAR, "11:30", "13:30", "Despacho D19")
            ),
            tone = "blue"
        ),
        Professor(
            id = 9,
            initials = "GN",
            name = "Gabriel Navarro Garulo",
            role = "Catedrático de Universidad",
            department = "Álgebra",
            email = "gnavarro@ugr.es",
            phone = "958 241 000 ext. 20586",
            office = "Facultad de Ciencias (despacho no publicado)",
            officePoiId = null,
            subjects = listOf("Criptografía y Computación", "Lógica y Métodos Discretos (GIIM)"),
            schedule = listOf(
                clase(2, MIE, "9:30", "11:30", "LMD (GIIM) · Teoría", "1.7"),
                clase(2, VIE, "8:30", "9:30", "LMD (GIIM) · Teoría", "1.7"),
                clase(2, VIE, "10:30", "12:30", "CRIP · Prácticas", "2.2"),
                clase(2, VIE, "12:30", "14:30", "CRIP · Teoría", "1.7")
            ),
            note = "No tiene tutorías asignadas este curso. Lo mejor es escribirle por correo.",
            tone = "purple"
        ),
        Professor(
            id = 10,
            initials = "AB",
            name = "Antonio Bautista Bailón Morillas",
            role = "Profesor Colaborador Indefinido",
            department = "Ciencias de la Computación e IA (DECSAI)",
            email = "bailon@ugr.es",
            phone = "958 240 511",
            office = "Despacho no publicado (ETSIIT)",
            officePoiId = null,
            subjects = listOf("Programación Lúdica", "Computación Ubicua e Inteligencia Ambiental", "Algorítmica"),
            schedule = listOf(
                clase(2, LUN, "12:30", "13:30", "Algorítmica · Prácticas grupo 7", "2.3"),
                clase(2, MAR, "15:30", "17:30", "CUIA · Prácticas A2", "2.3"),
                clase(2, MAR, "17:30", "19:30", "CUIA · Teoría", "0.7"),
                clase(2, JUE, "9:30", "11:30", "PLD · Teoría", "1.3"),
                clase(2, JUE, "11:30", "13:30", "PLD · Prácticas", "3.5"),
                clase(2, JUE, "15:30", "17:30", "CUIA · Prácticas A1", "2.8"),
                tutoria(1, LUN, "8:00", "14:00", "Aula M5 · Facultad de Ciencias"),
                tutoria(2, LUN, "8:00", "12:30", "ETSIIT"),
                tutoria(2, JUE, "8:00", "9:30", "ETSIIT")
            ),
            tone = "red"
        ),
        Professor(
            id = 11,
            initials = "JG",
            name = "José Andrés González López",
            role = "Profesor Titular",
            department = "Teoría de la Señal, Telemática y Comunicaciones",
            email = "joseangl@ugr.es",
            phone = "958 241 000 ext. 20619",
            office = "Despacho 0.10 · Edificio Auxiliar",
            officePoiId = null,
            subjects = listOf("Robótica Industrial", "Procesamiento Digital de Señales"),
            schedule = listOf(
                clase(2, LUN, "9:30", "11:30", "RI · Teoría", "1.8"),
                clase(2, JUE, "9:30", "11:30", "Proc. Digital de Señales · Teoría", "1.5"),
                clase(2, JUE, "11:30", "13:30", "Proc. Digital de Señales · Prácticas", "3.2"),
                tutoria(1, LUN, "12:00", "15:00", "Despacho 0.10 · Edificio Auxiliar"),
                tutoria(1, JUE, "10:00", "13:00", "Despacho 0.10 · Edificio Auxiliar"),
                tutoria(2, MAR, "10:00", "13:00", "Despacho 0.10 · Edificio Auxiliar"),
                tutoria(2, MAR, "14:00", "17:00", "Despacho 0.10 · Edificio Auxiliar")
            ),
            tone = "blue"
        ),
        Professor(
            id = 12,
            initials = "FS",
            name = "Fermín Segovia Román",
            role = "Profesor Titular",
            department = "Teoría de la Señal, Telemática y Comunicaciones",
            email = "fsegovia@ugr.es",
            phone = "958 241 000 ext. 20618",
            office = "Despacho 1.13 · Edificio Auxiliar",
            officePoiId = null,
            subjects = listOf("Robótica Industrial"),
            schedule = listOf(
                clase(2, LUN, "11:30", "13:30", "RI · Prácticas", "3.11").copy(place = "Aula 3.11 / 3.7"),
                tutoria(1, MAR, "10:00", "13:00", "Despacho 1.13 · Edificio Auxiliar"),
                tutoria(1, JUE, "10:00", "13:00", "Despacho 1.13 · Edificio Auxiliar"),
                tutoria(2, MAR, "10:00", "13:00", "Despacho 1.13 · Edificio Auxiliar"),
                tutoria(2, JUE, "10:00", "13:00", "Despacho 1.13 · Edificio Auxiliar")
            ),
            tone = "purple"
        )
    )

    // --- Alumna de la cuenta de prueba: 4º CSI, grupo A, prácticas A1, optativas TIC y SS ---

    object Student {
        const val name = "Lucía Martín García"
        const val firstName = "Lucía"
        const val initials = "LM"
        const val email = "lucia@correo.ugr.es"
        const val degree = "Grado en Ingeniería Informática"
        const val course = "4º · Computación y Sistemas Inteligentes"
        const val group = "Grupo A · prácticas A1"
        const val academicYear = "2026/2027"
    }

    /** Asignaturas del 1er semestre en las que está matriculada la alumna. */
    val studentSubjects = listOf(
        "Nuevos Paradigmas de Interacción",
        "Procesadores de Lenguajes",
        "Visión por Computador",
        "Teoría de la Información y la Codificación",
        "Simulación de Sistemas"
    )

    private val subjectTones = mapOf(
        "NPI" to "red",
        "PL" to "blue",
        "VC" to "purple",
        "TIC" to "orange",
        "SS" to "green"
    )

    fun toneFor(slot: WeeklySlot): String = subjectTones[slot.title.substringBefore(" ")] ?: "blue"

    val studentSchedule = listOf(
        clase(1, LUN, "11:30", "13:30", "SS · Prácticas", "2.7"),
        clase(1, MAR, "9:30", "11:30", "TIC · Prácticas", "2.3"),
        clase(1, MAR, "11:30", "13:30", "SS · Teoría", "0.1"),
        clase(1, MIE, "9:30", "11:30", "TIC · Teoría", "1.1"),
        clase(1, JUE, "8:30", "10:30", "NPI · Teoría", "0.5"),
        clase(1, JUE, "10:30", "12:30", "VC · Prácticas A1", "3.8"),
        clase(1, JUE, "12:30", "14:30", "VC · Teoría", "0.5"),
        clase(1, VIE, "8:30", "10:30", "PL · Prácticas A1", "2.8"),
        clase(1, VIE, "10:30", "12:30", "PL · Teoría", "0.5"),
        clase(1, VIE, "12:30", "14:30", "NPI · Prácticas A1", "2.7")
    )
}
