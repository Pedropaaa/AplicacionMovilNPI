package com.example.aplicacionmovilnpi.data

import java.util.Calendar

/**
 * Proveedor de datos de prueba y elementos estáticos para las vistas de la aplicación.
 *
 * Suministra información sobre horarios semanales de la estudiante de prueba, asignaturas,
 * eventos del calendario (entregas y exámenes), notas de expediente y preguntas frecuentes (FAQs).
 */
object SampleData {

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

    /**
     * Resuelve el nombre completo de una asignatura a partir del título corto o código de la franja.
     *
     * @param slot Franja horaria semanal.
     * @return Título expandido con el nombre completo de la asignatura.
     */
    fun lessonName(slot: WeeklySlot): String {
        val code = slot.title.substringBefore(" · ")
        val full = subjectNames[code] ?: return slot.title
        return slot.title.replaceFirst(code, full)
    }

    /**
     * Convierte una franja semanal [WeeklySlot] a un modelo de vista de clase [ClassLesson].
     *
     * @param slot Franja horaria a convertir.
     * @return Objeto [ClassLesson] listo para ser renderizado en la interfaz de usuario.
     */
    fun toLesson(slot: WeeklySlot) = ClassLesson(
        time = slot.start.padStart(5, '0'),
        end = slot.end.padStart(5, '0'),
        name = lessonName(slot),
        room = slot.place,
        tone = FacultyData.toneFor(slot),
        poiId = slot.poiId
    )

    /**
     * Genera la lista de horarios diarios de lunes a viernes para la semana correspondiente a la fecha dada.
     *
     * @param today Instancia de [Calendar] indicando el día de referencia (por defecto el día actual).
     * @return Lista de 5 objetos [DaySchedule] con las clases ordenadas por hora de inicio.
     */
    fun weekSchedule(today: Calendar = Calendar.getInstance()): List<DaySchedule> {
        // 1. Obtener el semestre lectivo actual a partir de la fecha
        val semester = Moment.now(today).semester
        
        // 2. Calcular la fecha correspondiente al lunes de la semana en curso
        val monday = (today.clone() as Calendar).apply {
            val dayIndex = (get(Calendar.DAY_OF_WEEK) + 5) % 7 // lunes = 0 … domingo = 6
            add(Calendar.DAY_OF_MONTH, if (dayIndex >= 5) 7 - dayIndex else -dayIndex)
        }
        
        // 3. Mapear de lunes a viernes agregando la fecha del mes y filtrando las clases asignadas
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

    /**
     * Busca la próxima clase matriculada de la alumna (o la clase que se está impartiendo en el momento).
     *
     * @param moment Instante semanal de consulta (por defecto el momento actual).
     * @return La [WeeklySlot] correspondiente a la clase activa o siguiente, o `null` si no hay más clases.
     */
    fun nextLesson(moment: Moment = Moment.now()): WeeklySlot? {
        val ongoing = FacultyData.studentSchedule.firstOrNull {
            it.semester == moment.semester && it.day == moment.day &&
                moment.minutes >= it.startMinutes && moment.minutes < it.endMinutes
        }
        return ongoing ?: ProfessorLocator.nextSlot(FacultyData.studentSchedule, moment)
    }

    /** Lista completa de profesores cargados desde [FacultyData]. */
    val professors: List<Professor> get() = FacultyData.professors

    /** Lista inicial de entregas y eventos académicos para el calendario. */
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

    /** Lista inicial de mensajes de correo. */
    val initialMails = emptyList<MailMessage>()

    /** Preguntas frecuentes (FAQs) organizadas por categorías de uso. */
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

    /** Histórico de calificaciones de la estudiante organizado por cursos académicos. */
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
