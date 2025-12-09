package com.example.presaber.ui.simulacro.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SimulacroResumen

private val AccentBlue = Color(0xFF5685FF)
private val AccentGreen = Color(0xFF38C771)
private val AccentOrange = Color(0xFFFCB35A)
private val TextDark = Color(0xFF1A1B21)
private val TextGray = Color(0xFF757575)

@Composable
fun SimulacroHomeScreen(
    idDocente: String,
    grado: String,
    grupo: String,
    cohorte: Int,
    idInstitucion: Int,
    onCrearSimulacro: () -> Unit,
    onVerSimulacro: (Int) -> Unit
) {
    var simulacros by remember { mutableStateOf<List<SimulacroResumen>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.obtenerSimulacrosCurso(
                grado, grupo, cohorte, idInstitucion
            )
            if (response.success) {
                simulacros = response.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Fondo decorativo
        BackgroundBlobs()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Simulacro",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "$grado$grupo - Cohorte $cohorte",
                        fontSize = 16.sp,
                        color = TextGray
                    )
                }
            }

            // Botón crear simulacro
            item {
                Surface(
                    onClick = onCrearSimulacro,
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
                                text = "Crear nuevo simulacro",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Para este curso",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Título historial
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Simulacros del curso",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }

            // Lista de simulacros
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
            // Estado badge
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

            // Info
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

                // Fecha
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
            text = "Crea el primero para tu curso",
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