package com.example.aplicacionmovilnpi.data

import android.content.Context

/**
 * Almacena de forma persistente la preferencia de idioma de la app (Español / Inglés).
 */
object LanguageStore {

    private const val PREFS = "language_store"
    private const val KEY_ENGLISH = "is_english"

    fun isEnglish(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENGLISH, false)

    fun setEnglish(context: Context, isEnglish: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENGLISH, isEnglish)
            .apply()
    }
}
