package com.example.redsocial.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.redsocial.R
import com.example.redsocial.models.Evidencia
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.redsocial.ui.theme.BackgroundDark

@Composable
fun EvidenciaViewerDialog(
    evidencia: Evidencia?,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(0) }
    var isLiked by remember { mutableStateOf(false) }
    var commentsCount by remember { mutableStateOf(0) }
    var showComentarios by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var commentsRestricted by remember { mutableStateOf(false) }
    var downloadsAllowed by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(evidencia?.id) {
        evidencia?.let {
            val likesRef = db.collection("evidencias").document(it.id).collection("likes")
            likesRef.addSnapshotListener { snapshot, _ ->
                likesCount = snapshot?.size() ?: 0
                isLiked = snapshot?.documents?.any { doc -> doc.id == currentUser?.uid } == true
            }
            val commentsRef = db.collection("evidencias").document(it.id).collection("comentarios")
            commentsRef.addSnapshotListener { snapshot, _ ->
                commentsCount = snapshot?.size() ?: 0
            }
            db.collection("evidencias").document(it.id)
                .addSnapshotListener { snapshot, _ ->
                    commentsRestricted = snapshot?.getBoolean("commentsRestricted") ?: false
                    downloadsAllowed = snapshot?.getBoolean("downloadsAllowed") ?: true
                }
        }
    }

    if (visible && evidencia != null) {
        // Incrementar vistas solo una vez
        LaunchedEffect(evidencia.id) {
            db.collection("evidencias").document(evidencia.id)
                .update("views", com.google.firebase.firestore.FieldValue.increment(1))
        }
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(BackgroundDark.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                // Media
                when (evidencia.tipo) {
                    "video" -> {
                        val url = evidencia.url ?: ""
                        val exoPlayer = remember(url) {
                            ExoPlayer.Builder(context).build().apply {
                                setMediaItem(MediaItem.fromUri(url))
                                prepare()
                                playWhenReady = true
                                repeatMode = Player.REPEAT_MODE_ONE
                            }
                        }
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
                        Box(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                factory = {
                                    PlayerView(it).apply {
                                        player = exoPlayer
                                        useController = false
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                exoPlayer.playWhenReady = !exoPlayer.playWhenReady
                                                showControls = true
                                            }
                                        )
                                    }
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
                                    kotlinx.coroutines.delay(800)
                                    showControls = false
                                }
                            }
                        }
                    }
                    "imagen" -> {
                        evidencia.url?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Imagen",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .aspectRatio(9f / 16f)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                    "texto" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1A1F2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                evidencia.texto ?: "",
                                color = Color.White,
                                fontSize = 18.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    "audio" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1A1F2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Audiotrack, contentDescription = "Audio", tint = Color.White, modifier = Modifier.size(64.dp))
                            // Aquí podrías agregar un botón para reproducir el audio
                        }
                    }
                }
                // Overlay de info y acciones sociales
                evidencia.let { ev ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Info izquierda
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = ev.descripcion ?: "Evidencia completada",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By @${ev.userName}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        // Acciones sociales
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            // Botón Like
                            IconButton(
                                onClick = {
                                    val likesRef = db.collection("evidencias").document(ev.id).collection("likes")
                                    val userLikeRef = likesRef.document(currentUser!!.uid)
                                    if (isLiked) {
                                        userLikeRef.delete()
                                        db.collection("evidencias").document(ev.id).update("likes", com.google.firebase.firestore.FieldValue.increment(-1))
                                        db.collection("usuarios").document(ev.userId).update("totalLikes", com.google.firebase.firestore.FieldValue.increment(-1))
                                    } else {
                                        userLikeRef.set(mapOf("userId" to currentUser.uid, "timestamp" to System.currentTimeMillis()))
                                        db.collection("evidencias").document(ev.id).update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                                        db.collection("usuarios").document(ev.userId).update("totalLikes", com.google.firebase.firestore.FieldValue.increment(1))
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
                            val puedeComentar = !commentsRestricted || (currentUser != null && ev.userId == currentUser.uid)
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
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, ev.url ?: ev.texto ?: "Evidencia de FLUXI")
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
                            // Menú de 3 puntos solo para el dueño
                            if (currentUser != null && ev.userId == currentUser.uid) {
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = Color.White, modifier = Modifier.size(32.dp))
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                                            expanded = false
                                            db.collection("evidencias").document(ev.id).delete()
                                        })
                                        DropdownMenuItem(text = { Text(if (commentsRestricted) "Activar comentarios" else "Restringir comentarios") }, onClick = {
                                            expanded = false
                                            db.collection("evidencias").document(ev.id).update("commentsRestricted", !commentsRestricted)
                                        })
                                    }
                                }
                            } else {
                                Spacer(Modifier.height(32.dp))
                            }
                        }
                    }
                }
                if (showComentarios && (!commentsRestricted || (currentUser != null && evidencia.userId == currentUser.uid))) {
                    com.example.redsocial.ui.screens.ComentariosDialog(evidenciaId = evidencia.id, onDismiss = { showComentarios = false })
                }
            }
        }
    }
} 