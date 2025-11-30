package com.example.presaber.ui.pvp

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
        Text("Duración (minutos)", fontWeight = FontWeight.SemiBold)
        Slider(
            value = duracionMinutos.toFloat(),
            onValueChange = { duracionMinutos = it.toInt() },
            valueRange = 1f..60f,
            steps = 59
        )
        Text(
            text = "$duracionMinutos minutos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        // ---------------- CANTIDAD DE PREGUNTAS ----------------
        Text("Cantidad de preguntas", fontWeight = FontWeight.SemiBold)

        if (selectedArea == null) {
            Text("Selecciona un área para ver la cantidad disponible")
        } else {
            OutlinedTextField(
                value = cantidadPreguntas.toString(),
                onValueChange = { value ->
                    val num = value.toIntOrNull() ?: 1
                    cantidadPreguntas = num.coerceIn(1, cantidadDisponible)
                },
                label = { Text("Mínimo 1, máximo $cantidadDisponible") },
                modifier = Modifier.fillMaxWidth()
            )
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
