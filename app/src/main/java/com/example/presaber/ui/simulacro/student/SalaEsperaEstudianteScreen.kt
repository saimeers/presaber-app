package com.example.presaber.ui.simulacro.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SimulacroGrupal
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun SalaEsperaEstudianteScreen(
    idSimulacro: Int,
    onStartQuiz: () -> Unit
) {
    var simulacro by remember { mutableStateOf<SimulacroGrupal?>(null) }

    // Polling: Revisa estado cada 3 segundos
    LaunchedEffect(idSimulacro) {
        while (isActive) {
            try {
                val response = RetrofitClient.api.obtenerSimulacro(idSimulacro)
                if (response.success) {
                    simulacro = response.data
                    // Si el estado cambia a 'en_curso', navegamos al quiz
                    if (simulacro?.estado == "en_curso") {
                        onStartQuiz()
                        break // Salir del loop
                    }
                    // Si el estado es 'finalizado', sacarlo (opcional)
                }
            } catch (e: Exception) { e.printStackTrace() }
            delay(3000)
        }
    }

    if (simulacro == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Esperando al Docente...",
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1B21)
        )
        Text(
            text = "El simulacro iniciará pronto",
            color = Color.Gray, fontSize = 14.sp
        )
        Spacer(Modifier.height(32.dp))

        // Info Simulacro
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Área/Curso", fontSize = 12.sp, color = Color.Gray)
                Text("${simulacro!!.curso.grado}${simulacro!!.curso.grupo}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("${simulacro!!.duracion_minutos} min", fontWeight = FontWeight.Bold, color = Color(0xFF5685FF))
                    Text("${simulacro!!.cantidad_preguntas} preguntas", fontWeight = FontWeight.Bold, color = Color(0xFF5685FF))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Compañeros unidos (${simulacro!!.participantes.size})", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(16.dp))

        // Grid Participantes
        LazyVerticalGrid(
            columns = GridCells.Adaptive(60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(simulacro!!.participantes) { p ->
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (p.estudiante.photoURL != null) {
                        AsyncImage(model = p.estudiante.photoURL, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Text(p.estudiante.nombre.take(1), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}