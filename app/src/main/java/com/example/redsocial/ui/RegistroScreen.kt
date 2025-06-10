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
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults

@Composable
fun RegistroScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var nombreCompleto by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var mostrarDialogoGenero by remember { mutableStateOf(false) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var mostrarExito by remember { mutableStateOf(false) }
    var mostrarPassword by remember { mutableStateOf(false) }
    var mostrarConfirmPassword by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState(initial = AuthState.Idle)
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val mensaje by authViewModel.mensaje.collectAsState(initial = null)
    val context = LocalContext.current

    // Configuración del DatePicker
    val year = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val month = remember { Calendar.getInstance().get(Calendar.MONTH) }
    val day = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            fechaNacimiento = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day
    )

    // Diálogo para seleccionar género
    if (mostrarDialogoGenero) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoGenero = false },
            title = { Text("Selecciona tu género", color = Color.White) },
            text = {
                Column {
                    Button(
                        onClick = { 
                            genero = "Hombre"
                            mostrarDialogoGenero = false 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (genero == "Hombre") Color(0xFFA259FF) else Color(0xFF3D2C5A))
                    ) {
                        Text("Hombre")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            genero = "Mujer"
                            mostrarDialogoGenero = false 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (genero == "Mujer") Color(0xFFA259FF) else Color(0xFF3D2C5A))
                    ) {
                        Text("Mujer")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            genero = "Otro"
                            mostrarDialogoGenero = false 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (genero == "Otro") Color(0xFFA259FF) else Color(0xFF3D2C5A))
                    ) {
                        Text("Otro")
                    }
                }
            },
            containerColor = Color(0xFF2D1846),
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarDialogoGenero = false }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }

    fun limpiarCampos() {
        nombreCompleto = ""
        nombreUsuario = ""
        correo = ""
        password = ""
        confirmPassword = ""
        fechaNacimiento = ""
        genero = ""
        aceptaTerminos = false
        mostrarPassword = false
        mostrarConfirmPassword = false
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> navController.navigate("intereses") { popUpTo("registro") { inclusive = true } }
            is AuthState.Error -> {/* Mostrar error si quieres */}
            else -> {}
        }
    }

    LaunchedEffect(mensaje) {
        if (mensaje == "¡Registro exitoso!") {
            mostrarExito = true
        }
        mensaje?.let {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(it)
                authViewModel.limpiarMensaje()
            }
        }
    }

    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = { 
                mostrarExito = false
                navController.navigate("login") {
                    popUpTo("registro") { inclusive = true }
                }
            },
            title = { Text("¡Registro Exitoso!", color = Color.White) },
            text = { Text("Tu cuenta ha sido creada correctamente. Por favor, inicia sesión para continuar.", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarExito = false
                        navController.navigate("login") {
                            popUpTo("registro") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF))
                ) {
                    Text("Ir a Iniciar Sesión")
                }
            },
            containerColor = Color(0xFF2D1846)
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
                    .fillMaxWidth(0.95f)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFF2D1846), shape = RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Crear Cuenta", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nombreCompleto,
                    onValueChange = { nombreCompleto = it },
                    label = { Text("Tu nombre Completo", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Nombre completo", tint = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = { nombreUsuario = it },
                    label = { Text("Elige un Nombre de Usuario", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Nombre de usuario", tint = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("tu@email.com", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Correo", tint = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar Contraseña", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Confirmar contraseña", tint = Color.White) },
                    trailingIcon = {
                        val image = if (mostrarConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { mostrarConfirmPassword = !mostrarConfirmPassword }) {
                            Icon(image, contentDescription = if (mostrarConfirmPassword) "Ocultar" else "Mostrar", tint = Color.White)
                        }
                    },
                    visualTransformation = if (mostrarConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fechaNacimiento,
                    onValueChange = { },
                    label = { Text("Fecha de Nacimiento", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Fecha de nacimiento", tint = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha", tint = Color.White)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = genero,
                    onValueChange = { },
                    label = { Text("Género", color = Color.White) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Género", tint = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        IconButton(onClick = { mostrarDialogoGenero = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Seleccionar género", tint = Color.White)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = aceptaTerminos, onCheckedChange = { aceptaTerminos = it })
                    Text("Acepto los ", color = Color.White, fontSize = 13.sp)
                    Text(
                        text = "Términos de Servicio",
                        color = Color(0xFFA259FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(" y la ", color = Color.White, fontSize = 13.sp)
                    Text(
                        text = "Política de Privacidad",
                        color = Color(0xFFA259FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (nombreCompleto.isBlank() || nombreUsuario.isBlank() || correo.isBlank() || password.isBlank() || confirmPassword.isBlank() || fechaNacimiento.isBlank() || !aceptaTerminos) {
                            scope.launch { scaffoldState.snackbarHostState.showSnackbar("Completa todos los campos y acepta los términos.") }
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                            scope.launch { scaffoldState.snackbarHostState.showSnackbar("Correo inválido.") }
                        } else if (password != confirmPassword) {
                            scope.launch { scaffoldState.snackbarHostState.showSnackbar("Las contraseñas no coinciden.") }
                        } else {
                            authViewModel.registerWithEmail(
                                correo, password, nombreCompleto, nombreUsuario, fechaNacimiento, genero
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Crear cuenta", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate("login") }) {
                    Text("¿Ya tienes cuenta? Iniciar sesión", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                }
            }
        }
    }
} 