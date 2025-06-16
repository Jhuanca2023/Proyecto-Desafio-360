package com.example.redsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocial.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NavController

@Composable
fun ProfileScreen(
    navController: NavController,
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    var desafios by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var totalLikes by remember { mutableStateOf(0) }
    var seguidores by remember { mutableStateOf(0) }
    var siguiendo by remember { mutableStateOf(0) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("desafios").whereEqualTo("authorId", uid).get().await()
            desafios = snapshot.documents.mapNotNull { it.data }
            totalLikes = desafios.sumOf { (it["likes"] as? Long ?: 0L).toInt() }
            val userDoc = db.collection("usuarios").document(uid).get().await()
            seguidores = (userDoc.get("seguidores") as? Long ?: 0L).toInt()
            siguiendo = (userDoc.get("siguiendo") as? Long ?: 0L).toInt()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF1A1333),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { navController.navigate("ajustes") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mi Perfil",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Información del usuario
            userData?.let { data ->
                Text(
                    text = data["nombreCompleto"] as? String ?: "Sin nombre",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "@${data["nombreUsuario"] as? String ?: ""}",
                    color = Color(0xFFA259FF),
                    fontSize = 18.sp
                )

                Text(
                    text = data["biografia"] as? String ?: "Sin biografía",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Estadísticas visuales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(Icons.Default.Verified, "Completado", (desafios.count { (it["participants"] as? List<*>)?.size == (it["maxParticipants"] as? Long)?.toInt() }).takeIf { it > 0 }?.toString() ?: "0", Color(0xFF00C853))
                StatisticCard(Icons.Default.Star, "En Curso", (desafios.count { ((it["participants"] as? List<*>)?.size ?: 0) < ((it["maxParticipants"] as? Long) ?: 0L).toInt() }).takeIf { it > 0 }?.toString() ?: "0", Color(0xFF2962FF))
                StatisticCard(Icons.Default.Favorite, "Likes", totalLikes.takeIf { it > 0 }?.toString() ?: "0", Color(0xFFFF4081))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(Icons.Default.People, "Seguidores", seguidores.takeIf { it > 0 }?.toString() ?: "0", Color(0xFFA259FF))
                StatisticCard(Icons.Default.People, "Siguiendo", siguiendo.takeIf { it > 0 }?.toString() ?: "0", Color(0xFF00B8D4))
                StatisticCard(Icons.Default.Star, "Badges", "0", Color(0xFFFFD600))
            }

            Text("Mis Desafíos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "Desliza hacia abajo para ver todos tus desafíos",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Lista de desafíos
            desafios.forEach { desafio: Map<String, Any> ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        AsyncImage(
                            model = desafio["coverImageUrl"] as? String,
                            contentDescription = "Imagen de portada",
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(desafio["title"] as? String ?: "", fontWeight = FontWeight.Bold)
                            val participantes = (desafio["participants"] as? List<*>)?.size ?: 0
                            val maxP = (desafio["maxParticipants"] as? Long)?.toInt() ?: 1
                            val activo = participantes < maxP
                            Text(if (activo) "Activo" else "Inactivo", color = if (activo) Color.Green else Color.Red)
                            Text("Participantes: $participantes/$maxP")
                            Text("Likes: ${(desafio["likes"] as? Long ?: 0L)}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(100.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = color)
            Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
            Text(label, color = Color.White, fontSize = 12.sp)
        }
    }
} 