package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed
import com.example.aplicacionmovilnpi.ui.theme.UgrRedDark

/**
 * Pantalla de inicio de sesión e identificación corporativa para la comunidad UGR.
 *
 * Ofrece campos para introducir credenciales institucionales (@correo.ugr.es), opción
 * de recordar cuenta, recuperación de contraseña y un botón de acceso directo en modo demostración.
 *
 * @param onLoginSuccess Callback de éxito al autenticar, recibiendo el indicador [rememberAccount].
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (rememberAccount: Boolean) -> Unit
) {
    // 1. Obtener idioma local para adaptar los textos bilingües de la interfaz
    val isEnglish = LocalIsEnglish.current

    // 2. Estados locales para almacenar correo, contraseña y casilla de verificación de sesión
    var email by remember { mutableStateOf("lucia@correo.ugr.es") }
    var password by remember { mutableStateOf("••••••••") }
    var rememberAccount by remember { mutableStateOf(true) }

    // 3. Gradiente institucional de color rojo UGR para la sección superior (Hero)
    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF85132E),
                Color(0xFFB62042),
                Color(0xFFC63655)
            )
        )
    }

    // 4. Estructura vertical desplazable
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Sección Hero con el logotipo de UGR y eslogan de bienvenida
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 40.dp)
        ) {
            Column {
                // Insignia del logotipo de la UGR
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 16.dp, bottomEnd = 8.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "UGR",
                            color = UgrRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "UGR CAMPUS",
                    color = Color(0xFFF7C8D3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isEnglish) "Your school,\ncloser to you." else "Tu facultad,\nmás cerca.",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isEnglish) "Find your classes and get around campus without getting lost." else "Encuentra tus clases y muévete por el centro sin perderte.",
                    color = Color(0xFFF5D3DA),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // 5. Contenedor de la tarjeta de inicio de sesión
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            color = Color.White,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column {
                Text(
                    text = if (isEnglish) "Sign in" else "Acceso Identificado",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Text(
                    text = if (isEnglish) "Use your institutional @correo.ugr.es account" else "Usa tu cuenta institucional @correo.ugr.es",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Campo de texto: Correo institucional
                Text(
                    text = if (isEnglish) "INSTITUTIONAL EMAIL" else "CORREO INSTITUCIONAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Text(text = "✉️", fontSize = 16.sp)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UgrRed,
                        unfocusedBorderColor = Color(0xFFDFE2E6)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de texto: Contraseña
                Text(
                    text = if (isEnglish) "PASSWORD" else "CONTRASEÑA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Text(text = "🔒", fontSize = 16.sp)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UgrRed,
                        unfocusedBorderColor = Color(0xFFDFE2E6)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fila de opciones: Recordar cuenta y Recuperar contraseña
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberAccount,
                            onCheckedChange = { rememberAccount = it },
                            colors = CheckboxDefaults.colors(checkedColor = UgrRed)
                        )
                        Text(
                            text = if (isEnglish) "Remember account" else "Recordar cuenta",
                            fontSize = 11.sp,
                            color = Ink
                        )
                    }

                    TextButton(onClick = { }) {
                        Text(
                            text = if (isEnglish) "Forgot password?" else "Olvidé mi contraseña",
                            fontSize = 11.sp,
                            color = UgrRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Botón de acceso principal con credenciales UGR
                Button(
                    onClick = { onLoginSuccess(rememberAccount) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Sign in with UGR email" else "Acceder con correo UGR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 7. Botón de acceso rápido para pruebas / demostración del proyecto
                OutlinedButton(
                    onClick = { onLoginSuccess(rememberAccount) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UgrRedDark),
                    border = BorderStroke(1.dp, UgrRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Quick student access (Demo)" else "Acceso rápido como estudiante (Demo)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pie de página con información de soporte técnico
                Text(
                    text = if (isEnglish) "UGR Identity Service · Technical Support" else "Servicio de Identificación UGR · Soporte Técnico",
                    fontSize = 10.sp,
                    color = MutedText,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
