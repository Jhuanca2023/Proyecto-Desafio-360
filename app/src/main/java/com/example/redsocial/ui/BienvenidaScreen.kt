package com.example.redsocial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redsocial.AuthViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController

@Composable
fun BienvenidaScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181818)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .background(Color(0xFF2D1846), shape = RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Bienvenido a Desafíos 360!",
                fontSize = 28.sp,
                color = Color(0xFF6C63FF),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Motívate, Aprende y Comparte",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA259FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Iniciar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("registro") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Registrarse", color = Color(0xFFA259FF), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = {
                authViewModel.loginAsGuest()
                navController.navigate("intereses")
            }) {
                Text("Continuar como Invitado", color = Color(0xFFBDBDBD), fontWeight = FontWeight.Medium)
            }
        }
    }
} 