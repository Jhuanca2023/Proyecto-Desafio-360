package com.example.redsocial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocial.ui.components.ChallengePreview
import com.example.redsocial.ui.components.ChipPreview
import com.example.redsocial.ui.components.ChallengeCard
import com.example.redsocial.models.Evidencia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedTipo by remember { mutableStateOf("video") }
    var evidencias by remember { mutableStateOf(listOf<Evidencia>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedTipo) {
        isLoading = true
        errorMsg = null
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collectionGroup("evidencias").get().await()
            evidencias = snapshot.documents.mapNotNull { doc ->
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
            }.filter { it.tipo == selectedTipo }
        } catch (e: Exception) {
            errorMsg = "Error al cargar evidencias: ${e.localizedMessage}"
            evidencias = emptyList()
        }
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Explorar") },
                    label = { Text("Explorar") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        onNavigateToExplore()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                    label = { Text("Crear") },
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        onNavigateToCreate()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Avisos") },
                    label = { Text("Avisos") },
                    selected = selectedTab == 3,
                    onClick = { 
                        selectedTab = 3
                        onNavigateToNotifications()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedTab == 4,
                    onClick = { 
                        selectedTab = 4
                        onNavigateToProfile()
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (selectedTab == 0) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("video", "imagen", "texto", "audio").forEach { tipo ->
                            Button(
                                onClick = { selectedTipo = tipo },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTipo == tipo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(tipo.replaceFirstChar { it.uppercase() })
                            }
                        }
                    }
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMsg != null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(errorMsg ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        EvidenciasFeed(evidencias)
                    }
                }
            }
        }
    }
}

@Composable
fun EvidenciasFeed(evidencias: List<Evidencia>) {
    if (evidencias.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Aún no hay evidencias, ¡sé el primero en participar!", style = MaterialTheme.typography.titleMedium)
        }
    } else {
        LazyColumn {
            items(evidencias) { evidencia ->
                EvidenciaCard(evidencia)
            }
        }
    }
}

@Composable
fun EvidenciaCard(evidencia: Evidencia) {
    // Aquí puedes mostrar el contenido según el tipo (video, imagen, texto, audio)
    // Por ejemplo, si es video, usa un reproductor de video, si es imagen usa AsyncImage, etc.
    // Puedes personalizar este componente según tus necesidades.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("@${evidencia.userName}", fontWeight = FontWeight.Bold)
            Text(evidencia.tipo.uppercase())
            evidencia.url?.let { url ->
                if (evidencia.tipo == "imagen") {
                    // Mostrar imagen
                    // AsyncImage(model = url, contentDescription = null)
                } else if (evidencia.tipo == "video") {
                    // Mostrar video (puedes usar un VideoPlayer si tienes uno)
                } else if (evidencia.tipo == "audio") {
                    // Mostrar audio (puedes usar un AudioPlayer si tienes uno)
                }
            }
            evidencia.texto?.let { texto ->
                if (evidencia.tipo == "texto") {
                    Text(texto)
                }
            }
        }
    }
}

@Composable
fun ChallengesFeed(challenges: List<ChallengePreview>) {
    LazyColumn {
        items(challenges) { challenge ->
            ChallengeCard(challenge = challenge)
        }
    }
}

@Composable
fun ChallengeCard(challenge: ChallengePreview) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
    ) {
        //  contenido del desafío (video/imagen)
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

private fun generateSampleChallenges() = listOf(
    ChallengePreview("1", "30-Day Sentadillas", "Desafío de sentadillas por 30 días", "Deporte", "30 días", 100, listOf("video", "imagen"), null),
    ChallengePreview("2", "Pinta tu Atardecer Favorito", "Desafío de pintura de atardeceres", "Arte", "7 días", 50, listOf("imagen", "texto"), null),
    ChallengePreview("3", "Reto Musical Semanal", "Desafío de música semanal", "Música", "1 semana", 75, listOf("audio", "video"), null)
) 