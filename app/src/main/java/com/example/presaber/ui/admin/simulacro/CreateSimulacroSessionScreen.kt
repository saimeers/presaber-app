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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.*
import com.example.presaber.ui.admin.viewmodel.SimulacroAdminViewModel

@Composable
fun CreateSimulacroSessionScreen(
    idSimulacro: Int,
    numeroSesion: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onNavigateToCreateQuestion: (Int) -> Unit, // id_area
    onNavigateToQuestionBank: (Int) -> Unit, // id_area
    viewModel: SimulacroAdminViewModel = viewModel()
) {
    val estructuraICFES by viewModel.estructuraICFES.collectAsState()
    val simulacroActual by viewModel.simulacroActual.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var expandedAreas by remember { mutableStateOf<Set<Int>>(emptySet()) }

    LaunchedEffect(idSimulacro) {
        viewModel.cargarEstructuraICFES()
        viewModel.cargarSimulacroPorId(idSimulacro)
    }

    val sesionEstructura = estructuraICFES.find { it.orden == numeroSesion }
    val sesionCompleta = simulacroActual?.sesions?.find { it.orden == numeroSesion }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Título "Crear Simulacro" - Azul oscuro, grande
        Text(
            text = "Crear Simulacro",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Barra gris con "Sesión X"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Sesión $numeroSesion",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575)
            )
        }

        // Lista de áreas
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            sesionEstructura?.areas?.forEach { areaEstructura ->
                val idArea = areaEstructura.id_area
                val isExpanded = expandedAreas.contains(idArea)
                val preguntasEnArea = sesionCompleta?.sesion_areas
                    ?.find { it.id_area == idArea }
                    ?.sesion_preguntas ?: emptyList()
                val cantidadEsperada = areaEstructura.cantidad

                item {
                    AreaCard(
                        area = areaEstructura,
                        preguntasActuales = preguntasEnArea.size,
                        cantidadEsperada = cantidadEsperada,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedAreas = if (isExpanded) {
                                expandedAreas - idArea
                            } else {
                                expandedAreas + idArea
                            }
                        },
                        onCrearPregunta = {
                            onNavigateToCreateQuestion(idArea)
                        },
                        onCargarDelBanco = {
                            onNavigateToQuestionBank(idArea)
                        },
                        preguntas = preguntasEnArea,
                        idSimulacro = idSimulacro,
                        numeroSesion = numeroSesion,
                        viewModel = viewModel
                    )
                }
            }
        }

        // Botones en la parte inferior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón Atrás - Azul sólido
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5685FF)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Atrás", fontSize = 16.sp, color = Color.White)
            }

            // Botón Siguiente - Gris claro
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E9E9E)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Siguiente", fontSize = 16.sp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Rounded.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AreaCard(
    area: AreaEstructura,
    preguntasActuales: Int,
    cantidadEsperada: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCrearPregunta: () -> Unit,
    onCargarDelBanco: () -> Unit,
    preguntas: List<SesionPreguntaCompleta>,
    idSimulacro: Int,
    numeroSesion: Int,
    viewModel: SimulacroAdminViewModel
) {
    // Colores según el área (exactos de las imágenes)
    val (colorArea, textColor) = when (area.id_area) {
        1 -> Color(0xFFFFEB3B) to Color(0xFF757575) // Lectura Crítica - Amarillo
        2 -> Color(0xFF2196F3) to Color.White // Matemáticas - Azul oscuro
        3 -> Color(0xFF4CAF50) to Color.White // Ciencias Naturales - Verde claro
        4 -> Color(0xFF9E9E9E) to Color.White // Ciencias Sociales - Gris
        5 -> Color(0xFFF44336) to Color.White // Inglés - Rojo
        else -> Color(0xFFE0E0E0) to Color(0xFF1A1B21)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) colorArea.copy(alpha = 0.2f) else colorArea
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Header del área - Barra de color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .background(if (!isExpanded) colorArea else Color.Transparent)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${area.nombre} ($cantidadEsperada)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (!isExpanded) textColor else Color(0xFF1A1B21)
                )
                Icon(
                    if (isExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    tint = if (!isExpanded) textColor else Color(0xFF757575)
                )
            }

            // Contenido expandido
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Lista de preguntas
                    preguntas.forEachIndexed { index, sesionPregunta ->
                        PreguntaItem(
                            numeroPregunta = index + 1,
                            pregunta = sesionPregunta.pregunta,
                            puntajeBase = sesionPregunta.puntaje_base,
                            onPuntajeChange = { nuevoPuntaje ->
                                // TODO: Implementar actualización de puntaje
                            }
                        )
                    }

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón "Crear Pregunta" - Verde/azul claro con icono circular
                        Button(
                            onClick = onCrearPregunta,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Crear Pregunta", fontSize = 14.sp, color = Color.White)
                        }

                        // Botón "Cargar del banco" - Azul claro con icono circular
                        Button(
                            onClick = onCargarDelBanco,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Cargar del banco", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreguntaItem(
    numeroPregunta: Int,
    pregunta: Pregunta,
    puntajeBase: Double,
    onPuntajeChange: (Double) -> Unit
) {
    var puntajeText by remember { mutableStateOf(puntajeBase.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pregunta $numeroPregunta",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1B21)
            )
            
            OutlinedTextField(
                value = puntajeText,
                onValueChange = {
                    puntajeText = it
                    it.toDoubleOrNull()?.let { valor ->
                        onPuntajeChange(valor)
                    }
                },
                label = { Text("Puntaje base", fontSize = 12.sp) },
                modifier = Modifier.width(120.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1565C0),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
        }
    }
}
