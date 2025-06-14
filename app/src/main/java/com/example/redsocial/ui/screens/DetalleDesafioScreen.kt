package com.example.redsocial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.redsocial.ui.components.ChipPreview
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun DetalleDesafioScreen(challengeId: String, navController: NavController) {
    var challenge by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(challengeId) {
        isLoading = true
        val db = FirebaseFirestore.getInstance()
        val doc = db.collection("desafios").document(challengeId).get().await()
        challenge = doc.data
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        challenge?.let { data ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Text("Detalle del Desafío", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = data["coverImageUrl"] as? String,
                    contentDescription = "Imagen de portada",
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(data["title"] as? String ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Creado por @${data["authorName"] as? String ?: "Usuario"}", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChipPreview("${(data["points"] as? Long ?: 0)} pts")
                    Spacer(Modifier.width(8.dp))
                    (data["category"] as? String)?.let { ChipPreview(it) }
                    Spacer(Modifier.width(8.dp))
                    (data["contentTypes"] as? List<*>)?.forEach {
                        ChipPreview(it.toString())
                        Spacer(Modifier.width(4.dp))
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Descripción del Desafío", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(data["description"] as? String ?: "")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = "Likes")
                    Text("${(data["likes"] as? Long ?: 0)}")
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comentarios")
                    Text("${(data["comments"] as? Long ?: 0)}")
                }
                Spacer(Modifier.height(8.dp))
                Text("Reglas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                (data["rules"] as? List<*>)?.forEach {
                    Text("• $it")
                }
                Spacer(Modifier.height(8.dp))
                Text("Etiquetas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow {
                    (data["tags"] as? List<*>)?.forEach { tag ->
                        item { ChipPreview(tag.toString()) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { /* Participar en el desafío */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Participar en el Desafío")
                }
                Spacer(Modifier.height(16.dp))
                Text("Participaciones Recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                //  mostrar una lista de participaciones recientes
            }
        }
    }
} 