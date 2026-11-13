package com.example.aplicacionmovilnpi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.FaqItem
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val isEnglish = LocalIsEnglish.current
    var ticketSubject by remember { mutableStateOf("") }
    var ticketDesc by remember { mutableStateOf("") }
    var submittedNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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

        Text(
            text = if (isEnglish) "Help Center" else "Centro de Ayuda",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = if (isEnglish) "Get answers or report a technical issue" else "Resuelve dudas o envía una incidencia técnica",
            fontSize = 12.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isEnglish) "Frequently Asked Questions (FAQ)" else "Preguntas Frecuentes (FAQ)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(10.dp))

        val faqTranslations = listOf(
            "How do I get to my next class?" to "On Home, tap ‘Guide me to the classroom’ on your next class card. You can also tap ‘Directions’ for any class in your schedule.",
            "Can I avoid stairs?" to "Yes. Turn on ‘Accessible route’ on the route screen and we will guide you using elevators and ramps.",
            "My location is inaccurate inside the building" to "GPS is less accurate indoors. Turn on Wi-Fi and Bluetooth to improve location, or select your floor manually on the map.",
            "How do I change floors on the map?" to "On Map › View building plan, use the floor selector (P2, P1, GF, B1) at the top.",
            "Where does my schedule come from?" to "Your schedule is generated from the courses in which you are enrolled at UGR.",
            "How do I add an assignment or exam?" to "Go to More › Calendar, select the date and tap ‘+ Add’. You can enter the subject, time and a description.",
            "I forgot my password" to "Your password is for your UGR institutional account. Reset it from UGR Identified Access using your ID and personal email.",
            "How do I sign out?" to "Tap your avatar in the top-right corner and choose ‘Log out’, or sign out from My Profile."
        )
        SampleData.faqs.forEachIndexed { index, faq ->
            FaqItemCard(
                faq = faq,
                isEnglish = isEnglish,
                translatedQuestion = faqTranslations[index].first,
                translatedAnswer = faqTranslations[index].second
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isEnglish) "Report an issue" else "Reportar una incidencia",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = ticketSubject,
                    onValueChange = { ticketSubject = it },
                    placeholder = { Text(text = if (isEnglish) "Issue subject..." else "Asunto de la incidencia...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UgrRed,
                        unfocusedBorderColor = CardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ticketDesc,
                    onValueChange = { ticketDesc = it },
                    placeholder = { Text(text = if (isEnglish) "Describe the problem or question..." else "Describe el problema o duda...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UgrRed,
                        unfocusedBorderColor = CardBorder
                    ),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (ticketSubject.isNotBlank()) {
                            submittedNotice = if (isEnglish) "Issue INC-0143 submitted successfully. We will reply within 48 hours." else "Incidencia INC-0143 enviada correctamente. Te responderemos en 48h."
                            ticketSubject = ""
                            ticketDesc = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgrRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isEnglish) "Submit Issue" else "Enviar Incidencia", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                if (submittedNotice != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = submittedNotice!!, fontSize = 11.sp, color = UgrRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun FaqItemCard(faq: FaqItem, isEnglish: Boolean, translatedQuestion: String, translatedAnswer: String) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) translatedQuestion else faq.question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                Text(text = if (expanded) "▲" else "▼", fontSize = 10.sp, color = MutedText)
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Text(text = if (isEnglish) translatedAnswer else faq.answer, fontSize = 11.sp, color = MutedText, lineHeight = 16.sp)
                }
            }
        }
    }
}
