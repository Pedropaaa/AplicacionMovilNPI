package com.example.aplicacionmovilnpi.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AppEmojiIcon(
    emoji: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp
) {
    Text(
        text = emoji,
        fontSize = size,
        modifier = modifier
    )
}
