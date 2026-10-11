
package com.example.aplicacionmovilnpi


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp




@Composable
fun PantallaSensorProximidad() {

    // Obtenemos el contexto de la aplicación
    val context = LocalContext.current

 

    // SensorManager permite acceder a los sensores
    // disponibles en nuestro dispositivo Android.

    val sensorManager = remember(context) {
        context.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    }

  

    // Buscamos el sensor de proximidad.
    // Si el dispositivo no lo tiene, devuelve null.

    val sensorProximidad = remember(sensorManager) {
        sensorManager.getDefaultSensor(
            Sensor.TYPE_PROXIMITY
        )
    }

    

    // Almacenamos la lectura recibida del sensor.
    // Al principio vale null porque no hay datos.
    //
    // mutableStateOf permite actualizar la pantalla
    // automáticamente cuando cambia el valor.

    var distancia by remember {
        mutableStateOf<Float?>(null)
    }

 

    DisposableEffect(sensorManager, sensorProximidad) {

        // Creamos un listener que recibe los datos
        // enviados por el sensor de proximidad.

        val listener = object : SensorEventListener {

            // Se ejecuta cuando Android proporciona
            // una nueva lectura del sensor.

            override fun onSensorChanged(
                event: SensorEvent
            ) {

                // Comprobamos que el evento corresponde
                // al sensor de proximidad.

                if (event.sensor.type == Sensor.TYPE_PROXIMITY) {

                    // Guardamos la lectura del sensor.
                    // Normalmente se expresa en cm.
                    // Algunos móviles solo distinguen
                    // entre cerca y lejos.

                    distancia = event.values[0]
                }
            }

            // Se ejecuta si cambia la precisión
            // informada por el sensor.

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int
            ) {
                // No necesitamos realizar ninguna acción.
            }
        }

       

        // Comprobamos que el móvil dispone
        // del sensor de proximidad.

        if (sensorProximidad != null) {

            // Registramos el listener para empezar
            // a recibir las mediciones.

            sensorManager.registerListener(
                listener,
                sensorProximidad,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

       

        // Cuando la pantalla deja de utilizarse,
        // eliminamos el listener para evitar
        // mantener una escucha innecesaria.

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

   

    // Muchos sensores de proximidad son binarios:
    // indican si hay un objeto cerca o lejos.
    //
    // maximumRange indica el alcance máximo
    // comunicado por el sensor.
    //
    // Una lectura inferior al máximo suele
    // indicar que hay un objeto cercano.

    val estado = when {

        // El móvil no dispone del sensor.
        sensorProximidad == null ->
            "Sensor no disponible"

        // Todavía no hemos recibido datos.
        distancia == null ->
            "Esperando datos..."

        // Hay un objeto cerca del sensor.
        distancia < sensorProximidad.maximumRange ->
            "Objeto cercano"

        // No se detecta un objeto cercano.
        else ->
            "Objeto alejado"
    }


    // Column coloca los elementos verticalmente.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Título de nuestra pantalla.
        Text(
            text = "Testeo del sensor de proximidad",
            fontSize = 24.sp
        )

        // Espacio entre los textos.
        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // Mostramos el valor recibido.
        Text(
            text = distancia?.let {
                "Distancia: $it cm"
            } ?: "Sin lectura de proximidad",

            fontSize = 20.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Mostramos si el objeto está cerca o lejos.
        Text(
            text = estado,
            fontSize = 20.sp
        )
    }
}

