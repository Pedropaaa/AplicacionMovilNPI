package com.example.aplicacionmovilnpi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import com.example.aplicacionmovilnpi.location.ProximitySensor
import com.example.aplicacionmovilnpi.ui.LocalHighContrast
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.LocalLargeText
import com.example.aplicacionmovilnpi.ui.MainScreen
import com.example.aplicacionmovilnpi.ui.screens.LoginScreen
import com.example.aplicacionmovilnpi.ui.theme.AplicacionMovilNPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

            // Estado para almacenar el valor de proximidad
            var isNear by remember { mutableStateOf(false) }

            // Control de ciclo de vida para el sensor de proximidad
            DisposableEffect(context) {
                val sensor = ProximitySensor(context).apply {
                    onProximityChanged = { near ->
                        isNear = near
                        Log.d("MainActivity", "Estado de proximidad en UI: ${if (near) "CERCA" else "LEJOS"}")
                    }
                    start()
                }

                onDispose {
                    sensor.stop()
                }
            }

            val currentDensity = LocalDensity.current
            val adaptedDensity = remember(currentDensity, isLargeText) {
                Density(
                    density = currentDensity.density,
                    fontScale = if (isLargeText) currentDensity.fontScale * 1.25f else currentDensity.fontScale
                )
            }

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
