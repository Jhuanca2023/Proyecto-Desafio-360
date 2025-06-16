package com.example.redsocial.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.redsocial.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommentsDialog(
    challengeId: String,
    onDismiss: () -> Unit
) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(challengeId) {
        try {
            isLoading = true
            val commentsSnapshot = db.collection("desafios")
                .document(challengeId)
                .collection("comentarios")
                .orderBy("timestamp")
                .get()
                .await()
            
            comments = commentsSnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Comment(
                    id = doc.id,
                    challengeId = challengeId,
                    userId = data["userId"] as? String ?: "",
                    userName = data["userName"] as? String ?: "",
                    content = data["content"] as? String ?: "",
                    timestamp = data["timestamp"] as? Long ?: 0
                )
            }
        } catch (e: Exception) {
            // Manejar el error
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comentarios") },
        text = {
            Column {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(comments) { comment ->
                            CommentItem(comment)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un comentario...") }
                    )
                    IconButton(
                        onClick = {
                            if (newComment.isNotBlank() && currentUser != null) {
                                val comment = Comment(
                                    challengeId = challengeId,
                                    userId = currentUser.uid,
                                    userName = currentUser.displayName ?: "Usuario",
                                    content = newComment
                                )
                                
                                db.collection("desafios")
                                    .document(challengeId)
                                    .collection("comentarios")
                                    .add(comment)
                                    .addOnSuccessListener {
                                        newComment = ""
                                        db.collection("desafios")
                                            .document(challengeId)
                                            .update("comments", comments.size + 1)
                                    }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, "Enviar comentario")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun CommentItem(comment: Comment) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "@${comment.userName}",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = dateFormat.format(Date(comment.timestamp)),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 