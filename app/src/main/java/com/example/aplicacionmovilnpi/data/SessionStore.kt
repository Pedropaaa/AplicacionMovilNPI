package com.example.aplicacionmovilnpi.data

import android.content.Context

/**
 * Sesión persistente para la opción «Recordar cuenta» del login.
 * Si no se marca, la sesión solo dura mientras la app siga abierta.
 */
object SessionStore {

    private const val PREFS = "session"
    private const val KEY_LOGGED_IN = "logged_in"

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOGGED_IN, loggedIn)
            .apply()
    }
}
