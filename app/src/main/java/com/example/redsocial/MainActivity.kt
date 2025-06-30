package com.example.redsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redsocial.navigation.AppNavigation
import com.example.redsocial.ui.theme.RedSocialTheme
import com.example.redsocial.ui.theme.ConsistentBackground
import com.example.redsocial.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedSocialTheme {
                ConsistentBackground {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        val authViewModel: AuthViewModel = viewModel()
                        AppNavigation(authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}