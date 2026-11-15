package com.example.aplicacionmovilnpi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.aplicacionmovilnpi.data.AccessibilityStore
import com.example.aplicacionmovilnpi.data.LanguageStore
import com.example.aplicacionmovilnpi.data.SessionStore
import com.example.aplicacionmovilnpi.ui.LocalHighContrast
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.LocalLargeText
import com.example.aplicacionmovilnpi.ui.MainScreen
import com.example.aplicacionmovilnpi.ui.screens.LoginScreen
import com.example.aplicacionmovilnpi.ui.theme.AplicacionMovilNPITheme

/**
 * Actividad principal de la aplicación móvil NPI para la Escuela Técnica Superior de
 * Ingenierías Informática y de Telecomunicación (ETSIIT) de la Universidad de Granada.
 *
 * Gestiona el ciclo de vida principal de la aplicación, el estado global de autenticación,
 * las preferencias de accesibilidad e idioma.
 */
class MainActivity : ComponentActivity() {

    /**
     * Punto de entrada de la actividad. Configura el diseño edge-to-edge, inicializa
     * los proveedores de estado local (Density, Idioma, Accesibilidad) y renderiza
     * la UI principal o la pantalla de inicio de sesión según el estado de autenticación.
     *
     * @param savedInstanceState Estado guardado de la instancia previa de la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Activar renderizado Edge-to-Edge para aprovechar toda la pantalla
        enableEdgeToEdge()

        setContent {
            // 2. Obtener contexto de Compose e inicializar estados persistentes de sesión y configuración
            val context = LocalContext.current
            var isAuthenticated by rememberSaveable {
                mutableStateOf(SessionStore.isLoggedIn(context))
            }
            var isEnglish by rememberSaveable {
                mutableStateOf(LanguageStore.isEnglish(context))
            }
            var isLargeText by rememberSaveable {
                mutableStateOf(AccessibilityStore.isLargeText(context))
            }
            var isHighContrast by rememberSaveable {
                mutableStateOf(AccessibilityStore.isHighContrast(context))
            }
            var isDefaultAccessibleRoute by rememberSaveable {
                mutableStateOf(AccessibilityStore.isDefaultAccessibleRoute(context))
            }

            val currentDensity = LocalDensity.current
            val adaptedDensity = remember(currentDensity, isLargeText) {
                Density(
                    density = currentDensity.density,
                    fontScale = if (isLargeText) currentDensity.fontScale * 1.25f else currentDensity.fontScale
                )
            }

            // 3. Inyección de variables globales mediante CompositionLocalProvider y renderizado de la UI
            CompositionLocalProvider(
                LocalDensity provides adaptedDensity,
                LocalIsEnglish provides isEnglish,
                LocalLargeText provides isLargeText,
                LocalHighContrast provides isHighContrast
            ) {
                AplicacionMovilNPITheme(highContrast = isHighContrast) {
                    if (!isAuthenticated) {
                        LoginScreen(
                            onLoginSuccess = { rememberAccount ->
                                SessionStore.setLoggedIn(context, rememberAccount)
                                isAuthenticated = true
                            }
                        )
                    } else {
                        MainScreen(
                            onLanguageChange = { selected ->
                                LanguageStore.setEnglish(context, selected)
                                isEnglish = selected
                            },
                            isLargeText = isLargeText,
                            onLargeTextChange = { enabled ->
                                AccessibilityStore.setLargeText(context, enabled)
                                isLargeText = enabled
                            },
                            isHighContrast = isHighContrast,
                            onHighContrastChange = { enabled ->
                                AccessibilityStore.setHighContrast(context, enabled)
                                isHighContrast = enabled
                            },
                            defaultAccessibleRoute = isDefaultAccessibleRoute,
                            onDefaultAccessibleRouteChange = { enabled ->
                                AccessibilityStore.setDefaultAccessibleRoute(context, enabled)
                                isDefaultAccessibleRoute = enabled
                            },
                            onLogout = {
                                SessionStore.setLoggedIn(context, false)
                                isAuthenticated = false
                            }
                        )
                    }
                }
            }
        }
    }
}
