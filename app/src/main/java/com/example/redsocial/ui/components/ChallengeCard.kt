package com.example.redsocial.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocial.ui.components.ChipPreview

data class ChallengePreview(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val duration: String = "",
    val points: Int = 0,
    val contentTypes: List<String> = emptyList(),
    val coverImageUrl: String? = null
)

@Composable
fun ChallengePreviewCard(
    challenge: ChallengePreview
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            challenge.coverImageUrl?.let { url ->
                // Aquí podrías usar Coil o Glide para cargar la imagen desde la URL
                // Image(painter = rememberImagePainter(url), contentDescription = "Imagen de portada", modifier = Modifier.fillMaxWidth().height(120.dp))
            }
            Text(challenge.title, style = MaterialTheme.typography.titleLarge)
            Text(challenge.description, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (challenge.category.isNotBlank()) ChipPreview(challenge.category)
                if (challenge.duration.isNotBlank()) ChipPreview(challenge.duration)
                if (challenge.points > 0) ChipPreview("${challenge.points} pts")
            }
            Spacer(Modifier.height(4.dp))
            Text("Contenido aceptado:", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                challenge.contentTypes.forEach { tipo ->
                    ChipPreview(tipo.replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: ChallengePreview,
    isCompact: Boolean = false
) {
    if (isCompact) {
        CompactChallengeCard(challenge)
    } else {
        FullChallengeCard(challenge)
    }
}

@Composable
private fun FullChallengeCard(challenge: ChallengePreview) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
    ) {
        // Información del desafío
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = challenge.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = challenge.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { /* Participar */ }) {
                    Text("Participar")
                }
                Button(onClick = { /* Ver categoría */ }) {
                    Text("Categoría")
                }
            }
        }
        // No mostrar acciones laterales
    }
}

@Composable
private fun CompactChallengeCard(challenge: ChallengePreview) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium
            )
            // No mostrar likes ni comentarios
            Button(
                onClick = { /* Ver desafío */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Ver Desafío →")
            }
        }
    }
} 