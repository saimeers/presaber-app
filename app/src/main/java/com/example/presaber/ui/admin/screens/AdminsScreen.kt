package com.example.presaber.ui.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.ui.admin.components.AdminCard
import com.example.presaber.ui.admin.viewmodel.AdminsViewModel
import com.example.presaber.ui.admin.components.AddCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminsScreen(
    onAddAdmin: () -> Unit,
    viewModel: AdminsViewModel = viewModel()
) {
    val admins by viewModel.admins.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Estado del buscador
    var searchQuery by remember { mutableStateOf("") }

    // Lógica de filtrado (Nombre, Apellido o Correo)
    val adminsFiltrados = remember(admins, searchQuery) {
        if (searchQuery.isEmpty()) {
            admins
        } else {
            admins.filter { admin ->
                admin.nombre.contains(searchQuery, ignoreCase = true) ||
                        admin.apellido.contains(searchQuery, ignoreCase = true) ||
                        admin.correo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarAdministradores()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- HEADER Y BUSCADOR (Igual a Instituciones) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Administradores",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1B21)
                )

                Spacer(Modifier.height(16.dp))

                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nombre o correo...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF5B7BC6))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Limpiar", tint = Color.Gray)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B7BC6),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // --- LISTA DE RESULTADOS ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                // Botón Crear (Siempre visible arriba)
                item {
                    AddCard(
                        text = "Registrar nuevo administrador",
                        onClick = onAddAdmin
                    )
                }

                if (loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF5B7BC6))
                        }
                    }
                } else if (error != null) {
                    item {
                        Text(
                            text = error ?: "Error desconocido",
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                } else if (adminsFiltrados.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SentimentDissatisfied,
                                null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No se encontraron resultados" else "No hay administradores registrados",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(adminsFiltrados) { admin ->
                        AdminCard(admin = admin)
                    }
                }
            }
        }
    }
}