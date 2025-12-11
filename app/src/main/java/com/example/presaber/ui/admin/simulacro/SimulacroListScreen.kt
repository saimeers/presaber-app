package com.example.presaber.ui.admin.simulacro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.SimulacroAdmin
import com.example.presaber.ui.admin.viewmodel.SimulacroAdminViewModel

// Colores de la paleta
private val PrimaryBlue = Color(0xFF5B7BC6)
private val TextDark = Color(0xFF1A1B21)
private val TextGray = Color(0xFF757575)
private val BackgroundColor = Color(0xFFF8F9FA)
private val EnabledGreen = Color(0xFF4CAF50)
private val DisabledRed = Color(0xFFF44336)

@Composable
fun SimulacroListScreen(
    onCrearSimulacro: () -> Unit,
    onAsignarSimulacro: (Int) -> Unit,
    viewModel: SimulacroAdminViewModel = viewModel()
) {
    val simulacros by viewModel.simulacros.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Filtrar simulacros
    val simulacrosFiltrados = remember(simulacros, searchQuery) {
        if (searchQuery.isEmpty()) simulacros
        else simulacros.filter { it.nombre.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarSimulacros()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Gestión de Simulacros",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar simulacro...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )
            }

            // --- LISTA ---
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Botón Crear dentro de la lista (como primer elemento o header)
                    item {
                        Button(
                            onClick = onCrearSimulacro,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Crear Nuevo Simulacro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Listado de Simulacros", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    if (simulacrosFiltrados.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay simulacros registrados", color = TextGray)
                            }
                        }
                    } else {
                        items(simulacrosFiltrados) { simulacro ->
                            SimulacroItem(
                                simulacro = simulacro,
                                onClick = { onAsignarSimulacro(simulacro.id_simulacro) },
                                onToggleEstado = {
                                    viewModel.actualizarEstadoSimulacro(
                                        simulacro.id_simulacro,
                                        !simulacro.estado
                                    )
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) } // Espacio extra al final
                }
            }
        }
    }
}

@Composable
fun SimulacroItem(
    simulacro: SimulacroAdmin,
    onClick: () -> Unit,
    onToggleEstado: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Surface(
                color = if (simulacro.estado) PrimaryBlue.copy(alpha = 0.1f) else Color(0xFFEEEEEE),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Assignment,
                        contentDescription = null,
                        tint = if (simulacro.estado) PrimaryBlue else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = simulacro.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Estado como Chip pequeño
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (simulacro.estado) EnabledGreen else DisabledRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (simulacro.estado) "Habilitado" else "Deshabilitado",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            }

            // Menú opciones
            IconButton(onClick = onToggleEstado) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = TextGray
                )
            }
        }
    }
}