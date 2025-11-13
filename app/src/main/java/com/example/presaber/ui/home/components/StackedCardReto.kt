package com.example.presaber.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.Reto
import com.example.presaber.data.remote.Tema

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StackedCardReto(reto: Reto, onStart: (Reto) -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFFF3F6F8)
        ),
        onClick = {  }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reto.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                DificultadBadge(nivel = reto.nivel_dificultad)
            }

            Text(
                text = "${reto.duracion.take(5)} - ${reto.cantidad_preguntas} preguntas",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ⬇️ VERIFICAR QUE IMAGEN NO SEA NULL NI VACÍA
            if (!reto.imagen.isNullOrEmpty()) {
                AsyncImage(
                    model = reto.imagen,
                    contentDescription = reto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = reto.descripcion,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onStart(reto) },
                modifier = Modifier.align(Alignment.End),
                border = BorderStroke(1.dp, Color(0xFF1976D2))
            ) {
                Text(text = "Comenzar", color = Color(0xFF1976D2))
            }
        }
    }
}

@Composable
fun DificultadBadge(nivel: String) {
    val (colorFondo, texto) = when (nivel.lowercase()) {
        "bajo", "básico", "basico" -> Color(0xFFB8F397) to "Bajo"
        "medio", "intermedio" -> Color(0xFFFFE173) to "Medio"
        "alto", "avanzado" -> Color(0xFFFFA6A6) to "Alto"
        else -> Color.LightGray to nivel
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
