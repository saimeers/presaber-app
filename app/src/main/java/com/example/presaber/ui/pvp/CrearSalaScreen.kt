package com.example.presaber.ui.pvp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.Area
import com.example.presaber.data.remote.CrearSalaRequest
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.ui.theme.PresaberTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSalaScreen(
    idEstudiante: String,
    areas: List<Area>,
    onSalaCreada: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var selectedDificultad by remember { mutableStateOf("medio") }

    var duracionMinutos by remember { mutableStateOf(10) }

    var cantidadDisponible by remember { mutableStateOf(0) }
    var cantidadPreguntas by remember { mutableStateOf(1) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    var expandedArea by remember { mutableStateOf(false) }
    var expandedDificultad by remember { mutableStateOf(false) }

    val dificultades = listOf(
        "bajo" to "Bajo",
        "medio" to "Medio",
        "alto" to "Alto"
    )

    // ---------- CARGAR CANTIDAD DISPONIBLE AL SELECCIONAR ÁREA O NIVEL ----------
    LaunchedEffect(selectedArea, selectedDificultad) {
        if (selectedArea != null) {
            try {
                val response = RetrofitClient.api.contarPreguntas(
                    idArea = selectedArea!!.id_area,
                    nivel = selectedDificultad
                )

                cantidadDisponible = response.total

                if (cantidadPreguntas > cantidadDisponible) {
                    cantidadPreguntas = cantidadDisponible
                }
            } catch (e: Exception) {
                cantidadDisponible = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Atrás", tint = Color(0xFF1A1B21))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Crear sala",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21)
            )
        }

        Spacer(Modifier.height(24.dp))

        // ---------------- SELECCIONAR ÁREA ----------------
        ExposedDropdownMenuBox(
            expanded = expandedArea,
            onExpandedChange = { expandedArea = !expandedArea }
        ) {
            OutlinedTextField(
                value = selectedArea?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Área saber") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedArea) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = expandedArea,
                onDismissRequest = { expandedArea = false }
            ) {
                areas.forEach { area ->
                    DropdownMenuItem(
                        text = { Text(area.nombre) },
                        onClick = {
                            selectedArea = area
                            expandedArea = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------------- DIFICULTAD ----------------
        ExposedDropdownMenuBox(
            expanded = expandedDificultad,
            onExpandedChange = { expandedDificultad = !expandedDificultad }
        ) {
            OutlinedTextField(
                value = dificultades.find { it.first == selectedDificultad }?.second ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Nivel de dificultad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDificultad) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = expandedDificultad,
                onDismissRequest = { expandedDificultad = false }
            ) {
                dificultades.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            selectedDificultad = value
                            expandedDificultad = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------------- DURACIÓN ----------------
        Text("Duración", fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4A6FA5).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display grande de la duración
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$duracionMinutos",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A6FA5)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (duracionMinutos == 1) "minuto" else "minutos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A6FA5).copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Slider mejorado
                Slider(
                    value = duracionMinutos.toFloat(),
                    onValueChange = { duracionMinutos = it.toInt() },
                    valueRange = 1f..60f,
                    steps = 58,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4A6FA5),
                        activeTrackColor = Color(0xFF4A6FA5),
                        inactiveTrackColor = Color(0xFF4A6FA5).copy(alpha = 0.2f)
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Labels de rango
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1 min",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "60 min",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------------- CANTIDAD DE PREGUNTAS ----------------
        Text("Cantidad de preguntas", fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(8.dp))

        if (selectedArea == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Selecciona un área para ver la cantidad disponible",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón menos (-)
                OutlinedIconButton(
                    onClick = {
                        if (cantidadPreguntas > 1) {
                            cantidadPreguntas--
                        }
                    },
                    enabled = cantidadPreguntas > 1,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = Color(0xFF4A6FA5),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (cantidadPreguntas > 1) Color(0xFF4A6FA5) else Color.LightGray
                    )
                ) {
                    Text(
                        text = "−",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Display central con cantidad
                Card(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4A6FA5).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$cantidadPreguntas",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A6FA5)
                        )
                        Text(
                            text = "de $cantidadDisponible disponibles",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Botón más (+)
                OutlinedIconButton(
                    onClick = {
                        if (cantidadPreguntas < cantidadDisponible) {
                            cantidadPreguntas++
                        }
                    },
                    enabled = cantidadPreguntas < cantidadDisponible,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = Color(0xFF4A6FA5),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (cantidadPreguntas < cantidadDisponible) Color(0xFF4A6FA5) else Color.LightGray
                    )
                ) {
                    Text(
                        text = "+",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (selectedArea == null) {
                    errorMessage = "Selecciona un área"
                    return@Button
                }
                if (cantidadDisponible == 0) {
                    errorMessage = "No hay preguntas disponibles"
                    return@Button
                }

                errorMessage = null
                isCreating = true

                scope.launch {
                    try {
                        val res = RetrofitClient.api.crearSala(
                            CrearSalaRequest(
                                id_estudiante = idEstudiante,
                                id_area = selectedArea!!.id_area,
                                nivel_dificultad = selectedDificultad,
                                duracion_minutos = duracionMinutos,
                                cantidad_preguntas = cantidadPreguntas
                            )
                        )

                        if (res.success) {
                            onSalaCreada(res.data.id_sala)
                        } else {
                            errorMessage = res.message
                        }

                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    } finally {
                        isCreating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCreating
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Crear sala")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CrearSalaScreenPreview() {
    PresaberTheme{

        // Datos fake para que el preview funcione
        val fakeAreas = listOf(
            Area(id_area = 1, nombre = "Matemáticas"),
            Area(id_area = 2, nombre = "Lectura Crítica"),
            Area(id_area = 3, nombre = "Ciencias Naturales")
        )
        CrearSalaScreen(
            idEstudiante = "123",
            areas = fakeAreas,
            onSalaCreada = {},
            onBack = {}
        )
    }
}
