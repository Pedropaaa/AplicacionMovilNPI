
package com.example.aplicacionmovilnpi

import android.content.Context

import android.hardware.Sensor

import android.hardware.SensorEvent

import android.hardware.SensorEventListener

import android.hardware.SensorManager

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.DisposableEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp


// FUNCIÓN PRINCIPAL DEL SENSOR DE LUZ

@Composable
fun PantallaSensorLuz() {

    
    val context = LocalContext.current

    // OBTENER EL ADMINISTRADOR DE SENSORES
   
    val sensorManager = remember(context) {

        context.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    }


    
    // BUSCAR EL SENSOR DE LUZ

    val sensorLuz = remember(sensorManager) {

        sensorManager.getDefaultSensor(
            Sensor.TYPE_LIGHT
        )
    }

    //  CREAR LA VARIABLE DE LUMINOSIDAD

    var luminosidad by remember {
        mutableStateOf<Float?>(null)
    }

    // ESCUCHAR LOS CAMBIOS DEL SENSOR


    DisposableEffect(sensorManager, sensorLuz) {


        val listener = object : SensorEventListener {


            override fun onSensorChanged(
                event: SensorEvent
            ) {



                if (event.sensor.type == Sensor.TYPE_LIGHT) {

          
                    luminosidad = event.values[0]
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int
            ) {

            }
        }

        // ACTIVAR EL SENSOR

        if (sensorLuz != null) {



            sensorManager.registerListener(

                listener,

                sensorLuz,

                SensorManager.SENSOR_DELAY_NORMAL
            )
        }



        // DESACTIVAR EL SENSOR


        onDispose {

            sensorManager.unregisterListener(listener)
        }
    }

    //  INTERPRETAR LOS VALORES DE LUMINOSIDAD


    val estado = when {


        sensorLuz == null -> "Sensor no disponible"

 
        luminosidad == null -> "Esperando datos..."


        luminosidad < 50f -> "Poca luz"

        luminosidad < 500f -> "Luz normal"


        else -> "Mucha luz"
    }


    // MOSTRAR LOS DATOS EN PANTALLA



    Column(

        modifier = Modifier

     
            .fillMaxSize()

        
            .padding(24.dp),

   
        verticalArrangement = Arrangement.Center,

  
        horizontalAlignment = Alignment.CenterHorizontally

    ) {


        Text(
            text = "Testeo del sensor de luz",
            fontSize = 24.sp
        )



        Spacer(
            modifier = Modifier.height(24.dp)
        )



        Text(
            text = luminosidad?.let {
                "Luminosidad: $it lux"
            } ?: "Sin lectura de luminosidad",

            fontSize = 20.sp
        )


      
        Spacer(
            modifier = Modifier.height(16.dp)
        )


        

        Text(
            text = estado,
            fontSize = 20.sp
        )
    }
}

