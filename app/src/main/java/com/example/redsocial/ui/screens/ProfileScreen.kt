package com.example.redsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocial.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1333))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mi Perfil",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Información del usuario
        userData?.let { data ->
            Text(
                text = data["nombreCompleto"] as? String ?: "Sin nombre",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "@${data["nombreUsuario"] as? String ?: ""}",
                color = Color(0xFFA259FF),
                fontSize = 18.sp
            )

            Text(
                text = data["biografia"] as? String ?: "Sin biografía",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onSignOut() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA259FF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Cerrar Sesión",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatisticItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 