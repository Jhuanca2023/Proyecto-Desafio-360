package com.example.redsocial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun ChipPreview(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .wrapContentWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF3B82F6).copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        color = Color.White
    )
} 