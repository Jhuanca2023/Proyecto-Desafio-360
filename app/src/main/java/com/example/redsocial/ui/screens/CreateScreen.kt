package com.example.redsocial.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.InputStream
import android.util.Base64
import okio.IOException
import com.example.redsocial.ui.components.ChipPreview
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun CreateScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clientId = "e88c7011ed88321" // <--  Imgur

    // Estados para los campos del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var points by remember { mutableStateOf(0) }
    var contentTypes by remember { mutableStateOf(listOf<String>()) }
    var tags by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf("public") }
    var deadline by remember { mutableStateOf("") }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var maxParticipants by remember { mutableStateOf(1) }

    // Selector de imagen
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        coverImageUri = uri
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            coverImageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Desafío", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") })
        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duración") })
        OutlinedTextField(value = points.toString(), onValueChange = { points = it.toIntOrNull() ?: 0 }, label = { Text("Puntos") })
        OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Etiquetas (separadas por coma)") })
        OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Fecha límite (opcional)") })
        OutlinedTextField(value = maxParticipants.toString(), onValueChange = { maxParticipants = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 }, label = { Text("Número de participantes") })
        // Selector de tipos de contenido permitidos
        Spacer(Modifier.height(8.dp))
        Text("Tipos de contenido permitidos para evidencia:", style = MaterialTheme.typography.bodyMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            val tipos = listOf("video", "imagen", "texto", "audio")
            tipos.forEach { tipo ->
                val seleccionado = contentTypes.contains(tipo)
                Button(
                    onClick = {
                        contentTypes = if (seleccionado) contentTypes - tipo else contentTypes + tipo
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(tipo.replaceFirstChar { it.uppercase() })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Seleccionar imagen de portada")
        }
        coverImageBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Imagen de portada", modifier = Modifier.size(120.dp))
        }

        Spacer(Modifier.height(16.dp))
        // Selector de visibilidad
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Visibilidad: ", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = privacy == "public",
                onCheckedChange = { checked -> privacy = if (checked) "public" else "private" }
            )
            Spacer(Modifier.width(8.dp))
            Text(if (privacy == "public") "Público" else "Privado", style = MaterialTheme.typography.bodyMedium)
        }

        // Vista previa del desafío
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Vista Previa del Desafío", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                coverImageBitmap?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "Imagen de portada", modifier = Modifier.fillMaxWidth().height(120.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(description, style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (category.isNotBlank()) ChipPreview(category)
                    if (duration.isNotBlank()) ChipPreview(duration)
                    if (points > 0) ChipPreview("$points pts")
                }
                Spacer(Modifier.height(4.dp))
                Text("Contenido aceptado:", style = MaterialTheme.typography.bodySmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    contentTypes.forEach { tipo ->
                        ChipPreview(tipo.replaceFirstChar { it.uppercase() })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Visibilidad: ${if (privacy == "public") "Público" else "Privado"}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                scope.launch(Dispatchers.IO) {
                    try {
                        var imageUrl: String? = null
                        coverImageUri?.let { uri ->
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val bytes = inputStream?.readBytes()
                            if (bytes != null) {
                                // Subir a Imgur
                                uploadImageToImgur(bytes, clientId,
                                    onSuccess = { url ->
                                        imageUrl = url

                                        saveChallengeToFirestore(
                                            title, description, category, duration, points, contentTypes, tags, privacy, deadline, imageUrl, maxParticipants
                                        )
                                        isLoading = false
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                        isLoading = false
                                    }
                                )
                            }
                        } ?: run {

                            saveChallengeToFirestore(
                                title, description, category, duration, points, contentTypes, tags, privacy, deadline, null, maxParticipants
                            )
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Publicando..." else "Publicar Desafío")
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

// Función para guardar en Firestore
fun saveChallengeToFirestore(
    title: String,
    description: String,
    category: String,
    duration: String,
    points: Int,
    contentTypes: List<String>,
    tags: String,
    privacy: String,
    deadline: String?,
    imageUrl: String?,
    maxParticipants: Int
) {
    val user = FirebaseAuth.getInstance().currentUser
    val challenge = hashMapOf(
        "title" to title,
        "description" to description,
        "category" to category,
        "duration" to duration,
        "points" to points,
        "contentTypes" to contentTypes,
        "tags" to tags.split(",").map { it.trim() },
        "privacy" to privacy,
        "deadline" to deadline,
        "coverImageUrl" to imageUrl,
        "authorId" to (user?.uid ?: ""),
        "authorName" to (user?.displayName ?: ""),
        "authorAvatar" to (user?.photoUrl?.toString() ?: ""),
        "likes" to 0,
        "comments" to 0,
        "timestamp" to System.currentTimeMillis(),
        "maxParticipants" to maxParticipants,
        "participants" to emptyList<String>()
    )
    FirebaseFirestore.getInstance().collection("desafios").add(challenge)
}

// Función para subir imagen a Imgur
fun uploadImageToImgur(
    imageBytes: ByteArray,
    clientId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
    val client = OkHttpClient()
    val requestBody = FormBody.Builder()
        .add("image", imageBase64)
        .build()
    val request = Request.Builder()
        .url("https://api.imgur.com/3/image")
        .addHeader("Authorization", "Client-ID $clientId")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError(e.message ?: "Error desconocido")
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val link = json.getJSONObject("data").getString("link")
                onSuccess(link)
            } else {
                onError("Error al subir la imagen: $responseBody")
            }
        }
    })
} 