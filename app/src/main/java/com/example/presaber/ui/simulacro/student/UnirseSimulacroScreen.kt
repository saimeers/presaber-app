package com.example.presaber.ui.simulacro.student

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.HistorialSimulacro
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SimulacroResumen
import com.example.presaber.data.remote.Usuario
import com.example.presaber.utils.SimulacroSessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UnirseSimulacroScreen(
    usuario: Usuario,
    onNavigateToWaitingRoom: (Int) -> Unit,
    onVerResultados: (Int) -> Unit, // Nuevo callback para ver el podio del historial
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var simulacrosDisponibles by remember { mutableStateOf<List<SimulacroResumen>>(emptyList()) }
    var historialSimulacros by remember { mutableStateOf<List<HistorialSimulacro>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isJoiningId by remember { mutableStateOf<Int?>(null) }

    // Cargar datos
    LaunchedEffect(Unit) {
        try {
            // 1. Cargar disponibles
            val respDisponibles = RetrofitClient.api.obtenerSimulacrosCurso(
                usuario.grado, usuario.grupo, usuario.cohorte, usuario.institucion
            )
            if (respDisponibles.success) {
                simulacrosDisponibles = respDisponibles.data.filter { it.estado == "esperando" }
            }

            // 2. Cargar historial
            val respHistorial = RetrofitClient.api.obtenerHistorialSimulacroEstudiante(usuario.documento)
            if (respHistorial.success) {
                historialSimulacros = respHistorial.data
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF1A1B21))
            }
            Text(
                "Simulacros Grupales",
                fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF5685FF))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // SECCIÓN DISPONIBLES
                item {
                    Text("Disponibles para unirse", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                }

                if (simulacrosDisponibles.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No hay simulacros activos en este momento.",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(simulacrosDisponibles) { simulacro ->
                        SimulacroDisponibleCard(
                            simulacro = simulacro,
                            isJoining = isJoiningId == simulacro.id_simulacro,
                            onJoinClick = {
                                isJoiningId = simulacro.id_simulacro
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.api.unirseASimulacro(
                                            simulacro.id_simulacro,
                                            mapOf("id_estudiante" to usuario.documento)
                                        )
                                        if (response.success) {
                                            SimulacroSessionManager.saveActiveSession(context, simulacro.id_simulacro)
                                            onNavigateToWaitingRoom(simulacro.id_simulacro)
                                        } else {
                                            Toast.makeText(context, response.message ?: "Error", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isJoiningId = null
                                    }
                                }
                            }
                        )
                    }
                }

                // SECCIÓN HISTORIAL
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Tu Historial", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                }

                if (historialSimulacros.isEmpty()) {
                    item {
                        Text("Aún no has participado en simulacros.", fontSize = 14.sp, color = Color.LightGray)
                    }
                } else {
                    items(historialSimulacros) { historial ->
                        HistorialCard(historial, onClick = { onVerResultados(historial.id_simulacro) })
                    }
                }
            }
        }
    }
}

@Composable
fun SimulacroDisponibleCard(
    simulacro: SimulacroResumen,
    isJoining: Boolean,
    onJoinClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape, color = Color(0xFFE3F2FD),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color(0xFF5685FF))
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Simulacro Grupal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${simulacro.cantidad_preguntas} preguntas • ${simulacro.duracion_minutos} min", fontSize = 12.sp, color = Color.Gray)
                Text("Docente: ${simulacro.docente}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }

            Button(
                onClick = onJoinClick,
                enabled = !isJoining,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5685FF)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                if (isJoining) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Unirme", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun HistorialCard(
    historial: HistorialSimulacro,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val fechaStr = try {
        dateFormat.format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(historial.fecha_finalizacion)!!)
    } catch (e: Exception) { historial.fecha_finalizacion }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de posición (Trofeo o número)
            Surface(
                shape = CircleShape,
                color = if (historial.posicion_final in 1..3) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFFF5F5F5),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (historial.posicion_final in 1..3) {
                        Icon(Icons.Rounded.EmojiEvents, null, tint = Color(0xFFFFA000), modifier = Modifier.size(20.dp))
                    } else {
                        Text("#${historial.posicion_final ?: "-"}", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Simulacro Finalizado", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(fechaStr, fontSize = 12.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${historial.puntaje_final}%", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF1A1B21))
                Text("+${historial.experiencia_ganada} XP", fontSize = 10.sp, color = Color(0xFF5685FF), fontWeight = FontWeight.Bold)
            }
        }
    }
}