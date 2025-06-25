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
import com.example.redsocial.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

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
                        placeholder = { Text("Escribe un comentario...") }
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
                                            }
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

    // Diálogo de edición
    if (showEditDialog && commentToEdit != null) {
        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                commentToEdit = null
                editCommentText = ""
            },
            title = { Text("Editar comentario") },
            text = {
                OutlinedTextField(
                    value = editCommentText,
                    onValueChange = { editCommentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escribe tu comentario...") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        commentToEdit?.let { comment ->
                            editComment(comment, editCommentText)
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        commentToEdit = null
                        editCommentText = ""
                    }
                ) {
                    Text("Cancelar")
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
            title = { Text("Eliminar comentario") },
            text = { Text("¿Estás seguro de que quieres eliminar este comentario? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        commentToDelete?.let { comment ->
                            deleteComment(comment)
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        commentToDelete = null
                    }
                ) {
                    Text("Cancelar")
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        color = MaterialTheme.colorScheme.primary
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
                
                // Menú de opciones solo para comentarios propios
                if (isOwnComment) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(Icons.Default.MoreVert, "Más opciones")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                leadingIcon = { Icon(Icons.Default.Edit, "Editar") },
                                onClick = {
                                    onEdit()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                leadingIcon = { Icon(Icons.Default.Delete, "Eliminar") },
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