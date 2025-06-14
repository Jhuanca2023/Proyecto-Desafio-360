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
import com.example.redsocial.viewmodel.AuthViewModel
import com.example.redsocial.viewmodel.AuthState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.example.redsocial.utils.NetworkUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarDialogoRecuperacion by remember { mutableStateOf(false) }
    var emailRecuperacion by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mensaje by authViewModel.mensaje.collectAsState()
    var mostrarPassword by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Config Google
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("69022461707-usdr27j6clhvfq8mrsdkvls5d0apitvc.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Google launcher
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account?.idToken
                    if (idToken != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Verificando credenciales...",
                                duration = SnackbarDuration.Short
                            )
                        }
                        authViewModel.loginWithGoogle(idToken)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Error: Token nulo",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                } catch (e: ApiException) {
                    val errorMessage = when (e.statusCode) {
                        GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Inicio de sesión cancelado"
                        GoogleSignInStatusCodes.NETWORK_ERROR -> "Error de red"
                        GoogleSignInStatusCodes.INVALID_ACCOUNT -> "Cuenta inválida"
                        GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> "Se requiere iniciar sesión"
                        else -> "Error: ${e.message}"
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = errorMessage,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Inicio de sesión cancelado",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    fun handleGoogleSignIn() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            scope.launch {
                snackbarHostState.showSnackbar("No hay conexión a internet. Por favor, verifica tu conexión.")
            }
            return
        }

        try {
            //  cerrar cualquier sesión anterior
            googleSignInClient.signOut().addOnCompleteListener {
                try {
                    val signInIntent = googleSignInClient.signInIntent
                    scope.launch {
                        snackbarHostState.showSnackbar("Abriendo selector de cuentas...")
                    }
                    googleLauncher.launch(signInIntent)
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error al iniciar Google Sign-In: ${e.message}")
                    }
                }
            }.addOnFailureListener { e ->
                scope.launch {
                    snackbarHostState.showSnackbar("Error al cerrar sesión: ${e.message}")
                }
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Error general: ${e.message}")
            }
        }
    }

    fun launchGitHubLogin() {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        val githubAuthUrl = authViewModel.getGitHubAuthUrl()
        customTabsIntent.launchUrl(context, Uri.parse(githubAuthUrl))
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "¡Inicio de sesión exitoso!",
                    duration = SnackbarDuration.Short
                )
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.NeedsIntereses -> {
                snackbarHostState.showSnackbar(
                    message = "Por favor configura tus intereses",
                    duration = SnackbarDuration.Short
                )
                navController.navigate("intereses") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (authState as AuthState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            is AuthState.Loading -> {
                snackbarHostState.showSnackbar(
                    message = "Verificando...",
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    //  autenticación de GitHub
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

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    action = {
                        data.visuals.actionLabel?.let { actionLabel ->
                            TextButton(onClick = { data.performAction() }) {
                                Text(
                                    text = actionLabel,
                                    color = MaterialTheme.colorScheme.inversePrimary
                                )
                            }
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Iniciar Sesión",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA259FF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                        Icon(
                            if (mostrarPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (mostrarPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA259FF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Button(
                onClick = {
                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        scope.launch {
                            snackbarHostState.showSnackbar("No hay conexión a internet")
                        }
                        return@Button
                    }
                    authViewModel.loginWithEmail(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF))
            ) {
                Text("Iniciar Sesión", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { mostrarDialogoRecuperacion = true },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = Color(0xFFA259FF),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿No tienes una cuenta? ", color = Color.Gray)
                TextButton(onClick = { navController.navigate("registro") }) {
                    Text("Regístrate", color = Color(0xFFA259FF))
                }
            }

            // Botones de sesión con redes sociales
            Button(
                onClick = { handleGoogleSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
            ) {
                Text("Continuar con Google", color = Color.White)
            }

            Button(
                onClick = { launchGitHubLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E))
            ) {
                Text("Continuar con GitHub", color = Color.White)
            }

            if (mostrarDialogoRecuperacion) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoRecuperacion = false },
                    title = { Text("Recuperar Contraseña") },
                    text = {
                        OutlinedTextField(
                            value = emailRecuperacion,
                            onValueChange = { emailRecuperacion = it },
                            label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = Color(0xFFA259FF),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (emailRecuperacion.isNotEmpty()) {
                                    authViewModel.resetPassword(emailRecuperacion)
                                    mostrarDialogoRecuperacion = false
                                    emailRecuperacion = ""
                                }
                            }
                        ) {
                            Text("Enviar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoRecuperacion = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
} 