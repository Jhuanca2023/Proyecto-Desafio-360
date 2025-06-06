package com.example.redsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.redsocial.ui.theme.RedSocialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.redsocial.ui.BienvenidaScreen
import com.example.redsocial.ui.LoginScreen
import com.example.redsocial.ui.RegistroScreen
import com.example.redsocial.ui.InteresesScreen
import com.example.redsocial.ui.HomeScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redsocial.AuthViewModel
import com.example.redsocial.AuthState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedSocialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val authState by authViewModel.authState.collectAsState()
                    LaunchedEffect(authState) {
                        if (authState is AuthState.Success) {
                            navController.navigate("home") { popUpTo(0) }
                        } else if (authState is AuthState.Idle || authState is AuthState.Error || authState is AuthState.Guest) {
                            navController.navigate("bienvenida") { popUpTo(0) }
                        }
                    }
                    NavHost(navController = navController, startDestination = "bienvenida") {
                        composable("bienvenida") { BienvenidaScreen(navController, authViewModel) }
                        composable("login") { LoginScreen(navController, authViewModel) }
                        composable("registro") { RegistroScreen(navController, authViewModel) }
                        composable("intereses") { InteresesScreen(navController, authViewModel) }
                        composable("home") {
                            if (authState is AuthState.Success) {
                                HomeScreen(navController)
                            } else {
                                LaunchedEffect(Unit) { navController.navigate("bienvenida") { popUpTo(0) } }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RedSocialTheme {
        Greeting("Android")
    }
}