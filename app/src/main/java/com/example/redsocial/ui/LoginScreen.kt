package com.example.redsocial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redsocial.AuthViewModel
import com.example.redsocial.AuthState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val mensaje by authViewModel.mensaje.collectAsState()
    var mostrarExito by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> navController.navigate("intereses") { popUpTo("login") { inclusive = true } }
            is AuthState.Error -> {/* Mostrar error si quieres */}
            else -> {}
        }
    }

    LaunchedEffect(mensaje) {
        if (mensaje == "¡Inicio de sesión exitoso!") {
            mostrarExito = true
        }
        mensaje?.let {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(it)
                authViewModel.limpiarMensaje()
            }
        }
    }

    // Google Sign-In launcher
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    authViewModel.loginWithGoogle(idToken)
                }
            } catch (e: Exception) {
                // Manejo de error
            }
        }
    }

    // GitHub: flujo base (debes completar con tu OAuth)
    fun launchGitHubLogin() {
        // Aquí deberías lanzar tu intent de OAuth de GitHub
        // Por ejemplo, abrir un CustomTab o WebView con la URL de autorización
        // Cuando recibas el token, llama a authViewModel.loginWithGitHub(token)
    }

    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Inicio de sesión exitoso!") },
            text = { Text("Bienvenido, has iniciado sesión correctamente.") },
            confirmButton = {
                Button(onClick = {
                    mostrarExito = false
                    navController.navigate("intereses") { popUpTo("login") { inclusive = true } }
                }) {
                    Text("Continuar")
                }
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(hostState = scaffoldState.snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF181818))
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFF2D1846), shape = RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Login", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("correo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { /* TODO: Recuperar contraseña */ }) {
                    Text("¿Olvidaste tu contraseña?", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            scope.launch { scaffoldState.snackbarHostState.showSnackbar("Completa todos los campos.") }
                        } else {
                            authViewModel.loginWithEmail(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Iniciar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Botón Google
                Button(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("69022461707-g2d0h024s7udf0r9jkq93fal1pb83v1i.apps.googleusercontent.com")
                            .requestEmail()
                            .build()
                        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
                        val signInIntent: Intent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar con Google", color = Color(0xFFA259FF), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Botón GitHub
                Button(
                    onClick = { launchGitHubLogin() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuar con GitHub", color = Color(0xFFA259FF), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Botón Apple (opcional)
                // Button(
                //     onClick = { /* TODO: Apple login */ },
                //     modifier = Modifier.fillMaxWidth().height(48.dp),
                //     colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                //     shape = RoundedCornerShape(12.dp)
                // ) {
                //     Text("Continue with Apple", color = Color(0xFFA259FF), fontWeight = FontWeight.Bold)
                // }
                // Spacer(modifier = Modifier.height(8.dp))
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate("registro") }) {
                    Text("¿No tienes cuenta? Regístrate", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                }
            }
        }
    }
} 