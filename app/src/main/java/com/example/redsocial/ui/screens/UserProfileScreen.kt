package com.example.redsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.redsocial.models.Evidencia
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.redsocial.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.Delete

@Composable
fun UserProfileScreen(
    userId: String,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var createdChallenges by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var completedEvidences by remember { mutableStateOf<List<Evidencia>>(emptyList()) }
    var totalLikes by remember { mutableStateOf(0) }
    var seguidores by remember { mutableStateOf(0) }
    var siguiendo by remember { mutableStateOf(0) }
    var isFollowing by remember { mutableStateOf<Boolean?>(null) }
    var isFollowLoading by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(userId) {
        try {
            isLoading = true
            
            // Obtener datos del usuario
            val userDoc = db.collection("usuarios").document(userId).get().await()
            userData = userDoc.data
            
            // Obtener desafíos creados por el usuario
            val challengesSnapshot = db.collection("desafios")
                .whereEqualTo("authorId", userId)
                .get()
                .await()
            
            createdChallenges = challengesSnapshot.documents.mapNotNull { doc ->
                doc.data?.toMutableMap()?.apply { put("id", doc.id) }
            }
            
            // Calcular total de likes de los desafíos creados
            totalLikes = createdChallenges.sumOf { (it["likes"] as? Long ?: 0L).toInt() }
            
            // Obtener evidencias completadas por el usuario
            val evidencesSnapshot = db.collection("evidencias")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            completedEvidences = evidencesSnapshot.documents.mapNotNull { doc ->
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
            
            // Obtener estadísticas de seguidores/siguiendo
            val userStats = userDoc.data
            seguidores = (userStats?.get("seguidores") as? Long ?: 0L).toInt()
            siguiendo = (userStats?.get("siguiendo") as? Long ?: 0L).toInt()
            
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    // Verificar si el usuario actual ya sigue a este usuario
    LaunchedEffect(userId, currentUser?.uid) {
        if (currentUser != null && userId != currentUser.uid) {
            authViewModel.isFollowing(userId) { result ->
                isFollowing = result
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        userData?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1333))
            ) {
                // Header con botón de regreso
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perfil de Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Información del usuario
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            AsyncImage(
                                model = data["photoUrl"] as? String,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFA259FF))
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Nombre y username
                            Text(
                                text = data["nombreCompleto"] as? String ?: "Sin nombre",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "@${data["nombreUsuario"] as? String ?: ""}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFA259FF)
                            )
                            
                            // Botón de seguir/dejar de seguir (solo si no es tu propio perfil)
                            if (currentUser != null && userId != currentUser.uid) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (isFollowing == true) {
                                            isFollowLoading = true
                                            authViewModel.unfollowUser(userId,
                                                onSuccess = {
                                                    isFollowing = false
                                                    isFollowLoading = false
                                                },
                                                onError = { isFollowLoading = false }
                                            )
                                        } else if (isFollowing == false) {
                                            isFollowLoading = true
                                            authViewModel.followUser(userId,
                                                onSuccess = {
                                                    isFollowing = true
                                                    isFollowLoading = false
                                                },
                                                onError = { isFollowLoading = false }
                                            )
                                        }
                                    },
                                    enabled = !isFollowLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing == true) Color(0xFFA259FF) else Color.White,
                                        contentColor = if (isFollowing == true) Color.White else Color(0xFFA259FF)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(44.dp)
                                ) {
                                    if (isFollowLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = if (isFollowing == true) Color.White else Color(0xFFA259FF))
                                    } else {
                                        Text(if (isFollowing == true) "Siguiendo" else "Seguir", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            // Biografía
                            if ((data["biografia"] as? String)?.isNotBlank() == true) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = data["biografia"] as? String ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Estadísticas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatisticCard(
                                    icon = Icons.Default.Favorite,
                                    label = "Likes",
                                    value = totalLikes.toString(),
                                    color = Color(0xFFFF4081)
                                )
                                StatisticCard(
                                    icon = Icons.Default.People,
                                    label = "Seguidores",
                                    value = seguidores.toString(),
                                    color = Color(0xFFA259FF)
                                )
                                StatisticCard(
                                    icon = Icons.Default.People,
                                    label = "Siguiendo",
                                    value = siguiendo.toString(),
                                    color = Color(0xFF00B8D4)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    
                    // Tabs
                    item {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color(0xFF2A1B3D),
                            contentColor = Color(0xFFA259FF)
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Mis Desafíos") },
                                modifier = Modifier.padding(8.dp)
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Evidencias") },
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Contenido de las tabs
                    when (selectedTab) {
                        0 -> {
                            // Mis Desafíos
                            if (createdChallenges.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No ha creado desafíos aún",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            } else {
                                items(createdChallenges) { challenge ->
                                    CreatedChallengeCard(
                                        challenge = challenge,
                                        onChallengeClick = { challengeId ->
                                            navController.navigate("detalleDesafio/$challengeId")
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                        1 -> {
                            // Evidencias
                            if (completedEvidences.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No ha completado desafíos aún",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            } else {
                                items(completedEvidences) { evidencia ->
                                    EvidenceCard(
                                        evidencia = evidencia,
                                        onEvidenceClick = { challengeId ->
                                            navController.navigate("detalleDesafio/$challengeId")
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun CreatedChallengeCard(
    challenge: Map<String, Any>,
    onChallengeClick: (String) -> Unit
) {
    val challengeId = challenge["id"] as? String ?: ""
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChallengeClick(challengeId) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A1B3D)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = challenge["coverImageUrl"] as? String,
                contentDescription = "Imagen de portada",
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Color(0xFFA259FF),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge["title"] as? String ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${challenge["likes"] as? Long ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = "Comentarios",
                        tint = Color(0xFFA259FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${challenge["comments"] as? Long ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun EvidenceCard(
    evidencia: Evidencia,
    onEvidenceClick: (String) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEvidenceClick(evidencia.challengeId) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A1B3D)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (evidencia.tipo) {
                    "imagen" -> Icons.Default.Image
                    "video" -> Icons.Default.VideoLibrary
                    else -> Icons.Default.TextFields
                },
                contentDescription = evidencia.tipo,
                tint = Color(0xFFA259FF),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Completó un desafío",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if ((evidencia.texto)?.isNotBlank() == true) {
                    Text(
                        text = evidencia.texto,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
                Text(
                    text = "Tipo: ${evidencia.tipo.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA259FF)
                )
            }
            // Icono de papelera solo si es del usuario actual
            if (currentUser != null && evidencia.userId == currentUser.uid) {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar evidencia") },
            text = { Text("¿Estás seguro de que deseas eliminar esta evidencia? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    // Aquí irá la lógica para eliminar la evidencia
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 