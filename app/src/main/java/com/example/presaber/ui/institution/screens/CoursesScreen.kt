package com.example.presaber.ui.institution.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.presaber.ui.components.AddCard
import com.example.presaber.ui.institution.viewmodel.CoursesViewModel

@Composable
fun CoursesScreen(
    idInstitucion: Int,
    navController: NavController,
    viewModel: CoursesViewModel = viewModel()
) {
    val cursos by viewModel.cursos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    val cursosList = remember(cursos, refreshTrigger) { cursos }

    var expandedCursoId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idInstitucion) {
        viewModel.setIdInstitucion(idInstitucion)
        viewModel.cargarCursos(idInstitucion)
    }

    LaunchedEffect(refreshTrigger) {
        println("UI recompuesta - Trigger: $refreshTrigger, Cursos: ${cursos.size}")
        cursos.forEach { curso ->
            println("   - ${curso.id}: habilitado=${curso.habilitado}")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            item {
                Text(
                    text = "Cursos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF485E92),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth() .padding(bottom = 16.dp, top = 8.dp)
                )
            }

            // Botón crear curso
            item {
                AddCard(
                    text = "Crear nuevo curso",
                    onClick = { 
                        navController.navigate("CreateCourseScreen/$idInstitucion")
                    }
                )
            }

            // Lista de cursos
            if (loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (error != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(
                    items = cursosList,
                    key = { curso -> "${curso.id}-${curso.habilitado}-$refreshTrigger" }
                ) { curso ->
                    CourseCard(
                        curso = curso,
                        isExpanded = expandedCursoId == curso.id,
                        onExpandClick = {
                            expandedCursoId = if (expandedCursoId == curso.id) null else curso.id
                        },
                        onToggleHabilitado = {
                            viewModel.toggleHabilitado(curso, idInstitucion)
                        }
                    )
                }
            }


            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCard(
    curso: Curso,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onToggleHabilitado: () -> Unit
) {

    val cardColor = if (curso.habilitado) Color(0xFF5B9EFF) else Color(0xFFE0E0E0)
    val textColor = if (curso.habilitado) Color.White else Color(0xFF666666)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Grupo ${curso.grado}-${curso.grupo}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                    Spacer(modifier= Modifier.height(2.dp))
                    Text(
                        text = "Cohorte ${curso.cohorte}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }

                IconButton(onClick = onExpandClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Información del curso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estudiantes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Estudiantes",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${curso.cantidadEstudiantes} estudiantes",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White
                        )
                    )
                }

                Switch(
                    checked = curso.habilitado,
                    onCheckedChange = { onToggleHabilitado() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF9E9E9E)
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (curso.fotoDocente != null) {
                        AsyncImage(
                            model = curso.fotoDocente,
                            contentDescription = "Foto docente",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = curso.nombreDocente ?: "Sin docente asignado",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = if (curso.nombreDocente == null) FontWeight.Normal else FontWeight.Medium
                    )
                )
            }

            // Menú expandible
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Clave de acceso: ${curso.claveAcceso}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}


// Data class para los cursos
data class Curso(
    val id: String,
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val claveAcceso: String,
    val habilitado: Boolean,
    val cantidadEstudiantes: Int = 0,
    val nombreDocente: String? = null,
    val fotoDocente: String? = null
)