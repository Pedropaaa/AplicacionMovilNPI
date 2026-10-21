package com.example.aplicacionmovilnpi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.aplicacionmovilnpi.data.SessionStore
import com.example.aplicacionmovilnpi.ui.MainScreen
import com.example.aplicacionmovilnpi.ui.screens.LoginScreen
import com.example.aplicacionmovilnpi.ui.theme.AplicacionMovilNPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AplicacionMovilNPITheme {
                // rememberSaveable: la sesión sobrevive a que Android recree la actividad
                // (girar la pantalla, modo oscuro, falta de memoria en segundo plano...)
                var isAuthenticated by rememberSaveable { mutableStateOf(SessionStore.isLoggedIn(this)) }

                if (!isAuthenticated) {
                    LoginScreen(
                        onLoginSuccess = { rememberAccount ->
                            SessionStore.setLoggedIn(this, rememberAccount)
                            isAuthenticated = true
                        }
                    )
                } else {
                    MainScreen(
                        onLogout = {
                            SessionStore.setLoggedIn(this, false)
                            isAuthenticated = false
                        }
                    )
                }
            }
        }
    }
}
