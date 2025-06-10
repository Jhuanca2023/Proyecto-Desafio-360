package com.example.redsocial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarDialogoRecuperacion by remember { mutableStateOf(false) }
    var emailRecuperacion by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState(initial = AuthState.Idle)
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val mensaje by authViewModel.mensaje.collectAsState(initial = null)
    var mostrarExito by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var mostrarPassword by remember { mutableStateOf(false) }

    fun launchGitHubLogin() {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            val githubAuthUrl = authViewModel.getGitHubAuthUrl()
            customTabsIntent.launchUrl(context, Uri.parse(githubAuthUrl))
        } catch (e: Exception) {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Error al iniciar la autenticación con GitHub")
            }
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate("intereses") { 
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Error -> {/* Mostrar error si quieres */}
            else -> {}
        }
    }

    LaunchedEffect(mensaje) {
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

    // Maneja el resultado de la autenticación de GitHub
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        activity?.intent?.data?.let { uri ->
            if (uri.toString().startsWith(AuthViewModel.GITHUB_REDIRECT_URI)) {
                uri.getQueryParameter("code")?.let { code ->
                    authViewModel.handleGitHubCallback(code)
                }
            }
        }
    }

    // Diálogo de recuperación de contraseña
    if (mostrarDialogoRecuperacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRecuperacion = false },
            title = { Text("Recuperar Contraseña", color = Color.White) },
            text = {
                Column {
                    Text(
                        "Ingresa tu correo electrónico y te enviaremos las instrucciones para recuperar tu contraseña.",
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = emailRecuperacion,
                        onValueChange = { emailRecuperacion = it },
                        label = { Text("Correo electrónico", color = Color.White) },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFFBDBDBD),
                            cursorColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailRecuperacion.isBlank()) {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Por favor, ingresa tu correo electrónico")
                            }
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailRecuperacion).matches()) {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Por favor, ingresa un correo válido")
                            }
                        } else {
                            authViewModel.resetPassword(emailRecuperacion)
                            mostrarDialogoRecuperacion = false
                            emailRecuperacion = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF))
                ) {
                    Text("Enviar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRecuperacion = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color(0xFF2D1846),
            textContentColor = Color.White
        )
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
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    "Login",
                    fontSize = 28.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Correo", tint = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Contraseña", tint = Color.White) },
                    trailingIcon = {
                        val image = if (mostrarPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                            Icon(image, contentDescription = if (mostrarPassword) "Ocultar" else "Mostrar", tint = Color.White)
                        }
                    },
                    visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    )
                )

                TextButton(
                    onClick = { mostrarDialogoRecuperacion = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("¿Olvidaste tu contraseña?", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            scope.launch { scaffoldState.snackbarHostState.showSnackbar("Completa todos los campos.") }
                        } else {
                            authViewModel.loginWithEmail(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Iniciar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Text(
                    "O continúa con",
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Google", color = Color(0xFFA259FF), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { launchGitHubLogin() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("GitHub", color = Color(0xFFA259FF), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = { navController.navigate("registro") },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("¿No tienes cuenta? Regístrate", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                }
            }
        }
    }
} 