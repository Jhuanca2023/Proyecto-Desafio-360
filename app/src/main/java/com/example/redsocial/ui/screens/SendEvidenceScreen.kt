package com.example.redsocial.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.redsocial.ui.components.ChipPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.redsocial.utils.uploadImageToImgur
import com.example.redsocial.utils.uploadVideoToSupabase
import com.example.redsocial.utils.AudioUtils
import android.widget.Toast
import com.example.redsocial.utils.NetworkUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import java.io.File
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.redsocial.ui.theme.BackgroundDark

@Composable
fun SendEvidenceScreen(
    challengeId: String,
    challengeTitle: String,
    onEvidenceSent: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clientId = "e88c7011ed88321" // Imgur
    val user = FirebaseAuth.getInstance().currentUser
    var currentUserName by remember { mutableStateOf("") }

    // Obtener el nombre de usuario real desde Firestore
    LaunchedEffect(user?.uid) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("usuarios").document(user.uid).get().await()
            currentUserName = userDoc.getString("nombreUsuario") ?: user.displayName ?: "Usuario"
        }
    }

    var tipo by remember { mutableStateOf("imagen") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var texto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var videoFileName by remember { mutableStateOf<String?>(null) }
    
    // Variables para audio
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var audioFileName by remember { mutableStateOf<String?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        videoUri = uri
        videoFileName = uri?.lastPathSegment
    }

    val tipos = listOf("imagen", "video", "texto", "audio")
    val scrollState = rememberScrollState()

    // Pantalla completa con color de fondo de la app
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Header con botón de regreso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "📸 Enviar Evidencia", 
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        "Desafío: $challengeTitle", 
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Tipo de evidencia
            Text(
                "Selecciona el tipo de evidencia", 
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Cuadrícula 2x2 para tipos de evidencia
            val evidenciaItems = listOf(
                Triple("video", Icons.Default.Videocam, "Video"),
                Triple("imagen", Icons.Default.Image, "Imagen"),
                Triple("texto", Icons.Default.TextFields, "Texto"),
                Triple("audio", Icons.Default.Audiotrack, "Audio")
            )
            Column(Modifier.fillMaxWidth()) {
                for (row in 0..1) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (col in 0..1) {
                            val idx = row * 2 + col
                            val (t, icon, label) = evidenciaItems[idx]
                            val seleccionado = tipo == t
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                border = if (seleccionado) BorderStroke(3.dp, Color.White) else BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                color = if (seleccionado) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { tipo = t }
                                    .shadow(
                                        elevation = if (seleccionado) 12.dp else 4.dp,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                            ) {
                                Column(
                                    Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 18.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    if (row == 0) Spacer(Modifier.height(16.dp))
                }
            }
            Spacer(Modifier.height(40.dp))

            // Contenido específico del tipo seleccionado
            when (tipo) {
                "imagen" -> {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = { imageLauncher.launch("image/*") },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(28.dp), tint = BackgroundDark)
                                Spacer(Modifier.width(16.dp))
                                Text("Seleccionar Imagen", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = BackgroundDark)
                            }
                            imageBitmap?.let {
                                Spacer(Modifier.height(20.dp))
                                Image(
                                    bitmap = it.asImageBitmap(), 
                                    contentDescription = "Imagen seleccionada", 
                                    modifier = Modifier
                                        .size(180.dp)
                                        .shadow(12.dp, RoundedCornerShape(20.dp))
                                )
                            }
                        }
                    }
                }
                "video" -> {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = { videoLauncher.launch("video/*") },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(28.dp), tint = BackgroundDark)
                                Spacer(Modifier.width(16.dp))
                                Text("Seleccionar Video", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = BackgroundDark)
                            }
                            videoFileName?.let {
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    "Video seleccionado: $it", 
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                "texto" -> {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { texto = it },
                            label = { Text("Escribe tu evidencia", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.8f))) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(28.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                cursorColor = Color.White
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = Color.White)
                        )
                    }
                }
                "audio" -> {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(
                            Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            if (!isRecording && audioFile == null) {
                                Button(
                                    onClick = {
                                        val success = AudioUtils.startRecording(context) { error ->
                                            errorMessage = error
                                        }
                                        if (success) {
                                            isRecording = true
                                            errorMessage = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = "Grabar", modifier = Modifier.size(28.dp), tint = BackgroundDark)
                                    Spacer(Modifier.width(16.dp))
                                    Text("Iniciar Grabación", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = BackgroundDark)
                                }
                            } else if (isRecording) {
                                Button(
                                    onClick = {
                                        audioFile = AudioUtils.stopRecording()
                                        isRecording = false
                                        audioFileName = "audio_${System.currentTimeMillis()}.mp3"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = "Detener", modifier = Modifier.size(28.dp), tint = Color.White)
                                    Spacer(Modifier.width(16.dp))
                                    Text("Detener Grabación", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                                }
                                Text(
                                    "🎤 Grabando...", 
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                )
                            } else if (audioFile != null) {
                                Text(
                                    "✅ Audio grabado", 
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Green
                                    )
                                )
                                Button(
                                    onClick = {
                                        audioFile = null
                                        audioFileName = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("Regrabar", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            
            // Campo de descripción mejorado
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Column(Modifier.padding(28.dp)) {
                    Text(
                        "Descripción Adicional", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Cuéntanos más sobre tu evidencia (opcional)", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Describe tu evidencia...", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.8f))) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = Color.White
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = Color.White),
                        maxLines = 6
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                }
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch(Dispatchers.IO) {
                            try {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (tipo == "imagen") {
                                    imageUri?.let { uri ->
                                        val inputStream = context.contentResolver.openInputStream(uri)
                                        val bytes = inputStream?.readBytes()
                                        if (bytes != null) {
                                            uploadImageToImgur(
                                                imageBytes = bytes,
                                                clientId = clientId,
                                                onSuccess = { url: String ->
                                                    saveEvidenceToFirestore(
                                                        challengeId,
                                                        user?.uid ?: "",
                                                        currentUserName,
                                                        tipo,
                                                        url,
                                                        null,
                                                        descripcion
                                                    )
                                                    isLoading = false
                                                    onEvidenceSent()
                                                },
                                                onError = { error: String ->
                                                    errorMessage = error
                                                    isLoading = false
                                                }
                                            )
                                        }
                                    } ?: run {
                                        errorMessage = "Selecciona una imagen."
                                        isLoading = false
                                    }
                                } else if (tipo == "video") {
                                    videoUri?.let { uri ->
                                        val inputStream = context.contentResolver.openInputStream(uri)
                                        val bytes = inputStream?.readBytes()
                                        val fileName = videoFileName ?: "video_${System.currentTimeMillis()}.mp4"
                                        if (bytes != null) {
                                            com.example.redsocial.utils.uploadVideoToSupabase(
                                                videoBytes = bytes,
                                                fileName = fileName,
                                                onSuccess = { url: String ->
                                                    scope.launch(Dispatchers.Main) {
                                                        saveEvidenceToFirestore(
                                                            challengeId,
                                                            user?.uid ?: "",
                                                            currentUserName,
                                                            tipo,
                                                            url,
                                                            null,
                                                            descripcion
                                                        )
                                                        isLoading = false
                                                        onEvidenceSent()
                                                    }
                                                },
                                                onError = { errorMsg ->
                                                    scope.launch(Dispatchers.Main) {
                                                        errorMessage = errorMsg
                                                        isLoading = false
                                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            )
                                        } else {
                                            scope.launch(Dispatchers.Main) {
                                                errorMessage = "Selecciona un video."
                                                isLoading = false
                                            }
                                        }
                                    } ?: run {
                                        scope.launch(Dispatchers.Main) {
                                            errorMessage = "Selecciona un video."
                                            isLoading = false
                                        }
                                    }
                                } else if (tipo == "audio") {
                                    audioFile?.let { file ->
                                        val fileName = audioFileName ?: "audio_${System.currentTimeMillis()}.mp3"
                                        AudioUtils.uploadAudioToSupabase(
                                            audioFile = file,
                                            fileName = fileName,
                                            onSuccess = { url: String ->
                                                scope.launch(Dispatchers.Main) {
                                                    saveEvidenceToFirestore(
                                                        challengeId,
                                                        user?.uid ?: "",
                                                        currentUserName,
                                                        tipo,
                                                        url,
                                                        null,
                                                        descripcion
                                                    )
                                                    isLoading = false
                                                    onEvidenceSent()
                                                }
                                            },
                                            onError = { errorMsg ->
                                                scope.launch(Dispatchers.Main) {
                                                    errorMessage = errorMsg
                                                    isLoading = false
                                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        )
                                    } ?: run {
                                        scope.launch(Dispatchers.Main) {
                                            errorMessage = "Graba un audio primero."
                                            isLoading = false
                                        }
                                    }
                                } else if (tipo == "texto") {
                                    if (texto.isNotBlank()) {
                                        saveEvidenceToFirestore(
                                            challengeId,
                                            user?.uid ?: "",
                                            currentUserName,
                                            tipo,
                                            null,
                                            texto,
                                            descripcion
                                        )
                                        isLoading = false
                                        onEvidenceSent()
                                    } else {
                                        errorMessage = "El campo de texto no puede estar vacío."
                                        isLoading = false
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message
                                isLoading = false
                            }
                        }
                    },
                    enabled = (!isLoading && ((tipo == "imagen" && imageUri != null) || (tipo == "video" && videoUri != null) || (tipo == "texto" && texto.isNotBlank()) || (tipo == "audio" && audioFile != null))),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (isLoading) "Enviando..." else "Enviar",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = BackgroundDark
                    )
                }
            }
            
            // Mensaje de error
            errorMessage?.let {
                Spacer(Modifier.height(24.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.2f))
                ) {
                    Text(
                        it, 
                        color = Color.White, 
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Espacio adicional al final para scroll
            Spacer(Modifier.height(40.dp))
        }
    }
}

fun saveEvidenceToFirestore(
    challengeId: String,
    userId: String,
    userName: String,
    tipo: String,
    url: String?,
    texto: String?,
    descripcion: String?
) {
    val db = FirebaseFirestore.getInstance()
    
    // Primero guardamos la evidencia
    val evidencia = hashMapOf(
        "challengeId" to challengeId,
        "userId" to userId,
        "userName" to userName,
        "tipo" to tipo,
        "url" to url,
        "texto" to texto,
        "descripcion" to descripcion,
        "timestamp" to System.currentTimeMillis()
    )
    
    db.collection("evidencias").add(evidencia)
        .addOnSuccessListener { evidenciaDoc ->
            // Actualizamos el contador de participantes y contentTypes en el desafío
            val updates = hashMapOf<String, Any>(
                "participants" to com.google.firebase.firestore.FieldValue.increment(1)
            )
            
            // Si es una imagen, video o audio, incrementamos el contador correspondiente
            if (tipo == "imagen") {
                updates["imageCount"] = com.google.firebase.firestore.FieldValue.increment(1)
            } else if (tipo == "video") {
                updates["videoCount"] = com.google.firebase.firestore.FieldValue.increment(1)
            } else if (tipo == "audio") {
                updates["audioCount"] = com.google.firebase.firestore.FieldValue.increment(1)
            }
            
            db.collection("desafios").document(challengeId)
                .update(updates)
                .addOnSuccessListener {
                    // Actualizamos el perfil del usuario para marcar el desafío como completado
                    db.collection("usuarios").document(userId)
                        .collection("completedChallenges")
                        .document(challengeId)
                        .set(hashMapOf(
                            "completedAt" to System.currentTimeMillis(),
                            "evidenceId" to evidenciaDoc.id,
                            "contentType" to tipo
                        ))
                    // Verificar si el desafío está completo para repartir puntos
                    db.collection("desafios").document(challengeId).get().addOnSuccessListener { desafioDoc ->
                        val participants = (desafioDoc.getLong("participants") ?: 0L).toInt()
                        val maxParticipants = (desafioDoc.getLong("maxParticipants") ?: 1L).toInt()
                        val puntosTotales = (desafioDoc.getLong("points") ?: 0L).toInt()
                        if (participants >= maxParticipants && maxParticipants > 0 && puntosTotales > 0) {
                            // Obtener todos los participantes (evidencias)
                            db.collection("evidencias")
                                .whereEqualTo("challengeId", challengeId)
                                .get()
                                .addOnSuccessListener { evidenciasSnapshot ->
                                    val userIds = evidenciasSnapshot.documents.mapNotNull { it.getString("userId") }.distinct()
                                    val puntosPorUsuario = puntosTotales / userIds.size
                                    userIds.forEach { uid ->
                                        val userRef = db.collection("usuarios").document(uid)
                                        userRef.update("badges", com.google.firebase.firestore.FieldValue.increment(puntosPorUsuario.toLong()))
                                    }
                                }
                        }
                    }
                }
            // Notificar al autor del desafío
            db.collection("desafios").document(challengeId)
                .get()
                .addOnSuccessListener { challengeDoc ->
                    val autorId = challengeDoc.getString("authorId")
                    if (autorId != null && autorId != userId) {
                        val mensaje = "$userName participó en tu desafío"
                        NetworkUtils.notificarEvento(
                            usuarioObjetivoId = autorId,
                            tipo = "participacion",
                            mensaje = mensaje,
                            actorId = userId,
                            actorPhotoUrl = null // Puedes pasar la foto si la tienes
                        )
                    }
                }
        }
} 