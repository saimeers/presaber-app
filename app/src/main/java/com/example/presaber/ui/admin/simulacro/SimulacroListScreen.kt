package com.example.presaber.ui.admin.simulacro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
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

@Composable
fun SimulacroListScreen(
    onCrearSimulacro: () -> Unit,
    onAsignarSimulacro: (Int) -> Unit,
    viewModel: SimulacroAdminViewModel = viewModel()
) {
    val simulacros by viewModel.simulacros.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarSimulacros()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Título "Simulacro"
        Text(
            text = "Simulacros",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1B21),
            modifier = Modifier.padding(top=10.dp, bottom = 16.dp).align(Alignment.CenterHorizontally)

        )

        // Botón "Crear nuevo simulacro" - Morado claro con icono circular
        Button(
            onClick = onCrearSimulacro,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C88FF)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Icono circular con plus
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Crear nuevo simulacro",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        // Lista de simulacros
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Simulacros",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1B21)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { /* Filtrar/Ordenar */ }) {
                                Icon(
                                    Icons.Rounded.ArrowDownward,
                                    contentDescription = "Ordenar",
                                    tint = Color(0xFF757575),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = { /* Filtrar/Ordenar */ }) {
                                Icon(
                                    Icons.Rounded.Menu,
                                    contentDescription = "Filtrar",
                                    tint = Color(0xFF757575),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                items(simulacros) { simulacro ->
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
        }
    }
}

@Composable
fun SimulacroItem(
    simulacro: com.example.presaber.data.remote.SimulacroAdmin,
    onClick: () -> Unit,
    onToggleEstado: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (simulacro.estado) Color(0xFF2196F3) else Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = simulacro.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (simulacro.estado) Color.White else Color(0xFF1A1B21)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (simulacro.estado) "Habilitado" else "Deshabilitado",
                    fontSize = 14.sp,
                    color = if (simulacro.estado) Color.White.copy(alpha = 0.9f) else Color(0xFF757575)
                )
            }
            IconButton(onClick = onToggleEstado) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "Opciones",
                    tint = if (simulacro.estado) Color.White else Color(0xFF757575)
                )
            }
        }
    }
}


// -----------------------------
// PREVIEW
// -----------------------------
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSimulacroListScreen() {

    // Fake ViewModel que no llama Retrofit ni corrutinas
    val fakeViewModel = object : SimulacroAdminViewModel() {

        init {
            _simulacros.value = listOf(
                SimulacroAdmin(
                    id_simulacro = 1,
                    nombre = "Simulacro PreSaber #1",
                    estado = true,
                    descripcion = "",
                    fecha_creacion = ""
                ),
                SimulacroAdmin(
                    id_simulacro = 2,
                    nombre = "Simulacro Final Lenguaje",
                    estado = false,
                    descripcion = "",
                    fecha_creacion = ""
                ),
                SimulacroAdmin(
                    id_simulacro = 3,
                    nombre = "Simulacro Diagnóstico Inicial",
                    estado = true,
                    descripcion = "",
                    fecha_creacion = ""
                )
            )
        }

        override fun cargarSimulacros() {
            // No hace nada en preview
        }

        override fun actualizarEstadoSimulacro(idSimulacro: Int, estado: Boolean) {
            // Simula el cambio de estado
            val listaActual = _simulacros.value.toMutableList()
            val index = listaActual.indexOfFirst { it.id_simulacro == idSimulacro }
            if (index != -1) {
                val simulacro = listaActual[index]
                listaActual[index] = simulacro.copy(estado = estado)
                _simulacros.value = listaActual
            }
        }
    }

    SimulacroListScreen(
        onCrearSimulacro = {},
        onAsignarSimulacro = {},
        viewModel = fakeViewModel
    )
}
