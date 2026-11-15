package com.example.aplicacionmovilnpi.data

/**
 * Representa una clase o lección dentro del horario diario de un estudiante.
 *
 * @property time Hora de inicio de la clase (formato HH:mm).
 * @property end Hora de finalización de la clase (formato HH:mm).
 * @property name Nombre o título de la asignatura o sesión formativa.
 * @property room Aula o espacio físico donde se imparte (ej: "Aula 0.5").
 * @property tone Identificador de color/tono visual asignado para la tarjeta en la interfaz.
 * @property poiId Identificador del punto de interés en el plano para la navegación indoor (null si no está localizado).
 */
data class ClassLesson(
    val time: String,
    val end: String,
    val name: String,
    val room: String,
    val tone: String,
    val poiId: String? = null
)

/**
 * Representa la programación académica completa de un día concreto.
 *
 * @property short Nombre abreviado del día de la semana (ej: "LUN").
 * @property day Nombre completo del día de la semana (ej: "Lunes").
 * @property date Día numérico del mes en formato texto (ej: "13").
 * @property month Nombre abreviado del mes (ej: "OCT").
 * @property classes Lista de lecciones impartidas durante la jornada.
 */
data class DaySchedule(
    val short: String,
    val day: String,
    val date: String,
    val month: String,
    val classes: List<ClassLesson>
)

/**
 * Tipo de actividad docente o tutorial.
 */
enum class ActivityKind {
    /** Clase magistral o práctica ordinaria */
    CLASE,
    /** Horario de tutorías de atención al alumnado */
    TUTORIA
}

/**
 * Franja semanal fija de un profesor o de la alumna (una clase o una tutoría).
 *
 * @property semester Cuatrimestre o semestre académico (1 o 2).
 * @property day Día de la semana en formato numérico: 1 = Lunes … 5 = Viernes.
 * @property start Hora de inicio en formato "H:mm" (ej: "8:30").
 * @property end Hora de finalización en formato "H:mm" (ej: "10:30").
 * @property kind Tipo de actividad programada ([ActivityKind.CLASE] o [ActivityKind.TUTORIA]).
 * @property title Nombre de la asignatura o título de la tutoría.
 * @property place Texto descriptivo de la ubicación (ej: "Aula 0.5", "Despacho 25 · planta 3").
 * @property poiId Identificador del punto de interés en el plano indoor, o null si no está digitalizado.
 */
data class WeeklySlot(
    val semester: Int,
    val day: Int,
    val start: String,
    val end: String,
    val kind: ActivityKind,
    val title: String,
    val place: String,
    val poiId: String? = null
) {
    /** Minutos desde el inicio del día para la hora de comienzo */
    val startMinutes: Int get() = toMinutes(start)

    /** Minutos desde el inicio del día para la hora de finalización */
    val endMinutes: Int get() = toMinutes(end)

    /**
     * Convierte una cadena de texto en formato "H:mm" a minutos totales desde las 00:00.
     *
     * @param time Hora en formato "H:mm".
     * @return Número total de minutos transcurridos en el día.
     */
    private fun toMinutes(time: String): Int {
        // 1. Divide la cadena por los dos puntos para separar hora y minutos
        val (h, m) = time.split(":").map { it.toInt() }
        // 2. Multiplica las horas por 60 y suma los minutos resultantes
        return h * 60 + m
    }
}

/**
 * Información completa y perfil docente de un profesor de la facultad.
 *
 * @property id Identificador único del profesor.
 * @property initials Iniciales del profesor para avatares y miniaturas.
 * @property name Nombre y apellidos completos del docente.
 * @property role Cargo o categoría académica (ej: "Catedrático de Universidad").
 * @property department Departamento académico al que pertenece.
 * @property email Dirección de correo electrónico institucional.
 * @property phone Número de teléfono o extensión de despacho.
 * @property office Descripción física del despacho (ej: "Despacho 25 · Planta 3").
 * @property officePoiId Identificador del despacho en el mapa indoor.
 * @property subjects Lista de asignaturas que imparte en el curso.
 * @property schedule Lista de franjas semanales fijas de docencia y tutorías.
 * @property note Nota o aclaración adicional sobre la disponibilidad del profesor.
 * @property tone Tono de color asignado a la tarjeta del docente.
 */
data class Professor(
    val id: Int,
    val initials: String,
    val name: String,
    val role: String,
    val department: String,
    val email: String,
    val phone: String,
    val office: String,
    val officePoiId: String?,
    val subjects: List<String>,
    val schedule: List<WeeklySlot>,
    val note: String? = null,
    val tone: String
)

/**
 * Categoría de lugares o puntos de interés dentro del recinto o centro.
 *
 * @property name Nombre descriptivo de la categoría (ej: "Cafetería y Comedor").
 * @property detail Descripción abreviada del servicio.
 * @property icon Nombre o identificador del icono representativo.
 * @property tone Tono de color asociado a la categoría.
 */
data class PlaceCategory(
    val name: String,
    val detail: String,
    val icon: String,
    val tone: String
)

/**
 * Representa una instalación o espacio relevante dentro del campus universitario.
 *
 * @property key Clave de identificación del elemento.
 * @property name Nombre abreviado del recinto o edificio.
 * @property campus Nombre del campus universitario donde se sitúa.
 * @property full Denominación completa del edificio o recinto.
 * @property distance Distancia estimada o tiempo de desplazamiento desde la ubicación actual.
 * @property hours Horario de apertura y servicio del recinto.
 */
data class CampusItem(
    val key: String,
    val name: String,
    val campus: String,
    val full: String,
    val distance: String,
    val hours: String
)

/**
 * Evento agendado en el calendario académico del alumno (entregas de trabajos, exámenes, etc.).
 *
 * @property id Identificador único del evento.
 * @property title Título o descripción corta de la entrega o examen.
 * @property subject Asignatura asociada al evento.
 * @property date Fecha programada en formato AAAA-MM-DD.
 * @property time Hora límite o de inicio del evento.
 * @property type Tipo de hito académico ("Entrega" o "Examen").
 * @property description Detalles ampliados e instrucciones del evento.
 */
data class CalendarEventItem(
    val id: Long,
    val title: String,
    val subject: String,
    val date: String,
    val time: String,
    val type: String,
    val description: String
)

/**
 * Carpetas principales del sistema de mensajería e incido institucional.
 *
 * @property key Identificador clave de la carpeta.
 * @property label Etiqueta traducible visible en la interfaz.
 */
enum class MailFolder(val key: String, val label: String) {
    /** Bandeja de entrada con correos recibidos */
    INBOX("inbox", "Recibidos"),
    /** Carpeta de mensajes enviados por el usuario */
    SENT("sent", "Enviados"),
    /** Mensajes archivados para consulta posterior */
    ARCHIVED("archived", "Archivados")
}

/**
 * Mensaje de correo o aviso del sistema de comunicación de la UGR.
 *
 * @property id Identificador único del mensaje.
 * @property folder Carpeta a la que pertenece el correo ([MailFolder]).
 * @property outgoing Indica si es un mensaje enviado por el usuario actual (`true`) o recibido (`false`).
 * @property name Nombre del remitente o destinatario.
 * @property email Correo electrónico de contacto.
 * @property subject Asunto del correo electrónico.
 * @property body Cuerpo de texto o contenido del mensaje.
 * @property time Hora o fecha relativa del envío.
 * @property unread Estado de lectura del mensaje (`true` si no ha sido leído).
 */
data class MailMessage(
    val id: Long,
    val folder: MailFolder,
    val outgoing: Boolean,
    val name: String,
    val email: String,
    val subject: String,
    val body: String,
    val time: String,
    val unread: Boolean
)

/**
 * Elemento de pregunta frecuente (FAQ) para el centro de ayuda.
 *
 * @property topic Tema o área temática de la consulta.
 * @property question Enunciado de la pregunta planteada por los usuarios.
 * @property answer Respuesta explicativa detallada.
 */
data class FaqItem(
    val topic: String,
    val question: String,
    val answer: String
)

/**
 * Elemento del expediente académico correspondiente a la calificación de una asignatura.
 *
 * @property course Grado o título universitario al que pertenece la asignatura.
 * @property subject Nombre de la asignatura evaluada.
 * @property grade Calificación o nota obtenida en el acta.
 */
data class GradeItem(
    val course: String,
    val subject: String,
    val grade: String
)
