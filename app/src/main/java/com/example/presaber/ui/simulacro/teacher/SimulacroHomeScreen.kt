package com.example.presaber.ui.simulacro.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.CursoUsuario
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SimulacroResumen

private val AccentBlue = Color(0xFF5685FF)
private val AccentGreen = Color(0xFF38C771)
private val AccentOrange = Color(0xFFFCB35A)
private val TextDark = Color(0xFF1A1B21)
private val TextGray = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulacroHomeScreen(
    idDocente: String,
    onCrearSimulacro: (CursoUsuario) -> Unit, // Pasamos el curso seleccionado al crear
    onVerSimulacro: (Int) -> Unit
) {
    // Estado para los cursos del docente
    var cursos by remember { mutableStateOf<List<CursoUsuario>>(emptyList()) }
    var cursoSeleccionado by remember { mutableStateOf<CursoUsuario?>(null) }
    var expandedCursoSelector by remember { mutableStateOf(false) } // Para el menú dropdown

    // Estado para los simulacros del curso seleccionado
    var simulacros by remember { mutableStateOf<List<SimulacroResumen>>(emptyList()) }
    var isLoadingSimulacros by remember { mutableStateOf(false) }

    // Estado inicial de carga de cursos
    var isLoadingCursos by remember { mutableStateOf(true) }

    // 1. Cargar Cursos del Docente al iniciar
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.obtenerCursosDeUsuario(idDocente)
            if (response.success && response.data.isNotEmpty()) {
                cursos = response.data
                cursoSeleccionado = cursos[0] // Seleccionar el primero por defecto
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingCursos = false
        }
    }

    // 2. Cargar Simulacros cuando cambia el curso seleccionado
    LaunchedEffect(cursoSeleccionado) {
        if (cursoSeleccionado != null) {
            isLoadingSimulacros = true
            try {
                val response = RetrofitClient.api.obtenerSimulacrosCurso(
                    cursoSeleccionado!!.grado,
                    cursoSeleccionado!!.grupo,
                    cursoSeleccionado!!.cohorte,
                    cursoSeleccionado!!.idInstitucion
                )
                if (response.success) {
                    simulacros = response.data
                } else {
                    simulacros = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                simulacros = emptyList()
            } finally {
                isLoadingSimulacros = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BackgroundBlobs()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // HEADER Y SELECTOR DE CURSO
            item {
                Column {
                    Text(
                        text = "Simulacros",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(Modifier.height(8.dp))

                    if (isLoadingCursos) {
                        LinearProgressIndicator(modifier = Modifier.width(100.dp), color = AccentBlue)
                    } else if (cursoSeleccionado != null) {
                        // SELECTOR DE CURSO
                        Box {
                            Surface(
                                onClick = { expandedCursoSelector = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF5F7FA),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Curso Seleccionado",
                                            fontSize = 10.sp,
                                            color = TextGray
                                        )
                                        Text(
                                            text = "${cursoSeleccionado!!.grado}${cursoSeleccionado!!.grupo} - Cohorte ${cursoSeleccionado!!.cohorte}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlue
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Icon(Icons.Rounded.ArrowDropDown, null, tint = TextGray)
                                }
                            }

                            // Dropdown Menu
                            DropdownMenu(
                                expanded = expandedCursoSelector,
                                onDismissRequest = { expandedCursoSelector = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                cursos.forEach { curso ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${curso.grado}${curso.grupo} - ${curso.cohorte}",
                                                fontWeight = if(curso == cursoSeleccionado) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            cursoSeleccionado = curso
                                            expandedCursoSelector = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No tienes cursos asignados", color = Color.Red)
                    }
                }
            }

            // BOTÓN CREAR (Solo si hay curso seleccionado)
            if (cursoSeleccionado != null) {
                item {
                    Surface(
                        onClick = { onCrearSimulacro(cursoSeleccionado!!) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = AccentBlue,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Crear simulacro",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Para ${cursoSeleccionado!!.grado}${cursoSeleccionado!!.grupo}",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // TÍTULO HISTORIAL
            item {
                Text(
                    text = "Historial",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            // LISTA DE SIMULACROS
            if (isLoadingSimulacros) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
            } else if (simulacros.isEmpty()) {
                item {
                    EmptyStateSimulacro()
                }
            } else {
                items(simulacros) { simulacro ->
                    SimulacroItemCard(
                        simulacro = simulacro,
                        onClick = { onVerSimulacro(simulacro.id_simulacro) }
                    )
                }
            }
        }
    }
}

// ... (SimulacroItemCard, EmptyStateSimulacro y BackgroundBlobs se mantienen igual) ...
// Asegúrate de copiar las funciones auxiliares que ya tenías para que compile.
@Composable
fun SimulacroItemCard(
    simulacro: SimulacroResumen,
    onClick: () -> Unit
) {
    val estadoColor = when (simulacro.estado) {
        "esperando" -> AccentOrange
        "en_curso" -> AccentGreen
        "finalizado" -> TextGray
        else -> Color.Gray
    }

    val estadoTexto = when (simulacro.estado) {
        "esperando" -> "Esperando"
        "en_curso" -> "En curso"
        "finalizado" -> "Finalizado"
        else -> simulacro.estado
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = estadoColor.copy(alpha = 0.1f),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = estadoTexto.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = estadoColor,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Quiz,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${simulacro.cantidad_preguntas} preguntas",
                            fontSize = 14.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${simulacro.duracion_minutos} minutos",
                            fontSize = 14.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = simulacro.fecha_creacion.take(10),
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
fun EmptyStateSimulacro() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Quiz,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Aún no hay simulacros",
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Crea el primero para este curso",
            color = Color.LightGray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BackgroundBlobs() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFE3F2FD).copy(alpha = 0.5f),
            radius = size.width * 0.6f,
            center = Offset(size.width * 1.2f, size.height * 0.1f)
        )
        drawCircle(
            color = Color(0xFFFFF3E0).copy(alpha = 0.5f),
            radius = size.width * 0.4f,
            center = Offset(0f, size.height * 0.9f)
        )
    }
}