package com.example.aplicacionmovilnpi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UgrColorScheme = lightColorScheme(
    primary = UgrRed,
    onPrimary = Color.White,
    primaryContainer = UgrRedSoft,
    onPrimaryContainer = UgrRedDark,
    secondary = Ink,
    onSecondary = Color.White,
    background = PageBg,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = SoftBg,
    onSurfaceVariant = MutedText,
    outline = CardBorder
)

@Composable
fun AplicacionMovilNPITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UgrColorScheme,
        typography = Typography,
        content = content
    )
}
