package com.example.presaber.ui.simulacro.teacher

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.FinalizarSimulacroRequest
import com.example.presaber.data.remote.ProgresoSimulacro
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SimulacroGrupal
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

// --- Colores ---
private val AccentBlue = Color(0xFF5685FF)
private val AccentGreen = Color(0xFF00C853)
private val AccentRed = Color(0xFFFF5252)
private val TextDark = Color(0xFF1A1B21)
private val SurfaceGray = Color(0xFFF5F7FA)

@Composable
fun SimulacroProgresoScreen(
    idSimulacro: Int,
    idDocente: String,
    onFinalizar: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var simulacro by remember { mutableStateOf<SimulacroGrupal?>(null) }
    var progreso by remember { mutableStateOf<List<ProgresoSimulacro>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isFinalizando by remember { mutableStateOf(false) }
    var tiempoRestanteSegundos by remember { mutableStateOf(0) }

    // --- Lógica de Tiempo Corregida ---
    LaunchedEffect(idSimulacro) {
        while (isActive) {
            try {
                val simResponse = RetrofitClient.api.obtenerSimulacro(idSimulacro)
                if (simResponse.success) {
                    simulacro = simResponse.data
                    if (simResponse.data.fecha_inicio != null) {
                        // 1. Duración total en milisegundos
                        val duracionMs = simResponse.data.duracion_minutos * 60 * 1000L

                        // 2. CORRECCIÓN DE ZONA HORARIA
                        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        // IMPORTANTE: Le decimos que la fecha del string viene en UTC
                        format.timeZone = TimeZone.getTimeZone("UTC")

                        val inicioDate = format.parse(simResponse.data.fecha_inicio!!)
                        val inicioMs = inicioDate?.time ?: System.currentTimeMillis()

                        // 3. Hora actual (El sistema ya sabe su zona horaria, esto devuelve UTC epoch)
                        val ahoraMs = System.currentTimeMillis()

                        // 4. Cálculo
                        val transcurridoMs = ahoraMs - inicioMs
                        val restanteMs = max(0, duracionMs - transcurridoMs)

                        tiempoRestanteSegundos = (restanteMs / 1000).toInt()
                    }
                }

                val progResponse = RetrofitClient.api.obtenerProgresoSimulacro(idSimulacro)
                if (progResponse.success) {
                    progreso = progResponse.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
            delay(2000)
        }
    }

    // Contador local para suavidad visual
    LaunchedEffect(tiempoRestanteSegundos) {
        if (tiempoRestanteSegundos > 0) {
            delay(1000)
            tiempoRestanteSegundos--
        }
    }

    if (isLoading || simulacro == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BackgroundBlobsProgreso()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header y Timer
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "En Curso: ${simulacro!!.curso.grado}${simulacro!!.curso.grupo}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(24.dp))
                ModernTimer(tiempoRestanteSegundos)
            }

            // Resumen Rápido
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Groups, contentDescription = null, tint = AccentBlue)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Progreso Estudiantes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    color = AccentBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${progreso.size} Activos",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Lista de Progreso
            LazyColumn(
                modifier = Modifier.weight(1f),
                // CORRECCIÓN PADDING VALUES: Definimos cada lado explícitamente
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 8.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(progreso) { estudiante ->
                    StudentProgressCard(
                        estudiante = estudiante,
                        totalPreguntas = simulacro!!.cantidad_preguntas
                    )
                }
            }
        }

        // Botón Flotante
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0f), Color.White)
                    )
                )
                .padding(24.dp)
        ) {
            Button(
                onClick = {
                    isFinalizando = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.finalizarSimulacro(
                                idSimulacro,
                                FinalizarSimulacroRequest(id_docente = idDocente)
                            )
                            if (response.success) onFinalizar()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isFinalizando = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(16.dp),
                enabled = !isFinalizando
            ) {
                if (isFinalizando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Rounded.StopCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Finalizar Simulacro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ... Resto de componentes (ModernTimer, StudentProgressCard, BackgroundBlobsProgreso) se mantienen igual ...
@Composable
fun ModernTimer(segundosTotales: Int) {
    val minutos = segundosTotales / 60
    val segundos = segundosTotales % 60
    val isLowTime = segundosTotales < 60

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by if (isLowTime) {
        infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 1.05f,
            animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
            label = "scale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    val timerColor = if (isLowTime) AccentRed else AccentBlue
    val bgColor = if (isLowTime) AccentRed.copy(alpha = 0.1f) else AccentBlue.copy(alpha = 0.05f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = BorderStroke(1.dp, timerColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = null,
                    tint = timerColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "TIEMPO RESTANTE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = timerColor
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.scale(scale)
            ) {
                Text(
                    text = String.format("%02d", minutos),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                    fontFamily = FontFamily.Monospace
                )
                BlinkingSeparator(color = TextDark)
                Text(
                    text = String.format("%02d", segundos),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = timerColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun BlinkingSeparator(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "alpha"
    )
    Text(
        text = ":",
        fontSize = 56.sp,
        fontWeight = FontWeight.Black,
        color = color.copy(alpha = alpha),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun StudentProgressCard(
    estudiante: ProgresoSimulacro,
    totalPreguntas: Int
) {
    val progresoReal = if (totalPreguntas > 0) {
        estudiante.preguntas_respondidas.toFloat() / totalPreguntas
    } else 0f

    val progresoAnimado by animateFloatAsState(
        targetValue = progresoReal,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, SurfaceGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceGray)
                ) {
                    if (estudiante.photoURL != null) {
                        AsyncImage(
                            model = estudiante.photoURL,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = estudiante.nombre.take(1),
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = estudiante.nombre,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        maxLines = 1
                    )
                    Text(
                        text = if (estudiante.preguntas_respondidas == totalPreguntas) "Finalizado" else "Respondiendo...",
                        fontSize = 12.sp,
                        color = if (estudiante.preguntas_respondidas == totalPreguntas) AccentGreen else Color.Gray
                    )
                }

                Surface(
                    color = AccentBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${estudiante.preguntas_respondidas}/$totalPreguntas",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(SurfaceGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progresoAnimado)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AccentBlue, Color(0xFF42A5F5))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun BackgroundBlobsProgreso() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawCircle(
            color = Color(0xFFE3F2FD).copy(alpha = 0.5f),
            radius = width * 0.4f,
            center = Offset(width * 0.9f, height * 0.05f)
        )
        drawCircle(
            color = Color(0xFFFFEBEE).copy(alpha = 0.5f),
            radius = width * 0.3f,
            center = Offset(0f, height * 0.95f)
        )
    }
}
// Extensión scale si es necesaria
fun Modifier.scale(scale: Float) = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)