package com.example.redsocial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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

    LaunchedEffect(challengeId) {
        isLoading = true
        val db = FirebaseFirestore.getInstance()
        val doc = db.collection("desafios").document(challengeId).get().await()
        challenge = doc.data
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
                //  mostrar una lista de participaciones recientes
            }
            if (showEnviarEvidencia) {
                EnviarEvidenciaDialog(
                    challengeId = challengeId,
                    puntosTotales = (data["points"] as? Long ?: 0L).toInt(),
                    participantesDisponibles = (data["participantesDisponibles"] as? Long ?: 1L).toInt(),
                    onDismiss = { showEnviarEvidencia = false },
                    onEvidenciaEnviada = {
                        showEnviarEvidencia = false
                        // Recargar challenge
                        navController.popBackStack()
                        navController.navigate("detalleDesafio/$challengeId")
                    }
                )
            }
        }
    }
}

@Composable
fun EnviarEvidenciaDialog(
    challengeId: String,
    puntosTotales: Int,
    participantesDisponibles: Int,
    onDismiss: () -> Unit,
    onEvidenciaEnviada: () -> Unit
) {
    // Aquí va la UI para seleccionar tipo, subir archivo/texto, etc.
    // Por simplicidad, solo imagen y texto (puedes expandir a video/audio)
    var tipo by remember { mutableStateOf("imagen") }
    var url by remember { mutableStateOf("") }
    var texto by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 8.dp) {
            Column(Modifier.padding(24.dp)) {
                Text("Enviar Evidencia", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("imagen", "texto", "video", "audio").forEach {
                        Button(onClick = { tipo = it }, enabled = !isUploading) {
                            Text(it.replaceFirstChar { c -> c.uppercase() })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (tipo == "imagen") {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL de la imagen (subida a Imgur)") },
                        enabled = !isUploading
                    )
                } else if (tipo == "texto") {
                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = it },
                        label = { Text("Texto de la evidencia") },
                        enabled = !isUploading
                    )
                } else {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL del archivo (video/audio)") },
                        enabled = !isUploading
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isUploading) { Text("Cancelar") }
                    Button(
                        onClick = {
                            if (currentUser != null && !isUploading) {
                                isUploading = true
                                val evidencia = Evidencia(
                                    challengeId = challengeId,
                                    userId = currentUser.uid,
                                    userName = currentUser.displayName ?: "Usuario",
                                    tipo = tipo,
                                    url = if (tipo != "texto") url else null,
                                    texto = if (tipo == "texto") texto else null
                                )
                                // Guardar evidencia en subcolección
                                db.collection("desafios").document(challengeId)
                                    .collection("evidencias").add(evidencia)
                                    .addOnSuccessListener {
                                        // Actualizar participantes y puntos
                                        db.runTransaction { transaction ->
                                            val desafioRef = db.collection("desafios").document(challengeId)
                                            val snapshot = transaction.get(desafioRef)
                                            val participantes = (snapshot.getLong("participantesDisponibles") ?: 1L).toInt()
                                            val enviados = (snapshot.getLong("participantesEnviados") ?: 0L).toInt() + 1
                                            val puntosTot = (snapshot.getLong("points") ?: 0L).toInt()
                                            val puntosPorParticipante = if (enviados > 0) puntosTot / enviados else 0
                                            transaction.update(desafioRef, mapOf(
                                                "participantesDisponibles" to (participantes - 1),
                                                "participantesEnviados" to enviados,
                                                "puntosPorParticipante" to puntosPorParticipante,
                                                "habilitado" to (participantes - 1 > 0)
                                            ))
                                            // Sumar puntos al usuario
                                            val userRef = db.collection("usuarios").document(currentUser.uid)
                                            val userSnap = transaction.get(userRef)
                                            val puntosActuales = (userSnap.getLong("puntos") ?: 0L).toInt()
                                            transaction.update(userRef, "puntos", puntosActuales + puntosPorParticipante)
                                        }.addOnSuccessListener {
                                            isUploading = false
                                            onEvidenciaEnviada()
                                        }
                                    }
                            }
                        },
                        enabled = !isUploading
                    ) {
                        if (isUploading) CircularProgressIndicator(Modifier.size(16.dp))
                        else Text("Enviar Evidencia")
                    }
                }
            }
        }
    }
} 