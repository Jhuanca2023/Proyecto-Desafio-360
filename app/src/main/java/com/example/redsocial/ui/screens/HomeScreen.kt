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
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FavoriteBorder
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChallengeDetail: (String) -> Unit
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { /* Already on Home */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Explorar") },
                    label = { Text("Explorar") },
                    selected = false,
                    onClick = onNavigateToExplore
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                    label = { Text("Crear") },
                    selected = false,
                    onClick = onNavigateToCreate
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Avisos") },
                    label = { Text("Avisos") },
                    selected = false,
                    onClick = onNavigateToNotifications
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
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
                    CircularProgressIndicator()
                }
            } else if (errorMsg != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMsg ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
                }
            } else if (evidencias.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay contenido disponible.", style = MaterialTheme.typography.titleMedium)
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
                        }
                    )
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Categorías", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
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
                                Text(tipo.replaceFirstChar { it.uppercase() })
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
    onCategoryClick: () -> Unit
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
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = { onNavigateToChallengeDetail(evidencia.challengeId) }) {
                        Text("Participate")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onCategoryClick) {
                        Text("Category")
                    }
                }
            }

            // Right side: Social Actions
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("2.3k", color = Color.White)
                Spacer(Modifier.height(24.dp))

                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comment", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("1.2k", color = Color.White)
                Spacer(Modifier.height(24.dp))

                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("500", color = Color.White)
                Spacer(Modifier.height(24.dp))

                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                 Text("100", color = Color.White)
            }
        }
    }

    if (showOptionsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsBottomSheet = false }
        ) {
            Column(Modifier.padding(vertical = 16.dp)) {
                TextButton(
                    onClick = {
                        evidencia.url?.let {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Link copiado", Toast.LENGTH_SHORT).show()
                        }
                        showOptionsBottomSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Copiar link")
                }
                TextButton(
                    onClick = {
                        evidencia.url?.let {
                            downloadVideo(context, it)
                        }
                        showOptionsBottomSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Descargar")
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
    var challengeTitle by remember { mutableStateOf("") }
    var challengeDescription by remember { mutableStateOf("") }

    // Obtener información del desafío
    LaunchedEffect(evidencia.challengeId) {
        val db = FirebaseFirestore.getInstance()
        val challengeDoc = db.collection("desafios").document(evidencia.challengeId).get().await()
        challengeTitle = challengeDoc.getString("title") ?: ""
        challengeDescription = challengeDoc.getString("description") ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = challengeTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Por: @${evidencia.userName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            evidencia.url?.let { url ->
                if (evidencia.tipo == "imagen") {
                    AsyncImage(
                        model = url,
                        contentDescription = "Imagen de evidencia",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else if (evidencia.tipo == "video") {
                    // Mostrar video usando AndroidView y VideoView
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { context ->
                            android.widget.VideoView(context).apply {
                                setVideoPath(url)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (evidencia.texto != null) {
                Text(
                    text = evidencia.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
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