package com.example.aplicacionmovilnpi.data

import java.util.Calendar

object SampleData {

    val places = listOf(
        PlaceCategory("Aulas", "Todas las plantas", "building", "rose"),
        PlaceCategory("Despachos", "Profesorado", "person", "purple"),
        PlaceCategory("Biblioteca", "Planta 1", "book", "blue"),
        PlaceCategory("Cafetería", "Planta baja", "coffee", "orange")
    )

    private val subjectNames = mapOf(
        "NPI" to "Nuevos Paradigmas de Interacción",
        "PL" to "Procesadores de Lenguajes",
        "VC" to "Visión por Computador",
        "TIC" to "Teoría de la Información y la Codificación",
        "SS" to "Simulación de Sistemas"
    )

    private val monthNames = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    /** "NPI · Teoría" -> "Nuevos Paradigmas de Interacción · Teoría". */
    fun lessonName(slot: WeeklySlot): String {
        val code = slot.title.substringBefore(" · ")
        val full = subjectNames[code] ?: return slot.title
        return slot.title.replaceFirst(code, full)
    }

    fun toLesson(slot: WeeklySlot) = ClassLesson(
        time = slot.start.padStart(5, '0'),
        end = slot.end.padStart(5, '0'),
        name = lessonName(slot),
        room = slot.place,
        tone = FacultyData.toneFor(slot),
        poiId = slot.poiId
    )

    /** Horario de la alumna (lunes a viernes) con las fechas de la semana de [today]. */
    fun weekSchedule(today: Calendar = Calendar.getInstance()): List<DaySchedule> {
        val semester = Moment.now(today).semester
        val monday = (today.clone() as Calendar).apply {
            // Sábado y domingo muestran ya la semana siguiente.
            val dayIndex = (get(Calendar.DAY_OF_WEEK) + 5) % 7 // lunes = 0 … domingo = 6
            add(Calendar.DAY_OF_MONTH, if (dayIndex >= 5) 7 - dayIndex else -dayIndex)
        }
        return (1..5).map { day ->
            val date = (monday.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, day - 1) }
            val dayName = ProfessorLocator.dayName(day)
            DaySchedule(
                short = if (day == 3) "X" else dayName.take(1),
                day = dayName,
                date = date.get(Calendar.DAY_OF_MONTH).toString(),
                month = monthNames[date.get(Calendar.MONTH)],
                classes = FacultyData.studentSchedule
                    .filter { it.semester == semester && it.day == day }
                    .sortedBy { it.startMinutes }
                    .map(::toLesson)
            )
        }
    }

    /** Próxima clase de la alumna (o la que está en curso), buscando hacia delante en la semana. */
    fun nextLesson(moment: Moment = Moment.now()): WeeklySlot? {
        val ongoing = FacultyData.studentSchedule.firstOrNull {
            it.semester == moment.semester && it.day == moment.day &&
                moment.minutes >= it.startMinutes && moment.minutes < it.endMinutes
        }
        return ongoing ?: ProfessorLocator.nextSlot(FacultyData.studentSchedule, moment)
    }

    val professors: List<Professor> get() = FacultyData.professors

    val campuses = listOf(
        CampusItem(
            key = "etsiit",
            name = "ETSIIT",
            campus = "CAMPUS AYNADAMAR",
            full = "Escuela Técnica Superior de Ingenierías Informática y de Telecomunicación",
            distance = "1,2 km",
            hours = "Abierto hasta 21:30"
        ),
        CampusItem(
            key = "sciences",
            name = "Ciencias",
            campus = "CAMPUS FUENTENUEVA",
            full = "Facultad de Ciencias",
            distance = "2,8 km",
            hours = "Abierto hasta 21:00"
        ),
        CampusItem(
            key = "cartuja",
            name = "Cartuja",
            campus = "CAMPUS CARTUJA",
            full = "Facultades de Psicología, Ciencias Económicas y Empresariales",
            distance = "0,9 km",
            hours = "Abierto hasta 21:30"
        )
    )

    val floorRooms = mapOf(
        "P2" to listOf("Despachos 2.01–2.20", "Sala de juntas", "Laboratorio 5", "Laboratorio 6", "Seminario 2"),
        "P1" to listOf("Aulas B-10–B-13", "Biblioteca", "Sala de estudio", "Laboratorio 2", "Aula B-08"),
        "PB" to listOf("Aulas 0.1–0.4", "Secretaría", "Conserjería", "Cafetería", "Biblioteca"),
        "S1" to listOf("Laboratorios S1.1–S1.4", "Taquillas", "Almacén", "Reprografía", "Aparcamiento de bicis")
    )

    val pointsOfInterest = listOf(
        PointOfInterest("Biblioteca", "book", "Planta 1", "Biblioteca"),
        PointOfInterest("Cafetería", "coffee", "Planta baja", "Cafetería"),
        PointOfInterest("Secretaría", "services", "Planta baja", "Secretaría"),
        PointOfInterest("Conserjería", "person", "Entrada", "Conserjería"),
        PointOfInterest("Baños cercanos", "location", "A 24 m", "Baños más cercanos"),
        PointOfInterest("Sala de estudio", "book", "Planta 1", "Sala de estudio"),
        PointOfInterest("Reprografía", "services", "Sótano 1", "Reprografía")
    )

    fun getRouteSteps(accessible: Boolean, destination: String): List<RouteStep> = listOf(
        RouteStep("↑", "Continúa recto por el pasillo", "32 m"),
        RouteStep("←", "Gira a la izquierda en el pasillo principal", "18 m"),
        if (accessible) RouteStep("⇅", "Toma el ascensor hasta la planta 1", "1 planta")
        else RouteStep("⇅", "Sube por las escaleras a la planta 1", "1 planta"),
        RouteStep("→", "Gira a la derecha al salir", if (accessible) "64 m" else "24 m"),
        RouteStep("✓", "Has llegado: $destination", "")
    )

    // Fechas de ejemplo: no son las fechas oficiales de entregas ni de exámenes.
    val initialEvents = listOf(
        CalendarEventItem(
            id = 1L,
            title = "Entrega práctica 1 de NPI",
            subject = "Nuevos Paradigmas de Interacción",
            date = "2026-10-30",
            time = "23:59",
            type = "Entrega",
            description = "Prototipo de la aplicación móvil y memoria en PRADO."
        ),
        CalendarEventItem(
            id = 2L,
            title = "Práctica 1 de Visión por Computador",
            subject = "Visión por Computador",
            date = "2026-11-06",
            time = "23:59",
            type = "Entrega",
            description = "Filtrado y detección de bordes. Entrega en PRADO."
        ),
        CalendarEventItem(
            id = 3L,
            title = "Analizador léxico de PL",
            subject = "Procesadores de Lenguajes",
            date = "2026-11-13",
            time = "23:59",
            type = "Entrega",
            description = "Especificación del lenguaje y analizador léxico."
        ),
        CalendarEventItem(
            id = 4L,
            title = "Examen de Teoría de la Información",
            subject = "Teoría de la Información y la Codificación",
            date = "2027-01-18",
            time = "09:30",
            type = "Examen",
            description = "Convocatoria ordinaria. Consulta el aula en la web de la ETSIIT."
        )
    )

    val initialMails = listOf(
        MailMessage(
            id = 1L,
            folder = MailFolder.INBOX,
            outgoing = false,
            name = "PRADO · Nuevos Paradigmas de Interacción",
            email = "prado@ugr.es",
            subject = "Nuevo material disponible",
            body = "Se ha publicado nuevo material en la asignatura Nuevos Paradigmas de Interacción.\n\nAccede a PRADO para consultarlo.",
            time = "10:24",
            unread = true
        ),
        MailMessage(
            id = 2L,
            folder = MailFolder.INBOX,
            outgoing = false,
            name = "Secretaría ETSIIT",
            email = "secretariaetsiit@ugr.es",
            subject = "Matrícula del curso 2026/2027",
            body = "Estimada estudiante:\n\nTu solicitud de modificación de matrícula ha sido procesada correctamente. Puedes consultar el resguardo actualizado en la sede electrónica.\n\nAtentamente,\nSecretaría ETSIIT",
            time = "Ayer",
            unread = true
        ),
        MailMessage(
            id = 3L,
            folder = MailFolder.INBOX,
            outgoing = false,
            name = "Biblioteca UGR",
            email = "biblioteca@ugr.es",
            subject = "Reserva disponible",
            body = "El ejemplar que reservaste, «Computer Vision: Algorithms and Applications», ya está disponible para su recogida en el mostrador de préstamo de la Biblioteca de la ETSIIT.\n\nTienes 3 días para recogerlo.",
            time = "Ayer",
            unread = true
        ),
        MailMessage(
            id = 4L,
            folder = MailFolder.INBOX,
            outgoing = false,
            name = "PRADO · Visión por Computador",
            email = "prado@ugr.es",
            subject = "Publicado el guion de la práctica 1",
            body = "Ya está disponible el guion de la práctica 1 en la plataforma. Revisadlo antes de la próxima sesión de prácticas.",
            time = "14 oct.",
            unread = false
        ),
        MailMessage(
            id = 5L,
            folder = MailFolder.SENT,
            outgoing = true,
            name = "Delegación de Estudiantes ETSIIT",
            email = "delegacion.etsiit@ugr.es",
            subject = "Consulta sobre las optativas de 2º semestre",
            body = "Hola:\n\n¿Sabéis si este año se oferta Programación Lúdica en el segundo semestre?\n\nMuchas gracias,\nLucía",
            time = "13 oct.",
            unread = false
        ),
        MailMessage(
            id = 6L,
            folder = MailFolder.ARCHIVED,
            outgoing = false,
            name = "Secretaría ETSIIT",
            email = "secretariaetsiit@ugr.es",
            subject = "Bienvenida al curso 2026/2027",
            body = "Te damos la bienvenida al nuevo curso académico. Consulta el calendario académico y los horarios en la web del centro.",
            time = "9 sept.",
            unread = false
        )
    )

    val faqs = listOf(
        FaqItem("navigation", "¿Cómo llego a mi próxima clase?", "En Inicio, pulsa «Guiarme al aula» en la tarjeta de tu próxima clase. También puedes pulsar «Cómo llegar» en cualquier clase de tu horario."),
        FaqItem("navigation", "¿Puedo evitar las escaleras?", "Sí. En la pantalla de ruta activa «Ruta accesible» y te guiaremos usando ascensores y rampas."),
        FaqItem("navigation", "La ubicación no es precisa dentro del edificio", "Dentro de los edificios el GPS pierde precisión. Activa el wifi y el Bluetooth para mejorar la localización, o selecciona la planta manualmente en el plano."),
        FaqItem("navigation", "¿Cómo cambio de planta en el plano?", "En Mapa › Ver plano del edificio, usa el selector de plantas (P2, P1, PB, S1) situado en la parte superior."),
        FaqItem("schedule", "¿De dónde sale mi horario?", "Tu horario se genera automáticamente a partir de las asignaturas en las que estás matriculada en la UGR."),
        FaqItem("schedule", "¿Cómo añado una entrega o un examen?", "Ve a Más › Calendario, selecciona el día y pulsa «+ Añadir». Puedes indicar la asignatura, la hora y una descripción."),
        FaqItem("account", "He olvidado mi contraseña", "La contraseña es la de tu cuenta institucional UGR. Puedes restablecerla desde el Acceso Identificado de la UGR con tu DNI y tu correo personal."),
        FaqItem("account", "¿Cómo cierro sesión?", "Pulsa tu avatar en la esquina superior derecha y elige «Cerrar sesión», o hazlo desde Mi perfil.")
    )

    // Notas de ejemplo de la alumna; las asignaturas son las del plan de estudios del GII (mención CSI).
    val gradesMap = mapOf(
        "1º" to listOf(
            GradeItem("1º", "Fundamentos de Programación", "8,4"),
            GradeItem("1º", "Cálculo", "7,1"),
            GradeItem("1º", "Álgebra Lineal y Estructuras Matemáticas", "6,8"),
            GradeItem("1º", "Fundamentos del Software", "8,0"),
            GradeItem("1º", "Fundamentos Físicos y Tecnológicos", "7,6"),
            GradeItem("1º", "Metodología de la Programación", "8,9"),
            GradeItem("1º", "Estadística", "7,3"),
            GradeItem("1º", "Lógica y Métodos Discretos", "6,9")
        ),
        "2º" to listOf(
            GradeItem("2º", "Estructuras de Datos", "8,7"),
            GradeItem("2º", "Sistemas Operativos", "7,9"),
            GradeItem("2º", "Programación y Diseño Orientado a Objetos", "8,5"),
            GradeItem("2º", "Algorítmica", "7,8"),
            GradeItem("2º", "Fundamentos de Bases de Datos", "9,1"),
            GradeItem("2º", "Fundamentos de Ingeniería del Software", "8,2"),
            GradeItem("2º", "Inteligencia Artificial", "9,0")
        ),
        "3º" to listOf(
            GradeItem("3º", "Aprendizaje Automático", "8,8"),
            GradeItem("3º", "Metaheurísticas", "9,3"),
            GradeItem("3º", "Ingeniería del Conocimiento", "8,1"),
            GradeItem("3º", "Modelos de Computación", "7,7"),
            GradeItem("3º", "Técnicas de los Sistemas Inteligentes", "8,6")
        ),
        "4º" to FacultyData.studentSubjects.map { GradeItem("4º", it, "En curso") }
    )
}
