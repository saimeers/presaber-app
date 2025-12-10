package com.example.presaber.ui.simulacro.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.SimulacroDisponible
import com.example.presaber.ui.layout.StudentLayout
import com.example.presaber.viewmodel.SimulacroEstudianteViewModel

@Composable
fun SimulacrosDisponiblesScreen(
    idEstudiante: String,
    onBack: () -> Unit,
    onSimulacroSelected: (SimulacroDisponible) -> Unit,
    viewModel: SimulacroEstudianteViewModel = viewModel()
) {
    var selectedNavItem by remember { mutableStateOf(0) }
    val showAccountDialog = remember { mutableStateOf(false) }

    LaunchedEffect(idEstudiante) {
        viewModel.cargarSimulacrosDisponibles(idEstudiante)
    }

    val simulacrosDisponibles by viewModel.simulacrosDisponibles.collectAsState()
    val loading by viewModel.loading.collectAsState()

    StudentLayout(
        selectedNavItem = selectedNavItem,
        onNavItemSelected = { },
        showAccountDialog = showAccountDialog,
        usuario = null,
        onSignOut = {}
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color(0xFF1A1B21)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulacros Disponibles",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1B21)
                    )
                }

                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF5685FF))
                    }
                } else if (simulacrosDisponibles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay simulacros disponibles en este momento",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(simulacrosDisponibles) { simulacro ->
                            SimulacroDisponibleCard(
                                simulacro = simulacro,
                                onClick = { onSimulacroSelected(simulacro) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulacroDisponibleCard(
    simulacro: SimulacroDisponible,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = simulacro.simulacro.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sesiones: ${simulacro.simulacro.sesions.size}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Curso: ${simulacro.grado}${simulacro.grupo}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

