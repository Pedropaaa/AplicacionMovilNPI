package com.example.aplicacionmovilnpi.data

import android.content.Context

object AccessibilityStore {
    private const val PREFS_NAME = "accessibility_prefs"
    private const val KEY_LARGE_TEXT = "large_text"
    private const val KEY_HIGH_CONTRAST = "high_contrast"
    private const val KEY_ACCESSIBLE_ROUTE = "accessible_route"

    fun isLargeText(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_LARGE_TEXT, false)
    }

    fun setLargeText(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_LARGE_TEXT, enabled).apply()
    }

    fun isHighContrast(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HIGH_CONTRAST, false)
    }

    fun setHighContrast(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }

    fun isDefaultAccessibleRoute(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ACCESSIBLE_ROUTE, false)
    }

    fun setDefaultAccessibleRoute(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ACCESSIBLE_ROUTE, enabled).apply()
    }
}
