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

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    val tipos = listOf("imagen", "texto") // audio y video después
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
            "texto" -> {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Escribe tu evidencia") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
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
                enabled = (!isLoading && ((tipo == "imagen" && imageUri != null) || (tipo == "texto" && texto.isNotBlank())))
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
            
            // Si es una imagen o video, incrementamos el contador correspondiente
            if (tipo == "imagen") {
                updates["imageCount"] = com.google.firebase.firestore.FieldValue.increment(1)
            } else if (tipo == "video") {
                updates["videoCount"] = com.google.firebase.firestore.FieldValue.increment(1)
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
        }
} 