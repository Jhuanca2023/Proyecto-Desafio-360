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

@Composable
fun DetalleDesafioScreen(challengeId: String, navController: NavController) {
    var challenge by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEnviarEvidencia by remember { mutableStateOf(false) }
    var participaciones by remember { mutableStateOf<List<Evidencia>>(emptyList()) }

    LaunchedEffect(challengeId) {
        isLoading = true
        val db = FirebaseFirestore.getInstance()
        val doc = db.collection("desafios").document(challengeId).get().await()
        challenge = doc.data

        // Obtener participaciones recientes
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
        }.sortedByDescending { it.timestamp }

        isLoading = false
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
                    modifier = Modifier.clickable { navController.navigate("profile/${data["authorId"] as? String ?: ""}") }
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChipPreview("${(data["points"] as? Long ?: 0)} pts")
                    Spacer(Modifier.width(8.dp))
                    (data["category"] as? String)?.let { ChipPreview(it) }
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
                    Icon(Icons.Default.Favorite, contentDescription = "Likes")
                    Text("${(data["likes"] as? Long ?: 0)}")
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comentarios")
                    Text("${(data["comments"] as? Long ?: 0)}")
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
        }
    }
} 