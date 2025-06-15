package com.example.redsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redsocial.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ExitToApp

@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var idioma by remember { mutableStateOf("Español") }
    val user = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18122B))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ajustes", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))

        // Cuenta
        SettingsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFA259FF))
                Spacer(Modifier.width(8.dp))
                Text("Cuenta", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFA259FF))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Email", color = Color.White)
                    Text(user?.email ?: "Usuario@example.com", color = Color.Gray, fontSize = 13.sp)
                    Button(onClick = { /* Cambiar email */ }, modifier = Modifier.padding(top = 4.dp)) {
                        Text("Cambiar Email")
                    }
                }
            }
        }
        // Contraseña
        SettingsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFA259FF))
                Spacer(Modifier.width(8.dp))
                Text("Contraseña", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { /* Cambiar contraseña */ }) {
                Text("Cambiar Contraseña")
            }
        }
        // Preferencias
        SettingsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFFA259FF))
                Spacer(Modifier.width(8.dp))
                Text("Preferencias", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFFA259FF))
                Spacer(Modifier.width(8.dp))
                Text("Modo de Apariencia", color = Color.White)
                Spacer(Modifier.weight(1f))
                Switch(checked = darkMode, onCheckedChange = { darkMode = it })
            }
            Text("Actualmente en modo ${if (darkMode) "oscuro" else "claro"}", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(start = 32.dp))
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFA259FF))
                Spacer(Modifier.width(8.dp))
                Text("Notificaciones", color = Color.White)
                Spacer(Modifier.weight(1f))
                Switch(checked = notifications, onCheckedChange = { notifications = it })
            }
            Text("Recibir alertas sobre actividad relevante", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(start = 32.dp))
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFFA259FF))
                Spacer(Modifier.width(8.dp))
                Text("Idioma", color = Color.White)
                Spacer(Modifier.weight(1f))
                DropdownMenuBox(idioma, onChange = { idioma = it })
            }
            Button(onClick = { /* Guardar preferencias */ }, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
                Text("Guardar Preferencias")
            }
        }
        // Ayuda
        SettingsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = Color(0xFFA259FF))
                Spacer(Modifier.width(8.dp))
                Text("Ayuda", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            SettingsLink(Icons.Default.QuestionAnswer, "Preguntas Frecuentes (FAQs)")
            SettingsLink(Icons.Default.Support, "Contactar Soporte")
            SettingsLink(Icons.Default.Policy, "Política de Privacidad")
            SettingsLink(Icons.Default.Description, "Términos de Servicio")
        }
        // Zona peligrosa
        SettingsCard(borderColor = Color(0xFFFF1744)) {
            Text("Zona Peligrosa", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onSignOut() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Eliminar Cuenta")
            }
            Text("Esta acción es permanente e irreversible.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("¿Eliminar cuenta?") },
            text = { Text("Esta acción es permanente e irreversible. ¿Seguro que deseas continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        deleteError = null
                        scope.launch {
                            val user = FirebaseAuth.getInstance().currentUser
                            try {
                                // Eliminar datos en Firestore
                                user?.uid?.let { uid ->
                                    FirebaseFirestore.getInstance().collection("usuarios").document(uid).delete().await()
                                    // Eliminar desafíos creados por el usuario
                                    val desafios = FirebaseFirestore.getInstance().collection("desafios").whereEqualTo("authorId", uid).get().await()
                                    desafios.documents.forEach { it.reference.delete() }
                                }
                                // Eliminar usuario de Auth
                                user?.delete()?.addOnCompleteListener {
                                    isDeleting = false
                                    showDeleteDialog = false
                                    onSignOut()
                                }?.addOnFailureListener {
                                    isDeleting = false
                                    deleteError = it.message
                                }
                            } catch (e: Exception) {
                                isDeleting = false
                                deleteError = e.message
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    enabled = !isDeleting
                ) {
                    Text(if (isDeleting) "Eliminando..." else "Eliminar Cuenta")
                }
            },
            dismissButton = {
                if (!isDeleting) {
                    OutlinedButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            },
            containerColor = Color(0xFF18122B)
        )
        if (deleteError != null) {
            Text(deleteError!!, color = Color.Red, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun SettingsCard(
    borderColor: Color = Color(0xFF28243C),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF28243C)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SettingsLink(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navegar a la sección */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFA259FF))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White)
    }
}

@Composable
fun DropdownMenuBox(selected: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val idiomas = listOf("Español", "Inglés", "Portugués")
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            idiomas.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onChange(it)
                    expanded = false
                })
            }
        }
    }
} 