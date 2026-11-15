package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.localizedCourseText
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseBg
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

/**
 * Muestra el perfil académico y de contacto de la estudiante autenticada.
 *
 * Presenta un resumen estructurado con datos del centro, titulación, asignaturas
 * matriculadas y canales de contacto institucional.
 *
 * @param onBack Callback de navegación para regresar a la pantalla anterior (Más opciones).
 */
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    // 1. Obtener idioma activo desde el proveedor de composición local
    val isEnglish = LocalIsEnglish.current

    // 2. Estructura principal con soporte de desplazamiento vertical
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 3. Botón de retroceso a la pantalla previa
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isEnglish) "⬅ Back to More" else "⬅ Volver a Más",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UgrRed,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Encabezado de la pantalla
        Text(
            text = if (isEnglish) "Student Profile" else "Perfil de Estudiante",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = if (isEnglish) "Academic and contact details" else "Datos académicos y de contacto",
            fontSize = 12.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Tarjeta principal con el avatar y datos del estudiante
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circular con iniciales
                Surface(
                    color = ToneRoseBg,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = FacultyData.Student.initials,
                            color = ToneRoseText,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nombre completo
                Text(
                    text = FacultyData.Student.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                // Titulación actual
                Text(
                    text = localizedCourseText(FacultyData.Student.degree, isEnglish),
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Filas detalladas de información académica y de contacto
                ProfileDetailRow(
                    label = if (isEnglish) "School" else "Centro",
                    value = if (isEnglish) "School of Computer and Telecommunication Engineering" else "E.T.S. de Ingenierías Informática y de Telecomunicación"
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProfileDetailRow(
                    label = if (isEnglish) "Subjects" else "Asignaturas",
                    value = FacultyData.studentSubjects.joinToString(", ")
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProfileDetailRow(
                    label = if (isEnglish) "UGR email" else "Correo UGR",
                    value = FacultyData.Student.email
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProfileDetailRow(
                    label = if (isEnglish) "Phone" else "Teléfono",
                    value = "+34 600 000 000"
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * Componente auxiliar para mostrar un par campo-valor con formato consistente dentro del perfil.
 *
 * @param label Etiqueta o título del campo descriptivo (ej: "Correo UGR").
 * @param value Valor textual asociado (ej: "lucia@correo.ugr.es").
 */
@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedText)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Ink)
    }
}
