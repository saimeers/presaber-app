package com.example.presaber.ui.simulacro.student

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.presaber.data.remote.ParticipanteResultadoSimulacro
import com.example.presaber.data.remote.ResultadoSimulacro
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

// --- Colores y Estilos ---
private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFC0C0C0)
private val BronzeColor = Color(0xFFCD7F32)
private val AccentBlue = Color(0xFF5685FF)
private val TextDark = Color(0xFF1A1B21)

@Composable
fun SimulacroPodioScreen(
    idSimulacro: Int,
    onAceptar: () -> Unit
) {
    var resultado by remember { mutableStateOf<ResultadoSimulacro?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(idSimulacro) {
        try {
            val response = RetrofitClient.api.obtenerResultadoSimulacro(idSimulacro)
            if (response.success) {
                resultado = response.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading || resultado == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    val participantes = resultado!!.participantes

    // Ordenamiento seguro
    val participantesOrdenados = remember(participantes) {
        participantes.sortedByDescending {
            it.puntaje_final.toString().toDoubleOrNull() ?: 0.0
        }
    }

    // Top 3 para el podio visual
    val top3 = participantesOrdenados.take(3)

    // CORRECCIÓN: Usamos la lista completa para el ranking de abajo,
    // así siempre se ve la tabla de posiciones aunque haya pocos jugadores.
    val listaRanking = participantesOrdenados

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Efecto Confeti
        ConfettiEffectPodio()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. HEADER Y PODIO
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¡Simulacro Finalizado!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${resultado!!.total_preguntas} preguntas • ${resultado!!.duracion_minutos} min",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(40.dp))

                    // --- PODIO VISUAL (Solo Top 3) ---
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 2DO LUGAR
                            if (top3.size >= 2) {
                                PodioItem(
                                    participante = top3[1],
                                    posicion = 2,
                                    color = SilverColor,
                                    height = 140.dp,
                                    modifier = Modifier.offset(x = 10.dp)
                                )
                            }

                            // 1ER LUGAR
                            if (top3.isNotEmpty()) {
                                PodioItem(
                                    participante = top3[0],
                                    posicion = 1,
                                    color = GoldColor,
                                    height = 180.dp,
                                    isWinner = true,
                                    modifier = Modifier.zIndex(1f)
                                )
                            }

                            // 3ER LUGAR
                            if (top3.size >= 3) {
                                PodioItem(
                                    participante = top3[2],
                                    posicion = 3,
                                    color = BronzeColor,
                                    height = 110.dp,
                                    modifier = Modifier.offset(x = (-10).dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Título siempre visible
                    Text(
                        text = "Ranking General",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                }
            }

            // 2. LISTA COMPLETA (Ahora muestra a todos, incluyendo el top 3)
            items(listaRanking) { participante ->
                ParticipanteListItem(participante)
            }
        }

        // 3. BOTÓN FLOTANTE
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0f), Color.White, Color.White)
                    )
                )
                .padding(24.dp)
        ) {
            Button(
                onClick = onAceptar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(
                    text = "Volver al Inicio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ... (El resto de funciones PodioItem, ParticipanteListItem y ConfettiEffectPodio se mantienen igual) ...
// Asegúrate de copiar las funciones auxiliares que ya tenías en el archivo anterior para que compile.
@Composable
fun PodioItem(
    participante: ParticipanteResultadoSimulacro,
    posicion: Int,
    color: Color,
    height: Dp,
    isWinner: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Sin cambios, mantener lógica anterior (sin puntaje debajo del nombre)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(110.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .size(if (isWinner) 80.dp else 60.dp)
                    .border(3.dp, color, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                if (participante.photoURL != null) {
                    AsyncImage(
                        model = participante.photoURL,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = participante.nombre_completo.firstOrNull()?.toString() ?: "?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .offset(y = if(isWinner) 65.dp else 45.dp)
                    .background(color, CircleShape)
                    .size(24.dp)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$posicion", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = participante.nombre_completo.split(" ").firstOrNull() ?: "Anon",
            fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextDark
        )
        // Eliminado texto de puntaje aquí
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(if (isWinner) 100.dp else 80.dp)
                .height(height)
                .background(
                    brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.8f), color.copy(alpha = 0.3f))),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            if(isWinner) {
                Box(modifier = Modifier.padding(top = 10.dp)) {
                    Icon(Icons.Rounded.EmojiEvents, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}

@Composable
fun ParticipanteListItem(participante: ParticipanteResultadoSimulacro) {
    // Sin cambios, mantener lógica anterior
    val colorPosicion = when (participante.posicion) {
        1 -> GoldColor; 2 -> SilverColor; 3 -> BronzeColor; else -> Color.Gray
    }
    val puntajeDisplay = try { participante.puntaje_final.toString().replace(".0", "") } catch (e: Exception) { "0" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "#${participante.posicion}", fontWeight = FontWeight.Bold, color = colorPosicion, fontSize = 16.sp, modifier = Modifier.width(30.dp))
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White)) {
            if (participante.photoURL != null) {
                AsyncImage(model = participante.photoURL, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(participante.nombre_completo.take(1), fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = participante.nombre_completo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "${participante.preguntas_correctas} aciertos", fontSize = 12.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (participante.experiencia_ganada > 0) {
                Text(text = "+${participante.experiencia_ganada} XP", fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ConfettiEffectPodio() {
    val particles = remember { List(40) { ConfettiParticle(Random.nextFloat(), Random.nextFloat() * -1f, listOf(GoldColor, AccentBlue, Color.Red, Color.Green).random()) } }
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
        tween (2500, easing = LinearEasing), RepeatMode.Restart), label = "time")
    Canvas (modifier = Modifier.fillMaxSize()) {
        val width = size.width; val height = size.height
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            val currentY = progress * height
            val currentX = (particle.x * width) + (sin(progress * 10) * 20)
            drawCircle(color = particle.color, radius = 6f, center = Offset(currentX, currentY), alpha = (1f - progress).coerceIn(0f, 1f))
        }
    }
}
data class ConfettiParticle(val x: Float, val y: Float, val color: Color, val offset: Float = Random.nextFloat())