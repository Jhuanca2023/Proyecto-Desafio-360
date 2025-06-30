package com.example.redsocial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redsocial.viewmodel.AuthViewModel
import com.example.redsocial.viewmodel.AuthState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.example.redsocial.R

@Composable
fun BienvenidaScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
    
    // Animación del astronauta
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.astronaut)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate("home") {
                    popUpTo("bienvenida") { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Celeste muy oscuro
                        Color(0xFF1E3A8A), // Celeste oscuro
                        Color(0xFF3B82F6)  // Celeste medio
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animación del astronauta
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp)
            )
            
            // Título principal "FLUXI"
            Text(
                text = "FLUXI",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Subtítulo
            Text(
                text = "¡Bienvenido al futuro!",
                color = Color(0xFFCBD5E1),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Botón Crear cuenta
            Button(
                onClick = { 
                    navController.navigate("registro") {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Crear cuenta", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Iniciar sesión
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Iniciar sesión", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Continuar como invitado
            TextButton(
                onClick = { authViewModel.loginAsGuest() },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "Continuar como invitado",
                    color = Color(0xFF60A5FA),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 