package com.example.aplicacionmovilnpi.data

import android.content.Context

/**
 * Gestor de almacenamiento persistente en SharedPreferences para las preferencias de accesibilidad.
 *
 * Guarda y recupera los ajustes de texto grande, modo de alto contraste y preferencia de rutas accesibles (PMR).
 */
object AccessibilityStore {
    private const val PREFS_NAME = "accessibility_prefs"
    private const val KEY_LARGE_TEXT = "large_text"
    private const val KEY_HIGH_CONTRAST = "high_contrast"
    private const val KEY_ACCESSIBLE_ROUTE = "accessible_route"

    /**
     * Comprueba si el ajuste de texto grande está activado.
     *
     * @param context Contexto de la aplicación para acceder a las SharedPreferences.
     * @return `true` si la opción está activada, `false` en caso contrario.
     */
    fun isLargeText(context: Context): Boolean {
        // 1. Acceder al fichero de SharedPreferences de accesibilidad y leer el valor booleano
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_LARGE_TEXT, false)
    }

    /**
     * Guarda el estado del ajuste de texto grande.
     *
     * @param context Contexto de la aplicación.
     * @param enabled `true` para activar el texto grande, `false` para desactivarlo.
     */
    fun setLargeText(context: Context, enabled: Boolean) {
        // 1. Abrir el editor de SharedPreferences y persistir la preferencia
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_LARGE_TEXT, enabled).apply()
    }

    /**
     * Comprueba si el modo de alto contraste está activado.
     *
     * @param context Contexto de la aplicación.
     * @return `true` si el modo alto contraste está activo.
     */
    fun isHighContrast(context: Context): Boolean {
        // 1. Leer el valor persistido para alto contraste
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HIGH_CONTRAST, false)
    }

    /**
     * Establece el estado del modo de alto contraste.
     *
     * @param context Contexto de la aplicación.
     * @param enabled `true` para activar el modo de alto contraste.
     */
    fun setHighContrast(context: Context, enabled: Boolean) {
        // 1. Guardar la opción en SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }

    /**
     * Comprueba si la ruta accesible por defecto (sin escaleras/PMR) está activada.
     *
     * @param context Contexto de la aplicación.
     * @return `true` si la ruta accesible por defecto está seleccionada.
     */
    fun isDefaultAccessibleRoute(context: Context): Boolean {
        // 1. Obtener la preferencia de ruta accesible por defecto
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ACCESSIBLE_ROUTE, false)
    }

    /**
     * Guarda la preferencia de ruta accesible por defecto.
     *
     * @param context Contexto de la aplicación.
     * @param enabled `true` para establecer la preferencia accesible por defecto.
     */
    fun setDefaultAccessibleRoute(context: Context, enabled: Boolean) {
        // 1. Escribir y aplicar la preferencia en SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ACCESSIBLE_ROUTE, enabled).apply()
    }
}
