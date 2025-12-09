package com.example.presaber.ui.pvp

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// Colores consistentes
private val MyColor = Color(0xFF5685FF)
private val RivalColor = Color(0xFFFF7043)

@Composable
fun QuizPvPScreen(
    idSala: Int,
    idEstudiante: String,
    onFinish: () -> Unit
) {
    // Evitar salir por error
    BackHandler {}

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var preguntas by remember { mutableStateOf<List<PreguntaSala>?>(null) }
    var duracionMinutos by remember { mutableStateOf(20) }

    // Estado del rival (Progreso)
    var rival by remember { mutableStateOf<ProgresoJugador?>(null) }
    var miProgresoActual by remember { mutableStateOf(0) } // Para la barra local

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOptionId by remember { mutableStateOf<Int?>(null) }
    var elapsedSeconds by remember { mutableStateOf(0L) }
    var submitting by remember { mutableStateOf(false) }
    var timerRunning by remember { mutableStateOf(false) }

    // Helpers de tiempo
    fun formatDuration(sec: Long): String {
        val h = TimeUnit.SECONDS.toHours(sec)
        val m = TimeUnit.SECONDS.toMinutes(sec) % 60
        val s = sec % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    // 1. Cargar preguntas
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

    // 2. Cronómetro
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

    // 3. Polling del Rival (PvP)
    LaunchedEffect(idSala) {
        while (true) {
            try {
                val response = RetrofitClient.api.obtenerProgresoSala(idSala)
                if (response.success) {
                    val lista = response.data
                    // Encontrar al rival (el que no soy yo)
                    val oponenteData = lista.find { it.id_estudiante != idEstudiante }
                    if (oponenteData != null) {
                        rival = oponenteData
                    }

                    // Actualizar mi progreso real desde el backend por si acaso
                    val miData = lista.find { it.id_estudiante == idEstudiante }
                    if (miData != null) {
                        // Sincronizar visualmente
                        // miProgresoActual = miData.preguntas_respondidas
                    }
                }
            } catch (e: Exception) { }
            delay(2000) // Más rápido en PvP (2s)
        }
    }

    // Scroll al top al cambiar de pregunta
    LaunchedEffect(currentIndex) {
        scrollState.scrollTo(0)
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = MyColor)
        }
        return
    }

    if (errorMessage != null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = onFinish) { Text("Salir") }
            }
        }
        return
    }

    if (preguntas.isNullOrEmpty()) return

    val preguntaActual = preguntas!![currentIndex]
    val totalPreguntas = preguntas!!.size
    val tiempoRestanteSegundos = (duracionMinutos * 60L - elapsedSeconds).toInt()

    Scaffold(
        topBar = {
            Column {
                // Header PvP (Igual al simulacro)
                PvPRivalHeader(
                    miProgresoActual = currentIndex, // Usamos el índice local para respuesta instantánea
                    rival = rival,
                    totalPreguntas = totalPreguntas
                )

                // Timer Compacto debajo del header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PvPTimer(tiempoRestanteSegundos)
                }
            }
        },
        bottomBar = {
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
                                    id_pregunta = preguntaActual.id_pregunta,
                                    id_opcion = opcion,
                                    tiempo_respuesta = 0
                                )
                            )

                            if (currentIndex == preguntas!!.size - 1) {
                                timerRunning = false
                                RetrofitClient.api.finalizarPvP(
                                    FinalizarPvPRequest(idSala, idEstudiante, formatDuration(elapsedSeconds))
                                )
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
                    containerColor = MyColor,
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (submitting) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = if (currentIndex == totalPreguntas - 1) "Finalizar PvP" else "Siguiente",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Botón Rendirse (Discreto arriba)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
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
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                ) {
                    Icon(Icons.Rounded.Flag, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rendirse", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Enunciado
            Text(
                text = "Pregunta ${currentIndex + 1}",
                color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = preguntaActual.enunciado ?: "Sin enunciado",
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark,
                lineHeight = 24.sp
            )

            if (preguntaActual.imagen != null) {
                Spacer(Modifier.height(16.dp))
                AsyncImage(
                    model = preguntaActual.imagen,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(24.dp))

            // Opciones
            preguntaActual.opciones.forEach { opcion ->
                val isSelected = selectedOptionId == opcion.id_opcion
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { if(!submitting) selectedOptionId = opcion.id_opcion },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MyColor.copy(alpha = 0.1f) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MyColor else Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { if(!submitting) selectedOptionId = opcion.id_opcion },
                            colors = RadioButtonDefaults.colors(selectedColor = MyColor)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = opcion.texto_opcion ?: "Ver imagen",
                            fontSize = 16.sp,
                            color = if (isSelected) MyColor else TextDark
                        )
                    }
                }
            }
        }
    }
}

// --- Componentes UI Reutilizados del diseño anterior ---

@Composable
fun PvPRivalHeader(
    miProgresoActual: Int,
    rival: ProgresoJugador?, // Usamos el objeto de progreso de PvP
    totalPreguntas: Int
) {
    val rivalProgreso = rival?.preguntas_respondidas ?: 0

    val miPorcentaje by animateFloatAsState(
        targetValue = if (totalPreguntas > 0) miProgresoActual.toFloat() / totalPreguntas else 0f,
        label = "miProgreso"
    )

    val rivalPorcentaje by animateFloatAsState(
        targetValue = if (totalPreguntas > 0) rivalProgreso.toFloat() / totalPreguntas else 0f,
        label = "rivalProgreso"
    )

    Card(
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // YO
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MyColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("YO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MyColor)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Tú", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$miProgresoActual/$totalPreguntas", fontSize = 12.sp, color = MyColor, fontWeight = FontWeight.Bold)
                    }
                }

                // VS
                Box(
                    modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                }

                // RIVAL
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = rival?.nombre?.split(" ")?.firstOrNull() ?: "Esperando...",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$rivalProgreso/$totalPreguntas",
                            fontSize = 12.sp, color = RivalColor, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    if (rival?.photoURL != null) {
                        AsyncImage(
                            model = rival.photoURL,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, RivalColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(if(rival!=null) RivalColor.copy(alpha = 0.2f) else Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(rival?.nombre?.take(1) ?: "?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(rival!=null) RivalColor else Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Barras de Progreso "Carrera"
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { miPorcentaje },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MyColor,
                    trackColor = MyColor.copy(alpha = 0.1f),
                )
                LinearProgressIndicator(
                    progress = { rivalPorcentaje },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = RivalColor,
                    trackColor = RivalColor.copy(alpha = 0.1f),
                )
            }
        }
    }
}

@Composable
fun PvPTimer(segundosRestantes: Int) {
    val minutos = segundosRestantes / 60
    val segundos = segundosRestantes % 60
    val isLowTime = segundosRestantes < 60

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isLowTime) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
        border = BorderStroke(1.dp, if (isLowTime) Color(0xFFE53935) else MyColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.AccessTime,
                contentDescription = null,
                tint = if (isLowTime) Color(0xFFE53935) else MyColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = String.format("%02d:%02d", minutos, segundos),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLowTime) Color(0xFFE53935) else MyColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}