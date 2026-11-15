package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Accessible
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.SoftBg
import com.example.aplicacionmovilnpi.ui.theme.ToneBlueBg
import com.example.aplicacionmovilnpi.ui.theme.ToneOrangeBg
import com.example.aplicacionmovilnpi.ui.theme.TonePurpleBg
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseBg
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

/**
 * Pantalla principal del menú "Más", que actúa como hub de accesos directos
 * a secciones secundarias como Expediente, Calendario, Mensajes, Ayuda, Accesibilidad y Perfil.
 *
 * @param onNavigateToSubScreen Callback para navegar a la subpantalla correspondiente según la clave dada.
 * @param onLanguageChange Callback invocado al modificar la preferencia de idioma (Español/Inglés).
 */
@Composable
fun MoreScreen(
    onNavigateToSubScreen: (String) -> Unit,
    onLanguageChange: (Boolean) -> Unit
) {
    // 1. Obtener idioma actual del proveedor de estado de composición
    val isEnglish = LocalIsEnglish.current

    // 2. Control de visibilidad del diálogo de cambio de idioma
    var showLanguageDialog by remember { mutableStateOf(false) }

    // 3. Renderizado condicional del diálogo emergente de idioma
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            isEnglish = isEnglish,
            onSelectLanguage = { selectEnglish ->
                onLanguageChange(selectEnglish)
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // 4. Estructura principal en columna con desplazamiento vertical
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Encabezado del menú
        Text(
            text = if (isEnglish) "More options" else "Más opciones",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = if (isEnglish) "Academic record, submissions, messaging & settings" else "Expediente, entregas, mensajería y configuración",
            fontSize = 12.sp,
            color = MutedText,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Opciones principales del menú representadas mediante [MoreOptionCard]
        MoreOptionCard(
            title = if (isEnglish) "Language" else "Idioma",
            subtitle = if (isEnglish) "English 🇬🇧" else "Español 🇪🇸",
            icon = Icons.Rounded.Language,
            bgTone = ToneBlueBg,
            onClick = { showLanguageDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = if (isEnglish) "Academic Record & Grades" else "Expediente y Notas",
            subtitle = if (isEnglish) "Grades and GPA" else "Calificaciones y nota media",
            icon = Icons.Rounded.BarChart,
            bgTone = ToneRoseBg,
            onClick = { onNavigateToSubScreen("grades") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = if (isEnglish) "Calendar & Deadlines" else "Calendario y Entregas",
            subtitle = if (isEnglish) "Exams, deadlines & events" else "Exámenes, entregas y eventos",
            icon = Icons.Rounded.CalendarMonth,
            bgTone = TonePurpleBg,
            onClick = { onNavigateToSubScreen("calendar") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = if (isEnglish) "Messages & Notices" else "Mensajes y Avisos",
            subtitle = if (isEnglish) "UGR institutional inbox" else "Buzón de correo institucional UGR",
            icon = Icons.Rounded.Email,
            bgTone = ToneBlueBg,
            onClick = { onNavigateToSubScreen("messages") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = if (isEnglish) "Support & Help" else "Soporte y Ayuda",
            subtitle = if (isEnglish) "FAQs and support tickets" else "Preguntas frecuentes e incidencias",
            icon = Icons.AutoMirrored.Rounded.Help,
            bgTone = ToneOrangeBg,
            onClick = { onNavigateToSubScreen("help") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = if (isEnglish) "Accessibility" else "Accesibilidad",
            subtitle = if (isEnglish) "Large text, contrast & adapted routes" else "Texto grande, contraste y rutas adaptadas",
            icon = Icons.AutoMirrored.Rounded.Accessible,
            bgTone = SoftBg,
            onClick = { onNavigateToSubScreen("accessibility") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreOptionCard(
            title = if (isEnglish) "My Profile" else "Mi Perfil",
            subtitle = FacultyData.Student.name,
            icon = Icons.Rounded.Person,
            bgTone = SoftBg,
            onClick = { onNavigateToSubScreen("profile") }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * Diálogo modal para la selección preferente del idioma de la aplicación.
 *
 * @param isEnglish Estado actual del idioma (`true` si es inglés, `false` si es español).
 * @param onSelectLanguage Callback invocado al hacer clic en una de las opciones de idioma.
 * @param onDismiss Callback invocado para cerrar el diálogo emergente.
 */
@Composable
fun LanguageSelectionDialog(
    isEnglish: Boolean,
    onSelectLanguage: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isEnglish) "Select Language" else "Seleccionar Idioma",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Text(
                    text = if (isEnglish) "Choose your preferred language for the app" else "Elige el idioma preferido para la aplicación",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                LanguageRow(
                    label = "Español 🇪🇸",
                    selected = !isEnglish,
                    onClick = {
                        onSelectLanguage(false)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LanguageRow(
                    label = "English 🇬🇧",
                    selected = isEnglish,
                    onClick = {
                        onSelectLanguage(true)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = if (isEnglish) "Close" else "Cerrar", color = Ink)
                    }
                }
            }
        }
    }
}

/**
 * Fila seleccionable con botón de radio para una opción de idioma dentro del diálogo.
 *
 * @param label Etiqueta del idioma a mostrar (ej: "Español 🇪🇸").
 * @param selected Indica si esta opción es la seleccionada actualmente.
 * @param onClick Callback ejecutado al presionar sobre la fila o el botón de radio.
 */
@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (selected) SoftBg else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) UgrRed else CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.weight(1f)
            )

            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = UgrRed)
            )
        }
    }
}

/**
 * Tarjeta interactiva reutilizable para representar opciones en la pantalla [MoreScreen].
 *
 * @param title Título representativo de la opción (ej: "Expediente y Notas").
 * @param subtitle Descripción o resumen complementario de la opción.
 * @param icon Icono vectorial característico de la acción.
 * @param bgTone Color de fondo del icono.
 * @param onClick Acción a realizar al pulsar sobre la tarjeta.
 */
@Composable
fun MoreOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgTone: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = bgTone,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Ink,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text(text = subtitle, fontSize = 11.sp, color = MutedText, modifier = Modifier.padding(top = 2.dp))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
