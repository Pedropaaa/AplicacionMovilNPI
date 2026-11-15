package com.example.aplicacionmovilnpi.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Proveedor de estado en árbol de Compose (CompositionLocal) para el ajuste de texto grande.
 * Cuando es `true`, aplica un factor multiplicador de 1.25x a la escala de fuentes en toda la aplicación.
 */
val LocalLargeText = staticCompositionLocalOf { false }

/**
 * Proveedor de estado en árbol de Compose (CompositionLocal) para el ajuste de alto contraste.
 * Cuando es `true`, activa la paleta de colores con contraste reinforced en `AplicacionMovilNPITheme`.
 */
val LocalHighContrast = staticCompositionLocalOf { false }
