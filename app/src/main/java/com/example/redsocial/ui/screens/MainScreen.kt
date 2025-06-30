package com.example.redsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.redsocial.navigation.NavigationItem
import com.example.redsocial.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.util.Log

@Composable
fun MainScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    
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
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavigationGraph(navController = navController, authViewModel = authViewModel)
            }
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
    
    // Estado para el contador de notificaciones
    var notificationCount by remember { mutableStateOf(0) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Escuchar notificaciones no leídas
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios")
                .document(currentUser.uid)
                .collection("notificaciones")
                .whereEqualTo("leido", false)
                .addSnapshotListener { snapshot, _ ->
                    Log.d("BadgeDebug", "Cambios en notificaciones: ${snapshot?.size()} - user: ${currentUser.uid}")
                    notificationCount = snapshot?.size() ?: 0
                }
        }
    }
    
    NavigationBar(
        containerColor = Color(0xFF1E293B),
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.route == "notifications") {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(28.dp),
                                tint = if (currentRoute == item.route) Color(0xFF3B82F6) else Color(0xFFCBD5E1)
                            )
                            if (notificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .padding(start = 8.dp, top = 2.dp)
                                        .background(
                                            color = Color(0xFFFF3B30),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (notificationCount > 99) "99+" else notificationCount.toString(),
                                        color = Color.White,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        Icon(
                            item.icon, 
                            contentDescription = item.title,
                            tint = if (currentRoute == item.route) Color(0xFF3B82F6) else Color(0xFFCBD5E1)
                        )
                    }
                },
                label = { 
                    Text(
                        text = item.title,
                        color = if (currentRoute == item.route) Color(0xFF3B82F6) else Color(0xFFCBD5E1)
                    ) 
                },
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
                onNavigateToProfile = { navController.navigate(NavigationItem.Profile.route) },
                onNavigateToChallengeDetail = { challengeId ->
                    navController.navigate("detalleDesafio/$challengeId")
                }
            )
        }
        composable(NavigationItem.Explore.route) {
            ExploreScreen(navController)
        }
        composable(NavigationItem.Create.route) {
            CreateScreen()
        }
        composable(NavigationItem.Notifications.route) {
            NotificationsScreen(navController)
        }
        composable(NavigationItem.Profile.route) {
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("bienvenida") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable("ajustes") {
            SettingsScreen(
                navController = navController,
                authViewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("bienvenida") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(
            route = "detalleDesafio/{challengeId}",
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId")
            if (challengeId != null) {
                DetalleDesafioScreen(challengeId = challengeId, navController = navController)
            }
        }
        composable(
            route = "userProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                UserProfileScreen(userId = userId, navController = navController, authViewModel = authViewModel)
            }
        }
    }
} 