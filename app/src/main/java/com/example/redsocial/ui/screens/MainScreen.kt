package com.example.redsocial.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.redsocial.navigation.NavigationItem
import com.example.redsocial.viewmodel.AuthViewModel

@Composable
fun MainScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavigationGraph(navController = navController, authViewModel = authViewModel)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Explore,
        NavigationItem.Create,
        NavigationItem.Notifications,
        NavigationItem.Profile
    )
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(navController = navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            HomeScreen(
                onNavigateToExplore = { navController.navigate(NavigationItem.Explore.route) },
                onNavigateToCreate = { navController.navigate(NavigationItem.Create.route) },
                onNavigateToNotifications = { navController.navigate(NavigationItem.Notifications.route) },
                onNavigateToProfile = { navController.navigate(NavigationItem.Profile.route) }
            )
        }
        composable(NavigationItem.Explore.route) {
            ExploreScreen(navController)
        }
        composable(NavigationItem.Create.route) {
            CreateScreen()
        }
        composable(NavigationItem.Notifications.route) {
            NotificationsScreen()
        }
        composable(NavigationItem.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    // Navegar a la pantalla de bienvenida
                    navController.navigate("bienvenida") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
} 