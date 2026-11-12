package com.example.aplicacionmovilnpi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.aplicacionmovilnpi.ui.LocalHighContrast

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

private val HighContrastColorScheme = lightColorScheme(
    primary = Color(0xFF900000),
    onPrimary = Color.White,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.White,
    secondary = Color.Black,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color.Black,
    outline = Color.Black
)

@Composable
fun AplicacionMovilNPITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = LocalHighContrast.current,
    content: @Composable () -> Unit
) {
    val colors = if (highContrast) HighContrastColorScheme else UgrColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
