package com.example.presaber.ui.institution.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.presaber.data.remote.Usuario
import com.example.presaber.ui.admin.components.AddCard
import com.example.presaber.ui.institution.components.courses.CourseCard
import com.example.presaber.ui.institution.viewmodel.CoursesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    idInstitucion: Int,
    usuario: Usuario,
    navController: NavController,
    viewModel: CoursesViewModel = viewModel()
) {
    val cursos by viewModel.cursos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val esDirector = usuario.rol == 4
    val esDocente = usuario.rol == 2

    // Filtrar cursos según ROL y BÚSQUEDA
    val cursosFiltrados = remember(cursos, searchQuery, refreshTrigger, usuario) {
        // 1. Filtro de seguridad por Rol
        val cursosVisibles = if (esDocente) {
            // El docente solo ve cursos donde su documento coincida con el documento del docente del curso
            cursos.filter { it.documentoDocente == usuario.documento }
        } else {
            // El director (o admin) ve todos los cursos
            cursos
        }

        // 2. Filtro por texto de búsqueda
        if (searchQuery.isEmpty()) {
            cursosVisibles
        } else {
            cursosVisibles.filter { curso ->
                val query = searchQuery.lowercase()
                "grupo ${curso.grado}-${curso.grupo}".lowercase().contains(query) ||
                        "grado ${curso.grado}".lowercase().contains(query) ||
                        "grupo ${curso.grupo}".lowercase().contains(query) ||
                        curso.cohorte.toString().contains(query) ||
                        curso.nombreDocente?.lowercase()?.contains(query) == true
            }
        }
    }

    LaunchedEffect(idInstitucion) {
        viewModel.setIdInstitucion(idInstitucion)
        // Cargamos todos los cursos de la institución. El filtrado visual se hace arriba.
        viewModel.cargarCursos(idInstitucion)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Espaciador superior
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Título dinámico
            item {
                Text(
                    text = if (esDocente) "Mis Cursos Asignados" else "Gestión de Cursos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF485E92),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp)
                )
            }

            // Botón crear curso: SOLO VISIBLE PARA DIRECTOR
            if (esDirector) {
                item {
                    AddCard(
                        text = "Crear nuevo curso",
                        onClick = {
                            navController.navigate("CreateCourseScreen/$idInstitucion")
                        }
                    )
                }
            }

            // Buscador
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    placeholder = {
                        Text("Buscar...", color = Color(0xFF9E9E9E), fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Buscar", tint = Color(0xFF5B7BC6))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Limpiar", tint = Color(0xFF9E9E9E))
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B7BC6),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Contador de resultados
            if (searchQuery.isNotEmpty()) {
                item {
                    Text(
                        text = "${cursosFiltrados.size} curso(s) encontrado(s)",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF666666)),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Estados de Carga / Error / Lista
            if (loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF5B7BC6))
                    }
                }
            } else if (error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = Color(0xFFC62828))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(error ?: "Error desconocido", color = Color(0xFFC62828))
                        }
                    }
                }
            } else if (cursosFiltrados.isEmpty()) {
                item {
                    // Empty State personalizado según rol
                    EmptyStateCard(
                        isSearching = searchQuery.isNotEmpty(),
                        esDocente = esDocente
                    )
                }
            } else {
                items(
                    items = cursosFiltrados,
                    key = { curso -> "${curso.id}-${curso.habilitado}-$refreshTrigger" }
                ) { curso ->
                    CourseCard(
                        grado = curso.grado,
                        grupo = curso.grupo,
                        cohorte = curso.cohorte,
                        claveAcceso = curso.claveAcceso,
                        habilitado = curso.habilitado,
                        cantidadEstudiantes = curso.cantidadEstudiantes,
                        nombreDocente = curso.nombreDocente,
                        fotoDocente = curso.fotoDocente,
                        onClick = {
                            navController.navigate("courseDetail/${curso.grado}/${curso.grupo}/${curso.cohorte}/$idInstitucion")
                        },
                        onToggleHabilitado = {
                            if (esDirector) {
                                viewModel.toggleHabilitado(curso, idInstitucion)
                            }
                        },
                        showSwitch = esDirector
                    )
                }
            }

            // Espaciador inferior
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// Data class para los cursos (Asegurando que tenga documentoDocente)
data class Curso(
    val id: String,
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val claveAcceso: String,
    val habilitado: Boolean,
    val cantidadEstudiantes: Int = 0,
    val nombreDocente: String? = null,
    val documentoDocente: String? = null, // ID vital para filtrar
    val fotoDocente: String? = null
)

@Composable
fun EmptyStateCard(isSearching: Boolean, esDocente: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.School,
                contentDescription = null,
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Texto dinámico según la situación
            val mensajeTitulo = when {
                isSearching -> "No se encontraron cursos"
                esDocente -> "No tienes cursos asignados"
                else -> "No hay cursos registrados"
            }

            Text(
                text = mensajeTitulo,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Solo sugerimos crear si NO es docente y NO está buscando
            if (!esDocente && !isSearching) {
                Text(
                    text = "Crea tu primer curso para comenzar",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9E9E9E)),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}