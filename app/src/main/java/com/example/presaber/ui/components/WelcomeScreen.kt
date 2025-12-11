package com.example.presaber.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Colores
private val PrimaryBlue = Color(0xFF5B7BC6)
private val RoyalBlueLight = Color(0xFF448AFF) // Azul Rey Claro para el degradado
private val TextDark = Color(0xFF1A1B21)
private val BackgroundLight = Color(0xFFEAF4FA)

@Composable
fun WelcomeScreen(onNavigateToLogin: () -> Unit) {
    var isAnimating by remember { mutableStateOf(false) }

    // Variables de animación
    val rotation = remember { Animatable(0f) }
    val figureAlpha = remember { Animatable(1f) } // Controla la opacidad de la figura

    val scope = rememberCoroutineScope()

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            // 1. Iniciar la rotación (esto corre en paralelo)
            launch {
                rotation.animateTo(
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }

            // 2. Esperar 1.5 segundos viendo la figura girar sola
            delay(1500)

            // 3. Desvanecer la figura suavemente (tarda 500ms)
            figureAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(500)
            )

            // 4. Navegar al login (ahora la pantalla está limpia)
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 150.dp, y = (-100).dp)
                .alpha(figureAlpha.value) // <--- AQUI APLICAMOS EL DESVANECIMIENTO
        ) {
            WavyCircleShape(
                color = PrimaryBlue,
                rotationDegrees = rotation.value,
                modifier = Modifier.size(600.dp)
            )
        }


        AnimatedVisibility(
            visible = !isAnimating,
            exit = fadeOut(animationSpec = tween(800)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 80.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Bienvenido, a",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark,
                    textAlign = TextAlign.Start,
                    lineHeight = 44.sp
                )
                Text(
                    text = buildAnnotatedString {
                        append("Pre")
                        withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                            append("Saber")
                        }
                    },
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Start,
                    lineHeight = 52.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // BOTÓN CON DEGRADADO
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .width(240.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(RoyalBlueLight, PrimaryBlue)
                            ),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Button(
                        onClick = { isAnimating = true },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Empecemos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dibuja una forma circular ondulada
 */
@Composable
fun WavyCircleShape(
    color: Color,
    rotationDegrees: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .graphicsLayer { rotationZ = rotationDegrees }
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 - 40.dp.toPx()

        val path = Path()
        val waves = 12
        val amplitude = 30.dp.toPx()

        for (angle in 0..360 step 1) {
            val radians = Math.toRadians(angle.toDouble())

            // Fórmula de onda
            val currentRadius = radius + amplitude * sin(waves * radians)

            val x = centerX + currentRadius * cos(radians)
            val y = centerY + currentRadius * sin(radians)

            if (angle == 0) {
                path.moveTo(x.toFloat(), y.toFloat())
            } else {
                path.lineTo(x.toFloat(), y.toFloat())
            }
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 20.dp.toPx())
        )
    }
}