package com.example.redsocial.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import com.example.redsocial.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import com.example.redsocial.utils.NetworkUtils
import com.example.redsocial.utils.NotificationUtils

@Composable
fun CommentsDialog(
    challengeId: String,
    onDismiss: () -> Unit,
    onUserProfileClick: (String) -> Unit
) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    var editCommentText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    // Función para cargar comentarios
    fun loadComments() {
        db.collection("desafios")
            .document(challengeId)
            .collection("comentarios")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { commentsSnapshot ->
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
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    LaunchedEffect(challengeId) {
        loadComments()
    }

    // Función para editar comentario
    fun editComment(comment: Comment, newContent: String) {
        if (newContent.isBlank()) return
        
        db.collection("desafios")
            .document(challengeId)
            .collection("comentarios")
            .document(comment.id)
            .update("content", newContent)
            .addOnSuccessListener {
                showEditDialog = false
                commentToEdit = null
                editCommentText = ""
                loadComments() // Recargar comentarios
            }
    }

    // Función para eliminar comentario
    fun deleteComment(comment: Comment) {
        db.collection("desafios")
            .document(challengeId)
            .collection("comentarios")
            .document(comment.id)
            .delete()
            .addOnSuccessListener {
                // Actualizar contador de comentarios en el desafío
                db.collection("desafios")
                    .document(challengeId)
                    .update("comments", comments.size - 1)
                
                showDeleteDialog = false
                commentToDelete = null
                loadComments() // Recargar comentarios
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comentarios", color = Color.White) },
        containerColor = Color(0xFF1A1F2E),
        text = {
            Column {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color(0xFF3B82F6)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                isOwnComment = comment.userId == currentUser?.uid,
                                onEdit = {
                                    commentToEdit = comment
                                    editCommentText = comment.content
                                    showEditDialog = true
                                },
                                onDelete = {
                                    commentToDelete = comment
                                    showDeleteDialog = true
                                },
                                onUserClick = { userId ->
                                    onUserProfileClick(userId)
                                    onDismiss() // Cerrar el diálogo de comentarios
                                }
                            )
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
                        placeholder = { Text("Escribe un comentario...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF64748B)
                        )
                    )
                    IconButton(
                        onClick = {
                            if (newComment.isNotBlank() && currentUser != null) {
                                // Obtener el nombre de usuario desde Firestore
                                db.collection("usuarios")
                                    .document(currentUser.uid)
                                    .get()
                                    .addOnSuccessListener { userDoc ->
                                        val userName = userDoc.getString("nombreUsuario") ?: 
                                                     currentUser.displayName ?: 
                                                     currentUser.email?.split("@")?.first() ?: 
                                                     "Usuario"
                                        
                                        val comment = Comment(
                                            challengeId = challengeId,
                                            userId = currentUser.uid,
                                            userName = userName,
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
                                                loadComments() // Recargar comentarios

                                                // Enviar notificación de comentario
                                                db.collection("desafios")
                                                    .document(challengeId)
                                                    .get()
                                                    .addOnSuccessListener { desafioDoc ->
                                                        val autorId = desafioDoc.getString("authorId")
                                                        val title = desafioDoc.getString("title") ?: "Desafío"
                                                        if (autorId != null && autorId != currentUser.uid) {
                                                            NotificationUtils.sendCommentNotification(
                                                                challengeAuthorId = autorId,
                                                                challengeId = challengeId,
                                                                challengeTitle = title,
                                                                commentText = comment.content
                                                            )
                                                        }
                                                    }
                                            }
                                    }
                                    .addOnFailureListener {
                                        // Si falla, usar un nombre por defecto
                                        val userName = currentUser.displayName ?: 
                                                     currentUser.email?.split("@")?.first() ?: 
                                                     "Usuario"
                                        
                                        val comment = Comment(
                                            challengeId = challengeId,
                                            userId = currentUser.uid,
                                            userName = userName,
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
                                                loadComments() // Recargar comentarios

                                                // Enviar notificación de comentario
                                                db.collection("desafios")
                                                    .document(challengeId)
                                                    .get()
                                                    .addOnSuccessListener { desafioDoc ->
                                                        val autorId = desafioDoc.getString("authorId")
                                                        val title = desafioDoc.getString("title") ?: "Desafío"
                                                        if (autorId != null && autorId != currentUser.uid) {
                                                            NotificationUtils.sendCommentNotification(
                                                                challengeAuthorId = autorId,
                                                                challengeId = challengeId,
                                                                challengeTitle = title,
                                                                commentText = comment.content
                                                            )
                                                        }
                                                    }
                                            }
                                    }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, "Enviar comentario", tint = Color(0xFF3B82F6))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF3B82F6)
                )
            ) {
                Text("Cerrar", color = Color(0xFF3B82F6))
            }
        }
    )

    // Diálogo de edición
    if (showEditDialog && commentToEdit != null) {
        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                commentToEdit = null
                editCommentText = ""
            },
            title = { Text("Editar comentario", color = Color.White) },
            containerColor = Color(0xFF1A1F2E),
            text = {
                OutlinedTextField(
                    value = editCommentText,
                    onValueChange = { editCommentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escribe tu comentario...", color = Color(0xFF64748B)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF64748B)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        commentToEdit?.let { comment ->
                            editComment(comment, editCommentText)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF3B82F6)
                    )
                ) {
                    Text("Guardar", color = Color(0xFF3B82F6))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        commentToEdit = null
                        editCommentText = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF64748B)
                    )
                ) {
                    Text("Cancelar", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                commentToDelete = null
            },
            title = { Text("Eliminar comentario", color = Color.White) },
            text = { Text("¿Estás seguro de que quieres eliminar este comentario? Esta acción no se puede deshacer.", color = Color(0xFFCBD5E1)) },
            containerColor = Color(0xFF1A1F2E),
            confirmButton = {
                TextButton(
                    onClick = {
                        commentToDelete?.let { comment ->
                            deleteComment(comment)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Text("Eliminar", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        commentToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF64748B)
                    )
                ) {
                    Text("Cancelar", color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    isOwnComment: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2F3E)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "@${comment.userName}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.clickable { onUserClick(comment.userId) },
                        color = Color(0xFF60A5FA)
                    )
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = dateFormat.format(Date(comment.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
                
                // Menú de opciones solo para comentarios propios
                if (isOwnComment) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(Icons.Default.MoreVert, "Más opciones", tint = Color(0xFF60A5FA))
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF1A1F2E))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Edit, "Editar", tint = Color(0xFF60A5FA)) },
                                onClick = {
                                    onEdit()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFFF6B6B)) },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
} 