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
import coil.compose.AsyncImage
import com.example.presaber.ui.components.AddCard
import com.example.presaber.ui.institution.viewmodel.CoursesViewModel

@Composable
fun CoursesScreen(
    idInstitucion: Int,
    viewModel: CoursesViewModel = viewModel()
) {
    val cursos by viewModel.cursos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var expandedCursoId by remember { mutableStateOf<String?>(null) }

    // Cargar cursos al iniciar
    LaunchedEffect(idInstitucion) {
        viewModel.setIdInstitucion(idInstitucion)
        viewModel.cargarCursos(idInstitucion)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            item {
                Text(
                    text = "Cursos",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFF1E3A8A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Botón crear curso
            item {
                AddCard(
                    text = "Crear nuevo curso",
                    onClick = { showCreateDialog = true },
                    circleColor = Color(0xFF5B7BC6)
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
                items(cursos) { curso ->
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

            // Espaciado final
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Dialog para crear curso
        if (showCreateDialog) {
            CreateCourseDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { grado, grupo, cohorte, clave ->
                    viewModel.crearCurso(
                        grado = grado,
                        grupo = grupo,
                        cohorte = cohorte,
                        claveAcceso = clave,
                        idInstitucion = idInstitucion
                    ) { success, message ->
                        if (success) {
                            showCreateDialog = false
                            viewModel.cargarCursos(idInstitucion)
                        }
                    }
                }
            )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (curso.habilitado) Color(0xFF5B9EFF) else Color(0xFFE0E0E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: Nombre y menú
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

            Spacer(modifier = Modifier.height(8.dp))

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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${curso.cantidadEstudiantes} estudiantes",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White
                        )
                    )
                }

                // Toggle habilitado
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

            // Docente
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
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
                Spacer(modifier = Modifier.width(8.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var grado by remember { mutableStateOf("") }
    var grupo by remember { mutableStateOf("") }
    var cohorte by remember { mutableStateOf("") }
    var claveAcceso by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Crear Nuevo Curso",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showError) {
                    Text(
                        text = "Todos los campos son obligatorios",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = grado,
                    onValueChange = { if (it.length <= 2) grado = it },
                    label = { Text("Grado (ej: 11)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = grupo,
                    onValueChange = { if (it.length <= 2) grupo = it },
                    label = { Text("Grupo (ej: 01)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cohorte,
                    onValueChange = { if (it.length <= 4) cohorte = it },
                    label = { Text("Cohorte (ej: 2025)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = claveAcceso,
                    onValueChange = { if (it.length <= 20) claveAcceso = it },
                    label = { Text("Clave de acceso") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (grado.isBlank() || grupo.isBlank() || cohorte.isBlank() || claveAcceso.isBlank()) {
                        showError = true
                    } else {
                        val cohorteInt = cohorte.toIntOrNull()
                        if (cohorteInt != null) {
                            onConfirm(grado, grupo, cohorteInt, claveAcceso)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B7BC6)
                )
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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