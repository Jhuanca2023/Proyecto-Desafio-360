@file:OptIn(androidx.media3.common.util.UnstableApi::class)

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
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.InsertComment
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.redsocial.models.Challenge
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.delay
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.media3.common.util.UnstableApi
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import com.example.redsocial.R
import com.example.redsocial.ui.theme.DarkBlue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import com.example.redsocial.ui.theme.BackgroundDark
import com.example.redsocial.ui.theme.ButtonPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChallengeDetail: (String) -> Unit,
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedTipo by remember { mutableStateOf("video") }
    var evidencias by remember { mutableStateOf(listOf<Evidencia>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTipo) {
        isLoading = true
        errorMsg = null
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("evidencias")
                .whereEqualTo("tipo", selectedTipo)
                .get()
                .await()
            
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
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            errorMsg = "Error al cargar evidencias: ${e.localizedMessage}"
            evidencias = emptyList()
        }
        isLoading = false
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
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF1A1F2E),
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF3B82F6)) },
                        label = { Text("Home", color = Color(0xFF3B82F6)) },
                        selected = true,
                        onClick = { /* Already on Home */ }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Explorar", tint = Color(0xFFCBD5E1)) },
                        label = { Text("Explorar", color = Color(0xFFCBD5E1)) },
                        selected = false,
                        onClick = onNavigateToExplore
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Crear", tint = Color(0xFFCBD5E1)) },
                        label = { Text("Crear", color = Color(0xFFCBD5E1)) },
                        selected = false,
                        onClick = onNavigateToCreate
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Avisos", tint = Color(0xFFCBD5E1)) },
                        label = { Text("Avisos", color = Color(0xFFCBD5E1)) },
                        selected = false,
                        onClick = onNavigateToNotifications
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color(0xFFCBD5E1)) },
                        label = { Text("Perfil", color = Color(0xFFCBD5E1)) },
                        selected = false,
                        onClick = onNavigateToProfile
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3B82F6))
                    }
                } else if (errorMsg != null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            errorMsg ?: "Error desconocido", 
                            color = Color(0xFFFF6B6B),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else if (evidencias.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No hay contenido disponible.", 
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { evidencias.size })
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        EvidenciaPage(
                            evidencia = evidencias[page],
                            onNavigateToChallengeDetail = onNavigateToChallengeDetail,
                            onCategoryClick = {
                                showBottomSheet = true
                            },
                            navController = navController
                        )
                    }
                }

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState,
                        containerColor = Color.Black.copy(alpha = 0.85f)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Categorías", 
                                style = MaterialTheme.typography.titleLarge, 
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = Color.White
                            )
                            listOf("video", "imagen", "texto", "audio").forEach { tipo ->
                                TextButton(
                                    onClick = {
                                        selectedTipo = tipo
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                showBottomSheet = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        tipo.replaceFirstChar { it.uppercase() },
                                        color = Color.White
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenciaPage(
    evidencia: Evidencia,
    onNavigateToChallengeDetail: (String) -> Unit,
    onCategoryClick: () -> Unit,
    navController: NavController
) {
    var challenge by remember { mutableStateOf<Challenge?>(null) }
    var showOptionsBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(evidencia.challengeId) {
        val db = FirebaseFirestore.getInstance()
        val challengeDoc = db.collection("desafios").document(evidencia.challengeId).get().await()
        challenge = challengeDoc.toObject(Challenge::class.java)?.copy(id = challengeDoc.id)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Media (Video/Image)
        when (evidencia.tipo) {
            "video" -> evidencia.url?.let { url ->
                VideoPlayer(
                    url = url,
                    onLongPress = { showOptionsBottomSheet = true }
                )
            }
            "imagen" -> evidencia.url?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Imagen de evidencia",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            "audio" -> evidencia.url?.let { url ->
                AudioPlayer(
                    url = url,
                    onLongPress = { showOptionsBottomSheet = true }
                )
            }
            "texto" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(evidencia.texto ?: "", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                }
            }
        }

        // UI Overlay
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Left side: Challenge Info & Actions
            Column(modifier = Modifier.weight(1f)) {
                challenge?.let {
                    Text(
                        text = it.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "By @${evidencia.userName}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            navController.navigate("userProfile/${evidencia.userId}")
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        onClick = { onNavigateToChallengeDetail(evidencia.challengeId) },
                        modifier = Modifier
                            .border(2.dp, Color.White, shape = RoundedCornerShape(50)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Participar", color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onCategoryClick,
                        modifier = Modifier
                            .border(2.dp, Color.White, shape = RoundedCornerShape(50)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Categoría", color = Color.White)
                    }
                }
            }

            // Right side: Social Actions (reemplazo la columna temporal por la lógica real)
            EvidenciaSocialActions(evidencia = evidencia, navController = navController)
        }
    }

    if (showOptionsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsBottomSheet = false },
            containerColor = Color.Black.copy(alpha = 0.85f)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            evidencia.url?.let {
                                clipboardManager.setText(AnnotatedString(it))
                                Toast.makeText(context, "Link copiado", Toast.LENGTH_SHORT).show()
                            }
                            showOptionsBottomSheet = false
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF444444), shape = CircleShape)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar link", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Copiar enlace", color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            evidencia.url?.let {
                                downloadVideo(context, it)
                            }
                            showOptionsBottomSheet = false
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF444444), shape = CircleShape)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Descargar", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Descargar", color = Color.White)
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(url: String, onLongPress: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    var showControls by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(exoPlayer, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = {
                        exoPlayer.playWhenReady = !exoPlayer.playWhenReady
                        showControls = true
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = if (exoPlayer.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )
        }

        if (showControls) {
            LaunchedEffect(Unit) {
                delay(800)
                showControls = false
            }
        }
    }
}

private fun downloadVideo(context: Context, url: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Descargando video...")
            .setDescription("Descarga en curso")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "video_${System.currentTimeMillis()}.mp4")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al descargar: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun EvidenciasFeed(evidencias: List<Evidencia>) {
    if (evidencias.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Aún no hay evidencias, ¡sé el primero en participar!", 
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    } else {
        LazyColumn {
            items(evidencias) { evidencia ->
                EvidenciaCard(evidencia)
            }
        }
    }
}

data class Comentario(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val texto: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ComentariosDialog(
    evidenciaId: String,
    onDismiss: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var comentarios by remember { mutableStateOf(listOf<Comentario>()) }
    var nuevoComentario by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }

    // Obtener el nombre del usuario actual
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            val userDoc = db.collection("usuarios").document(currentUser.uid).get().await()
            currentUserName = userDoc.getString("nombreUsuario") ?: currentUser.displayName ?: "Usuario"
        }
    }

    // Escuchar comentarios en tiempo real
    LaunchedEffect(evidenciaId) {
        db.collection("evidencias").document(evidenciaId)
            .collection("comentarios")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                comentarios = snapshot?.documents?.map { doc ->
                    Comentario(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        texto = doc.getString("texto") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comentarios", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (comentarios.isEmpty()) {
                    Text("Aún no hay comentarios.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(comentarios) { comentario ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "@${comentario.userName}",
                                            color = Color(0xFFA259FF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = formatTimestamp(comentario.timestamp),
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = comentario.texto,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comentando como: @$currentUserName",
                        color = Color(0xFFA259FF),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = nuevoComentario,
                    onValueChange = { nuevoComentario = it },
                    label = { Text("Escribe un comentario...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA259FF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFA259FF),
                        unfocusedLabelColor = Color.Gray
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nuevoComentario.isNotBlank() && currentUser != null) {
                        db.collection("evidencias").document(evidenciaId)
                            .collection("comentarios")
                            .add(
                                mapOf(
                                    "userId" to currentUser.uid,
                                    "userName" to currentUserName,
                                    "texto" to nuevoComentario,
                                    "timestamp" to System.currentTimeMillis()
                                )
                            )
                        nuevoComentario = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
            ) {
                Text("Enviar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cerrar", color = ButtonPrimary) 
            }
        },
        containerColor = Color.Black.copy(alpha = 0.85f)
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        else -> "${diff / 86400000}d"
    }
}

@Composable
fun EvidenciaCard(evidencia: Evidencia) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var likesCount by remember { mutableStateOf(0) }
    var isLiked by remember { mutableStateOf(false) }
    var commentsCount by remember { mutableStateOf(0) }
    var showComentarios by remember { mutableStateOf(false) }
    // Escuchar likes en tiempo real
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
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1B3D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ... Mostrar contenido de la evidencia (imagen, video, texto, audio) ...
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón Like
                IconButton(
                    onClick = {
                        val likesRef = db.collection("evidencias").document(evidencia.id).collection("likes")
                        val userLikeRef = likesRef.document(currentUser!!.uid)
                        if (isLiked) {
                            userLikeRef.delete()
                            db.collection("evidencias").document(evidencia.id).update("likes", FieldValue.increment(-1))
                            db.collection("usuarios").document(evidencia.userId).update("totalLikes", FieldValue.increment(-1))
                        } else {
                            userLikeRef.set(mapOf("userId" to currentUser.uid, "timestamp" to System.currentTimeMillis()))
                            db.collection("evidencias").document(evidencia.id).update("likes", FieldValue.increment(1))
                            db.collection("usuarios").document(evidencia.userId).update("totalLikes", FieldValue.increment(1))
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
                Spacer(Modifier.width(16.dp))
                // Botón Comentar
                IconButton(
                    onClick = { showComentarios = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(painterResource(id = R.drawable.ic_comment_outline), contentDescription = "Comentar", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("$commentsCount", color = Color.White)
                Spacer(Modifier.width(16.dp))
                // Botón Compartir
                IconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, evidencia.url ?: evidencia.texto ?: "Evidencia de FLUXI")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            if (showComentarios) {
                ComentariosDialog(evidenciaId = evidencia.id, onDismiss = { showComentarios = false })
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
                color = Color.White
            )
            Text(
                text = challenge.description,
                fontSize = 16.sp,
                color = Color(0xFFCBD5E1)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* Participar */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Participar", color = Color.White)
                }
                Button(
                    onClick = { /* Ver categoría */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                ) {
                    Text("Categoría", color = Color.White)
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

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayer(url: String, onLongPress: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(exoPlayer, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying) exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1333))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = {
                        if (isPlaying) {
                            exoPlayer.pause()
                            isPlaying = false
                        } else {
                            exoPlayer.play()
                            isPlaying = true
                        }
                        showControls = true
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Fondo con icono de micrófono
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFA259FF).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Audio",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "🎵 Audio Evidence",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Toca para reproducir",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Controles de reproducción
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        if (showControls) {
            LaunchedEffect(Unit) {
                delay(2000)
                showControls = false
            }
        }
    }
}

@Composable
fun EvidenciaSocialActions(evidencia: Evidencia, navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var likesCount by remember { mutableStateOf(0) }
    var isLiked by remember { mutableStateOf(false) }
    var commentsCount by remember { mutableStateOf(0) }
    var showComentarios by remember { mutableStateOf(false) }
    var commentsRestricted by remember { mutableStateOf(false) }
    var showRestrictDialog by remember { mutableStateOf(false) }
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
                    db.collection("evidencias").document(evidencia.id).update("likes", FieldValue.increment(-1))
                    db.collection("usuarios").document(evidencia.userId).update("totalLikes", FieldValue.increment(-1))
                } else {
                    userLikeRef.set(mapOf("userId" to currentUser.uid, "timestamp" to System.currentTimeMillis()))
                    db.collection("evidencias").document(evidencia.id).update("likes", FieldValue.increment(1))
                    db.collection("usuarios").document(evidencia.userId).update("totalLikes", FieldValue.increment(1))
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
        // Botón Comentar
        val puedeComentar = !commentsRestricted || (currentUser != null && evidencia.userId == currentUser.uid)
        IconButton(
            onClick = {
                if (puedeComentar) {
                    showComentarios = true
                } else {
                    showRestrictDialog = true
                }
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(painterResource(id = R.drawable.ic_comment_outline), contentDescription = "Comentar", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Text("$commentsCount", color = Color.White)
        Spacer(Modifier.height(24.dp))
        // Botón Compartir
        IconButton(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, evidencia.url ?: evidencia.texto ?: "Evidencia de FLUXI")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Text("", color = Color.White)
        Spacer(Modifier.height(24.dp))
        // Botón Guardar (opcional)
        IconButton(
            onClick = { /* TODO: lógica de guardado si la implementas */ },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Text("", color = Color.White)
    }
    if (showComentarios && (!commentsRestricted || (currentUser != null && evidencia.userId == currentUser.uid))) {
        ComentariosDialog(evidenciaId = evidencia.id, onDismiss = { showComentarios = false })
    }
    if (showRestrictDialog) {
        AlertDialog(
            onDismissRequest = { showRestrictDialog = false },
            title = { Text("Comentarios restringidos", color = Color.White) },
            text = { Text("El propietario de esta evidencia ha restringido los comentarios.", color = Color.White) },
            confirmButton = {
                TextButton(onClick = { showRestrictDialog = false }) {
                    Text("Aceptar", color = Color(0xFFA259FF))
                }
            },
            containerColor = Color.Black.copy(alpha = 0.85f)
        )
    }
} 