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
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background

import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.FavoriteBorder
import com.example.redsocial.ui.components.CommentsDialog
import com.example.redsocial.utils.NetworkUtils
import com.example.redsocial.utils.NotificationUtils
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.People

@OptIn(ExperimentalLayoutApi::class)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0F1C), // Celeste muy oscuro (noche)
                        Color(0xFF1A1F2E), // Celeste oscuro
                        Color(0xFF2A2F3E)  // Celeste medio oscuro
                    )
                )
            )
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF3B82F6))
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                        Text("Detalle del Desafío", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    }
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = data["coverImageUrl"] as? String,
                        contentDescription = "Imagen de portada",
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        data["title"] as? String ?: "", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Creado por @${data["authorName"] as? String ?: "Usuario"}",
                        color = Color(0xFF60A5FA),
                        modifier = Modifier.clickable { navController.navigate("userProfile/${data["authorId"] as? String ?: ""}") }
                    )
                    Spacer(Modifier.height(8.dp))
                    // Etiquetas principales (puntos, categoría, duración)
                    FlowRow {
                        ChipPreview("${(data["points"] as? Long ?: 0)} pts")
                        (data["category"] as? String)?.let { ChipPreview(it) }
                        (data["duration"] as? String)?.let { ChipPreview(it) }
                    }
                    
                    // Contador de participantes
                    val participantes = (data["participants"] as? Long)?.toInt() ?: 0
                    val maxParticipantes = (data["maxParticipants"] as? Long)?.toInt() ?: 1
                    val activo = participantes < maxParticipantes
                    
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.People, contentDescription = "Participantes", tint = Color(0xFF60A5FA))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Participantes: $participantes/$maxParticipantes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            if (activo) "Activo" else "Completado",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (activo) Color(0xFF00C853) else Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Content types
                    (data["contentTypes"] as? List<*>)?.let { contentTypes ->
                        if (contentTypes.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            FlowRow {
                                contentTypes.forEach { contentType ->
                                    ChipPreview(contentType.toString())
                                }
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFF3B82F6))
                    Text("Descripción del Desafío", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(data["description"] as? String ?: "", color = Color(0xFFCBD5E1))
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
                        Text("$currentLikes", color = Color.White)
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
                        Text("$currentComments", color = Color.White)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Reglas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    (data["rules"] as? List<*>)?.forEach {
                        Text("• $it", color = Color(0xFFCBD5E1))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Etiquetas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    (data["tags"] as? List<*>)?.let { tags ->
                        if (tags.isNotEmpty()) {
                            FlowRow {
                                tags.forEach { tag ->
                                    ChipPreview(tag.toString())
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showEnviarEvidencia = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activo) Color(0xFF3B82F6) else Color(0xFF64748B)
                        ),
                        enabled = activo
                    ) {
                        Text(
                            if (activo) "Participar en el Desafío" else "Desafío Completado", 
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Participaciones Recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    if (participaciones.isEmpty()) {
                        Text(
                            "Aún no hay participaciones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        participaciones.forEach { evidencia ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1A1F2E)
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Participó en el desafío: @${evidencia.userName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (evidencia.texto != null) {
                                        Text(
                                            text = evidencia.texto,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFCBD5E1)
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
                                .wrapContentHeight(),
                            color = Color(0xFF1A1F2E)
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
} 