package com.example.presaber.ui.simulacro.teacher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.IniciarSimulacroRequest
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SimulacroGrupal
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val AccentBlue = Color(0xFF5685FF)
private val AccentGreen = Color(0xFF38C771)
private val TextDark = Color(0xFF1A1B21)

@Composable
fun SimulacroEsperaScreen(
    idSimulacro: Int,
    idDocente: String,
    onIniciar: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var simulacro by remember { mutableStateOf<SimulacroGrupal?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isIniciando by remember { mutableStateOf(false) }

    // Polling para actualizar participantes
    LaunchedEffect(idSimulacro) {
        while (isActive) {
            try {
                val response = RetrofitClient.api.obtenerSimulacro(idSimulacro)
                if (response.success) {
                    simulacro = response.data
                    if (response.data.estado == "en_curso") {
                        delay(1000)
                        onIniciar()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
            delay(2000)
        }
    }

    if (isLoading || simulacro == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    val participantes = simulacro!!.participantes
    val cantidadParticipantes = participantes.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BackgroundBlobsSutil()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Título
            Text(
                text = "Simulacro creado",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "En unos minutos inicia el simulacro, estamos esperando más participantes.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Contador de participantes
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AccentBlue.copy(alpha = 0.1f),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$cantidadParticipantes estudiante${if (cantidadParticipantes != 1) "s" else ""} unido${if (cantidadParticipantes != 1) "s" else ""}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Grid de avatares
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(participantes) { participante ->
                    EstudianteAvatar(
                        nombre = participante.estudiante.nombre_completo,
                        photoURL = participante.estudiante.photoURL
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botón iniciar
            Button(
                onClick = {
                    if (cantidadParticipantes == 0) return@Button

                    isIniciando = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.iniciarSimulacro(
                                idSimulacro,
                                IniciarSimulacroRequest(id_docente = idDocente)
                            )

                            if (response.success) {
                                onIniciar()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isIniciando = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isIniciando && cantidadParticipantes > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                if (isIniciando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Iniciar simulacro",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (cantidadParticipantes == 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Esperando que al menos un estudiante se una",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun EstudianteAvatar(
    nombre: String,
    photoURL: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .border(3.dp, AccentBlue, CircleShape)
        ) {
            if (photoURL != null) {
                AsyncImage(
                    model = photoURL,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = nombre.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = nombre.split(" ").firstOrNull() ?: nombre,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun BackgroundBlobsSutil() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFE3F2FD).copy(alpha = 0.5f),
            radius = size.width * 0.4f,
            center = Offset(size.width, 0f)
        )
        drawCircle(
            color = Color(0xFFFFF3E0).copy(alpha = 0.5f),
            radius = size.width * 0.3f,
            center = Offset(0f, size.height)
        )
    }
}