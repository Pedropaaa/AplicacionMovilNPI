package com.example.aplicacionmovilnpi.data

import android.content.Context

/**
 * Almacena de forma persistente la preferencia de idioma de la app (Español / Inglés) mediante SharedPreferences.
 */
object LanguageStore {

    private const val PREFS = "language_store"
    private const val KEY_ENGLISH = "is_english"

    /**
     * Comprueba si el idioma seleccionado activamente es el inglés.
     *
     * @param context Contexto de la aplicación Android.
     * @return `true` si el idioma guardado es inglés, `false` si es español.
     */
    fun isEnglish(context: Context): Boolean {
        // 1. Consultar en SharedPreferences la clave del estado de idioma
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENGLISH, false)
    }

    /**
     * Guarda la preferencia de idioma seleccionada por el usuario.
     *
     * @param context Contexto de la aplicación Android.
     * @param isEnglish `true` para establecer el idioma en inglés, `false` para español.
     */
    fun setEnglish(context: Context, isEnglish: Boolean) {
        // 1. Guardar y aplicar el nuevo valor de preferencia de idioma
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENGLISH, isEnglish)
            .apply()
    }
}
