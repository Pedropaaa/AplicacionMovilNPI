package com.example.aplicacionmovilnpi.data

import java.util.Calendar

/**
 * Representa un instante específico dentro de la semana lectiva universitaria.
 *
 * @property semester Semestre lectivo: 1 (septiembre–enero) o 2 (febrero–agosto).
 * @property day Día de la semana (1 = lunes, 2 = martes, ..., 7 = domingo).
 * @property minutes Minutos transcurridos desde las 00:00 del día.
 */
data class Moment(val semester: Int, val day: Int, val minutes: Int) {
    companion object {
        /**
         * Obtiene el [Moment] correspondiente a la fecha y hora actuales del dispositivo.
         *
         * @param calendar Instancia opcional de [Calendar] (por defecto la fecha/hora actual del sistema).
         * @return Objeto [Moment] con el semestre, día de la semana y minutos del día calculados.
         */
        fun now(calendar: Calendar = Calendar.getInstance()): Moment {
            val month = calendar.get(Calendar.MONTH)
            val semester = if (month in Calendar.FEBRUARY..Calendar.AUGUST) 2 else 1
            // Calendar.DAY_OF_WEEK empieza en domingo = 1; lo normalizamos a lunes = 1 … domingo = 7.
            val day = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
            val minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
            return Moment(semester, day, minutes)
        }
    }
}

/**
 * Resultado de determinar la ubicación y actividad actual de un profesor en un instante dado.
 */
sealed interface Whereabouts {
    /**
     * Indica que el profesor ha sido localizado en una clase o tutoría activa.
     *
     * @property slot Franja horaria semanal [WeeklySlot] en la que se encuentra impartiendo clase o tutoría.
     */
    data class Located(val slot: WeeklySlot) : Whereabouts

    /**
     * Indica que no hay actividad registrada para el instante actual.
     *
     * @property next Próxima franja horaria conocida [WeeklySlot] del profesor en la semana, si existe.
     */
    data class Unknown(val next: WeeklySlot?) : Whereabouts
}

/**
 * Buscador y localizador en tiempo real de la posición y disponibilidad del profesorado.
 *
 * Determina si un profesor se encuentra impartiendo clase, atendiendo tutorías en su despacho o en reposo,
 * ofreciendo además el cálculo de la siguiente franja horaria lectiva.
 */
object ProfessorLocator {

    /**
     * Determina la ubicación o estado del profesor en un momento determinado.
     *
     * @param professor Profesor a consultar.
     * @param moment Instante semanal de consulta.
     * @return [Whereabouts.Located] si está en clase o tutoría, o [Whereabouts.Unknown] indicando la próxima franja.
     */
    fun locate(professor: Professor, moment: Moment): Whereabouts {
        // 1. Filtrar franjas horarias correspondientes al semestre y día de la semana actual
        val current = professor.schedule
            .filter { it.semester == moment.semester && it.day == moment.day }
            .filter { moment.minutes >= it.startMinutes && moment.minutes < it.endMinutes }
            // 2. En caso de solapamiento entre clase y tutoría, priorizar la clase lectiva
            .minByOrNull { if (it.kind == ActivityKind.CLASE) 0 else 1 }

        // 3. Retornar el resultado de localización o calcular la siguiente franja registrada
        return if (current != null) Whereabouts.Located(current)
        else Whereabouts.Unknown(nextSlot(professor.schedule, moment))
    }

    /**
     * Obtiene la siguiente franja horaria programada del profesor a partir de un instante dado.
     *
     * Si no quedan franjas en los días restantes de la semana, da la vuelta para encontrar la primera del lunes.
     *
     * @param schedule Horario completo del profesor.
     * @param moment Instante semanal actual.
     * @return La siguiente [WeeklySlot] programada, o `null` si no hay franjas en el semestre.
     */
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

    /**
     * Genera un resumen legible en texto de las tutorías de un profesor para el semestre indicado.
     *
     * @param professor Profesor a consultar.
     * @param semester Semestre a consultar (1 o 2).
     * @return Cadena de texto formateada con los días y horas de tutoría (ej: "Lunes 11:30–13:30 · Jueves 12:30–14:30").
     */
    fun officeHoursText(professor: Professor, semester: Int): String {
        val tutorias = professor.schedule
            .filter { it.semester == semester && it.kind == ActivityKind.TUTORIA }
            .sortedWith(compareBy({ it.day }, { it.startMinutes }))
        if (tutorias.isEmpty()) return "Sin tutorías publicadas este semestre"
        return tutorias.joinToString(" · ") { "${dayName(it.day)} ${it.start}–${it.end}" }
    }

    /**
     * Devuelve el nombre en español del día de la semana correspondiente al número dado.
     *
     * @param day Número de día de la semana (1 = Lunes, ..., 7 = Domingo).
     * @return Nombre del día en español.
     */
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
