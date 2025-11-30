package com.example.presaber.ui.pvp

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.presaber.R
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
    var duracionMinutos by remember { mutableStateOf(20) } // Duración de la sala
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

    // Calcular tiempo restante
    fun formatTiempoRestante(sec: Long, duracionMin: Int): String {
        val tiempoLimiteSegundos = duracionMin * 60L
        val restante = tiempoLimiteSegundos - sec
        if (restante <= 0) return "00:00"

        val m = restante / 60
        val s = restante % 60
        return String.format("%02d:%02d", m, s)
    }

    // Cargar preguntas y duración
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

    // Cronómetro con límite de tiempo
    LaunchedEffect(timerRunning, duracionMinutos) {
        while (timerRunning) {
            delay(1000L)
            elapsedSeconds += 1

            // Verificar si se acabó el tiempo
            val tiempoLimiteSegundos = duracionMinutos * 60L
            if (elapsedSeconds >= tiempoLimiteSegundos) {
                // Tiempo agotado, finalizar automáticamente
                timerRunning = false
                scope.launch {
                    try {
                        RetrofitClient.api.finalizarPvP(
                            FinalizarPvPRequest(
                                idSala,
                                idEstudiante,
                                formatDuration(elapsedSeconds)
                            )
                        )
                    } catch (e: Exception) {
                        // Ignorar errores
                    }
                    onFinish()
                }
            }
        }
    }

    // Polling del progreso de ambos jugadores
    LaunchedEffect(idSala) {
        while (timerRunning) {
            try {
                val response = RetrofitClient.api.obtenerProgresoSala(idSala)
                if (response.success) {
                    progreso = response.data
                }
            } catch (e: Exception) {
                // Silenciar errores de polling
            }
            delay(3000) // Actualizar cada 3 segundos
        }
    }

    // Scroll al inicio cuando cambia la pregunta
    LaunchedEffect(currentIndex) {
        scrollState.scrollTo(0)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header con temporizador y botón rendirse
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
                                    FinalizarPvPRequest(
                                        idSala,
                                        idEstudiante,
                                        formatDuration(elapsedSeconds)
                                    )
                                )
                            } catch (e: Exception) {
                                // Ignorar errores
                            }
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4A6FA5)
                    ),
                    enabled = !submitting
                ) {
                    Text("Rendirse", fontSize = 14.sp)
                }

                // Temporizador con cuenta regresiva
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (elapsedSeconds >= (duracionMinutos * 60L) - 60)
                            Color(0xFFE53935) // Rojo cuando queda 1 minuto
                        else
                            Color(0xFF4A6FA5)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = formatTiempoRestante(elapsedSeconds, duracionMinutos),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "restante",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Barra de progreso de ambos jugadores (horizontal)
            BarraProgresoJugadoresHorizontal(
                progreso = progreso,
                idEstudiante = idEstudiante,
                totalPreguntas = preguntas?.size ?: 0
            )

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
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

                    // Progreso de preguntas
                    Text(
                        text = "Pregunta ${currentIndex + 1} de ${preguntas!!.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = (currentIndex + 1).toFloat() / preguntas!!.size,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4A6FA5)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Contenido con scroll
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

                    // Botones de navegación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                currentIndex -= 1
                                selectedOptionId = null
                            },
                            enabled = currentIndex > 0 && !submitting
                        ) {
                            Text("Anterior")
                        }

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
                                            // Finalizar
                                            timerRunning = false
                                            val dur = formatDuration(elapsedSeconds)

                                            RetrofitClient.api.finalizarPvP(
                                                FinalizarPvPRequest(idSala, idEstudiante, dur)
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
                                containerColor = Color(0xFF4A6FA5)
                            )
                        ) {
                            if (submitting) {
                                CircularProgressIndicator(
                                    Modifier.size(20.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(if (currentIndex == preguntas!!.size - 1) "Finalizar" else "Siguiente")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarraProgresoJugadoresHorizontal(
    progreso: List<ProgresoJugador>,
    idEstudiante: String,
    totalPreguntas: Int
) {
    if (progreso.isEmpty() || totalPreguntas == 0) return

    val miProgreso = progreso.find { it.id_estudiante == idEstudiante }
    val oponenteProgreso = progreso.find { it.id_estudiante != idEstudiante }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mi progreso
        if (miProgreso != null) {
            ProgresoJugadorHorizontal(
                jugador = miProgreso,
                totalPreguntas = totalPreguntas,
                esYo = true
            )
        }

        // Oponente
        if (oponenteProgreso != null) {
            ProgresoJugadorHorizontal(
                jugador = oponenteProgreso,
                totalPreguntas = totalPreguntas,
                esYo = false
            )
        }
    }
}

@Composable
fun ProgresoJugadorHorizontal(
    jugador: ProgresoJugador,
    totalPreguntas: Int,
    esYo: Boolean
) {
    val progresoFraction = jugador.preguntas_respondidas.toFloat() / totalPreguntas
    val colorPrincipal = if (esYo) Color(0xFF4A6FA5) else Color(0xFFF4A261)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        if (jugador.photoURL != null) {
            AsyncImage(
                model = jugador.photoURL,
                contentDescription = jugador.nombre,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBDBDBD)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jugador.nombre.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Barra de progreso horizontal
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (esYo) "Tú" else jugador.nombre.split(" ").first(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1B21)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${jugador.preguntas_correctas}",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progresoFraction,
                    animationSpec = tween(durationMillis = 500),
                    label = "progress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(colorPrincipal)
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${jugador.preguntas_respondidas}/$totalPreguntas preguntas",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BarraProgresoJugadores(
    progreso: List<ProgresoJugador>,
    idEstudiante: String,
    totalPreguntas: Int
) {
    if (progreso.isEmpty() || totalPreguntas == 0) return

    val jugador1 = progreso.find { it.posicion == 1 }
    val jugador2 = progreso.find { it.posicion == 2 }
    val miProgreso = progreso.find { it.id_estudiante == idEstudiante }
    val oponenteProgreso = progreso.find { it.id_estudiante != idEstudiante }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mi progreso
        if (miProgreso != null) {
            ProgresoJugadorCard(
                jugador = miProgreso,
                totalPreguntas = totalPreguntas,
                esYo = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Oponente
        if (oponenteProgreso != null) {
            ProgresoJugadorCard(
                jugador = oponenteProgreso,
                totalPreguntas = totalPreguntas,
                esYo = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProgresoJugadorCard(
    jugador: ProgresoJugador,
    totalPreguntas: Int,
    esYo: Boolean,
    modifier: Modifier = Modifier
) {
    val progresoFraction = jugador.preguntas_respondidas.toFloat() / totalPreguntas

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (esYo) Color(0xFFE3F2FD) else Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar
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
                        .background(Color(0xFFBDBDBD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = jugador.nombre.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (esYo) "Tú" else jugador.nombre.split(" ").first(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1B21),
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                // Barra de progreso con animación
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = progresoFraction,
                        animationSpec = tween(durationMillis = 500),
                        label = "progress"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(
                                if (esYo) Color(0xFF4A6FA5) else Color(0xFFF4A261),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(Modifier.height(2.dp))

                // Correctas con animación
                val animatedCorrectas by animateIntAsState(
                    targetValue = jugador.preguntas_correctas,
                    animationSpec = tween(durationMillis = 300),
                    label = "correctas"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$animatedCorrectas",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
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