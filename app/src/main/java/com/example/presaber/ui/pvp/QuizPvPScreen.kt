package com.example.presaber.ui.pvp

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.*
import com.example.presaber.ui.home.components.QuestionCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun QuizPvPScreen(
    idSala: Int,
    idEstudiante: String,
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var preguntas by remember { mutableStateOf<List<PreguntaSala>?>(null) }
    var duracionMinutos by remember { mutableStateOf(20) }
    var progreso by remember { mutableStateOf<List<ProgresoJugador>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOptionId by remember { mutableStateOf<Int?>(null) }
    var elapsedSeconds by remember { mutableStateOf(0L) }
    var submitting by remember { mutableStateOf(false) }
    var timerRunning by remember { mutableStateOf(false) }

    fun formatDuration(sec: Long): String {
        val h = TimeUnit.SECONDS.toHours(sec)
        val m = TimeUnit.SECONDS.toMinutes(sec) % 60
        val s = sec % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun formatTiempoRestante(sec: Long, duracionMin: Int): String {
        val tiempoLimiteSegundos = duracionMin * 60L
        val restante = tiempoLimiteSegundos - sec
        if (restante <= 0) return "00:00"
        val m = restante / 60
        val s = restante % 60
        return String.format("%02d:%02d", m, s)
    }

    // Cargar preguntas
    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.api.obtenerPreguntasSala(idSala)
            if (resp.success && resp.data.preguntas.isNotEmpty()) {
                preguntas = resp.data.preguntas
                duracionMinutos = resp.data.sala.duracion_minutos
                timerRunning = true
            } else {
                errorMessage = "No hay preguntas disponibles"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Cronómetro
    LaunchedEffect(timerRunning, duracionMinutos) {
        while (timerRunning) {
            delay(1000L)
            elapsedSeconds += 1
            val tiempoLimiteSegundos = duracionMinutos * 60L
            if (elapsedSeconds >= tiempoLimiteSegundos) {
                timerRunning = false
                scope.launch {
                    try {
                        RetrofitClient.api.finalizarPvP(
                            FinalizarPvPRequest(idSala, idEstudiante, formatDuration(elapsedSeconds))
                        )
                    } catch (e: Exception) { }
                    onFinish()
                }
            }
        }
    }

    // Polling del progreso
    LaunchedEffect(idSala) {
        while (true) {
                try {
                    val response = RetrofitClient.api.obtenerProgresoSala(idSala)
                    if (response.success) {
                        progreso = response.data
                    }
                } catch (e: Exception) { }
            delay(500)
        }
    }

    LaunchedEffect(currentIndex) {
        scrollState.scrollTo(0)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header con temporizador mejorado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        timerRunning = false
                        scope.launch {
                            try {
                                RetrofitClient.api.finalizarPvP(
                                    FinalizarPvPRequest(idSala, idEstudiante, formatDuration(elapsedSeconds))
                                )
                            } catch (e: Exception) { }
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53935)
                    ),
                    enabled = !submitting,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Rendirse", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                // Temporizador mejorado
                TemporizadorModerno(
                    tiempoRestante = formatTiempoRestante(elapsedSeconds, duracionMinutos),
                    elapsedSeconds = elapsedSeconds,
                    duracionMinutos = duracionMinutos
                )
            }

            Spacer(Modifier.height(20.dp))

            // Barra de progreso de jugadores (mejorada)
            BarraProgresoJugadoresMejorada(
                progreso = progreso,
                idEstudiante = idEstudiante,
                totalPreguntas = preguntas?.size ?: 0
            )

            Spacer(Modifier.height(20.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4A6FA5))
                    }
                }

                errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = onFinish) { Text("Volver") }
                        }
                    }
                }

                preguntas == null || preguntas!!.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No hay preguntas disponibles")
                    }
                }

                else -> {
                    val pregunta = preguntas!![currentIndex]

                    // Indicador de pregunta actual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pregunta ${currentIndex + 1} de ${preguntas!!.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1B21)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Pregunta con scroll
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        QuestionCard(
                            pregunta = convertirAPregunta(pregunta),
                            selectedOptionId = selectedOptionId,
                            onSelect = { if (!submitting) selectedOptionId = it }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botón de siguiente/finalizar centrado
                    Button(
                        onClick = {
                            val opcion = selectedOptionId ?: return@Button
                            if (submitting) return@Button

                            submitting = true
                            scope.launch {
                                try {
                                    RetrofitClient.api.guardarRespuestaPvP(
                                        RespuestaPvPRequest(
                                            id_sala = idSala,
                                            id_estudiante = idEstudiante,
                                            id_pregunta = pregunta.id_pregunta,
                                            id_opcion = opcion,
                                            tiempo_respuesta = 0
                                        )
                                    )

                                    if (currentIndex == preguntas!!.size - 1) {
                                        timerRunning = false
                                        RetrofitClient.api.finalizarPvP(
                                            FinalizarPvPRequest(idSala, idEstudiante, formatDuration(elapsedSeconds))
                                        )
                                        delay(500)
                                        onFinish()
                                    } else {
                                        selectedOptionId = null
                                        currentIndex += 1
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                } finally {
                                    submitting = false
                                }
                            }
                        },
                        enabled = selectedOptionId != null && !submitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A6FA5),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(
                                Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (currentIndex == preguntas!!.size - 1) "Finalizar" else "Siguiente",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemporizadorModerno(
    tiempoRestante: String,
    elapsedSeconds: Long,
    duracionMinutos: Int
) {
    val tiempoLimiteSegundos = duracionMinutos * 60L
    val esCritico = elapsedSeconds >= (tiempoLimiteSegundos - 60)

    // Animación de pulso cuando es crítico
    val scale by animateFloatAsState(
        targetValue = if (esCritico) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                esCritico -> Color(0xFFE53935)
                elapsedSeconds >= (tiempoLimiteSegundos - 180) -> Color(0xFFFFA726)
                else -> Color(0xFF4A6FA5)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Tiempo",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = tiempoRestante,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun BarraProgresoJugadoresMejorada(
    progreso: List<ProgresoJugador>,
    idEstudiante: String,
    totalPreguntas: Int
) {
    if (progreso.isEmpty() || totalPreguntas == 0) return

    val miProgreso = progreso.find { it.id_estudiante == idEstudiante }
    val oponenteProgreso = progreso.find { it.id_estudiante != idEstudiante }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mi progreso
        miProgreso?.let {
            FilaProgresoJugadorCompacta(
                jugador = it,
                totalPreguntas = totalPreguntas,
                esYo = true
            )
        }

        // Oponente
        oponenteProgreso?.let {
            FilaProgresoJugadorCompacta(
                jugador = it,
                totalPreguntas = totalPreguntas,
                esYo = false
            )
        }
    }
}

@Composable
fun FilaProgresoJugadorCompacta(
    jugador: ProgresoJugador,
    totalPreguntas: Int,
    esYo: Boolean
) {
    val progresoFraction = jugador.preguntas_respondidas.toFloat() / totalPreguntas.coerceAtLeast(1)
    val colorPrincipal = if (esYo) Color(0xFF4A6FA5) else Color(0xFFF4A261)

    // Animación del progreso
    val animatedProgress by animateFloatAsState(
        targetValue = progresoFraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "progress"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar compacto
        Box(contentAlignment = Alignment.Center) {
            if (jugador.photoURL != null) {
                AsyncImage(
                    model = jugador.photoURL,
                    contentDescription = jugador.nombre,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(colorPrincipal, colorPrincipal.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = jugador.nombre.split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull() }
                            .joinToString("")
                            .uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Barra de progreso con segmentos de colores
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    val totalWidth = size.width
                    val totalHeight = size.height
                    val segmentWidth = totalWidth / totalPreguntas.coerceAtLeast(1)
                    val correctas = jugador.preguntas_correctas
                    val respondidas = jugador.preguntas_respondidas
                    val incorrectas = respondidas - correctas

                    // Fondo gris para todas las preguntas
                    drawRect(
                        color = Color(0xFFE0E0E0),
                        topLeft = Offset(0f, 0f),
                        size = Size(totalWidth, totalHeight)
                    )

                    // Dibujar segmentos de correctas (verde)
                    for (i in 0 until correctas) {
                        drawRect(
                            color = Color(0xFF4CAF50),
                            topLeft = Offset(i * segmentWidth + 0.5f, 0f),
                            size = Size((segmentWidth - 1f).coerceAtLeast(0f), totalHeight)
                        )
                    }

                    // Dibujar segmentos de incorrectas (rojo)
                    for (i in 0 until incorrectas) {
                        drawRect(
                            color = Color(0xFFE53935),
                            topLeft = Offset((correctas + i) * segmentWidth + 0.5f, 0f),
                            size = Size((segmentWidth - 1f).coerceAtLeast(0f), totalHeight)
                        )
                    }

                    // Overlay semi-transparente con color del jugador
                    drawRect(
                        color = colorPrincipal.copy(alpha = 0.15f),
                        topLeft = Offset(0f, 0f),
                        size = Size(totalWidth * animatedProgress, totalHeight)
                    )
                }
            }
        }
    }
}
// Función helper para convertir PreguntaSala a Pregunta
fun convertirAPregunta(preguntaSala: PreguntaSala): Pregunta {
    return Pregunta(
        id_pregunta = preguntaSala.id_pregunta,
        enunciado = preguntaSala.enunciado,
        nivel_dificultad = preguntaSala.nivel_dificultad,
        imagen = preguntaSala.imagen,
        id_area = 0,
        id_tema = null,
        area = null,
        tema = null,
        opciones = preguntaSala.opciones.map { opcionSala ->
            Opcion(
                id_opcion = opcionSala.id_opcion,
                texto_opcion = opcionSala.texto_opcion,
                imagen = opcionSala.imagen,
                es_correcta = null
            )
        }
    )
}