package com.example.presaber.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.R

// Colores corporativos
private val PrimaryBlue = Color(0xFF5B7BC6)
private val TextDark = Color(0xFF1A1B21)
private val BackgroundLight = Color(0xFFF8F9FA) // Blanco muy suave

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight), // Fondo claro
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Logo o Icono (Usamos el fuego que ya tienes como ejemplo de marca)
            Icon(
                painter = painterResource(id = R.drawable.fire),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Unspecified // Mantiene colores originales si es imagen, o usa tint si es vector
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Nombre de la App (Estilo corporativo)
            Text(
                text = buildAnnotatedString {
                    append("Pre")
                    withStyle(style = SpanStyle(color = PrimaryBlue)) {
                        append("Saber")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Indicador de carga (Azul corporativo)
            CircularProgressIndicator(
                color = PrimaryBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Texto de estado
            Text(
                text = "Iniciando sesión...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        // Opcional: Copyright o versión al pie de página
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Versión 1.0",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}