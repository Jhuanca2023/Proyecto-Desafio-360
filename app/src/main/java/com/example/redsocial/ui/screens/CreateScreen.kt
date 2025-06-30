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
import androidx.navigation.NavController
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.redsocial.utils.uploadImageToImgur

// Declarar fuera del Composable para evitar problemas de scope y reasignación
private val visibilityOptions = listOf(
    Pair("public", "Público (Visible para todos)"),
    Pair("friends", "Solo amigos (Visibles solo para tus amigos)")
)
private val visibilityIcons = mapOf(
    "public" to Icons.Default.Public,
    "friends" to Icons.Default.Group
)

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
    var expandedVisibility by remember { mutableStateOf(false) }
    var coverImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Selector de imagen
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        coverImageUri = uri
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            coverImageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    val scrollState = rememberScrollState()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Crear Desafío",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF3B82F6),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF60A5FA)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF60A5FA)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF60A5FA)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duración", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF60A5FA)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            OutlinedTextField(
                value = points.toString(),
                onValueChange = { points = it.toIntOrNull() ?: 0 },
                label = { Text("Puntos", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF60A5FA)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Etiquetas (separadas por coma)", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = Color(0xFF60A5FA)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            OutlinedTextField(
                value = deadline,
                onValueChange = { deadline = it },
                label = { Text("Fecha límite (opcional)", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF60A5FA)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            OutlinedTextField(
                value = maxParticipants.toString(),
                onValueChange = { maxParticipants = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 },
                label = { Text("Número de participantes", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF60A5FA)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            Spacer(Modifier.height(8.dp))
            Text("Tipos de contenido permitidos para evidencia:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3B82F6))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF232946), shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Tipos de Contenido Permitidos para Evidencia",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    // Cuadrícula 2x2
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TipoEvidenciaButton("video", Icons.Default.Videocam, contentTypes.contains("video")) { tipo ->
                                contentTypes = if (contentTypes.contains(tipo)) contentTypes - tipo else contentTypes + tipo
                            }
                            TipoEvidenciaButton("imagen", Icons.Default.Image, contentTypes.contains("imagen")) { tipo ->
                                contentTypes = if (contentTypes.contains(tipo)) contentTypes - tipo else contentTypes + tipo
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TipoEvidenciaButton("texto", Icons.Default.TextFields, contentTypes.contains("texto")) { tipo ->
                                contentTypes = if (contentTypes.contains(tipo)) contentTypes - tipo else contentTypes + tipo
                            }
                            TipoEvidenciaButton("audio", Icons.Default.Audiotrack, contentTypes.contains("audio")) { tipo ->
                                contentTypes = if (contentTypes.contains(tipo)) contentTypes - tipo else contentTypes + tipo
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Puntos Otorgados (${points})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            )
            LinearProgressIndicator(
                progress = (points / 100f).coerceIn(0f, 1f),
                color = Color(0xFF3B82F6),
                trackColor = Color(0xFF28243C),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Imagen de portada",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (coverImageUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF28243C))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Imagen actual",
                            color = Color(0xFFCBD5E1),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AsyncImage(
                            model = coverImageUri,
                            contentDescription = "Imagen de portada actual",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }

            // Botón para cambiar imagen
            OutlinedButton(
                onClick = {
                    if (!isUploadingImage) {
                        launcher.launch("image/*")
                    }
                },
                enabled = !isUploadingImage,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF3B82F6)
                )
            ) {
                if (isUploadingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF3B82F6),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subiendo imagen...", color = Color.White)
                } else {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (coverImageUrl != null) "Cambiar imagen" else "Agregar imagen", color = Color.White)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Visibilidad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF232946), shape = RoundedCornerShape(12.dp))
                    .clickable { expandedVisibility = true }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(visibilityIcons[privacy] ?: Icons.Default.Public, contentDescription = null, tint = Color(0xFF3B82F6))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            if (privacy == "friends") "Solo amigos" else "Público",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            if (privacy == "friends") "Visibles solo para tus amigos" else "Visible para todos",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.fillMaxWidth())
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }
                DropdownMenu(
                    expanded = expandedVisibility,
                    onDismissRequest = { expandedVisibility = false },
                    modifier = Modifier.background(Color(0xFF232946))
                ) {
                    visibilityOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(visibilityIcons[value] ?: Icons.Default.Public, contentDescription = null, tint = Color(0xFF3B82F6))
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, color = Color.White)
                                }
                            },
                            onClick = {
                                privacy = value
                                expandedVisibility = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF232946))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vista Previa del Desafío", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3B82F6))
                    Spacer(Modifier.height(8.dp))
                    coverImageBitmap?.let {
                        Image(bitmap = it.asImageBitmap(), contentDescription = "Imagen de portada", modifier = Modifier.fillMaxWidth().height(120.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCBD5E1))
                    Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (category.isNotBlank()) ChipPreview(category)
                        if (duration.isNotBlank()) ChipPreview(duration)
                        if (points > 0) ChipPreview("$points pts")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Contenido aceptado:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF3B82F6))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        contentTypes.forEach { tipo ->
                            ChipPreview(tipo.replaceFirstChar { it.uppercase() })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Visibilidad: ${if (privacy == "public") "Público" else "Privado"}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF3B82F6))
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
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6), contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(if (isLoading) "Publicando..." else "Publicar Desafío")
            }
            errorMessage?.let { Text(it, color = Color(0xFFFF6B6B)) }
        }
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
    val db = FirebaseFirestore.getInstance()
    
    // Obtener el nombre de usuario desde Firestore
    db.collection("usuarios")
        .document(user?.uid ?: "")
        .get()
        .addOnSuccessListener { userDoc ->
            val userName = userDoc.getString("nombreUsuario") ?: 
                         user?.displayName ?: 
                         user?.email?.split("@")?.first() ?: 
                         "Usuario"
            
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
                "authorName" to userName,
                "authorAvatar" to (user?.photoUrl?.toString() ?: ""),
                "likes" to 0,
                "comments" to 0,
                "timestamp" to System.currentTimeMillis(),
                "maxParticipants" to maxParticipants,
                "participants" to 0
            )
            db.collection("desafios").add(challenge)
        }
        .addOnFailureListener {
            // Si falla, usar un nombre por defecto
            val userName = user?.displayName ?: 
                         user?.email?.split("@")?.first() ?: 
                         "Usuario"
            
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
                "authorName" to userName,
                "authorAvatar" to (user?.photoUrl?.toString() ?: ""),
                "likes" to 0,
                "comments" to 0,
                "timestamp" to System.currentTimeMillis(),
                "maxParticipants" to maxParticipants,
                "participants" to 0
            )
            db.collection("desafios").add(challenge)
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChallengeScreen(desafioId: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

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
    var coverImageUrl by remember { mutableStateOf<String?>(null) }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var maxParticipants by remember { mutableStateOf(1) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Estados para dropdowns
    var categoryExpanded by remember { mutableStateOf(false) }
    var durationExpanded by remember { mutableStateOf(false) }
    var privacyExpanded by remember { mutableStateOf(false) }

    val clientId = "e88c7011ed88321"
    val scrollState = rememberScrollState()

    // Selector de imagen para edición
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            coverImageUri = uri
            isUploadingImage = true
            // Subir imagen a Imgur
            scope.launch {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    
                    if (bytes != null) {
                        uploadImageToImgur(
                            imageBytes = bytes,
                            clientId = clientId,
                            onSuccess = { imageUrl ->
                                coverImageUrl = imageUrl
                                successMessage = "Imagen actualizada exitosamente"
                                isUploadingImage = false
                            },
                            onError = { msg ->
                                errorMessage = "Error al subir imagen: $msg"
                                isUploadingImage = false
                            }
                        )
                    } else {
                        errorMessage = "No se pudo leer la imagen"
                        isUploadingImage = false
                    }
                } catch (e: Exception) {
                    errorMessage = "Error al procesar la imagen: ${e.message}"
                    isUploadingImage = false
                }
            }
        }
    }

    LaunchedEffect(desafioId) {
        isLoading = true
        try {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("desafios").document(desafioId).get().await()
            if (doc.exists()) {
                title = doc.get("title") as? String ?: ""
                description = doc.get("description") as? String ?: ""
                category = doc.get("category") as? String ?: ""
                duration = doc.get("duration") as? String ?: ""
                points = (doc.get("points") as? Long)?.toInt() ?: 0
                contentTypes = (doc.get("contentTypes") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                tags = (doc.get("tags") as? List<*>)?.joinToString(", ") { it.toString() } ?: ""
                privacy = doc.get("privacy") as? String ?: "public"
                deadline = doc.get("deadline") as? String ?: ""
                coverImageUrl = doc.get("coverImageUrl") as? String
                maxParticipants = (doc.get("maxParticipants") as? Long)?.toInt() ?: 1
            }
        } catch (e: Exception) {
            errorMessage = e.message
        }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF3B82F6))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFF18122B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header con botón de regreso
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Text(
                "Editar Desafío",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campos del formulario
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título", color = Color(0xFFCBD5E1)) },
            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF60A5FA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF64748B)
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción", color = Color(0xFFCBD5E1)) },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF60A5FA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            minLines = 3,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF64748B)
            )
        )

        // Categoría
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { },
                label = { Text("Categoría", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF60A5FA)) },
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false },
                modifier = Modifier.background(Color(0xFF28243C))
            ) {
                listOf("Fitness", "Creatividad", "Aprendizaje", "Social", "Otros").forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat, color = Color.White) },
                        onClick = { 
                            category = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Duración
        ExposedDropdownMenuBox(
            expanded = durationExpanded,
            onExpandedChange = { durationExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = duration,
                onValueChange = { },
                label = { Text("Duración", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF60A5FA)) },
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            DropdownMenu(
                expanded = durationExpanded,
                onDismissRequest = { durationExpanded = false },
                modifier = Modifier.background(Color(0xFF28243C))
            ) {
                listOf("1 día", "3 días", "1 semana", "2 semanas", "1 mes").forEach { dur ->
                    DropdownMenuItem(
                        text = { Text(dur, color = Color.White) },
                        onClick = { 
                            duration = dur
                            durationExpanded = false
                        }
                    )
                }
            }
        }

        // Puntos
        OutlinedTextField(
            value = points.toString(),
            onValueChange = { points = it.toIntOrNull() ?: 0 },
            label = { Text("Puntos", color = Color(0xFFCBD5E1)) },
            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF60A5FA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF64748B)
            )
        )

        // Tipos de contenido
        Text(
            "Tipos de evidencia permitidos",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TipoEvidenciaButton(
                tipo = "imagen",
                icon = Icons.Default.Image,
                seleccionado = contentTypes.contains("imagen")
            ) { tipo ->
                contentTypes = if (contentTypes.contains(tipo)) {
                    contentTypes.filter { it != tipo }
                } else {
                    contentTypes + tipo
                }
            }
            TipoEvidenciaButton(
                tipo = "video",
                icon = Icons.Default.VideoLibrary,
                seleccionado = contentTypes.contains("video")
            ) { tipo ->
                contentTypes = if (contentTypes.contains(tipo)) {
                    contentTypes.filter { it != tipo }
                } else {
                    contentTypes + tipo
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tags
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (separados por comas)", color = Color(0xFFCBD5E1)) },
            leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = Color(0xFF60A5FA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF64748B)
            )
        )

        // Visibilidad
        ExposedDropdownMenuBox(
            expanded = privacyExpanded,
            onExpandedChange = { privacyExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = if (privacy == "public") "Público" else "Privado",
                onValueChange = { },
                label = { Text("Visibilidad", color = Color(0xFFCBD5E1)) },
                leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF60A5FA)) },
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF64748B)
                )
            )
            DropdownMenu(
                expanded = privacyExpanded,
                onDismissRequest = { privacyExpanded = false },
                modifier = Modifier.background(Color(0xFF28243C))
            ) {
                DropdownMenuItem(
                    text = { Text("Público", color = Color.White) },
                    onClick = { 
                        privacy = "public"
                        privacyExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Privado", color = Color.White) },
                    onClick = { 
                        privacy = "private"
                        privacyExpanded = false
                    }
                )
            }
        }

        // Fecha límite
        OutlinedTextField(
            value = deadline,
            onValueChange = { deadline = it },
            label = { Text("Fecha límite (opcional)", color = Color(0xFFCBD5E1)) },
            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF60A5FA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF64748B)
            )
        )

        // Máximo de participantes
        OutlinedTextField(
            value = maxParticipants.toString(),
            onValueChange = { maxParticipants = it.toIntOrNull() ?: 1 },
            label = { Text("Máximo de participantes", color = Color(0xFFCBD5E1)) },
            leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF60A5FA)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF64748B)
            )
        )

        // Imagen de portada
        Text(
            "Imagen de portada",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (coverImageUrl != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF28243C))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Imagen actual",
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    AsyncImage(
                        model = coverImageUrl,
                        contentDescription = "Imagen de portada actual",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
        }

        // Botón para cambiar imagen
        OutlinedButton(
            onClick = {
                if (!isUploadingImage) {
                    imageLauncher.launch("image/*")
                }
            },
            enabled = !isUploadingImage,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF3B82F6)
            )
        ) {
            if (isUploadingImage) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF3B82F6),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subiendo imagen...", color = Color.White)
            } else {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (coverImageUrl != null) "Cambiar imagen" else "Agregar imagen", color = Color.White)
            }
        }

        // Mensajes de error/éxito
        errorMessage?.let {
            Text(
                text = it,
                color = Color(0xFFFF6B6B),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        successMessage?.let {
            Text(
                text = it,
                color = Color.Green,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Botón de guardar
        Button(
            onClick = {
                if (title.isBlank() || description.isBlank() || category.isBlank() || duration.isBlank() || contentTypes.isEmpty()) {
                    errorMessage = "Por favor completa todos los campos obligatorios"
                    return@Button
                }

                isSaving = true
                errorMessage = null
                successMessage = null

                scope.launch {
                    try {
                        updateChallengeInFirestore(
                            desafioId = desafioId,
                            title = title,
                            description = description,
                            category = category,
                            duration = duration,
                            points = points,
                            contentTypes = contentTypes,
                            tags = tags,
                            privacy = privacy,
                            deadline = if (deadline.isBlank()) null else deadline,
                            imageUrl = coverImageUrl,
                            maxParticipants = maxParticipants
                        )
                        successMessage = "Desafío actualizado exitosamente"
                        isSaving = false
                        
                        // Navegar de vuelta después de un breve delay
                        kotlinx.coroutines.delay(1500)
                        navController.popBackStack()
                    } catch (e: Exception) {
                        errorMessage = "Error al actualizar: ${e.message}"
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6), contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardando...")
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Cambios")
            }
        }
    }
}

fun updateChallengeInFirestore(
    desafioId: String,
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
        "maxParticipants" to maxParticipants
    )
    FirebaseFirestore.getInstance().collection("desafios").document(desafioId).update(challenge as Map<String, Any>)
}

// COMPONENTE PARA BOTÓN DE TIPO DE EVIDENCIA
@Composable
fun TipoEvidenciaButton(tipo: String, icon: ImageVector, seleccionado: Boolean, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(1f)
            .background(
                if (seleccionado) Color(0xFF3B82F6) else Color(0xFF28243C),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick(tipo) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(tipo.replaceFirstChar { it.uppercase() }, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
} 