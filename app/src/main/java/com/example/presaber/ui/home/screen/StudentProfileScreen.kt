package com.example.presaber.ui.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.data.remote.AreaEstadistica
import com.example.presaber.data.remote.ItemHistorial
import com.example.presaber.ui.home.viewmodel.PerfilViewModel

@Composable
fun StudentProfileScreen(
    studentId: String,
    onBack: () -> Unit = {}
) {
    val viewModel: PerfilViewModel = viewModel()
    val perfil by viewModel.perfilState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.cargarPerfil(studentId)
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF5B7BC6)) }
    } else if (perfil != null) {
        val p = perfil!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // HEADER
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Adjusted height
                ) {
                    // Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFE2E7EE))
                    )

                    // Back Button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF1A1B21))
                    }

                    // Avatar & Name
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(110.dp)
                        ) {
                            if (p.estudiante.photoURL != null) {
                                AsyncImage(
                                    model = p.estudiante.photoURL,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.icon_user),
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = p.estudiante.nombreCompleto,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1B21)
                        )
                        Text(
                            text = p.estudiante.institucion ?: "Sin institución",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            // RESUMEN
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A1B21))
                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            icon = R.drawable.fire,
                            value = "${p.resumen.rachaVictorias}",
                            label = "Victorias",
                            iconTint = Color(0xFFFF5252),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            icon = R.drawable.icon_destellos,
                            value = "${p.resumen.experienciaTotal}",
                            label = "Puntaje XP",
                            iconTint = Color(0xFFFFD700),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            icon = R.drawable.icon_modules,
                            value = "${p.resumen.modulosResueltos}",
                            label = "Módulos",
                            iconTint = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            icon = R.drawable.icon_simulacro,
                            value = "${p.resumen.ultimoPuntajeSimulacro}",
                            label = "Simulacro",
                            iconTint = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // AREAS
            item {
                Text(
                    "Áreas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1B21),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(p.areas) { area ->
                AreaStatRow(area)
            }

            // HISTORIAL
            item {
                Text(
                    "Historial",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1B21),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }
            items(p.historial) { history ->
                HistoryRow(history)
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    } else {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No se pudo cargar el perfil", color = Color.Gray) }
    }
}

@Composable
fun SummaryCard(icon: Int, value: String, label: String, iconTint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A1B21))
                Text(label, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AreaStatRow(area: AreaEstadistica) {
    val color = when(area.idArea) { // Simple color mapping
        1 -> Color(0xFFE8B959) // Lectura
        2 -> Color(0xFF5B7ABD) // Mate
        3 -> Color(0xFF7AB88E) // Ciencias
        4 -> Color(0xFFE87C7C) // Sociales
        5 -> Color(0xFF9E9E9E) // Ingles
        else -> Color.Gray
    }

    // Icon mapping (you might need to adjust resource IDs)
    val icon = when(area.idArea) {
        1 -> R.drawable.img_lectura
        2 -> R.drawable.img_matematicas
        3 -> R.drawable.img_ciencias
        4 -> R.drawable.img_sociales
        5 -> R.drawable.img_ingles
        else -> R.drawable.icon_pregunta
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = color.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(area.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1B21))
            Text(
                "${area.incorrectas} incorrectas, ${area.correctas} correctas",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            "${area.total}", // Or percentage if preferred
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
}

@Composable
fun HistoryRow(item: ItemHistorial) {
    val isWin = item.estado == "Victoria"
    val colorState = if(isWin) Color(0xFF4CAF50) else Color(0xFFF44336)
    val bgState = if(isWin) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(if(item.tipo == "Grupal") R.drawable.icon_grupos else R.drawable.icon_pvp),
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.tipo} - ${item.estado}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorState
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        item.fecha.take(10), // Simple date truncate
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(item.titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1B21))
                Text("Correctas ${item.correctas}/${item.total}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}