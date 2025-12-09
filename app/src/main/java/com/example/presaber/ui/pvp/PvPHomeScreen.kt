package com.example.presaber.ui.pvp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.HistorialSala
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.launch

// Colores del tema
private val AccentBlue = Color(0xFF5685FF)
private val AccentOrange = Color(0xFFFCB35A)
private val TextGray = Color(0xFF757575)
private val VictoryGreen = Color(0xFF38C771)
private val DefeatRed = Color(0xFFFF5252)

@Composable
fun PvPHomeScreen(
    idEstudiante: String,
    onCrearSala: () -> Unit,
    onUnirseSala: () -> Unit
) {
    var historial by remember { mutableStateOf<List<HistorialSala>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.obtenerHistorialSalas(idEstudiante)
            if (response.success) {
                historial = response.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Fondo decorativo
        BackgroundBlobsHome()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCard(
                        title = "Crear Sala",
                        subtitle = "Invita amigos",
                        icon = Icons.Rounded.Add,
                        color = AccentBlue,
                        onClick = onCrearSala,
                        modifier = Modifier.weight(1f)
                    )

                    ActionCard(
                        title = "Unirse",
                        subtitle = "Con código",
                        icon = Icons.Rounded.Login,
                        color = AccentOrange,
                        onClick = onUnirseSala,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Título Historial
            item {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Partidas Recientes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }

            // 4. Lista de Historial
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
            } else if (historial.isEmpty()) {
                item {
                    EmptyStateHome()
                }
            } else {
                items(historial) { sala ->
                    HistorialItemClean(sala)
                }
            }
        }
    }
}

// --- Componentes UI ---

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = color,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Círculo decorativo en el fondo de la tarjeta
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = size.height * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(size.width, 0f)
                )
            }

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Icono en burbuja blanca
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                }

                // Textos
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun HistorialItemClean(sala: HistorialSala) {
    val esVictoria = sala.resultado == "victoria"
    val colorEstado = if (esVictoria) VictoryGreen else DefeatRed

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp, // Sombra suave en lugar de borde
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del área (Visual)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorEstado.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (esVictoria) Icons.Rounded.EmojiEvents else Icons.Rounded.SportsEsports,
                    contentDescription = null,
                    tint = colorEstado,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Información Central
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sala.area,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = sala.fecha_finalizacion.take(10),
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            // Puntaje a la derecha
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${sala.preguntas_correctas}/${sala.total_preguntas}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (esVictoria) TextDark else TextGray
                )
                Text(
                    text = if (esVictoria) "VICTORIA" else "DERROTA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorEstado,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun EmptyStateHome() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Aún no tienes partidas",
            color = Color.Gray,
            fontSize = 16.sp
        )
        Text(
            text = "¡Crea una sala para empezar!",
            color = Color.LightGray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BackgroundBlobsHome() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Blob superior derecho
        drawCircle(
            color = Color(0xFFE3F2FD).copy(alpha = 0.5f),
            radius = width * 0.6f,
            center = androidx.compose.ui.geometry.Offset(width * 1.2f, height * 0.1f)
        )

        // Blob inferior izquierdo
        drawCircle(
            color = Color(0xFFFFF3E0).copy(alpha = 0.5f),
            radius = width * 0.4f,
            center = androidx.compose.ui.geometry.Offset(0f, height * 0.9f)
        )
    }
}