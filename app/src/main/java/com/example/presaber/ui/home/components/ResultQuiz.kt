package com.example.presaber.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.data.remote.ResultadoData
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultQuiz(resultado: ResultadoData, onAccept: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    val nombreUsuario = remember(user) {
        val displayName = user?.displayName ?: "Usuario"
        displayName.split(" ").joinToString(" ") { palabra ->
            palabra.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    val fotoPerfil = user?.photoUrl?.toString()

    // 🎨 Fondo degradado azul claro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFBBDEFB), // azul muy claro arriba
                        Color(0xFFE3F2FD), // azul blanquecino
                        Color.White
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 🧍 Imagen del usuario
            AsyncImage(
                model = fotoPerfil ?: R.drawable.ic_avatar_placeholder,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 👤 Nombre
            Text(
                text = nombreUsuario,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color(0xFF000000)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = resultado.frase_motivadora,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                ),
                color = Color(0xFF85AFD7),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "${resultado.preguntas_correctas}/${resultado.total_preguntas}",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 44.sp
                ),
                color = Color(0xFF1976D2),
                textAlign = TextAlign.Center
            )

            Text(
                text = "${resultado.experiencia} EXP",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp
                ),
                color = Color(0xFF1E88E5),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onAccept,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .width(160.dp)
                    .height(50.dp)
            ) {
                Text("Aceptar", fontSize = 17.sp)
            }
        }
    }
}
