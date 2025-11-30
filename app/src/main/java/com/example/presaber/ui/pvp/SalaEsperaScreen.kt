package com.example.presaber.ui.pvp

import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SalaPrivada
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SalaEsperaScreen(
    idSala: Int,
    idEstudiante: String,
    onIniciar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var sala by remember { mutableStateOf<SalaPrivada?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var copiado by remember { mutableStateOf(false) }

    // Polling para actualizar estado de la sala
    LaunchedEffect(idSala) {
        while (true) {
            try {
                val response = RetrofitClient.api.obtenerSala(idSala)
                if (response.success) {
                    sala = response.data

                    // Si la sala está en curso, iniciar juego
                    if (response.data.estado == "en_curso") {
                        delay(1000) // Pequeña pausa antes de iniciar
                        onIniciar()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }

            delay(2000) // Actualizar cada 2 segundos
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (sala == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Error al cargar sala", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    val esCreador = sala!!.id_creador == idEstudiante
    val participantes = sala!!.participantes
    val todoListos = participantes.size == 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // Título
        Text(
            text = "Código",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1B21)
        )

        Spacer(Modifier.height(16.dp))

        // Código de sala
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sala!!.codigo_sala,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1B21),
                    letterSpacing = 4.sp
                )

                Spacer(Modifier.height(20.dp))

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Copiar código
                    OutlinedButton(
                        onClick = {
                            copiarCodigo(context, sala!!.codigo_sala)
                            copiado = true
                            scope.launch {
                                delay(2000)
                                copiado = false
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4A6FA5)
                        )
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copiar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (copiado) "¡Copiado!" else "Copiar")
                    }

                    // Compartir
                    Button(
                        onClick = {
                            compartirCodigo(context, sala!!.codigo_sala, sala!!.area.nombre)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A6FA5)
                        )
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir")
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        // Lista de jugadores
        ParticipantesDisplay(
            participantes = participantes,
            todoListos = todoListos
        )

        Spacer(Modifier.weight(1f))

        // Estado
        if (todoListos) {
            Text(
                text = "Listos",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4CAF50)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Iniciando juego...",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = if (esCreador) "Esperando jugador..." else "Esperando...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1B21)
            )
            Spacer(Modifier.height(8.dp))
            AnimatedLoadingDots()
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ParticipantesDisplay(
    participantes: List<com.example.presaber.data.remote.ParticipanteSala>,
    todoListos: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Jugador 1
        val jugador1 = participantes.getOrNull(0)
        ParticipanteAvatar(
            nombre = jugador1?.estudiante?.nombre_completo ?: "Esperando",
            photoUrl = jugador1?.estudiante?.photoURL,
            listo = jugador1 != null
        )

        Spacer(Modifier.width(32.dp))

        // Círculo central con animación
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    if (todoListos) Color(0xFF4CAF50) else Color(0xFF90A4AE),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (todoListos) "VS" else "?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.width(32.dp))

        // Jugador 2
        val jugador2 = participantes.getOrNull(1)
        ParticipanteAvatar(
            nombre = jugador2?.estudiante?.nombre_completo ?: "Esperando",
            photoUrl = jugador2?.estudiante?.photoURL,
            listo = jugador2 != null
        )
    }
}

@Composable
fun ParticipanteAvatar(
    nombre: String,
    photoUrl: String?,
    listo: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(
                    width = 3.dp,
                    color = if (listo) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                    shape = CircleShape
                )
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = nombre,
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
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = nombre,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1B21),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun AnimatedLoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color(0xFF4A6FA5).copy(alpha = alpha1), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color(0xFF4A6FA5).copy(alpha = alpha2), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color(0xFF4A6FA5).copy(alpha = alpha3), CircleShape)
        )
    }
}

// Función para copiar al portapapeles
fun copiarCodigo(context: Context, codigo: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Código Sala", codigo)
    clipboard.setPrimaryClip(clip)
}

// Función para compartir


fun compartirCodigo(context: Context, codigo: String, area: String) {
    val deepLink = "presaber://sala/$codigo"

    val mensaje = """
        🎮 ¡Te invito a una sala privada en PreSaber!
        
        📚 Área: $area
        🔑 Código: $codigo
    """.trimIndent()

    // Copiar código al portapapeles
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Código sala PreSaber", codigo)
    clipboard.setPrimaryClip(clip)

    // Mostrar toast
    Toast.makeText(context, "Código copiado: $codigo", Toast.LENGTH_SHORT).show()

    // Compartir
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, mensaje)
    }

    context.startActivity(Intent.createChooser(intent, "Compartir sala"))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SalaEsperaScreenPreview() {
    com.example.presaber.ui.theme.PresaberTheme {
        SalaEsperaScreen(
            idSala = 1,
            idEstudiante = "12345",
            onIniciar = { }
        )
    }
}
