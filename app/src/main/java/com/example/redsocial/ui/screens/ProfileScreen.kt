@file:OptIn(androidx.media3.common.util.UnstableApi::class)
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocial.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage
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
import com.example.redsocial.utils.NotificationUtils
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.items
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MoreVert
import com.example.redsocial.R
import com.example.redsocial.ui.components.EvidenciaViewerDialog

@OptIn(ExperimentalFoundationApi::class, androidx.media3.common.util.UnstableApi::class)
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
    var badges by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showEvidenciaDialog by remember { mutableStateOf<Evidencia?>(null) }

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
                    descripcion = data["descripcion"] as? String,
                    timestamp = data["timestamp"] as? Long ?: 0L
                )
            }
            
            val userDoc = db.collection("usuarios").document(uid).get().await()
            val totalLikesEvidencias = (userDoc.get("totalLikes") as? Long ?: 0L).toInt()
            val totalLikesDesafios = desafios.sumOf { (it["likes"] as? Long ?: 0L).toInt() }
            totalLikes = totalLikesEvidencias + totalLikesDesafios
            seguidores = (userDoc.get("seguidores") as? Long ?: 0L).toInt()
            siguiendo = (userDoc.get("siguiendo") as? Long ?: 0L).toInt()
            badges = (userDoc.get("badges") as? Long ?: 0L).toInt()
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate("ajustes") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = Color(0xFF60A5FA))
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Color(0xFF60A5FA))
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
                            .background(Color(0xFF3B82F6))
                            .clickable(enabled = !isUploading) { launcher.launch("image/*") }
                    )
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color.White)
                    }
                }
                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color(0xFFFF6B6B), fontSize = 14.sp)
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
                        color = Color(0xFFCBD5E1),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Estadísticas visuales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatisticCard(Icons.Default.Verified, "Completado", (desafios.count { (it["participants"] as? Long)?.toInt() == (it["maxParticipants"] as? Long)?.toInt() }).takeIf { it > 0 }?.toString() ?: "0", Color(0xFF00C853))
                    ProfileStatisticCard(Icons.Default.Star, "En Curso", (desafios.count { ((it["participants"] as? Long)?.toInt() ?: 0) < ((it["maxParticipants"] as? Long) ?: 0L).toInt() }).takeIf { it > 0 }?.toString() ?: "0", Color(0xFF2962FF))
                    ProfileStatisticCard(Icons.Default.Favorite, "Likes", totalLikes.takeIf { it > 0 }?.toString() ?: "0", Color(0xFFFF4081))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatisticCard(Icons.Default.People, "Seguidores", seguidores.takeIf { it > 0 }?.toString() ?: "0", Color(0xFFA259FF))
                    ProfileStatisticCard(Icons.Default.People, "Siguiendo", siguiendo.takeIf { it > 0 }?.toString() ?: "0", Color(0xFF00B8D4))
                    ProfileStatisticCard(Icons.Default.Star, "Badges", badges.takeIf { it > 0 }?.toString() ?: "0", Color(0xFFFFD600))
                    if (badges >= 5000) {
                        ProfileStatisticCard(Icons.Default.Verified, "Verificado", "✔", Color(0xFF00C853))
                    }
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
                            color = Color(0xFFCBD5E1),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (desafios.isEmpty()) {
                            Text(
                                text = "No has creado desafíos aún",
                                color = Color(0xFFCBD5E1),
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
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F2E))
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            AsyncImage(
                                                model = desafio["coverImageUrl"] as? String,
                                                contentDescription = "Imagen de portada",
                                                modifier = Modifier.size(80.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    desafio["title"] as? String ?: "", 
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                val participantes = (desafio["participants"] as? Long)?.toInt() ?: 0
                                                val maxP = (desafio["maxParticipants"] as? Long)?.toInt() ?: 1
                                                val activo = participantes < maxP
                                                Text(
                                                    if (activo) "Activo" else "Inactivo", 
                                                    color = if (activo) Color(0xFF00C853) else Color(0xFFFF6B6B)
                                                )
                                                Text("Participantes: $participantes/$maxP", color = Color(0xFFCBD5E1))
                                                Text("Likes: ${(desafio["likes"] as? Long ?: 0L)}", color = Color(0xFFCBD5E1))
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
                            color = Color(0xFFCBD5E1),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (evidencias.isEmpty()) {
                            Text(
                                text = "No has completado desafíos aún",
                                color = Color(0xFFCBD5E1),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(32.dp)
                            )
                        } else {
                            // Agrupar evidencias por tipo
                            val videos = evidencias.filter { it.tipo == "video" }
                            val imagenes = evidencias.filter { it.tipo == "imagen" }
                            val textos = evidencias.filter { it.tipo == "texto" }
                            val audios = evidencias.filter { it.tipo == "audio" }

                            // Sección Videos
                            if (videos.isNotEmpty()) {
                                Text("Videos", color = Color(0xFFA259FF), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                Divider(color = Color(0xFFA259FF), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.heightIn(max = 300.dp)
                                ) {
                                    items(videos) { evidencia ->
                                        val context = LocalContext.current
                                        val exoPlayer = remember(evidencia.url) {
                                            androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                                                setMediaItem(androidx.media3.common.MediaItem.fromUri(evidencia.url!!))
                                                prepare()
                                                playWhenReady = true // autoplay
                                                volume = 0f // sin audio
                                                repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
                                            }
                                        }
                                        DisposableEffect(exoPlayer) {
                                            onDispose { exoPlayer.release() }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .aspectRatio(9f/16f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1A1F2E))
                                                .clickable { showEvidenciaDialog = evidencia },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AndroidView(
                                                factory = {
                                                    androidx.media3.ui.PlayerView(it).apply {
                                                        player = exoPlayer
                                                        useController = false
                                                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                                    }
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            // Vistas en la esquina inferior izquierda
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(8.dp)
                                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Vistas", tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = evidencia.views.toString(),
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            // Sección Imágenes
                            if (imagenes.isNotEmpty()) {
                                Text("Imágenes", color = Color(0xFFA259FF), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                Divider(color = Color(0xFFA259FF), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.heightIn(max = 300.dp)
                                ) {
                                    items(imagenes) { evidencia ->
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .aspectRatio(9f/16f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1A1F2E)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            evidencia.url?.let { url ->
                                                AsyncImage(
                                                    model = url,
                                                    contentDescription = "Imagen",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            // Sección Texto
                            if (textos.isNotEmpty()) {
                                Text("Textos", color = Color(0xFFA259FF), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                Divider(color = Color(0xFFA259FF), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.heightIn(max = 200.dp)
                                ) {
                                    items(textos) { evidencia ->
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .aspectRatio(9f/16f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1A1F2E)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                evidencia.texto ?: "",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            // Sección Audio
                            if (audios.isNotEmpty()) {
                                Text("Audios", color = Color(0xFFA259FF), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                Divider(color = Color(0xFFA259FF), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.heightIn(max = 200.dp)
                                ) {
                                    items(audios) { evidencia ->
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .aspectRatio(9f/16f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1A1F2E)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Audiotrack, contentDescription = "Audio", tint = Color.White, modifier = Modifier.size(32.dp))
                                            // Aquí podrías agregar un botón para reproducir el audio
                                        }
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
            title = { Text("¿Eliminar desafío?", color = Color.White) },
            text = { Text("Esta acción eliminará el desafío de tu perfil, de explorar y de la base de datos. ¿Deseas continuar?", color = Color(0xFFCBD5E1)) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF3B82F6)
                    )
                ) {
                    Text("Cancelar", color = Color(0xFF3B82F6))
                }
            },
            containerColor = Color(0xFF1A1F2E)
        )
    }

    // Modal de evidencia pantalla completa
    EvidenciaViewerDialog(
        evidencia = showEvidenciaDialog,
        visible = showEvidenciaDialog != null,
        onDismiss = { showEvidenciaDialog = null }
    )
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

@Composable
fun EvidenciaSocialActionsPerfilModal(evidencia: Evidencia) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var likesCount by remember { mutableStateOf(0) }
    var isLiked by remember { mutableStateOf(false) }
    var commentsCount by remember { mutableStateOf(0) }
    var showComentarios by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var commentsRestricted by remember { mutableStateOf(false) }
    // Escuchar likes, comentarios y restricción en tiempo real
    LaunchedEffect(evidencia.id) {
        val likesRef = db.collection("evidencias").document(evidencia.id).collection("likes")
        likesRef.addSnapshotListener { snapshot, _ ->
            likesCount = snapshot?.size() ?: 0
            isLiked = snapshot?.documents?.any { it.id == currentUser?.uid } == true
        }
        val commentsRef = db.collection("evidencias").document(evidencia.id).collection("comentarios")
        commentsRef.addSnapshotListener { snapshot, _ ->
            commentsCount = snapshot?.size() ?: 0
        }
        db.collection("evidencias").document(evidencia.id)
            .addSnapshotListener { snapshot, _ ->
                commentsRestricted = snapshot?.getBoolean("commentsRestricted") ?: false
            }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Botón Like
        IconButton(
            onClick = {
                val likesRef = db.collection("evidencias").document(evidencia.id).collection("likes")
                val userLikeRef = likesRef.document(currentUser!!.uid)
                if (isLiked) {
                    userLikeRef.delete()
                    db.collection("evidencias").document(evidencia.id).update("likes", com.google.firebase.firestore.FieldValue.increment(-1))
                    db.collection("usuarios").document(evidencia.userId).update("totalLikes", com.google.firebase.firestore.FieldValue.increment(-1))
                } else {
                    userLikeRef.set(mapOf("userId" to currentUser.uid, "timestamp" to System.currentTimeMillis()))
                    db.collection("evidencias").document(evidencia.id).update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                    db.collection("usuarios").document(evidencia.userId).update("totalLikes", com.google.firebase.firestore.FieldValue.increment(1))
                }
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Text("$likesCount", color = Color.White)
        Spacer(Modifier.height(24.dp))
        // Botón Comentar (solo si no está restringido o si es el dueño)
        val puedeComentar = !commentsRestricted || (currentUser != null && evidencia.userId == currentUser.uid)
        if (puedeComentar) {
            IconButton(
                onClick = { showComentarios = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(painterResource(id = R.drawable.ic_comment_outline), contentDescription = "Comentar", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text("$commentsCount", color = Color.White)
        } else {
            Icon(Icons.Filled.Comment, contentDescription = "Comentarios restringidos", tint = Color.Gray, modifier = Modifier.size(32.dp))
            Text("Comentarios restringidos", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(24.dp))
        // Botón Compartir
        IconButton(
            onClick = {
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, evidencia.url ?: evidencia.texto ?: "Evidencia de FLUXI")
                    type = "text/plain"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Text("", color = Color.White)
        Spacer(Modifier.height(24.dp))
        // Menú de 3 puntos solo para el dueño
        if (currentUser != null && evidencia.userId == currentUser.uid) {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                        expanded = false
                        db.collection("evidencias").document(evidencia.id).delete()
                    })
                    DropdownMenuItem(text = { Text(if (commentsRestricted) "Activar comentarios" else "Restringir comentarios") }, onClick = {
                        expanded = false
                        db.collection("evidencias").document(evidencia.id).update("commentsRestricted", !commentsRestricted)
                    })
                }
            }
        } else {
            Spacer(Modifier.height(32.dp))
        }
    }
    if (showComentarios && (!commentsRestricted || (currentUser != null && evidencia.userId == currentUser.uid))) {
        ComentariosDialog(evidenciaId = evidencia.id, onDismiss = { showComentarios = false })
    }
} 