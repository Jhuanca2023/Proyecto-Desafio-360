package com.example.redsocial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redsocial.AuthViewModel
import com.example.redsocial.AuthState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.FlowRow

@Composable
fun InteresesScreen(navController: NavController, authViewModel: AuthViewModel) {
    val intereses = listOf("Arte", "Deporte", "Música", "Lectura", "Viajar", "Cocinar", "Tecnología", "Moda", "Películas", "Juego", "Fotografía", "Escribiendo")
    val seleccionados = remember { mutableStateListOf<String>() }
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("home") { popUpTo("intereses") { inclusive = true } }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181818)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(Color(0xFF2D1846), shape = RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Barra de progreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Step 1/3", color = Color.White, fontSize = 13.sp)
                Text("Step 1/3", color = Color.White, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFBDBDBD), shape = RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(6.dp)
                        .background(Color.White, shape = RoundedCornerShape(3.dp))
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("En que estas Interesado????", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Seleccione al menos 3 intereses para comenzar", color = Color(0xFFBDBDBD), fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            // Chips de intereses en filas de 3
            val chunkedIntereses = intereses.chunked(3)
            chunkedIntereses.forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    fila.forEach { interes ->
                        Button(
                            onClick = {
                                if (seleccionados.contains(interes)) seleccionados.remove(interes)
                                else seleccionados.add(interes)
                            },
                            colors = if (seleccionados.contains(interes)) ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF)) else ButtonDefaults.buttonColors(containerColor = Color(0xFF3D2C5A)),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(interes, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { authViewModel.saveIntereses(seleccionados) },
                enabled = seleccionados.size >= 3,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hecho", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("home") }) {
                Text("Skip", color = Color(0xFFBDBDBD), fontSize = 13.sp)
            }
        }
    }
} 