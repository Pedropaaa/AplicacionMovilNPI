package com.example.aplicacionmovilnpi.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** Idioma seleccionado para que todas las pantallas reaccionen al instante. */
val LocalIsEnglish = staticCompositionLocalOf { false }

private val courseTranslations = mapOf(
    "En curso" to "In progress",
    "Grado en Ingeniería Informática" to "BSc in Computer Engineering",
    "Catedrático" to "Professor",
    "Titular" to "Associate Professor",
    "Aula " to "Room ",
    "Planta baja" to "Ground floor",
    "Planta " to "Floor "
)

/** Traduce etiquetas generales de la interfaz conservando los nombres propios. */
fun localizedCourseText(text: String, isEnglish: Boolean): String {
    if (!isEnglish) return text
    return courseTranslations.entries
        .sortedByDescending { it.key.length }
        .fold(text) { result, (spanish, english) -> result.replace(spanish, english) }
}

fun localizedFloorName(floorCode: String, isEnglish: Boolean): String {
    if (!isEnglish) return floorCode
    return when (floorCode) {
        "PB" -> "GF"
        "S1" -> "B1"
        else -> floorCode.removePrefix("P").let { "F$it" }
    }
}

fun localizedDayName(day: String, isEnglish: Boolean): String {
    if (!isEnglish) return day
    return when (day.lowercase()) {
        "lunes" -> "Monday"
        "martes" -> "Tuesday"
        "miércoles" -> "Wednesday"
        "jueves" -> "Thursday"
        "viernes" -> "Friday"
        "sábado" -> "Saturday"
        "domingo" -> "Sunday"
        else -> day
    }
}
