package com.example.redsocial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.redsocial.ui.components.Challenge
import com.example.redsocial.ui.components.ChallengeCard

@Composable
fun ExploreScreen() {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar desafíos, creadores....") },
            leadingIcon = { Icon(Icons.Default.Search, "Buscar") }
        )
        
        // Filtros
        FiltersSection()
        
        // Categorías
        CategoriesSection()
        
        // Lista de desafíos
        ChallengesList()
    }
}

@Composable
fun FiltersSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Filtros",
            style = MaterialTheme.typography.titleMedium
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { /* Limpiar filtros */ },
                label = { Text("Limpiar Filtros") }
            )
        }
    }
}

@Composable
fun CategoriesSection() {
    val categories = listOf("Arte", "Deporte", "Música", "Ciencia", "Bienestar", "Tecnología")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = false,
                    onClick = { /* Seleccionar categoría */ },
                    label = { Text(category) }
                )
            }
        }
    }
    
    // Dificultad
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Dificultad",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Fácil", "Medio", "Difícil")) { difficulty ->
                FilterChip(
                    selected = false,
                    onClick = { /* Seleccionar dificultad */ },
                    label = { Text(difficulty) }
                )
            }
        }
    }
    
    // Duración
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Duración",
            style = MaterialTheme.typography.titleMedium
        )
        
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("1 día", "3 días", "1 Semana", "Flexible")) { duration ->
                FilterChip(
                    selected = false,
                    onClick = { /* Seleccionar duración */ },
                    label = { Text(duration) }
                )
            }
        }
    }
}

@Composable
fun ChallengesList() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(generateSampleChallenges()) { challenge ->
            ChallengeCard(challenge = challenge, isCompact = true)
        }
    }
}

private fun generateSampleChallenges() = listOf(
    Challenge("1", "30-Day Sentadillas", "FitLife", 2300, 1200, 500, 100),
    Challenge("2", "Pinta tu Atardecer Favorito", "ArtDaily", 1500, 800, 300, 150),
    Challenge("3", "Reto Musical Semanal", "MusicPro", 3000, 1500, 700, 200)
) 