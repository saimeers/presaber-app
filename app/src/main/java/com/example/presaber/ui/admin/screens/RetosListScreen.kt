package com.example.presaber.ui.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.Area
import com.example.presaber.data.remote.Reto
import com.example.presaber.ui.admin.viewmodel.RetosViewModel

@Composable
fun RetosListScreen(
    onCreateReto: () -> Unit,
    viewModel: RetosViewModel = viewModel()
) {
    val areas by viewModel.areas.collectAsState()
    val retos by viewModel.retos.collectAsState()
    val selectedAreaId by viewModel.selectedAreaId.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Cargar datos al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarDatosIniciales()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateReto,
                containerColor = Color(0xFF5B7BC6),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Reto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // --- HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Gestión de Retos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1B21),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- FILTROS DE ÁREA (Scroll Horizontal) ---
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(areas) { area ->
                        AreaFilterChip(
                            area = area,
                            isSelected = area.id_area == selectedAreaId,
                            onClick = { viewModel.seleccionarArea(area.id_area) }
                        )
                    }
                }
            }

            // --- LISTA DE RETOS ---
            if (loading && retos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF5B7BC6))
                }
            } else if (retos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SentimentDissatisfied,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay retos en esta área", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(retos) { reto ->
                        RetoAdminCard(reto)
                    }

                    // Espacio para que el FAB no tape el último item
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}

@Composable
fun AreaFilterChip(area: Area, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
    val borderColor = if (isSelected) Color(0xFF5B7BC6) else Color(0xFFE0E0E0)
    val textColor = if (isSelected) Color(0xFF1976D2) else Color(0xFF757575)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .height(36.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = area.nombre,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
fun RetoAdminCard(reto: Reto) {
    // Determinar color de dificultad
    val diffColor = when(reto.nivel_dificultad.lowercase()) {
        "bajo" -> Color(0xFF4CAF50)
        "medio" -> Color(0xFFFF9800)
        "alto" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Título y Tema
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reto.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1B21)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reto.tema.descripcion, // Asumiendo que el objeto Tema tiene descripción
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Badge de Dificultad
                Surface(
                    color = diffColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = reto.nivel_dificultad,
                        color = diffColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Detalles: Duración y Preguntas
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reto.duracion,
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reto.cantidad_preguntas} pregs",
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
            }
        }
    }
}