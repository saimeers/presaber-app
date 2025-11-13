package com.example.presaber.ui.home.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "Quiz"

@Composable
fun Quiz(
    reto: Reto,
    idEstudiante: String,
    onFinish: (ResultadoData) -> Unit,
    onExit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var preguntas by remember { mutableStateOf<List<Pregunta>?>(null) }
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

    // Cargar preguntas
    LaunchedEffect(Unit) {
        Log.d(TAG, "Cargando preguntas para reto: ${reto.id_reto}")
        try {
            val resp = RetrofitClient.api.getPreguntasPorReto(reto.id_reto)
            if (resp.success && resp.data.preguntas.isNotEmpty()) {
                preguntas = resp.data.preguntas
                timerRunning = true
            } else {
                errorMessage = "No hay preguntas disponibles"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar preguntas", e)
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Cronómetro
    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000L)
            elapsedSeconds += 1
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
                .padding(16.dp),

        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reto.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (timerRunning) {
                        Text(
                            text = formatDuration(elapsedSeconds),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
/*
                TextButton(onClick = {
                    timerRunning = false
                    onExit()
                }) {
                    Text("Salir")
                }
 */
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            Button(onClick = onExit) { Text("Volver") }
                        }
                    }
                }

                preguntas == null || preguntas!!.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No hay preguntas disponibles")
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = onExit) { Text("Volver") }
                        }
                    }
                }

                else -> {
                    val pregunta = preguntas!![currentIndex]

                    // Progreso
                    Text(
                        text = "Pregunta ${currentIndex + 1} de ${preguntas!!.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = (currentIndex + 1).toFloat() / preguntas!!.size,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    // ⬇️ CONTENIDO CON SCROLL
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        QuestionCard(
                            pregunta = pregunta,
                            selectedOptionId = selectedOptionId,
                            onSelect = { if (!submitting) selectedOptionId = it }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botones
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
                                        RetrofitClient.api.postRespuesta(
                                            RespuestaRequest(idEstudiante, reto.id_reto, opcion)
                                        )

                                        if (currentIndex == preguntas!!.size - 1) {
                                            timerRunning = false
                                            val dur = formatDuration(elapsedSeconds)

                                            try {
                                                val res = RetrofitClient.api.postResultado(
                                                    ResultadoRequest(idEstudiante, reto.id_reto, dur)
                                                )
                                                onFinish(
                                                    if (res.success) res.data
                                                    else ResultadoData(-1, 0, preguntas!!.size, "0.00", 0, "Error", "", dur)
                                                )
                                            } catch (e: Exception) {
                                                onFinish(ResultadoData(-1, 0, preguntas!!.size, "0.00", 0, "Error de red", "", dur))
                                            }
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
                            enabled = selectedOptionId != null && !submitting
                        ) {
                            if (submitting) {
                                CircularProgressIndicator(
                                    Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
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