package com.example.redsocial.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : NavigationItem("home", Icons.Default.Home, "Inicio")
    object Explore : NavigationItem("explore", Icons.Default.Explore, "Explorar")
    object Create : NavigationItem("create", Icons.Default.Create, "Crear")
    object Notifications : NavigationItem("notifications", Icons.Default.Notifications, "Avisos")
    object Profile : NavigationItem("profile", Icons.Default.Person, "Perfil")
} 