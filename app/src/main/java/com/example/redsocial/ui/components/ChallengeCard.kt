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

data class Challenge(
    val id: String,
    val title: String,
    val author: String,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val saves: Int
)

@Composable
fun ChallengeCard(
    challenge: Challenge,
    isCompact: Boolean = false
) {
    if (isCompact) {
        CompactChallengeCard(challenge)
    } else {
        FullChallengeCard(challenge)
    }
}

@Composable
private fun FullChallengeCard(challenge: Challenge) {
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
                text = "Por: ${challenge.author}",
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
        
        // Acciones laterales
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { /* Like */ }) {
                Icon(Icons.Default.Favorite, "Me gusta")
            }
            Text("${challenge.likes}")
            
            IconButton(onClick = { /* Comentar */ }) {
                Icon(Icons.Default.Comment, "Comentar")
            }
            Text("${challenge.comments}")
            
            IconButton(onClick = { /* Compartir */ }) {
                Icon(Icons.Default.Share, "Compartir")
            }
            Text("${challenge.shares}")
            
            IconButton(onClick = { /* Guardar */ }) {
                Icon(Icons.Default.BookmarkBorder, "Guardar")
            }
            Text("${challenge.saves}")
        }
    }
}

@Composable
private fun CompactChallengeCard(challenge: Challenge) {
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
                text = "Por: ${challenge.author}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Icon(Icons.Default.Favorite, "Me gusta")
                    Text("${challenge.likes}")
                }
                Row {
                    Icon(Icons.Default.Comment, "Comentarios")
                    Text("${challenge.comments}")
                }
            }
            
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