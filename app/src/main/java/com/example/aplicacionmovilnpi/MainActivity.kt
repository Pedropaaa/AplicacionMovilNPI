
package com.example.aplicacionmovilnpi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.aplicacionmovilnpi.ui.theme.AplicacionMovilNPITheme


// Actividad principal de la aplicación.
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AplicacionMovilNPITheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    // Mostramos el menú de pruebas.
                    MenuPruebasSensores(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


// Menú temporal para probar los dos sensores.
@Composable
fun MenuPruebasSensores(
    modifier: Modifier = Modifier
) {

    // Guarda la pantalla que estamos mostrando.
    var pantalla by remember {
        mutableStateOf("menu")
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        // Botón para volver cuando estamos
        // dentro de la pantalla de un sensor.
        if (pantalla != "menu") {

            Button(
                onClick = {
                    pantalla = "menu"
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Volver al menú")
            }
        }

        // Elegimos qué pantalla mostrar.
        when (pantalla) {

            "luz" -> {

                // Función definida en SensorLuz.kt.
                PantallaSensorLuz()
            }

            "proximidad" -> {

                // Función definida en SensorProximidad.kt.
                PantallaSensorProximidad()
            }

            else -> {

                // Pantalla inicial con los dos botones.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Testeo de sensores",
                        fontSize = 26.sp
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Button(
                        onClick = {
                            pantalla = "luz"
                        }
                    ) {
                        Text("Probar sensor de luz")
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Button(
                        onClick = {
                            pantalla = "proximidad"
                        }
                    ) {
                        Text("Probar sensor de proximidad")
                    }
                }
            }
        }
    }
}
