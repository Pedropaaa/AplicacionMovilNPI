package com.example.aplicacionmovilnpi.data

import android.content.Context

/**
 * Gestor de sesión persistente para la opción «Recordar cuenta» en la autenticación.
 *
 * Si la casilla no está marcada al iniciar sesión, la sesión expira cuando la aplicación finaliza.
 */
object SessionStore {

    private const val PREFS = "session"
    private const val KEY_LOGGED_IN = "logged_in"

    /**
     * Comprueba si existe una sesión de usuario autenticado activa.
     *
     * @param context Contexto de la aplicación Android.
     * @return `true` si el usuario se encuentra autenticado.
     */
    fun isLoggedIn(context: Context): Boolean {
        // 1. Leer el estado de inicio de sesión desde las SharedPreferences
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LOGGED_IN, false)
    }

    /**
     * Actualiza el estado de la sesión guardada.
     *
     * @param context Contexto de la aplicación Android.
     * @param loggedIn `true` para marcar al usuario como autenticado, `false` para cerrar sesión.
     */
    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        // 1. Persistir el nuevo estado de sesión del estudiante
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOGGED_IN, loggedIn)
            .apply()
    }
}
