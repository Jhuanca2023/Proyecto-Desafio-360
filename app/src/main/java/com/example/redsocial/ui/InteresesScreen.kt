package com.example.redsocial.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.redsocial.AuthViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteresesScreen(navController: NavController, authViewModel: AuthViewModel) {
    var interesesSeleccionados by remember { mutableStateOf(setOf<String>()) }
    val intereses = listOf(
        "Música", "Deportes", "Arte", "Tecnología", "Viajes", 
        "Comida", "Moda", "Cine", "Literatura", "Fotografía",
        "Gaming", "Fitness", "Naturaleza", "Ciencia", "Baile"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selecciona tus intereses",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            intereses.forEach { interes ->
                InteresChip(
                    interes = interes,
                    selected = interesesSeleccionados.contains(interes),
                    onSelectedChange = { selected ->
                        interesesSeleccionados = if (selected) {
                            interesesSeleccionados + interes
                        } else {
                            interesesSeleccionados - interes
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                authViewModel.saveIntereses(interesesSeleccionados.toList())
                navController.navigate("home") {
                    popUpTo("intereses") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = interesesSeleccionados.isNotEmpty()
        ) {
            Text("Continuar")
        }
    }
}

@Composable
fun InteresChip(
    interes: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    ) {
        Row(modifier = Modifier
            .toggleable(
                value = selected,
                onValueChange = onSelectedChange
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = interes,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
} 