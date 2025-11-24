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
import com.example.presaber.ui.components.AddCard
import com.example.presaber.ui.institution.components.courses.CourseCard
import com.example.presaber.ui.institution.viewmodel.CoursesViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    var searchQuery by remember { mutableStateOf("") }

    // Filtrar cursos según la búsqueda
    val cursosFiltrados = remember(cursos, searchQuery, refreshTrigger) {
        if (searchQuery.isEmpty()) {
            cursos
        } else {
            cursos.filter { curso ->
                val query = searchQuery.lowercase()
                "Grupo ${curso.grado}-${curso.grupo}".lowercase().contains(query) ||
                        "grado ${curso.grado}".lowercase().contains(query) ||
                        "grupo ${curso.grupo}".lowercase().contains(query) ||
                        curso.cohorte.toString().contains(query) ||
                        curso.nombreDocente?.lowercase()?.contains(query) == true
            }
        }
    }

    LaunchedEffect(idInstitucion) {
        viewModel.setIdInstitucion(idInstitucion)
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
            // Buscador
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    placeholder = {
                        Text(
                            "Buscar..",
                            color = Color(0xFF9E9E9E),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color(0xFF5B7BC6)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = Color(0xFF9E9E9E)
                                )
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
                        text = "${cursosFiltrados.size} curso${if (cursosFiltrados.size != 1) "s" else ""} encontrado${if (cursosFiltrados.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
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
                        CircularProgressIndicator(color = Color(0xFF5B7BC6))
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
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error ?: "Error desconocido",
                                color = Color(0xFFC62828),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else if (cursosFiltrados.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isEmpty()) Icons.Default.School else Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Color(0xFFBDBDBD),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isEmpty())
                                    "No hay cursos registrados"
                                else
                                    "No se encontraron cursos",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFF666666),
                                    fontWeight = FontWeight.SemiBold
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isEmpty())
                                    "Crea tu primer curso para comenzar"
                                else
                                    "Intenta con otros términos de búsqueda",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF9E9E9E)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
                        onToggleHabilitado = {
                            viewModel.toggleHabilitado(curso, idInstitucion)
                        },
                        showSwitch = true // Muestra el switch porque es para institución
                    )
                }
            }

            // Espaciador inferior
            item { Spacer(modifier = Modifier.height(16.dp)) }
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