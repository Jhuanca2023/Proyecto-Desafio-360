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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage
import com.example.redsocial.ui.components.ChipPreview
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Comment
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import com.example.redsocial.ui.components.CommentsDialog
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ExploreScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var challenges by remember { mutableStateOf(listOf<ChallengeCardData>()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedDuration by remember { mutableStateOf<String?>(null) }

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
                comments = (data["comments"] as? Long)?.toInt() ?: 0,
                authorId = authorId,
                duration = data["duration"] as? String ?: ""
            )
        }
        challenges = desafios
        isLoading = false
    }

    val filteredChallenges = challenges.filter { challenge ->
        (selectedCategory == null || challenge.tags.any { it.equals(selectedCategory, ignoreCase = true) }) &&
        (selectedDuration == null || challenge.duration.equals(selectedDuration, ignoreCase = true)) &&
        (searchQuery.isEmpty() || challenge.title.contains(searchQuery, ignoreCase = true) || challenge.nombreUsuario.contains(searchQuery, ignoreCase = true))
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
        FiltersSection(
            selectedCategory = selectedCategory,
            selectedDuration = selectedDuration,
            onCategorySelected = { selectedCategory = it },
            onDurationSelected = { selectedDuration = it },
            onClearFilters = {
                selectedCategory = null
                selectedDuration = null
            }
        )
        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredChallenges) { challenge ->
                    ChallengePreviewCardFirestore(challenge, { challengeId ->
                        navController.navigate("detalleDesafio/$challengeId")
                    }, navController)
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
    val comments: Int,
    val authorId: String,
    val duration: String
)

@Composable
fun ChallengePreviewCardFirestore(
    challenge: ChallengeCardData,
    onVerDesafio: (String) -> Unit,
    navController: NavController
) {
    var showComments by remember { mutableStateOf(false) }
    var currentLikes by remember { mutableStateOf(challenge.likes) }
    var currentComments by remember { mutableStateOf(challenge.comments) }
    var isLiked by remember { mutableStateOf(false) }
    var participantes by remember { mutableStateOf<List<String>>(emptyList()) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    // Obtener lista de participantes
    LaunchedEffect(challenge.id) {
        val snapshot = db.collection("evidencias")
            .whereEqualTo("challengeId", challenge.id)
            .get()
            .await()
        
        participantes = snapshot.documents.mapNotNull { doc ->
            doc.getString("userName")
        }
    }

    // Verificar si el usuario actual ya dio like
    LaunchedEffect(challenge.id, currentUser?.uid) {
        if (currentUser != null) {
            val likeDoc = db.collection("desafios")
                .document(challenge.id)
                .collection("likes")
                .document(currentUser.uid)
                .get()
                .await()
            isLiked = likeDoc.exists()
        }
    }

    // Escuchar cambios en los likes
    LaunchedEffect(challenge.id) {
        db.collection("desafios")
            .document(challenge.id)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    currentLikes = it.getLong("likes")?.toInt() ?: 0
                }
            }
    }

    // Escuchar cambios en los comentarios
    LaunchedEffect(challenge.id) {
        db.collection("desafios")
            .document(challenge.id)
            .collection("comentarios")
            .addSnapshotListener { snapshot, _ ->
                currentComments = snapshot?.size() ?: 0
            }
    }

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
            Text(
                text = "Por: @${challenge.nombreUsuario}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { navController.navigate("profile/${challenge.authorId}") }
            )
            Row(Modifier.padding(vertical = 4.dp)) {
                challenge.tags.forEach { tag ->
                    ChipPreview(tag)
                    Spacer(Modifier.width(4.dp))
                }
            }
            
            // Mostrar participantes
            if (participantes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Participantes:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                participantes.forEach { userName ->
                    Text(
                        text = "@$userName",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }

            Row(
                Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentUser != null) {
                            val likesRef = db.collection("desafios").document(challenge.id)
                            val userLikeRef = likesRef.collection("likes").document(currentUser.uid)
                            
                            if (isLiked) {
                                // Quitar like
                                db.runTransaction { transaction ->
                                    val snapshot = transaction.get(likesRef)
                                    val currentLikes = snapshot.getLong("likes") ?: 0
                                    transaction.update(likesRef, "likes", currentLikes - 1)
                                    transaction.delete(userLikeRef)
                                }
                                isLiked = false
                            } else {
                                // Dar like
                                db.runTransaction { transaction ->
                                    val snapshot = transaction.get(likesRef)
                                    val currentLikes = snapshot.getLong("likes") ?: 0
                                    transaction.update(likesRef, "likes", currentLikes + 1)
                                    transaction.set(userLikeRef, mapOf("timestamp" to System.currentTimeMillis()))
                                }
                                isLiked = true
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text("$currentLikes")
                IconButton(
                    onClick = { showComments = true }
                ) {
                    Icon(Icons.Default.Comment, contentDescription = "Comentarios")
                }
                Text("$currentComments")
            }
            Button(
                onClick = { onVerDesafio(challenge.id) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Ver Desafío →")
            }
        }
    }

    if (showComments) {
        CommentsDialog(
            challengeId = challenge.id,
            onDismiss = { showComments = false }
        )
    }
}

@Composable
fun FiltersSection(
    selectedCategory: String?,
    selectedDuration: String?,
    onCategorySelected: (String) -> Unit,
    onDurationSelected: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    var showFilters by remember { mutableStateOf(true) }

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
                onClick = onClearFilters,
                label = { Text("Limpiar Filtros") }
            )
            selectedCategory?.let {
                AssistChip(
                    onClick = { onCategorySelected(it) },
                    label = { Text(it) }
                )
            }
            selectedDuration?.let {
                AssistChip(
                    onClick = { onDurationSelected(it) },
                    label = { Text(it) }
                )
            }
        }
    }

    if (showFilters) {
        CategoriesSection(
            onCategorySelected = { category ->
                onCategorySelected(category)
                showFilters = false
            }
        )
        DurationSection(
            onDurationSelected = { duration ->
                onDurationSelected(duration)
                showFilters = false
            }
        )
    }
}

@Composable
fun CategoriesSection(onCategorySelected: (String) -> Unit) {
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
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }
        }
    }
}

@Composable
fun DurationSection(onDurationSelected: (String) -> Unit) {
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
                    onClick = { onDurationSelected(duration) },
                    label = { Text(duration) }
                )
            }
        }
    }
} 