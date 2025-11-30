package com.example.presaber.ui.pvp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.data.remote.HistorialSala
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.ui.theme.PresaberTheme
import kotlinx.coroutines.launch

@Composable
fun PvPHomeScreen(
    idEstudiante: String,
    onCrearSala: () -> Unit,
    onUnirseSala: () -> Unit
) {
    val scope = rememberCoroutineScope()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Salas Privadas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1B21),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Botones principales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Crear sala
            ElevatedCard(
                onClick = onCrearSala,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFF4A6FA5)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Crear",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Crear sala",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Compite contra otros",
                        color = Color(0xFFE3E8F0),
                        fontSize = 11.sp
                    )
                }
            }

            // Unirse a sala
            ElevatedCard(
                onClick = onUnirseSala,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFFF4A261)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Login,
                        contentDescription = "Unirse",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Unirse a sala",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Entra en competiciones",
                        color = Color(0xFFFFF5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Historial
        Text(
            text = "Historial",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1B21),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (historial.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay historial de salas",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historial) { sala ->
                    HistorialSalaCard(sala)
                }
            }
        }
    }
}

@Composable
fun HistorialSalaCard(sala: HistorialSala) {
    val esVictoria = sala.resultado == "victoria"
    val colorResultado = if (esVictoria) Color(0xFF4CAF50) else Color(0xFFE57373)
    val textoResultado = if (esVictoria) "Victoria" else "Derrota"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono resultado
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colorResultado.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (esVictoria) "✓" else "✗",
                    fontSize = 28.sp,
                    color = colorResultado,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(16.dp))

            // Info sala
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textoResultado,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResultado
                    )
                    Text(
                        text = sala.fecha_finalizacion.substring(0, 10),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Simulacro ${sala.area}",
                    fontSize = 14.sp,
                    color = Color(0xFF1A1B21)
                )

                Text(
                    text = "Correctas ${sala.preguntas_correctas}/${sala.total_preguntas}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Preview(
    name = "PvP Home Preview",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewPvPHomeScreen() {
    PresaberTheme {
        PvPHomeScreen(
            idEstudiante = "12345",
            onCrearSala = {},
            onUnirseSala = {}
        )
    }
}
