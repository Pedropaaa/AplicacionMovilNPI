package com.example.aplicacionmovilnpi.data

data class ClassLesson(
    val time: String,
    val end: String,
    val name: String,
    val room: String,
    val tone: String,
    val poiId: String? = null // POI del plano para "Cómo llegar"; null si el aula no está en el plano
)

data class DaySchedule(
    val short: String,
    val day: String,
    val date: String,
    val month: String,
    val classes: List<ClassLesson>
)

enum class ActivityKind { CLASE, TUTORIA }

/**
 * Franja semanal fija de un profesor o de la alumna (una clase o una tutoría).
 *
 * @property semester 1 o 2.
 * @property day Día de la semana: 1 = lunes … 5 = viernes.
 * @property start Hora de inicio en formato "H:mm" (ej: "8:30").
 * @property place Texto visible del lugar (ej: "Aula 0.5", "Despacho 25 · planta 3").
 * @property poiId POI del plano al que guiar, o null si el lugar no está digitalizado.
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
    val startMinutes: Int get() = toMinutes(start)
    val endMinutes: Int get() = toMinutes(end)

    private fun toMinutes(time: String): Int {
        val (h, m) = time.split(":").map { it.toInt() }
        return h * 60 + m
    }
}

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

data class PlaceCategory(
    val name: String,
    val detail: String,
    val icon: String,
    val tone: String
)

data class CampusItem(
    val key: String,
    val name: String,
    val campus: String,
    val full: String,
    val distance: String,
    val hours: String
)

data class PointOfInterest(
    val name: String,
    val icon: String,
    val detail: String,
    val route: String
)

data class RouteStep(
    val icon: String,
    val text: String,
    val distance: String
)

data class CalendarEventItem(
    val id: Long,
    val title: String,
    val subject: String,
    val date: String,
    val time: String,
    val type: String, // "Entrega" or "Examen"
    val description: String
)

enum class MailFolder(val key: String, val label: String) {
    INBOX("inbox", "Recibidos"),
    SENT("sent", "Enviados"),
    ARCHIVED("archived", "Archivados")
}

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

data class FaqItem(
    val topic: String,
    val question: String,
    val answer: String
)

data class IncidenceTicket(
    val id: String,
    val topic: String,
    val subject: String,
    val description: String,
    val place: String,
    val status: String,
    val date: String
)

data class GradeItem(
    val course: String,
    val subject: String,
    val grade: String
)
