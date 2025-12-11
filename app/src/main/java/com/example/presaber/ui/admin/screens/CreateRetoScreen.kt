package com.example.presaber.ui.admin.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.Area
import com.example.presaber.data.remote.CrearRetoRequest
import com.example.presaber.data.remote.TemaConteo
import com.example.presaber.ui.admin.viewmodel.RetosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRetoScreen(
    onBack: () -> Unit,
    viewModel: RetosViewModel = viewModel()
) {
    val context = LocalContext.current
    val areas by viewModel.areas.collectAsState()
    val temas by viewModel.temas.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var dificultad by remember { mutableStateOf("Medio") } // Default
    var duracionMinutos by remember { mutableStateOf("") }
    var cantidadPreguntas by remember { mutableStateOf("") }

    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var selectedTema by remember { mutableStateOf<TemaConteo?>(null) }

    // Dropdowns states
    var expandedDificultad by remember { mutableStateOf(false) }
    var expandedArea by remember { mutableStateOf(false) }
    var expandedTema by remember { mutableStateOf(false) }

    val dificultades = listOf("Bajo", "Medio", "Alto")

    // Cargar áreas al iniciar (Ahora usamos cargarDatosIniciales que carga las áreas)
    LaunchedEffect(Unit) {
        // CORRECCIÓN 1: Usamos el método que ya existe en el ViewModel para cargar datos
        // Si tu ViewModel tiene 'cargarAreas', úsalo. Si tiene 'cargarDatosIniciales', usa ese.
        // Asumo que 'cargarDatosIniciales' carga las áreas según el código anterior.
        viewModel.cargarDatosIniciales()
    }

    // Mostrar error si ocurre
    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Reto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Información Básica", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF5B7BC6))
                        Spacer(Modifier.height(16.dp))

                        // Nombre
                        OutlinedTextField(
                            value = nombre, onValueChange = { nombre = it },
                            label = { Text("Nombre del Reto") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))

                        // Descripción
                        OutlinedTextField(
                            value = descripcion, onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Spacer(Modifier.height(8.dp))

                        // Dificultad
                        ExposedDropdownMenuBox(
                            expanded = expandedDificultad,
                            onExpandedChange = { expandedDificultad = !expandedDificultad }
                        ) {
                            OutlinedTextField(
                                value = dificultad,
                                onValueChange = {}, readOnly = true,
                                label = { Text("Dificultad") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDificultad) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDificultad,
                                onDismissRequest = { expandedDificultad = false }
                            ) {
                                dificultades.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = { dificultad = item; expandedDificultad = false }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Duración
                        OutlinedTextField(
                            value = duracionMinutos, onValueChange = { if(it.all { c -> c.isDigit() }) duracionMinutos = it },
                            label = { Text("Duración (minutos)") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Icon(Icons.Default.Timer, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Configuración de Preguntas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF5B7BC6))
                        Spacer(Modifier.height(16.dp))

                        // Selección de Área
                        ExposedDropdownMenuBox(
                            expanded = expandedArea,
                            onExpandedChange = { expandedArea = !expandedArea }
                        ) {
                            OutlinedTextField(
                                value = selectedArea?.nombre ?: "",
                                onValueChange = {}, readOnly = true,
                                label = { Text("Área de conocimiento") },
                                placeholder = { Text("Selecciona un área") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedArea) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
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
                                            selectedTema = null // Reset tema
                                            viewModel.cargarTemasPorArea(area.id_area)
                                            expandedArea = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Selección de Tema
                        ExposedDropdownMenuBox(
                            expanded = expandedTema,
                            onExpandedChange = { if (selectedArea != null) expandedTema = !expandedTema }
                        ) {
                            OutlinedTextField(
                                value = selectedTema?.descripcion ?: "",
                                onValueChange = {}, readOnly = true,
                                label = { Text("Tema específico") },
                                placeholder = { Text(if(selectedArea == null) "Primero selecciona un área" else "Selecciona un tema") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTema) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                enabled = selectedArea != null
                            )
                            ExposedDropdownMenu(
                                expanded = expandedTema,
                                onDismissRequest = { expandedTema = false }
                            ) {
                                temas.forEach { tema ->
                                    // Mostrar cantidad disponible en el dropdown
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(tema.descripcion)
                                                Text("${tema.total_preguntas} preguntas disponibles", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = { selectedTema = tema; expandedTema = false }
                                    )
                                }
                            }
                        }

                        if (selectedTema != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Preguntas disponibles en este tema: ${selectedTema!!.total_preguntas}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Cantidad de preguntas
                        OutlinedTextField(
                            value = cantidadPreguntas,
                            onValueChange = { if(it.all { c -> c.isDigit() }) cantidadPreguntas = it },
                            label = { Text("Cantidad de preguntas") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = selectedTema != null && (cantidadPreguntas.toIntOrNull() ?: 0) > selectedTema!!.total_preguntas,
                            supportingText = {
                                if (selectedTema != null && (cantidadPreguntas.toIntOrNull() ?: 0) > selectedTema!!.total_preguntas) {
                                    Text("No puedes exceder el máximo disponible (${selectedTema!!.total_preguntas})")
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val cant = cantidadPreguntas.toIntOrNull() ?: 0
                        val dur = duracionMinutos.toIntOrNull() ?: 0

                        if (nombre.isBlank() || descripcion.isBlank() || selectedTema == null || cant <= 0 || dur <= 0) {
                            Toast.makeText(context, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                        } else if (cant > selectedTema!!.total_preguntas) {
                            Toast.makeText(context, "La cantidad excede las preguntas disponibles", Toast.LENGTH_SHORT).show()
                        } else {
                            // Formatear duración a HH:MM:SS
                            val horas = dur / 60
                            val minutos = dur % 60
                            val duracionFormato = String.format("%02d:%02d:00", horas, minutos)

                            // CORRECCIÓN 2: Crear el objeto request aquí y pasarlo al ViewModel
                            val request = CrearRetoRequest(
                                nombre = nombre,
                                descripcion = descripcion,
                                nivel_dificultad = dificultad,
                                duracion = duracionFormato,
                                cantidad_preguntas = cant,
                                id_tema = selectedTema!!.id_tema
                            )

                            viewModel.crearReto(
                                req = request, // Pasamos el objeto 'req'
                                onSuccess = {
                                    Toast.makeText(context, "Reto creado exitosamente", Toast.LENGTH_LONG).show()
                                    onBack()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B7BC6))
                ) {
                    if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Crear Reto", fontSize = 16.sp)
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}