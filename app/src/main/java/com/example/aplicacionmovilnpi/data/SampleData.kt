package com.example.aplicacionmovilnpi.data

object SampleData {

    val places = listOf(
        PlaceCategory("Aulas", "Todas las plantas", "building", "rose"),
        PlaceCategory("Despachos", "Profesorado", "person", "purple"),
        PlaceCategory("Biblioteca", "Planta 1", "book", "blue"),
        PlaceCategory("Cafetería", "Planta baja", "coffee", "orange")
    )

    val weekSchedule = listOf(
        DaySchedule(
            short = "L",
            day = "Lunes",
            date = "14",
            classes = listOf(
                ClassLesson("08:30", "10:30", "Programación Web", "Aula A-03", "red"),
                ClassLesson("10:30", "12:30", "Diseño de Interfaces", "Aula B-12", "purple"),
                ClassLesson("15:30", "17:30", "Sistemas Multimedia", "Lab. 2", "blue")
            )
        ),
        DaySchedule(
            short = "M",
            day = "Martes",
            date = "15",
            classes = listOf(
                ClassLesson("08:30", "10:30", "Bases de Datos", "Aula C-05", "orange"),
                ClassLesson("12:30", "14:30", "Ingeniería del Software", "Aula B-08", "blue"),
                ClassLesson("17:30", "19:30", "Prácticas de Interfaces", "Lab. 4", "purple")
            )
        ),
        DaySchedule(
            short = "X",
            day = "Miércoles",
            date = "16",
            classes = listOf(
                ClassLesson("10:30", "12:30", "Diseño de Interfaces", "Aula B-12", "purple"),
                ClassLesson("15:30", "17:30", "Programación Web", "Lab. 1", "red"),
                ClassLesson("19:30", "21:30", "Sistemas Multimedia", "Aula A-10", "blue")
            )
        ),
        DaySchedule(
            short = "J",
            day = "Jueves",
            date = "17",
            classes = listOf(
                ClassLesson("08:30", "10:30", "Ingeniería del Software", "Aula B-08", "blue"),
                ClassLesson("12:30", "14:30", "Bases de Datos", "Lab. 3", "orange"),
                ClassLesson("17:30", "19:30", "Seminario de Diseño", "Aula C-02", "green")
            )
        ),
        DaySchedule(
            short = "V",
            day = "Viernes",
            date = "18",
            classes = listOf(
                ClassLesson("10:30", "12:30", "Prácticas de Interfaces", "Lab. 4", "purple"),
                ClassLesson("12:30", "14:30", "Programación Web", "Aula A-03", "red")
            )
        )
    )

    val professors = listOf(
        Professor(
            id = 1,
            initials = "AM",
            name = "Ana Martínez López",
            role = "Profesora Titular",
            email = "anamartinez@ugr.es",
            faculty = "E.T.S. de Ingenierías Informática y de Telecomunicación",
            subjects = listOf("Diseño de Interfaces", "Sistemas Multimedia"),
            officeHours = "Martes y jueves · 12:30–14:30",
            status = "Impartiendo clase",
            location = "Aula B-12",
            floor = "Planta 1",
            tone = "red"
        ),
        Professor(
            id = 2,
            initials = "JR",
            name = "Javier Ruiz Ortega",
            role = "Profesor Contratado Doctor",
            email = "jruiz@ugr.es",
            faculty = "Facultad de Ciencias",
            subjects = listOf("Programación Web"),
            officeHours = "Lunes y miércoles · 17:30–19:30",
            status = "Horario de tutorías",
            location = "Despacho 2.14",
            floor = "Planta 2",
            tone = "blue"
        ),
        Professor(
            id = 3,
            initials = "EC",
            name = "Elena Castillo Pérez",
            role = "Catedrática de Universidad",
            email = "ecastillo@ugr.es",
            faculty = "E.T.S. de Ingenierías Informática y de Telecomunicación",
            subjects = listOf("Bases de Datos"),
            officeHours = "Miércoles · 10:30–13:30",
            status = "Sin actividad programada",
            location = "Despacho 3.08",
            floor = "Planta 3",
            tone = "purple"
        ),
        Professor(
            id = 4,
            initials = "DL",
            name = "David López Sánchez",
            role = "Profesor Ayudante Doctor",
            email = "dlopez@ugr.es",
            faculty = "E.T.S. de Ingenierías Informática y de Telecomunicación",
            subjects = listOf("Ingeniería del Software"),
            officeHours = "Viernes · 08:30–11:30",
            status = "Impartiendo clase",
            location = "Aula B-08",
            floor = "Planta 1",
            tone = "orange"
        )
    )

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

    val initialEvents = listOf(
        CalendarEventItem(
            id = 1L,
            title = "Prototipo de Interfaces",
            subject = "Diseño de Interfaces",
            date = "2024-10-18",
            time = "23:59",
            type = "Entrega",
            description = "Prototipo navegable en Figma y memoria de diseño."
        ),
        CalendarEventItem(
            id = 2L,
            title = "Examen de Bases de Datos",
            subject = "Bases de Datos",
            date = "2024-10-24",
            time = "10:30",
            type = "Examen",
            description = "Temas 1 a 4. Aula C-05."
        ),
        CalendarEventItem(
            id = 3L,
            title = "Práctica de Programación Web",
            subject = "Programación Web",
            date = "2024-10-29",
            time = "23:59",
            type = "Entrega",
            description = "Práctica 3 en la plataforma."
        )
    )

    val initialMails = listOf(
        MailMessage(
            id = 1L,
            folder = MailFolder.INBOX,
            outgoing = false,
            name = "Ana Martínez",
            email = "anamartinez@ugr.es",
            subject = "Cambio de aula — Diseño de Interfaces",
            body = "Hola a todos:\n\nLa clase del miércoles se impartirá finalmente en el aula B-12, en la planta 1, en lugar del aula habitual.\n\nRecordad traer el prototipo avanzado para la revisión en grupo.\n\nUn saludo,\nAna",
            time = "10:24",
            unread = true
        ),
        MailMessage(
            id = 2L,
            folder = MailFolder.INBOX,
            outgoing = false,
            name = "Secretaría ETSIIT",
            email = "secretariaetsiit@ugr.es",
            subject = "Matrícula del curso 2024/2025",
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
            body = "El ejemplar que reservaste, «Diseño de interfaces de usuario», ya está disponible para su recogida en el mostrador de préstamo de la Biblioteca de la ETSIIT.\n\nTienes 3 días para recogerlo.",
            time = "Ayer",
            unread = true
        ),
        MailMessage(
            id = 4L,
            folder = MailFolder.INBOX,
            outgoing = false,
            name = "Javier Ruiz",
            email = "jruiz@ugr.es",
            subject = "Material de Programación Web",
            body = "Buenas tardes:\n\nYa tenéis publicados los ejemplos de la práctica 3 en la plataforma. Revisadlos antes de la próxima sesión de laboratorio.\n\nJavier",
            time = "14 oct.",
            unread = false
        ),
        MailMessage(
            id = 5L,
            folder = MailFolder.SENT,
            outgoing = true,
            name = "Elena Castillo Pérez",
            email = "ecastillo@ugr.es",
            subject = "Consulta sobre la práctica 2",
            body = "Hola, Elena:\n\n¿Podría pasar el miércoles por tutoría para resolver unas dudas sobre la normalización de la práctica 2?\n\nMuchas gracias,\nLucía",
            time = "13 oct.",
            unread = false
        ),
        MailMessage(
            id = 6L,
            folder = MailFolder.ARCHIVED,
            outgoing = false,
            name = "Secretaría ETSIIT",
            email = "secretariaetsiit@ugr.es",
            subject = "Bienvenida al curso 2024/2025",
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

    val gradesMap = mapOf(
        "1º" to listOf(
            GradeItem("1º", "Fundamentos de Programación", "8,4"),
            GradeItem("1º", "Cálculo", "7,1"),
            GradeItem("1º", "Álgebra Lineal", "6,8"),
            GradeItem("1º", "Tecnología de Computadores", "8,9"),
            GradeItem("1º", "Fundamentos Físicos", "7,6")
        ),
        "2º" to listOf(
            GradeItem("2º", "Estructuras de Datos", "8,7"),
            GradeItem("2º", "Sistemas Operativos", "7,9"),
            GradeItem("2º", "Bases de Datos", "9,1"),
            GradeItem("2º", "Redes de Computadores", "7,4"),
            GradeItem("2º", "Ingeniería del Software", "8,2")
        ),
        "3º" to listOf(
            GradeItem("3º", "Programación Web", "8,8"),
            GradeItem("3º", "Diseño de Interfaces", "9,3"),
            GradeItem("3º", "Sistemas Multimedia", "7,7")
        )
    )
}
