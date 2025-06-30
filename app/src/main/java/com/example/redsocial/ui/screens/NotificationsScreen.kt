package com.example.redsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.Brush
import com.example.redsocial.utils.NotificationUtils
import androidx.navigation.NavController

// Modelo de notificación
data class Notificacion(
    val id: String = "",
    val tipo: String = "",
    val mensaje: String = "",
    val fecha: Long = 0L,
    val leido: Boolean = false,
    val actorPhotoUrl: String? = null,
    val actorId: String? = null
)

@Composable
fun NotificationsScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var notificaciones by remember { mutableStateOf(listOf<Notificacion>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Listener en tiempo real
    LaunchedEffect(user?.uid) {
        if (user != null) {
            // Debug: verificar notificaciones existentes
            NotificationUtils.debugNotifications(user.uid)
            
            db.collection("usuarios")
                .document(user.uid)
                .collection("notificaciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    val lista = snapshot?.documents?.map { doc ->
                        Notificacion(
                            id = doc.id,
                            tipo = doc.getString("tipo") ?: "",
                            mensaje = doc.getString("mensaje") ?: "",
                            fecha = doc.getLong("fecha") ?: 0L,
                            leido = doc.getBoolean("leido") ?: false,
                            actorPhotoUrl = doc.getString("actorPhotoUrl"),
                            actorId = doc.getString("actorId")
                        )
                    } ?: emptyList()
                    notificaciones = lista
                    isLoading = false
                }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Notificaciones",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color.White
            )
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF3B82F6))
            } else if (notificaciones.isEmpty()) {
                Text("No tienes notificaciones.", color = Color(0xFFCBD5E1))
            } else {
                Button(
                    onClick = {
                        // Marcar todas como leídas
                        notificaciones.forEach { n ->
                            db.collection("usuarios")
                                .document(user!!.uid)
                                .collection("notificaciones")
                                .document(n.id)
                                .update("leido", true)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00CFFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Marcar todo como Leído", color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                // Agrupar por fecha (Hoy, Ayer, Anteriores)
                val hoy = Calendar.getInstance()
                val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val notisHoy = notificaciones.filter { formato.format(Date(it.fecha)) == formato.format(hoy.time) }
                val notisAyer = notificaciones.filter { formato.format(Date(it.fecha)) == formato.format(ayer.time) }
                val notisAnt = notificaciones.filter { it !in notisHoy && it !in notisAyer }
                if (notisHoy.isNotEmpty()) {
                    Text("Hoy", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
                    notisHoy.forEach { NotiCard(it, user!!.uid, db, navController) }
                }
                if (notisAyer.isNotEmpty()) {
                    Text("Ayer", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
                    notisAyer.forEach { NotiCard(it, user!!.uid, db, navController) }
                }
                if (notisAnt.isNotEmpty()) {
                    Text("Anteriores", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
                    notisAnt.forEach { NotiCard(it, user!!.uid, db, navController) }
                }
            }
        }
    }
}

@Composable
fun NotiCard(noti: Notificacion, userId: String, db: FirebaseFirestore, navController: NavController) {
    val fondo = if (noti.leido) Color(0xFF23223A) else Color(0xFF18181B)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil circular con borde y sombra
            val actorPhotoUrl = noti.actorPhotoUrl
            if (actorPhotoUrl != null) {
                AsyncImage(
                    model = actorPhotoUrl,
                    contentDescription = "Foto de usuario",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF3B82F6), CircleShape)
                        .shadow(4.dp, CircleShape)
                        .clickable {
                            noti.actorId?.let { actorId ->
                                navController.navigate("userProfile/$actorId")
                            }
                        }
                )
            } else {
                // Placeholder circular si no hay foto
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF64748B))
                        .border(2.dp, Color(0xFF3B82F6), CircleShape)
                        .shadow(4.dp, CircleShape)
                        .clickable {
                            noti.actorId?.let { actorId ->
                                navController.navigate("userProfile/$actorId")
                            }
                        }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Resaltar el texto entre comillas con color celeste y hacer clickeable el nombre
                val mensaje = noti.mensaje
                val regex = Regex("\"(.*?)\"")
                val partes = regex.findAll(mensaje).toList()
                if (partes.isNotEmpty()) {
                    val annotated = buildAnnotatedString {
                        var lastIndex = 0
                        partes.forEach { matchResult ->
                            val start = matchResult.range.first
                            val end = matchResult.range.last + 1
                            append(mensaje.substring(lastIndex, start))
                            withStyle(SpanStyle(color = Color(0xFF00CFFF), fontWeight = FontWeight.Bold)) {
                                append(mensaje.substring(start, end))
                            }
                            lastIndex = end
                        }
                        if (lastIndex < mensaje.length) {
                            append(mensaje.substring(lastIndex))
                        }
                    }
                    Text(
                        text = annotated,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            noti.actorId?.let { actorId ->
                                navController.navigate("userProfile/$actorId")
                            }
                        }
                    )
                } else {
                    Text(
                        text = mensaje,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            noti.actorId?.let { actorId ->
                                navController.navigate("userProfile/$actorId")
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Fecha/hora relativa
                val fechaRelativa = remember(noti.fecha) { getRelativeTime(noti.fecha) }
                Text(
                    text = fechaRelativa,
                    color = Color(0xFFB0B0B0),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Botón eliminar
            IconButton(onClick = {
                db.collection("usuarios").document(userId).collection("notificaciones").document(noti.id).delete()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF2D55))
            }
        }
    }
}

// Función para calcular el tiempo relativo tipo "hace 2 horas"
fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1 -> "justo ahora"
        minutes < 60 -> "hace $minutes minutos"
        hours < 24 -> "hace $hours horas"
        days == 1L -> "hace 1 día"
        days > 1L -> "hace $days días"
        else -> "hace un momento"
    }
} 