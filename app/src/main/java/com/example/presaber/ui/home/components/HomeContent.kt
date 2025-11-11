package com.example.presaber.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.R

data class SubjectArea(
    val title: String,
    val description: String,
    val imageRes: Int,
    val cardColor: Color
)

@Composable
fun HomeContent(
    onSubjectClick: (SubjectArea) -> Unit = {}
) {
    var showFabMenu by remember { mutableStateOf(false) }

    val subjects = listOf(
        SubjectArea(
            "Matemáticas",
            "Desarrolla tu razonamiento lógico y habilidades matemáticas",
            R.drawable.img_matematicas,
            Color(0xFF5B7ABD)
        ),
        SubjectArea(
            "Lectura Crítica",
            "Mejora tu comprensión lectora y análisis de textos",
            R.drawable.img_lectura,
            Color(0xFFE8B959)
        ),
        SubjectArea(
            "Ciencias Naturales",
            "Explora el mundo natural y sus fenómenos",
            R.drawable.img_ciencias,
            Color(0xFF7AB88E)
        ),
        SubjectArea(
            "Inglés",
            "Fortalece tus habilidades comunicativas en inglés",
            R.drawable.img_ingles,
            Color(0xFFE87C7C)
        ),
        SubjectArea(
            "Sociales y Ciudadanas",
            "Comprende la sociedad y tu rol como ciudadano",
            R.drawable.img_sociales,
            Color(0xFF9E9E9E)
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Retos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1B21),
                    modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
                )
            }

            items(subjects) { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = { onSubjectClick(subject) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Espacio adicional al final para que no quede detrás del FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Menu items
                if (showFabMenu) {
                    SmallFloatingActionButton(
                        onClick = { /* Acción Unirse */ },
                        containerColor = Color(0xFFADC3FE),
                        contentColor = Color.Black
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Group,
                                "Unirse",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Unirse", fontSize = 14.sp)
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = { /* Acción Simulacro */ },
                        containerColor = Color(0xFFADC3FE),
                        contentColor = Color.Black
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.icon_timer),
                                "Simulacro",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Simulacro", fontSize = 14.sp)
                        }
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    shape = CircleShape,
                    containerColor = Color(0xFF2C3E7A),
                    modifier = Modifier.size(56.dp)
                ) {
                    if (showFabMenu) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Abrir menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectCard(
    subject: SubjectArea,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Texto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subject.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1B21)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subject.description,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }

            // Imagen
            Surface(
                color = subject.cardColor,
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = subject.imageRes),
                    contentDescription = subject.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}