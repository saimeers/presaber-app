package com.example.presaber.ui.simulacro.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.AreaResultadoSimulacro
import com.example.presaber.viewmodel.SesionEstudianteViewModel

@Composable
fun SimulacroResultadoGlobalScreen(
    idSimulacro: Int,
    usuario: com.example.presaber.data.remote.Usuario,
    onContinuar: () -> Unit,
    viewModel: SesionEstudianteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val resultado by viewModel.resultadoGlobal.collectAsState()

    LaunchedEffect(idSimulacro, usuario.documento) {
        viewModel.cargarResultadoGlobal(idSimulacro, usuario.documento)
    }

    if (uiState.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF5685FF))
        }
        return
    }

    val res = resultado

    if (res == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No se pudieron cargar los resultados.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.cargarResultadoGlobal(idSimulacro, usuario.documento) }) {
                    Text("Reintentar")
                }
            }
        }
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState), // Make the whole screen scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // Title
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Resultado del Simulacro",
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1B21),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)), 
                contentAlignment = Alignment.Center
            ) {
                 Text(
                    text = usuario.nombre.take(1).uppercase() + usuario.apellido.take(1).uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                 )
            }
            
            Column {
                Text(
                    text = "${usuario.nombre} ${usuario.apellido}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1B21)
                )
                Text(
                    text = "Código: ${usuario.documento}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                 Text(
                    text = "Institución: ${usuario.institucion}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Total Score
        val puntajeObtenido = res.puntaje_total_obtenido?.toInt() ?: 0
        val puntajeMaximo = 500
        
        Row(verticalAlignment = Alignment.CenterVertically) {
             Icon(
                painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.star_big_on),
                contentDescription = null,
                tint = Color(0xFFFF9800), 
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(
                        color = Color(0xFFFF5722),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black
                    )) {
                        append(puntajeObtenido.toString())
                    }
                    withStyle(style = SpanStyle(
                        color = Color(0xFFFFCC80),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black
                    )) {
                        append("/")
                    }
                    withStyle(style = SpanStyle(
                        color = Color(0xFFFFCC80),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black
                    )) {
                        append(puntajeMaximo.toString())
                    }
                }
            )
        }
        
        Text(
            text = "¡Has completado el simulacro!",
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Details Areas Header
        Text(
            text = "Detalles por Área",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Areas List (Manual mapping instead of LazyColumn to avoid nesting issues)
        val areas = res.areas ?: emptyList()
        
        if (areas.isEmpty()) {
            Text("No hay detalles de áreas disponibles.", color = Color.Gray)
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                areas.forEach { area ->
                    AreaResultCard(area)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinuar,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Volver al Inicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AreaResultCard(area: AreaResultadoSimulacro) {
    val (icon, colorBg, colorText) = getAreaStyle(area.id_area)
    
    // Improved Card Design
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp), // Slight inset
        border = androidx.compose.foundation.BorderStroke(1.dp, colorBg.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Background box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorBg.copy(alpha = 0.2f)), // Softer background
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colorText, modifier = Modifier.size(28.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                 Text(
                    text = area.nombre_area,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                 Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Black, fontSize = 22.sp, color = colorText)) {
                            append("${area.porcentaje}")
                        }
                         withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.LightGray)) {
                            append("/100")
                        }
                    }
                )
            }

            // Arrow/Btn
            Icon(
                Icons.Rounded.ArrowForward, 
                contentDescription = null, 
                tint = colorText.copy(alpha = 0.6f), 
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun getAreaStyle(idArea: Int): Triple<ImageVector, Color, Color> {
    // Definir colores pastel más bonitos y consistentes
    return when(idArea) {
        2 -> Triple(Icons.Rounded.Calculate, Color(0xFFFFD54F), Color(0xFFF57F17)) // Matemáticas: Amarillo/Ambar
        1 -> Triple(Icons.Rounded.Book, Color(0xFFBA68C8), Color(0xFF8E24AA))      // Lectura: Morado
        3 -> Triple(Icons.Rounded.Science, Color(0xFF81C784), Color(0xFF2E7D32))   // Naturales: Verde
        4 -> Triple(Icons.Rounded.Public, Color(0xFFE57373), Color(0xFFC62828))    // Sociales: Rojo
        5 -> Triple(Icons.Rounded.Translate, Color(0xFF64B5F6), Color(0xFF1565C0)) // Inglés: Azul
        else -> Triple(Icons.Rounded.Book, Color.LightGray, Color.DarkGray)
    }
}
