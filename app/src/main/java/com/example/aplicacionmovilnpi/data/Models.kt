package com.example.aplicacionmovilnpi.data

data class ClassLesson(
    val time: String,
    val end: String,
    val name: String,
    val room: String,
    val tone: String
)

data class DaySchedule(
    val short: String,
    val day: String,
    val date: String,
    val classes: List<ClassLesson>
)

data class Professor(
    val id: Int,
    val initials: String,
    val name: String,
    val role: String,
    val email: String,
    val faculty: String,
    val subjects: List<String>,
    val officeHours: String,
    val status: String,
    val location: String,
    val floor: String,
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
