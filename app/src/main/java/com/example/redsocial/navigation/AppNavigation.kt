package com.example.redsocial.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.redsocial.ui.screens.*
import com.example.redsocial.ui.BienvenidaScreen
import com.example.redsocial.ui.LoginScreen
import com.example.redsocial.ui.RegistroScreen
import com.example.redsocial.ui.InteresesScreen
import com.example.redsocial.viewmodel.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = "bienvenida") {
        composable("bienvenida") {
            BienvenidaScreen(navController, authViewModel)
        }
        
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        
        composable("registro") {
            RegistroScreen(navController, authViewModel)
        }

        composable("intereses") {
            InteresesScreen(navController, authViewModel)
        }

        composable("home") {
            HomeScreen(
                onNavigateToExplore = { navController.navigate("explore") },
                onNavigateToCreate = { navController.navigate("create") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable("explore") {
            ExploreScreen(navController)
        }

        composable("create") {
            CreateScreen()
        }

        composable("notifications") {
            NotificationsScreen()
        }

        composable("profile") {
            ProfileScreen(
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("bienvenida") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable("detalleDesafio/{challengeId}") { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: ""
            DetalleDesafioScreen(challengeId = challengeId, navController = navController)
        }
    }
} 