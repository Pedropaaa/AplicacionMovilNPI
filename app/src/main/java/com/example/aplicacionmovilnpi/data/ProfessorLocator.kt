package com.example.aplicacionmovilnpi.data

import java.util.Calendar

/**
 * Instante de la semana lectiva.
 *
 * @property semester 1 (septiembre–enero) o 2 (febrero–agosto).
 * @property day 1 = lunes … 7 = domingo.
 * @property minutes Minutos desde las 00:00.
 */
data class Moment(val semester: Int, val day: Int, val minutes: Int) {
    companion object {
        fun now(calendar: Calendar = Calendar.getInstance()): Moment {
            val month = calendar.get(Calendar.MONTH)
            val semester = if (month in Calendar.FEBRUARY..Calendar.AUGUST) 2 else 1
            // Calendar.DAY_OF_WEEK empieza en domingo = 1; lo pasamos a lunes = 1 … domingo = 7.
            val day = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
            val minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
            return Moment(semester, day, minutes)
        }
    }
}

/** Resultado de "¿dónde está ahora este profesor?". */
sealed interface Whereabouts {
    /** Está en clase o en tutoría en [slot]. */
    data class Located(val slot: WeeklySlot) : Whereabouts

    /** No hay información para este momento; [next] es la siguiente franja conocida (si la hay). */
    data class Unknown(val next: WeeklySlot?) : Whereabouts
}

object ProfessorLocator {

    fun locate(professor: Professor, moment: Moment): Whereabouts {
        val current = professor.schedule
            .filter { it.semester == moment.semester && it.day == moment.day }
            .filter { moment.minutes >= it.startMinutes && moment.minutes < it.endMinutes }
            // Si una clase se solapa con una tutoría, manda la clase.
            .minByOrNull { if (it.kind == ActivityKind.CLASE) 0 else 1 }

        return if (current != null) Whereabouts.Located(current)
        else Whereabouts.Unknown(nextSlot(professor.schedule, moment))
    }

    /** Siguiente franja del mismo semestre a partir de [moment], dando la vuelta a la semana. */
    fun nextSlot(schedule: List<WeeklySlot>, moment: Moment): WeeklySlot? {
        val weekMinutes = 7 * 24 * 60
        val nowInWeek = (moment.day - 1) * 24 * 60 + moment.minutes
        return schedule
            .filter { it.semester == moment.semester }
            .minByOrNull {
                val startInWeek = (it.day - 1) * 24 * 60 + it.startMinutes
                Math.floorMod(startInWeek - nowInWeek, weekMinutes)
            }
    }

    /** Tutorías del semestre en texto, ej: "Lunes 11:30–13:30 · Jueves 12:30–14:30". */
    fun officeHoursText(professor: Professor, semester: Int): String {
        val tutorias = professor.schedule
            .filter { it.semester == semester && it.kind == ActivityKind.TUTORIA }
            .sortedWith(compareBy({ it.day }, { it.startMinutes }))
        if (tutorias.isEmpty()) return "Sin tutorías publicadas este semestre"
        return tutorias.joinToString(" · ") { "${dayName(it.day)} ${it.start}–${it.end}" }
    }

    fun dayName(day: Int): String = when (day) {
        1 -> "Lunes"
        2 -> "Martes"
        3 -> "Miércoles"
        4 -> "Jueves"
        5 -> "Viernes"
        6 -> "Sábado"
        else -> "Domingo"
    }
}
