package com.example.presaber.ui.pvp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.ParticipanteResultado
import com.example.presaber.data.remote.ResultadoSala
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

// --- Colores estilo Referencia ---
val ButtonColor = Color(0xFF4F6088) // Azul grisáceo del botón
val ScoreBlack = Color(0xFF000000)
val ExpText = Color(0xFF000000)
val SoftBlobBlue = Color(0xFFDCE2F0)
val SoftBlobPeach = Color(0xFFFFCCBC).copy(alpha = 0.6f)

@Composable
fun ResultadoPvPScreen(
    idSala: Int,
    idEstudiante: String,
    onAceptar: () -> Unit
) {
    var resultado by remember { mutableStateOf<ResultadoSala?>(null) }
    var esperandoOponente by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    // --- Lógica de Polling ---
    LaunchedEffect(idSala) {
        while (esperandoOponente && isActive) {
            try {
                val salaResponse = RetrofitClient.api.obtenerSala(idSala)
                if (salaResponse.success && salaResponse.data.estado == "finalizada") {
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
            if (esperandoOponente) delay(2000)
        }
    }

    // --- Pantalla de Espera (La que te gustó) ---
    if (isLoading || esperandoOponente) {
        PantallaEsperaModerna()
        return
    }

    // --- Manejo de Error ---
    if (resultado == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error al cargar resultados", color = MaterialTheme.colorScheme.error)
            Button(onClick = onAceptar, modifier = Modifier.padding(top = 16.dp)) { Text("Volver") }
        }
        return
    }

    val miResultado = resultado!!.participantes.find { it.id_estudiante == idEstudiante }
    val oponenteResultado = resultado!!.participantes.find { it.id_estudiante != idEstudiante }
    val heGanado = miResultado?.es_ganador == true

    // Frase Motivacional (Mantenemos la lógica de frases)
    val fraseMotivacional = if (heGanado) {
        "¡Excelente desempeño! Sigue practicando en ${resultado!!.area}, allí puedes mejorar aún más."
    } else {
        "No te rindas. Cada partida es una oportunidad de aprendizaje. ¡Sigue intentando!"
    }

    // --- UI NUEVA (Estilo Clean/Referencia) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Fondo Decorativo (Blobs abstractos)
        BackgroundBlobs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 2. Encabezado alineado a la izquierda (como la imagen)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Resultado ${resultado!!.area.lowercase()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${resultado!!.duracion_minutos} minutos - ${resultado!!.total_preguntas} preguntas",
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(40.dp))

            // 3. Avatares Centrales (Estilo Burbujas Agrupadas)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                // Avatar Oponente (Atrás, más pequeño)
                if (oponenteResultado != null) {
                    AvatarBurbuja(
                        url = oponenteResultado.photoURL,
                        size = 100.dp,
                        modifier = Modifier.offset(x = 80.dp, y = (-40).dp)
                    )
                }

                // Mi Avatar (Principal, grande)
                if (miResultado != null) {
                    AvatarBurbuja(
                        url = miResultado.photoURL,
                        size = 140.dp,
                        modifier = Modifier.offset(x = (-30).dp, y = 10.dp),
                        isMain = true
                    )
                }
            }

            // Nombre del usuario
            Text(
                text = miResultado?.nombre_completo ?: "Usuario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(16.dp))

            // 4. Frase (Italic)
            Text(
                text = fraseMotivacional,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            // 5. Puntaje Gigante (Estilo 5/25)
            Text(
                text = "${miResultado?.preguntas_correctas}/${resultado!!.total_preguntas}",
                fontSize = 72.sp, // Tamaño masivo como en la imagen
                fontWeight = FontWeight.Black,
                color = ScoreBlack,
                letterSpacing = (-2).sp
            )

            // 6. EXP (Espaciado)
            Text(
                text = "${miResultado?.experiencia_ganada} EXP",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = ExpText,
                letterSpacing = 4.sp // Espaciado ancho como en la imagen
            )

            Spacer(Modifier.weight(1f))

            // 7. Botón Aceptar (Estilo redondeado y color específico)
            Button(
                onClick = onAceptar,
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Aceptar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// --- Componentes Auxiliares Visuales ---

@Composable
fun AvatarBurbuja(
    url: String?,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    isMain: Boolean = false
) {
    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(4.dp, Color.White, CircleShape), // Borde blanco para separar burbujas
        shadowElevation = if (isMain) 10.dp else 4.dp,
        color = Color(0xFFF0F0F0),
        shape = CircleShape
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder si no hay imagen
            Box(
                modifier = Modifier.fillMaxSize().background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("?", fontSize = 30.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun BackgroundBlobs() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Blob Azul (Izquierda)
        val pathBlue = Path().apply {
            moveTo(0f, height * 0.3f)
            quadraticBezierTo(width * 0.1f, height * 0.2f, width * 0.4f, height * 0.35f)
            quadraticBezierTo(width * 0.6f, height * 0.45f, width * 0.3f, height * 0.55f)
            quadraticBezierTo(0f, height * 0.5f, 0f, height * 0.3f)
            close()
        }
        drawPath(path = pathBlue, color = SoftBlobBlue)

        // Blob Durazno/Rojo (Detrás de avatars)
        drawCircle(
            color = SoftBlobPeach,
            radius = width * 0.35f,
            center = Offset(width * 0.8f, height * 0.35f)
        )
    }
}

// --- Tu Pantalla de Espera (Intacta) ---
@Composable
fun PantallaEsperaModerna() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = Color(0xFF2962FF),
                trackColor = Color.LightGray.copy(alpha = 0.3f),
                strokeWidth = 6.dp
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "Esperando resultados...", fontSize = 18.sp,
                fontWeight = FontWeight.Medium, color = Color.Gray,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tu oponente está terminando", fontSize = 14.sp, color = Color.LightGray
            )
        }
    }
}