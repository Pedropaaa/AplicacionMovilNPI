package com.example.aplicacionmovilnpi.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Idioma seleccionado para que todas las pantallas reaccionen al instante ante cambios de bilingüismo (Español / Inglés).
 */
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

/**
 * Traduce etiquetas generales de la interfaz conservando los nombres propios académicos.
 *
 * @param text Texto original en español.
 * @param isEnglish `true` si el idioma activo es inglés.
 * @return Cadena traducida o el texto original si `isEnglish` es `false`.
 */
fun localizedCourseText(text: String, isEnglish: Boolean): String {
    if (!isEnglish) return text
    return courseTranslations.entries
        .sortedByDescending { it.key.length }
        .fold(text) { result, (spanish, english) -> result.replace(spanish, english) }
}

/**
 * Traduce códigos de plantas del edificio al estándar internacional.
 *
 * @param floorCode Código en español ("PB", "S1", "P1", etc.).
 * @param isEnglish `true` si el idioma activo es inglés.
 * @return Código de planta traducido ("GF", "B1", "F1", etc.).
 */
fun localizedFloorName(floorCode: String, isEnglish: Boolean): String {
    if (!isEnglish) return floorCode
    return when (floorCode) {
        "PB" -> "GF"
        "S1" -> "B1"
        else -> floorCode.removePrefix("P").let { "F$it" }
    }
}

/**
 * Traduce nombres de días de la semana de español a inglés.
 *
 * @param day Nombre del día en español (ej: "Lunes", "Martes").
 * @param isEnglish `true` si el idioma activo es inglés.
 * @return Nombre del día traducido al inglés.
 */
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
