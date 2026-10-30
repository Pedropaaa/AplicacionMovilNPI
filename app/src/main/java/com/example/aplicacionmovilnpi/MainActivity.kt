package com.example.aplicacionmovilnpi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.aplicacionmovilnpi.data.SessionStore
import com.example.aplicacionmovilnpi.location.ProximitySensor
import com.example.aplicacionmovilnpi.ui.MainScreen
import com.example.aplicacionmovilnpi.ui.screens.LoginScreen
import com.example.aplicacionmovilnpi.ui.theme.AplicacionMovilNPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AplicacionMovilNPITheme {
                val context = LocalContext.current
                var isAuthenticated by rememberSaveable {
                    mutableStateOf(SessionStore.isLoggedIn(context))
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

                if (!isAuthenticated) {
                    LoginScreen(
                        onLoginSuccess = { rememberAccount ->
                            SessionStore.setLoggedIn(context, rememberAccount)
                            isAuthenticated = true
                        }
                    )
                } else {
                    MainScreen(
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