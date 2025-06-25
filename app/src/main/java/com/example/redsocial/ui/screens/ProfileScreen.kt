package com.example.redsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.TextFields
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.navigation.NavController
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.redsocial.models.Evidencia
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import java.io.InputStream
import com.example.redsocial.utils.uploadImageToImgur
import android.util.Base64
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    var desafios by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var evidencias by remember { mutableStateOf(listOf<Evidencia>()) }
    var totalLikes by remember { mutableStateOf(0) }
    var seguidores by remember { mutableStateOf(0) }
    var siguiendo by remember { mutableStateOf(0) }
    var showActionButtonsForId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var desafioToDelete by remember { mutableStateOf<Map<String, Any>?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            
            // Obtener desafíos creados
            val snapshot = db.collection("desafios").whereEqualTo("authorId", uid).get().await()
            desafios = snapshot.documents.mapNotNull { doc ->
                doc.data?.toMutableMap()?.apply { put("id", doc.id) }
            }
            
            // Obtener evidencias completadas
            val evidenciasSnapshot = db.collection("evidencias").whereEqualTo("userId", uid).get().await()
            evidencias = evidenciasSnapshot.documents.mapNotNull { doc ->
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate("ajustes") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = Color.White)
                }
                IconButton(onClick = onSignOut) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Color.White)
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

            // FOTO DE PERFIL EDITABLE
            var isUploading by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }
            val clientId = "e88c7011ed88321" // Imgur
            val currentPhotoUrl = userData?.get("photoUrl") as? String
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    isUploading = true
                    errorMsg = null
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        if (bytes != null) {
                            uploadImageToImgur(
                                imageBytes = bytes,
                                clientId = clientId,
                                onSuccess = { imageUrl ->
                                    authViewModel.updateProfilePhoto(imageUrl,
                                        onSuccess = { isUploading = false },
                                        onError = { msg ->
                                            isUploading = false
                                            errorMsg = msg
                                        }
                                    )
                                },
                                onError = { msg ->
                                    isUploading = false
                                    errorMsg = msg
                                }
                            )
                        } else {
                            isUploading = false
                            errorMsg = "No se pudo leer la imagen"
                        }
                    } catch (e: Exception) {
                        isUploading = false
                        errorMsg = "Error al procesar la imagen: ${e.message}"
                    }
                }
            }
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = currentPhotoUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFA259FF))
                        .clickable(enabled = !isUploading) { launcher.launch("image/*") }
                )
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color.White)
                }
            }
            if (errorMsg != null) {
                Text(errorMsg!!, color = Color.Red, fontSize = 14.sp)
            }

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
                ProfileStatisticCard(Icons.Default.Verified, "Completado", (desafios.count { (it["participants"] as? List<*>)?.size == (it["maxParticipants"] as? Long)?.toInt() }).takeIf { it > 0 }?.toString() ?: "0", Color(0xFF00C853))
                ProfileStatisticCard(Icons.Default.Star, "En Curso", (desafios.count { ((it["participants"] as? List<*>)?.size ?: 0) < ((it["maxParticipants"] as? Long) ?: 0L).toInt() }).takeIf { it > 0 }?.toString() ?: "0", Color(0xFF2962FF))
                ProfileStatisticCard(Icons.Default.Favorite, "Likes", totalLikes.takeIf { it > 0 }?.toString() ?: "0", Color(0xFFFF4081))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatisticCard(Icons.Default.People, "Seguidores", seguidores.takeIf { it > 0 }?.toString() ?: "0", Color(0xFFA259FF))
                ProfileStatisticCard(Icons.Default.People, "Siguiendo", siguiendo.takeIf { it > 0 }?.toString() ?: "0", Color(0xFF00B8D4))
                ProfileStatisticCard(Icons.Default.Star, "Badges", "0", Color(0xFFFFD600))
            }

            // Tabs
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

            // Contenido de las tabs
            when (selectedTab) {
                0 -> {
                    // Mis Desafíos
                    Text("Mis Desafíos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Desliza hacia abajo para ver todos tus desafíos",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (desafios.isEmpty()) {
                        Text(
                            text = "No has creado desafíos aún",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(32.dp)
                        )
                    } else {
                        desafios.forEach { desafio: Map<String, Any> ->
                            val desafioId = desafio["id"] as? String ?: desafio["documentId"] as? String
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { showActionButtonsForId = desafioId }
                                )
                            ) {
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
                                if (showActionButtonsForId == desafioId) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(onClick = {
                                            navController.navigate("editarDesafio/${desafioId}")
                                            showActionButtonsForId = null
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFFA259FF))
                                        }
                                        IconButton(onClick = {
                                            desafioToDelete = desafio
                                            showDeleteDialog = true
                                            showActionButtonsForId = null
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Evidencias
                    Text("Mis Evidencias", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Desafíos que has completado",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (evidencias.isEmpty()) {
                        Text(
                            text = "No has completado desafíos aún",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(32.dp)
                        )
                    } else {
                        evidencias.forEach { evidencia ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(modifier = Modifier.padding(8.dp)) {
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
                                            text = "Completaste un desafío",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (evidencia.texto != null) {
                                            Text(
                                                text = evidencia.texto,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = "Tipo: ${evidencia.tipo.replaceFirstChar { it.uppercase() }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFA259FF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && desafioToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar desafío?") },
            text = { Text("Esta acción eliminará el desafío de tu perfil, de explorar y de la base de datos. ¿Deseas continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        val desafioId = desafioToDelete?.get("id") as? String ?: desafioToDelete?.get("documentId") as? String
                        if (desafioId != null) {
                            scope.launch {
                                try {
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("desafios").document(desafioId).delete().await()
                                    desafios = desafios.filterNot { (it["id"] ?: it["documentId"]) == desafioId }
                                } catch (e: Exception) {
                                    // Opcional: mostrar error
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = Color(0xFF18122B)
        )
    }
}

@Composable
private fun ProfileStatisticCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
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