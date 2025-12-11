package com.example.presaber.ui.simulacro.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SesionDisponible
import com.example.presaber.data.remote.SimulacroDisponible
import com.example.presaber.ui.layout.StudentLayout

@Composable
fun SimulacroSesionesScreen(
    idEstudiante: String,
    simulacro: SimulacroDisponible,
    onBack: () -> Unit,
    onComenzarSesion: (Int, Int) -> Unit
) {
    var selectedNavItem by remember { mutableStateOf(0) }
    val showAccountDialog = remember { mutableStateOf(false) }
    var racha by remember {mutableStateOf(0)}

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.obtenerRacha(idEstudiante)
            if (response.success) {
                racha = response.data.rachaVictorias
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    StudentLayout(
        selectedNavItem = selectedNavItem,
        onNavItemSelected = { },
        showAccountDialog = showAccountDialog,
        usuario = null,
        racha = racha,
        onSignOut = {}
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color(0xFF1A1B21)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = simulacro.simulacro.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1B21)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Sesiones",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1B21)
                        )
                    }

                    item {
                        Text(
                            text = "Una vez inicies no podrás salir; al terminar la primera sesión tienes 2 horas para comenzar la siguiente o el examen se reinicia.",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            lineHeight = 20.sp
                        )
                    }

                    items(simulacro.simulacro.sesions.sortedBy { it.orden }) { sesion ->
                        SesionCard(
                            sesion = sesion,
                            numeroSesion = sesion.orden,
                            onComenzar = {
                                onComenzarSesion(sesion.id_sesion, simulacro.id_curso_simulacro)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SesionCard(
    sesion: SesionDisponible,
    numeroSesion: Int,
    onComenzar: () -> Unit
) {
    val subtitulo = if (numeroSesion == 1) "Mañana" else "Tarde"
    val duracionHoras = formatDuracion(sesion.duracion_segundos)
    
    // Calcular cantidad de preguntas (esto debería venir del backend, por ahora usamos un valor por defecto)
    val preguntasPorSesion = when (numeroSesion) {
        1 -> 120
        2 -> 134
        else -> 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con número de sesión
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Círculo con número
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF5685FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numeroSesion.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sesion.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1B21)
                    )
                    Text(
                        text = subtitulo,
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            // Imagen placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder con formas geométricas simples
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFB0C4DE))
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFB0C4DE))
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFB0C4DE))
                    )
                }
            }

            // Título del área
            Text(
                text = getTituloAreaPorSesion(numeroSesion),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21)
            )

            // Duración y preguntas
            Text(
                text = "$duracionHoras - $preguntasPorSesion preguntas",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )

            // Descripción
            Text(
                text = getDescripcionSesion(numeroSesion),
                fontSize = 14.sp,
                color = Color(0xFF757575),
                lineHeight = 20.sp
            )

            // Botón Comenzar
            val esCompletada = sesion.completada
            val habilitada = sesion.habilitada && !esCompletada

            Button(
                onClick = onComenzar,
                enabled = habilitada,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (habilitada) Color(0xFF5685FF) else Color(0xFFBDBDBD),
                    disabledContainerColor = Color(0xFFBDBDBD)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when {
                        esCompletada -> "Completada"
                        sesion.habilitada -> "Comenzar"
                        else -> "No disponible"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun formatDuracion(segundos: Int): String {
    val horas = segundos / 3600
    val minutos = (segundos % 3600) / 60
    return if (horas > 0) {
        "$horas:${String.format("%02d", minutos)} horas"
    } else {
        "$minutos minutos"
    }
}

fun getTituloAreaPorSesion(numeroSesion: Int): String {
    return when (numeroSesion) {
        1 -> "Operaciones básicas"
        2 -> "Operaciones básicas"
        else -> "Área"
    }
}

fun getDescripcionSesion(numeroSesion: Int): String {
    return when (numeroSesion) {
        1 -> "Evalúa Matemáticas, Lectura Crítica, Ciencias Sociales y Ciudadanas, Ciencias Naturales y un cuestionario socioeconómico."
        2 -> "Evalúa Matemáticas, Inglés, Ciencias Sociales y Ciudadanas, Ciencias Naturales y un cuestionario socioeconómico."
        else -> ""
    }
}

