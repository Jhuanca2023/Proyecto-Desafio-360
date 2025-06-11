package com.example.redsocial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redsocial.viewmodel.AuthViewModel
import com.example.redsocial.viewmodel.AuthState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import kotlinx.coroutines.launch

private val intereses = listOf(
    "Deportes", "Música", "Arte", "Tecnología", "Viajes", "Cocina",
    "Fotografía", "Moda", "Gaming", "Libros", "Cine", "Naturaleza",
    "Fitness", "Baile", "Idiomas", "Mascotas", "DIY", "Ciencia"
)

@Composable
fun InteresesScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val interesesSeleccionados = remember { mutableStateOf(setOf<String>()) }
    val authState by authViewModel.authState.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val mensaje by authViewModel.mensaje.collectAsState()
    val isLoading = remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                isLoading.value = false
                navController.navigate("home") {
                    popUpTo("bienvenida") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                isLoading.value = false
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Error: ${(authState as AuthState.Error).message}")
                }
            }
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

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(hostState = scaffoldState.snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Qué te interesa?",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                Text(
                    text = "Selecciona al menos 3 intereses para personalizar tu experiencia",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(intereses) { interes ->
                        val isSelected = interesesSeleccionados.value.contains(interes)
                        Button(
                            onClick = {
                                interesesSeleccionados.value = if (isSelected) {
                                    interesesSeleccionados.value - interes
                                } else {
                                    interesesSeleccionados.value + interes
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFFA259FF) else Color(0xFF2E2E2E)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = interes,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (interesesSeleccionados.value.size < 3) {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "Por favor selecciona al menos 3 intereses"
                                )
                            }
                            return@Button
                        }
                        isLoading.value = true
                        authViewModel.saveIntereses(
                            interesesSeleccionados.value.toList()
                        ) { success ->
                            if (!success) {
                                isLoading.value = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFA259FF),
                        disabledContainerColor = Color.Gray
                    ),
                    enabled = !isLoading.value,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Continuar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteresButton(
    interes: String,
    selected: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Button(
        onClick = { onSelect(!selected) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFA259FF) else Color(0xFF2E2E2E)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = interes,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
} 