package com.example.redsocial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.redsocial.ui.components.ChallengePreview
import com.example.redsocial.ui.components.ChallengePreviewCard
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage
import com.example.redsocial.ui.components.ChipPreview
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Comment

@Composable
fun ExploreScreen(onVerDesafio: (String) -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    var challenges by remember { mutableStateOf(listOf<ChallengeCardData>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        val db = FirebaseFirestore.getInstance()
        val desafiosSnapshot = db.collection("desafios").get().await()
        val desafios = desafiosSnapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            val authorId = data["authorId"] as? String ?: return@mapNotNull null
            val userSnapshot = db.collection("usuarios").document(authorId).get().await()
            val nombreUsuario = userSnapshot.getString("nombreUsuario") ?: "Usuario"
            ChallengeCardData(
                id = doc.id,
                title = data["title"] as? String ?: "",
                coverImageUrl = data["coverImageUrl"] as? String,
                points = (data["points"] as? Long)?.toInt() ?: 0,
                nombreUsuario = nombreUsuario,
                tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                likes = (data["likes"] as? Long)?.toInt() ?: 0,
                comments = (data["comments"] as? Long)?.toInt() ?: 0
            )
        }
        challenges = desafios
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar desafíos, creadores....") },
            leadingIcon = { Icon(Icons.Default.Search, "Buscar") }
        )
        FiltersSection()
        CategoriesSection()
        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(challenges) { challenge ->
                    ChallengePreviewCardFirestore(challenge, onVerDesafio)
                }
            }
        }
    }
}

data class ChallengeCardData(
    val id: String,
    val title: String,
    val coverImageUrl: String?,
    val points: Int,
    val nombreUsuario: String,
    val tags: List<String>,
    val likes: Int,
    val comments: Int
)

@Composable
fun ChallengePreviewCardFirestore(
    challenge: ChallengeCardData,
    onVerDesafio: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            challenge.coverImageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Imagen de portada",
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(challenge.title, style = MaterialTheme.typography.titleLarge)
                ChipPreview("${challenge.points} pts")
            }
            Spacer(Modifier.height(4.dp))
            Text("Por: @${challenge.nombreUsuario}", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.padding(vertical = 4.dp)) {
                challenge.tags.forEach { tag ->
                    ChipPreview(tag)
                    Spacer(Modifier.width(4.dp))
                }
            }
            Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Favorite, contentDescription = "Likes")
                Text("${challenge.likes}")
                Icon(Icons.Default.Comment, contentDescription = "Comentarios")
                Text("${challenge.comments}")
            }
            Button(
                onClick = { onVerDesafio(challenge.id) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Ver Desafío →")
            }
        }
    }
}

@Composable
fun FiltersSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Filtros",
            style = MaterialTheme.typography.titleMedium
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { /* Limpiar filtros */ },
                label = { Text("Limpiar Filtros") }
            )
        }
    }
}

@Composable
fun CategoriesSection() {
    val categories = listOf("Arte", "Deporte", "Música", "Ciencia", "Bienestar", "Tecnología")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = false,
                    onClick = { /* Seleccionar categoría */ },
                    label = { Text(category) }
                )
            }
        }
    }
    
    // Dificultad
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Dificultad",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Fácil", "Medio", "Difícil")) { difficulty ->
                FilterChip(
                    selected = false,
                    onClick = { /* Seleccionar dificultad */ },
                    label = { Text(difficulty) }
                )
            }
        }
    }
    
    // Duración
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Duración",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("1 día", "3 días", "1 Semana", "Flexible")) { duration ->
                FilterChip(
                    selected = false,
                    onClick = { /* Seleccionar duración */ },
                    label = { Text(duration) }
                )
            }
        }
    }
} 