package com.example.aplicacionmovilnpi.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionmovilnpi.data.MailFolder
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.example.aplicacionmovilnpi.data.MailMessage
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.ui.theme.CardBorder
import com.example.aplicacionmovilnpi.ui.theme.Ink
import com.example.aplicacionmovilnpi.ui.theme.MutedText
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseBg
import com.example.aplicacionmovilnpi.ui.theme.ToneRoseText
import com.example.aplicacionmovilnpi.ui.theme.UgrRed

@Composable
fun MessagesScreen(onBack: () -> Unit) {
    val isEnglish = LocalIsEnglish.current
    var activeFolder by remember { mutableStateOf(MailFolder.INBOX) }

    val filteredMails = SampleData.initialMails.filter { it.folder == activeFolder }

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
            text = if (isEnglish) "Messages & Notices" else "Mensajes y Avisos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Text(
            text = if (isEnglish) "UGR inbox · lucia@correo.ugr.es" else "Buzón institucional UGR · lucia@correo.ugr.es",
            fontSize = 12.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Folders Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MailFolder.entries.forEach { folder ->
                val selected = folder == activeFolder
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable { activeFolder = folder },
                    color = if (selected) UgrRed else Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (folder) {
                                MailFolder.INBOX -> if (isEnglish) "Inbox" else folder.label
                                MailFolder.SENT -> if (isEnglish) "Sent" else folder.label
                                MailFolder.ARCHIVED -> if (isEnglish) "Archived" else folder.label
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else Ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (filteredMails.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MutedText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isEnglish) "No messages" else "Sin mensajes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEnglish) "Your inbox is currently empty." else "No tienes mensajes ni avisos en esta carpeta.",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }
        } else {
            filteredMails.forEach { mail ->
                MailCard(mail = mail, isEnglish = isEnglish)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun MailCard(mail: MailMessage, isEnglish: Boolean) {
    val englishSubject = when (mail.id) {
        1L -> "New course material available"
        2L -> "Enrollment for academic year 2026/2027"
        3L -> "Your reservation is available"
        4L -> "Lab 1 instructions published"
        5L -> "Question about second-semester electives"
        6L -> "Welcome to academic year 2026/2027"
        else -> mail.subject
    }
    val englishBody = when (mail.id) {
        1L -> "New material has been published for Nuevos Paradigmas de Interacción.\n\nVisit PRADO to view it."
        2L -> "Dear student:\n\nYour enrollment change request has been processed. You can view the updated receipt on the electronic office.\n\nRegards,\nETSIIT Office"
        3L -> "The copy you reserved, ‘Computer Vision: Algorithms and Applications’, is available for pickup at the ETSIIT Library loans desk.\n\nYou have 3 days to collect it."
        4L -> "The instructions for Lab 1 are now available on the platform. Please review them before the next lab session."
        5L -> "Hello:\n\nDo you know if Game Programming is offered in the second semester this year?\n\nThank you,\nLucía"
        6L -> "Welcome to the new academic year. See the academic calendar and schedules on the school website."
        else -> mail.body
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ToneRoseBg,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = mail.name.take(2).uppercase(),
                        color = ToneRoseText,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEnglish) when (mail.id) { 1L -> "PRADO · Nuevos Paradigmas de Interacción"; 2L, 6L -> "ETSIIT Office"; 3L -> "UGR Library"; 4L -> "PRADO · Visión por Computador"; 5L -> "ETSIIT Student Council"; else -> mail.name } else mail.name,
                        fontSize = 13.sp,
                        fontWeight = if (mail.unread) FontWeight.Black else FontWeight.Bold,
                        color = Ink
                    )
                    Text(text = if (isEnglish) when (mail.time) { "Ayer" -> "Yesterday"; "14 oct." -> "Oct 14"; "13 oct." -> "Oct 13"; "9 sept." -> "Sep 9"; else -> mail.time } else mail.time, fontSize = 10.sp, color = MutedText)
                }

                Text(
                    text = if (isEnglish) englishSubject else mail.subject,
                    fontSize = 12.sp,
                    fontWeight = if (mail.unread) FontWeight.Bold else FontWeight.Normal,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = if (isEnglish) englishBody else mail.body,
                    fontSize = 10.sp,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
