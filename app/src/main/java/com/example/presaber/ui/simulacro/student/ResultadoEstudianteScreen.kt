package com.example.presaber.ui.simulacro.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.ProgresoSimulacro
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val AccentBlue = Color(0xFF5685FF)
private val TextDark = Color(0xFF1A1B21)

@Composable
fun ResultadoEstudianteScreen(
    idSimulacro: Int,
    idEstudiante: String,
    onSimulacroFinalizado: () -> Unit // Callback para ir al Podio
) {
    // Bloquear botón atrás para que no se salga mientras espera a los demás
    BackHandler { }

    var progresoClase by remember { mutableStateOf<List<ProgresoSimulacro>>(emptyList()) }
    var totalPreguntas by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Polling: Revisar estado del simulacro y progreso de compañeros
    LaunchedEffect(idSimulacro) {
        while (isActive) {
            try {
                // 1. Ver si el simulacro ya terminó
                val simResponse = RetrofitClient.api.obtenerSimulacro(idSimulacro)
                if (simResponse.success) {
                    totalPreguntas = simResponse.data.cantidad_preguntas

                    if (simResponse.data.estado == "finalizado") {
                        onSimulacroFinalizado() // ¡El profe lo terminó! Ir al podio
                        break
                    }
                }

                // 2. Obtener progreso de la clase
                val progResponse = RetrofitClient.api.obtenerProgresoSimulacro(idSimulacro)
                if (progResponse.success) {
                    progresoClase = progResponse.data
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
            delay(3000) // Actualizar cada 3 segundos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Has terminado",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Espera a que el docente finalice el simulacro para ver el podio.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Progreso de la Clase",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(Modifier.height(16.dp))

        if (isLoading && progresoClase.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(progresoClase) { compa ->
                    CompañeroProgressItem(compa, totalPreguntas, idEstudiante)
                }
            }
        }
    }
}

@Composable
fun CompañeroProgressItem(
    estudiante: ProgresoSimulacro,
    totalPreguntas: Int,
    miId: String
) {
    val esMio = estudiante.id_estudiante == miId
    val progreso = if (totalPreguntas > 0) estudiante.preguntas_respondidas.toFloat() / totalPreguntas else 0f

    // Animación suave de la barra
    val progresoAnimado by animateFloatAsState(targetValue = progreso, label = "progreso")

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (esMio) Color(0xFFE3F2FD) else Color.White, // Resaltar mi usuario
        border = if (esMio) BorderStroke(1.dp, AccentBlue) else null,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                if (estudiante.photoURL != null) {
                    AsyncImage(
                        model = estudiante.photoURL,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(estudiante.nombre.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = if (esMio) "Tú" else estudiante.nombre.split(" ")[0],
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextDark
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "${estudiante.preguntas_respondidas}/$totalPreguntas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (estudiante.preguntas_respondidas == totalPreguntas) Color(0xFF4CAF50) else Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progresoAnimado },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (estudiante.preguntas_respondidas == totalPreguntas) Color(0xFF4CAF50) else AccentBlue,
                trackColor = Color(0xFFEEEEEE),
            )
        }
    }
}