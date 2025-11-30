package com.example.presaber.ui.pvp

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.data.remote.ParticipanteResultado
import com.example.presaber.data.remote.ResultadoSala
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.delay

@Composable
fun ResultadoPvPScreen(
    idSala: Int,
    idEstudiante: String,
    onAceptar: () -> Unit
) {
    var resultado by remember { mutableStateOf<ResultadoSala?>(null) }
    var esperandoOponente by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(idSala) {
        // Polling hasta que la sala esté finalizada
        while (esperandoOponente) {
            try {
                val salaResponse = RetrofitClient.api.obtenerSala(idSala)
                if (salaResponse.success && salaResponse.data.estado == "finalizada") {
                    // Ambos jugadores terminaron, obtener resultado
                    delay(500)
                    val resultadoResponse = RetrofitClient.api.obtenerResultadoSala(idSala)
                    if (resultadoResponse.success) {
                        resultado = resultadoResponse.data
                        esperandoOponente = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }

            if (esperandoOponente) {
                delay(2000) // Revisar cada 2 segundos
            }
        }
    }

    if (isLoading || esperandoOponente) {
        PantallaEsperaOponente()
        return
    }

    if (resultado == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error al cargar resultados", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAceptar) {
                    Text("Volver")
                }
            }
        }
        return
    }

    val miResultado = resultado!!.participantes.find { it.id_estudiante == idEstudiante }
    val oponenteResultado = resultado!!.participantes.find { it.id_estudiante != idEstudiante }
    val heGanado = miResultado?.es_ganador == true

    // Animación de escala para el ganador
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (heGanado) {
                        listOf(
                            Color(0xFFE8F5E9),
                            Color(0xFFC8E6C9),
                            Color.White
                        )
                    } else {
                        listOf(
                            Color(0xFFFFEBEE),
                            Color(0xFFFFCDD2),
                            Color.White
                        )
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Título resultado
            Text(
                text = if (heGanado) "¡Victoria!" else "Derrota",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (heGanado) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.scale(scale)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Resultado ${resultado!!.area}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Text(
                text = "${resultado!!.duracion_minutos} minutos - ${resultado!!.total_preguntas} preguntas",
                fontSize = 13.sp,
                color = Color.Gray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(Modifier.height(32.dp))

            // Comparación de jugadores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ganador (siempre primero)
                val ganador = resultado!!.participantes.find { it.es_ganador }
                val perdedor = resultado!!.participantes.find { !it.es_ganador }

                if (ganador != null) {
                    ParticipanteResultadoCard(
                        participante = ganador,
                        esGanador = true,
                        totalPreguntas = resultado!!.total_preguntas,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Icono VS
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF90A4AE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (perdedor != null) {
                    ParticipanteResultadoCard(
                        participante = perdedor,
                        esGanador = false,
                        totalPreguntas = resultado!!.total_preguntas,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Mi estadística detallada
            if (miResultado != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tu resultado",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1B21)
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            EstadisticaItem(
                                label = "Correctas",
                                value = "${miResultado.preguntas_correctas}/${resultado!!.total_preguntas}",
                                color = Color(0xFF4CAF50)
                            )

                            VerticalDivider(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(1.dp),
                                color = Color(0xFFE0E0E0)
                            )

                            EstadisticaItem(
                                label = "Puntaje",
                                value = "${miResultado.puntaje_final}%",
                                color = Color(0xFF2196F3)
                            )

                            VerticalDivider(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(1.dp),
                                color = Color(0xFFE0E0E0)
                            )

                            EstadisticaItem(
                                label = "EXP",
                                value = "+${miResultado.experiencia_ganada}",
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Mensaje motivacional
            Text(
                text = if (heGanado) {
                    "¡Excelente trabajo! Sigue practicando para mantener tu nivel."
                } else {
                    "No te rindas. Cada partida es una oportunidad para mejorar."
                },
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Botón aceptar
            Button(
                onClick = onAceptar,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (heGanado) Color(0xFF4CAF50) else Color(0xFF4A6FA5)
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Aceptar", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ParticipanteResultadoCard(
    participante: ParticipanteResultado,
    esGanador: Boolean,
    totalPreguntas: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Corona para el ganador
        if (esGanador) {
            Text(
                text = "👑",
                fontSize = 32.sp,
                modifier = Modifier.offset(y = 8.dp)
            )
        } else {
            Spacer(Modifier.height(40.dp))
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(if (esGanador) 100.dp else 80.dp)
                .background(
                    if (esGanador) Color(0xFFFFD700).copy(alpha = 0.3f) else Color.Transparent,
                    CircleShape
                )
                .padding(4.dp)
        ) {
            if (participante.photoURL != null) {
                AsyncImage(
                    model = participante.photoURL,
                    contentDescription = participante.nombre_completo,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFBDBDBD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participante.nombre_completo.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Nombre
        Text(
            text = participante.nombre_completo.split(" ").take(2).joinToString(" "),
            fontSize = if (esGanador) 16.sp else 14.sp,
            fontWeight = if (esGanador) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF1A1B21),
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(Modifier.height(4.dp))

        // Puntaje
        Text(
            text = "${participante.preguntas_correctas}/$totalPreguntas",
            fontSize = if (esGanador) 24.sp else 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (esGanador) Color(0xFF2E7D32) else Color(0xFF757575)
        )

        Text(
            text = "${participante.puntaje_final}%",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun EstadisticaItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun PantallaEsperaOponente() {
    // Animación de pulsación
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Animación de rotación para puntos suspensivos
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFE1BEE7),
                        Color(0xFFCE93D8)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {

            // Título animado
            Text(
                text = "Esperando al oponente",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A148C),
                modifier = Modifier.alpha(alpha)
            )

            Spacer(Modifier.height(12.dp))

            // Puntos animados
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 600,
                                delayMillis = index * 200,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .alpha(dotAlpha)
                            .background(Color(0xFF7B1FA2), CircleShape)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Mensaje motivacional
            Card(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💪 ¡Buen trabajo!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A148C)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Tu oponente todavía está respondiendo las preguntas. Los resultados se mostrarán cuando ambos terminen.",
                        fontSize = 14.sp,
                        color = Color(0xFF424242),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Decoración: círculos flotantes en el fondo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f)
        ) {
            val circleAnimation1 by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 50f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "circle1"
            )

            val circleAnimation2 by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "circle2"
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 30.dp, y = 100.dp + circleAnimation1.dp)
                    .background(Color(0xFF9C27B0), CircleShape)
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-50).dp, y = 150.dp + circleAnimation2.dp)
                    .background(Color(0xFFBA68C8), CircleShape)
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 100.dp, y = (-200).dp + circleAnimation1.dp)
                    .background(Color(0xFFAB47BC), CircleShape)
            )
        }
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Box(
        modifier = modifier.background(color)
    )
}