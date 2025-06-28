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
import java.io.File

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

    val tipos = listOf("imagen", "video", "texto", "audio") // ahora incluye audio
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enviar Evidencia", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Para el desafío: $challengeTitle", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Text("Tipo de Evidencia", style = MaterialTheme.typography.bodyMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            tipos.forEach { t ->
                val seleccionado = tipo == t
                Button(
                    onClick = { tipo = t },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(t.replaceFirstChar { it.uppercase() })
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        when (tipo) {
            "imagen" -> {
                Button(onClick = { imageLauncher.launch("image/*") }) {
                    Text("Selecciona tu archivo (imagen)")
                }
                imageBitmap?.let {
                    Spacer(Modifier.height(8.dp))
                    Image(bitmap = it.asImageBitmap(), contentDescription = "Imagen seleccionada", modifier = Modifier.size(120.dp))
                }
            }
            "video" -> {
                Button(onClick = { videoLauncher.launch("video/*") }) {
                    Text("Selecciona tu archivo (video)")
                }
                videoFileName?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("Video seleccionado: $it")
                }
            }
            "texto" -> {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Escribe tu evidencia") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
            "audio" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Grabar")
                            Spacer(Modifier.width(8.dp))
                            Text("Iniciar Grabación")
                        }
                    } else if (isRecording) {
                        Button(
                            onClick = {
                                audioFile = AudioUtils.stopRecording()
                                isRecording = false
                                audioFileName = "audio_${System.currentTimeMillis()}.mp3"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Detener")
                            Spacer(Modifier.width(8.dp))
                            Text("Detener Grabación")
                        }
                        Text("🎤 Grabando...", style = MaterialTheme.typography.bodyMedium)
                    } else if (audioFile != null) {
                        Text("✅ Audio grabado", style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = {
                                audioFile = null
                                audioFileName = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Regrabar")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción Adicional (Opcional)") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Cancelar")
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
                                                    user?.displayName ?: "",
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
                                                        user?.displayName ?: "",
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
                                                    user?.displayName ?: "",
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
                                        user?.displayName ?: "",
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
                enabled = (!isLoading && ((tipo == "imagen" && imageUri != null) || (tipo == "video" && videoUri != null) || (tipo == "texto" && texto.isNotBlank()) || (tipo == "audio" && audioFile != null)))
            ) {
                Text(if (isLoading) "Enviando..." else "Enviar Evidencia")
            }
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
                "participants" to com.google.firebase.firestore.FieldValue.increment(-1)
            )
            
            // Si es una imagen, video o audio, incrementamos el contador correspondiente
            if (tipo == "imagen") {
                updates["imageCount"] = com.google.firebase.firestore.FieldValue.increment(1)
            } else if (tipo == "video") {
                updates["videoCount"] = com.google.firebase.firestore.FieldValue.increment(1)
            } else if (tipo == "audio") {
                updates["audioCount"] = com.google.firebase.firestore.FieldValue.increment(1)
            }
            
            db.collection("challenges").document(challengeId)
                .update(updates)
                .addOnSuccessListener {
                    // Actualizamos el perfil del usuario para marcar el desafío como completado
                    db.collection("users").document(userId)
                        .collection("completedChallenges")
                        .document(challengeId)
                        .set(hashMapOf(
                            "completedAt" to System.currentTimeMillis(),
                            "evidenceId" to evidenciaDoc.id,
                            "contentType" to tipo
                        ))
                }
            // Notificar al autor del desafío
            db.collection("challenges").document(challengeId)
                .get()
                .addOnSuccessListener { challengeDoc ->
                    val autorId = challengeDoc.getString("creatorId")
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