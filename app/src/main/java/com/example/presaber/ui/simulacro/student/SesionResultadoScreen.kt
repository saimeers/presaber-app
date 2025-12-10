package com.example.presaber.ui.simulacro.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.R
import com.example.presaber.viewmodel.SesionEstudianteViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun SesionResultadoScreen(
    idSesion: Int,
    idEstudiante: String,
    onAceptar: () -> Unit,
    viewModel: SesionEstudianteViewModel = viewModel()
) {
    val resultado by viewModel.resultadoSesion.collectAsState()

    LaunchedEffect(idSesion, idEstudiante) {
        viewModel.cargarResultado(idSesion, idEstudiante)
    }

    val res = resultado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (res == null || res.disponible != true) {
            Text(text = res?.mensaje ?: "Resultados no disponibles aún")
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Resultado sesión 1",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1B21)
                )
                Text(
                    text = "4:30 horas - ${res.puntaje_total?.toInt() ?: 0} preguntas",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )

                Card(
                    shape = RoundedCornerShape(120.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(16.dp)
                    )
                }

                Text(
                    text = "Leydi Alejandra",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1B21)
                )

                Text(
                    text = "Podrás ver los detalles del simulacro al\nfinalizar todas las sesiones.",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF757575),
                    fontSize = 13.sp
                )

                Text(
                    text = "${res.puntaje_obtenido?.toInt() ?: 0}/${res.puntaje_total?.toInt() ?: 0}",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1B21)
                )

                Text(
                    text = "${res.experiencia_ganada ?: 0} EXP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1B21)
                )

                Button(
                    onClick = onAceptar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5685FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(160.dp)
                ) {
                    Text("Aceptar")
                }
            }
        }
    }
}

