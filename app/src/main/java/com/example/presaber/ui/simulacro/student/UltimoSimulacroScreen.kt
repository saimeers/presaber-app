package com.example.presaber.ui.simulacro.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.UltimoSimulacroResponse
import com.example.presaber.ui.layout.StudentLayout
import com.example.presaber.viewmodel.SimulacroEstudianteViewModel

@Composable
fun UltimoSimulacroScreen(
    idUsuario: String,
    onBack: () -> Unit,
    onComenzar: () -> Unit,
    viewModel: SimulacroEstudianteViewModel = viewModel()
) {
    var selectedNavItem by remember { mutableStateOf(0) }
    val showAccountDialog = remember { mutableStateOf(false) }

    LaunchedEffect(idUsuario) {
        viewModel.cargarUltimoSimulacro(idUsuario)
    }

    val ultimoSimulacro by viewModel.ultimoSimulacro.collectAsState()
    val loading by viewModel.loading.collectAsState()

    StudentLayout(
        selectedNavItem = selectedNavItem,
        onNavItemSelected = { },
        showAccountDialog = showAccountDialog,
        usuario = null,
        onSignOut = {}
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF5685FF))
                }
            } else if (ultimoSimulacro != null) {
                UltimoSimulacroContent(
                    ultimoSimulacro = ultimoSimulacro!!,
                    onBack = onBack,
                    onComenzar = onComenzar
                )
            } else {
                // Si no hay último simulacro, mostrar mensaje o redirigir
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No hay simulacros previos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Button(
                            onClick = onComenzar,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5685FF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ver simulacros disponibles")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UltimoSimulacroContent(
    ultimoSimulacro: UltimoSimulacroResponse,
    onBack: () -> Unit,
    onComenzar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Título
        Text(
            text = buildAnnotatedString {
                append("Simulacro Pre ")
                withStyle(style = SpanStyle(color = Color(0xFF5685FF))) {
                    append("ICFES 11")
                }
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1B21)
        )

        // Texto informativo
        Text(
            text = "Puedes hacer varios simulacros, pero debes esperar 7 días hábiles entre cada uno",
            fontSize = 14.sp,
            color = Color(0xFF757575),
            lineHeight = 20.sp
        )

        // Sección Detalles
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Detalles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.HourglassEmpty,
                        contentDescription = "Tiempo",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = formatTiempo(ultimoSimulacro.tiempo_total),
                        fontSize = 16.sp,
                        color = Color(0xFF1A1B21)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.QuestionMark,
                        contentDescription = "Preguntas",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "${ultimoSimulacro.preguntas_total} preguntas",
                        fontSize = 16.sp,
                        color = Color(0xFF1A1B21)
                    )
                }
            }
        }

        // Sección Anterior
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Anterior",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21)
            )

            // Puntaje grande con gradiente
            val puntajeMaximo = 500.0
            val puntajeNormalizado = (ultimoSimulacro.puntaje_total).toInt()
            val puntajeTexto = "${puntajeNormalizado}/500"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF9800),
                                Color(0xFFD32F2F)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.HourglassEmpty,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Icon(
                        Icons.Rounded.QuestionMark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = puntajeTexto,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E9E9E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atrás")
            }

            Button(
                onClick = onComenzar,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5685FF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Comenzar")
            }
        }
    }
}

fun formatTiempo(segundos: Int): String {
    val horas = segundos / 3600
    val minutos = (segundos % 3600) / 60
    return if (horas > 0) {
        "$horas ${if (horas == 1) "hora" else "horas"}"
    } else {
        "$minutos minutos"
    }
}

