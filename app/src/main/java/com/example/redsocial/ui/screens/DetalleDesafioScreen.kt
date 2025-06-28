package com.example.redsocial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.redsocial.ui.components.ChipPreview
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.clickable
import com.example.redsocial.models.Evidencia
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.FavoriteBorder
import com.example.redsocial.ui.components.CommentsDialog
import com.example.redsocial.utils.NetworkUtils
import com.example.redsocial.utils.NotificationUtils
import android.util.Log

@Composable
fun DetalleDesafioScreen(challengeId: String, navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var challenge by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentLikes by remember { mutableStateOf(0) }
    var currentComments by remember { mutableStateOf(0) }
    var isLiked by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var showEnviarEvidencia by remember { mutableStateOf(false) }
    var participaciones by remember { mutableStateOf(listOf<Evidencia>()) }

    // Cargar datos del desafío
    LaunchedEffect(challengeId) {
        try {
            val doc = db.collection("desafios").document(challengeId).get().await()
            if (doc.exists()) {
                challenge = doc.data
                currentLikes = (doc.getLong("likes") ?: 0L).toInt()
                currentComments = (doc.getLong("comments") ?: 0L).toInt()
                
                // Verificar si el usuario actual ya dio like
                if (currentUser != null) {
                    val likeDoc = db.collection("desafios")
                        .document(challengeId)
                        .collection("likes")
                        .document(currentUser.uid)
                        .get()
                        .await()
                    isLiked = likeDoc.exists()
                }
                
                // Cargar participaciones
                val evidenciasSnapshot = db.collection("evidencias")
                    .whereEqualTo("challengeId", challengeId)
                    .get()
                    .await()
                participaciones = evidenciasSnapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Evidencia(
                        id = doc.id,
                        challengeId = data["challengeId"] as? String ?: "",
                        userId = data["userId"] as? String ?: "",
                        userName = data["userName"] as? String ?: "",
                        tipo = data["tipo"] as? String ?: "",
                        url = data["url"] as? String,
                        texto = data["texto"] as? String,
                        timestamp = data["timestamp"] as? Long ?: 0L
                    )
                }
            }
        } catch (e: Exception) {
            // Manejar error
        }
        isLoading = false
    }

    // Función para manejar like/unlike
    fun handleLike() {
        if (currentUser == null) return
        
        Log.d("DetalleDesafioScreen", "handleLike - isLiked: $isLiked, challengeId: $challengeId")
        
        val likeRef = db.collection("desafios")
            .document(challengeId)
            .collection("likes")
            .document(currentUser.uid)
        
        if (isLiked) {
            // Quitar like
            likeRef.delete().addOnSuccessListener {
                db.collection("desafios")
                    .document(challengeId)
                    .update("likes", currentLikes - 1)
                isLiked = false
                currentLikes--
                
                // Eliminar notificación de like
                challenge?.let { data ->
                    val authorId = data["authorId"] as? String
                    Log.d("DetalleDesafioScreen", "Eliminando like - authorId: $authorId")
                    if (authorId != null) {
                        NotificationUtils.removeLikeNotification(
                            challengeAuthorId = authorId,
                            challengeId = challengeId
                        )
                    }
                }
            }
        } else {
            // Dar like
            likeRef.set(hashMapOf(
                "userId" to currentUser.uid,
                "timestamp" to System.currentTimeMillis()
            )).addOnSuccessListener {
                db.collection("desafios")
                    .document(challengeId)
                    .update("likes", currentLikes + 1)
                isLiked = true
                currentLikes++
                
                // Enviar notificación de like
                challenge?.let { data ->
                    val authorId = data["authorId"] as? String
                    val title = data["title"] as? String ?: "Desafío"
                    Log.d("DetalleDesafioScreen", "Enviando notificación de like - authorId: $authorId, title: $title")
                    if (authorId != null) {
                        NotificationUtils.sendLikeNotification(
                            challengeAuthorId = authorId,
                            challengeId = challengeId,
                            challengeTitle = title
                        )
                    } else {
                        Log.e("DetalleDesafioScreen", "authorId es null - data: $data")
                    }
                }
            }
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        challenge?.let { data ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Text("Detalle del Desafío", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = data["coverImageUrl"] as? String,
                    contentDescription = "Imagen de portada",
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(data["title"] as? String ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Creado por @${data["authorName"] as? String ?: "Usuario"}",
                    color = Color.Gray,
                    modifier = Modifier.clickable { navController.navigate("userProfile/${data["authorId"] as? String ?: ""}") }
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChipPreview("${(data["points"] as? Long ?: 0)} pts")
                    Spacer(Modifier.width(8.dp))
                    (data["category"] as? String)?.let { ChipPreview(it) }
                    Spacer(Modifier.width(8.dp))
                    (data["duration"] as? String)?.let { ChipPreview(it) }
                    Spacer(Modifier.width(8.dp))
                    (data["contentTypes"] as? List<*>)?.forEach {
                        ChipPreview(it.toString())
                        Spacer(Modifier.width(4.dp))
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Descripción del Desafío", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(data["description"] as? String ?: "")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { handleLike() }
                    ) {
                        Icon(
                            if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Likes",
                            tint = if (isLiked) Color(0xFFFF4081) else Color(0xFFA259FF)
                        )
                    }
                    Text("$currentLikes")
                    Spacer(Modifier.width(16.dp))
                    IconButton(
                        onClick = { showComments = true }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "Comentarios",
                            tint = Color(0xFFA259FF)
                        )
                    }
                    Text("$currentComments")
                }
                Spacer(Modifier.height(8.dp))
                Text("Reglas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                (data["rules"] as? List<*>)?.forEach {
                    Text("• $it")
                }
                Spacer(Modifier.height(8.dp))
                Text("Etiquetas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow {
                    (data["tags"] as? List<*>)?.forEach { tag ->
                        item { ChipPreview(tag.toString()) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showEnviarEvidencia = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Participar en el Desafío")
                }
                Spacer(Modifier.height(16.dp))
                Text("Participaciones Recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                if (participaciones.isEmpty()) {
                    Text(
                        "Aún no hay participaciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    participaciones.forEach { evidencia ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Participó en el desafío: @${evidencia.userName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (evidencia.texto != null) {
                                    Text(
                                        text = evidencia.texto,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (showEnviarEvidencia) {
                Dialog(onDismissRequest = { showEnviarEvidencia = false }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .wrapContentHeight()
                    ) {
                        SendEvidenceScreen(
                            challengeId = challengeId,
                            challengeTitle = data["title"] as? String ?: "",
                            onEvidenceSent = {
                                showEnviarEvidencia = false
                                // Recargar challenge
                                navController.popBackStack()
                                navController.navigate("detalleDesafio/$challengeId")
                            },
                            onCancel = { showEnviarEvidencia = false }
                        )
                    }
                }
            }
            if (showComments) {
                CommentsDialog(
                    challengeId = challengeId,
                    onDismiss = { showComments = false },
                    onUserProfileClick = { userId ->
                        navController.navigate("userProfile/$userId")
                    }
                )
            }
        }
    }
} 