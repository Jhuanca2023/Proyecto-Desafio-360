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
import com.example.redsocial.ui.theme.BackgroundDark
import com.example.redsocial.ui.theme.ButtonPrimary
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

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
        containerColor = BackgroundDark.copy(alpha = 0.95f),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = ButtonPrimary
                    )
                } else {
                    if (comments.isEmpty()) {
                        Text("Aún no hay comentarios.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
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
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comentando como: @${currentUser?.displayName ?: currentUser?.email?.split("@")?.first() ?: "Usuario"}",
                        color = ButtonPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    label = { Text("Escribe un comentario...", color = ButtonPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ButtonPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = ButtonPrimary,
                        unfocusedLabelColor = Color.Gray
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
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
                },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary)
            ) {
                Text("Enviar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ButtonPrimary)
            }
        },
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
            containerColor = BackgroundDark.copy(alpha = 0.95f),
            text = {
                OutlinedTextField(
                    value = editCommentText,
                    onValueChange = { editCommentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escribe tu comentario...", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ButtonPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = ButtonPrimary,
                        unfocusedLabelColor = Color.Gray
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
                        contentColor = ButtonPrimary
                    )
                ) {
                    Text("Guardar", color = Color.White)
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
                        contentColor = Color.Gray
                    )
                ) {
                    Text("Cancelar", color = Color.Gray)
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
            containerColor = BackgroundDark.copy(alpha = 0.95f),
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
                        contentColor = Color.Gray
                    )
                ) {
                    Text("Cancelar", color = Color.Gray)
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
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "@${comment.userName}",
                    color = ButtonPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onUserClick(comment.userId) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateFormat.format(Date(comment.timestamp)),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isOwnComment) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = ButtonPrimary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(BackgroundDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ButtonPrimary) },
                                onClick = {
                                    onEdit()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF6B6B)) },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
} 