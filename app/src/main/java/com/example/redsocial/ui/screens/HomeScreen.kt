package com.example.redsocial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocial.ui.components.Challenge
import com.example.redsocial.ui.components.ChallengeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val challenges = remember { generateSampleChallenges() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Explorar") },
                    label = { Text("Explorar") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        onNavigateToExplore()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                    label = { Text("Crear") },
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        onNavigateToCreate()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Avisos") },
                    label = { Text("Avisos") },
                    selected = selectedTab == 3,
                    onClick = { 
                        selectedTab = 3
                        onNavigateToNotifications()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedTab == 4,
                    onClick = { 
                        selectedTab = 4
                        onNavigateToProfile()
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> ChallengesFeed(challenges)
                else -> {} // Las otras pestañas se manejan a través de la navegación
            }
        }
    }
}

@Composable
fun ChallengesFeed(challenges: List<Challenge>) {
    LazyColumn {
        items(challenges) { challenge ->
            ChallengeCard(challenge = challenge)
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
    ) {
        // Aquí irá el contenido del desafío (video/imagen)
        
        // Información del desafío
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = challenge.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Por: ${challenge.author}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { /* Participar */ }) {
                    Text("Participar")
                }
                
                Button(onClick = { /* Ver categoría */ }) {
                    Text("Categoría")
                }
            }
        }
        
        // Acciones laterales
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { /* Like */ }) {
                Icon(Icons.Default.Favorite, "Me gusta")
            }
            Text("${challenge.likes}")
            
            IconButton(onClick = { /* Comentar */ }) {
                Icon(Icons.Default.Comment, "Comentar")
            }
            Text("${challenge.comments}")
            
            IconButton(onClick = { /* Compartir */ }) {
                Icon(Icons.Default.Share, "Compartir")
            }
            Text("${challenge.shares}")
            
            IconButton(onClick = { /* Guardar */ }) {
                Icon(Icons.Default.BookmarkBorder, "Guardar")
            }
            Text("${challenge.saves}")
        }
    }
}

private fun generateSampleChallenges() = listOf(
    Challenge("1", "30-Day Sentadillas", "FitLife", 2300, 1200, 500, 100),
    Challenge("2", "Pinta tu Atardecer Favorito", "ArtDaily", 1500, 800, 300, 150),
    Challenge("3", "Reto Musical Semanal", "MusicPro", 3000, 1500, 700, 200)
) 