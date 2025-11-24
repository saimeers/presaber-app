package com.example.presaber.ui.institution.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.presaber.data.remote.TeacherResponse
import com.example.presaber.ui.institution.components.CreateCourseHeader
import com.example.presaber.ui.institution.viewmodel.CoursesViewModel
import androidx.compose.foundation.layout.padding
import com.example.presaber.ui.institution.components.ErrorDialog
import com.example.presaber.ui.institution.components.SuccessDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    navController: NavController,
    idInstitucion: Int,
    coursesViewModel: CoursesViewModel = viewModel()
) {
    // Estados del formulario
    var grado by remember { mutableStateOf("") }
    var grupo by remember { mutableStateOf("") }
    var cohorte by remember { mutableStateOf("") }
    var claveAcceso by remember { mutableStateOf("") }
    var docenteSeleccionado by remember { mutableStateOf<TeacherResponse?>(null) }

    var expandedDocente by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) } // Nuevo estado

    val showAccountDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados del ViewModel
    val loading by coursesViewModel.loading.collectAsState()

    // Estado para docentes del API
    var docentesList by remember { mutableStateOf<List<TeacherResponse>>(emptyList()) }
    var loadingDocentes by remember { mutableStateOf(false) }

    // Cargar docentes desde el API
    LaunchedEffect(idInstitucion) {
        loadingDocentes = true
        try {
            docentesList = com.example.presaber.data.remote.RetrofitClient.api.getDocentes(idInstitucion)
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Error al cargar docentes: ${e.message}"
            showErrorDialog = true
        } finally {
            loadingDocentes = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 10.dp,
                        end = 20.dp,
                        top = 16.dp,
                        bottom = 24.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imagen/Ilustración "Nuevo curso"
                CreateCourseHeader()

                // Campo Grado
                OutlinedTextField(
                    value = grado,
                    onValueChange = { if (it.length <= 2) grado = it },
                    label = {
                        Text(
                            "Grado",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF5B7BC6)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B7BC6),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = Color(0xFF5B7BC6),
                        unfocusedLabelColor = Color(0xFF9E9E9E),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color.White
                    )
                )

                // Campo Grupo
                OutlinedTextField(
                    value = grupo,
                    onValueChange = { if (it.length <= 2) grupo = it },
                    label = {
                        Text(
                            "Grupo",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF5B7BC6)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B7BC6),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = Color(0xFF5B7BC6),
                        unfocusedLabelColor = Color(0xFF9E9E9E),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color.White
                    )
                )

                // Campo Cohorte
                OutlinedTextField(
                    value = cohorte,
                    onValueChange = { if (it.length <= 4) cohorte = it },
                    label = {
                        Text(
                            "Cohorte",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF5B7BC6)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B7BC6),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = Color(0xFF5B7BC6),
                        unfocusedLabelColor = Color(0xFF9E9E9E),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color.White
                    )
                )

                // Campo Clave de acceso
                OutlinedTextField(
                    value = claveAcceso,
                    onValueChange = { if (it.length <= 20) claveAcceso = it },
                    label = {
                        Text(
                            "Clave de acceso",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF5B7BC6)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B7BC6),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = Color(0xFF5B7BC6),
                        unfocusedLabelColor = Color(0xFF9E9E9E),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color.White
                    )
                )

                // Campo Docente (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedDocente,
                    onExpandedChange = { expandedDocente = !expandedDocente }
                ) {
                    OutlinedTextField(
                        value = docenteSeleccionado?.let { "${it.nombre} ${it.apellido}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                "Docente",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF5B7BC6)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF9E9E9E)
                            )
                        },
                        placeholder = {
                            Text(
                                "Seleccionar docente (opcional)",
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5B7BC6),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = Color(0xFF5B7BC6),
                            unfocusedLabelColor = Color(0xFF9E9E9E),
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDocente,
                        onDismissRequest = { expandedDocente = false }
                    ) {
                        if (loadingDocentes) {
                            DropdownMenuItem(
                                text = { Text("Cargando...") },
                                onClick = {}
                            )
                        } else {
                            // Opción "Sin docente"
                            DropdownMenuItem(
                                text = { Text("Sin docente") },
                                onClick = {
                                    docenteSeleccionado = null
                                    expandedDocente = false
                                }
                            )

                            // Lista de docentes
                            docentesList.forEach { docente ->
                                DropdownMenuItem(
                                    text = { Text("${docente.nombre} ${docente.apellido}") },
                                    onClick = {
                                        docenteSeleccionado = docente
                                        expandedDocente = false
                                    }
                                )
                            }

                            if (docentesList.isEmpty() && !loadingDocentes) {
                                DropdownMenuItem(
                                    text = { Text("No hay docentes disponibles") },
                                    onClick = {}
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botones inferiores
                Row(
                    modifier = Modifier
                        .width(280.dp)
                        .padding(top = 5.dp, bottom = 0.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Botón Atrás
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFFB0C6FF)
                        ),
                        border = BorderStroke(2.dp, Color(0xFFB0C6FF))
                    ) {
                        Text(
                            "Atrás",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    // Botón Registrar
                    Button(
                        onClick = {
                            // Validaciones
                            when {
                                grado.isBlank() -> {
                                    errorMessage = "El grado es obligatorio"
                                    showErrorDialog = true
                                }
                                grupo.isBlank() -> {
                                    errorMessage = "El grupo es obligatorio"
                                    showErrorDialog = true
                                }
                                cohorte.isBlank() -> {
                                    errorMessage = "La cohorte es obligatoria"
                                    showErrorDialog = true
                                }
                                claveAcceso.isBlank() -> {
                                    errorMessage = "La clave de acceso es obligatoria"
                                    showErrorDialog = true
                                }
                                else -> {
                                    val cohorteInt = cohorte.toIntOrNull()
                                    if (cohorteInt == null) {
                                        errorMessage = "La cohorte debe ser un número válido"
                                        showErrorDialog = true
                                    } else {
                                        // Obtener ID del docente si está seleccionado
                                        val idDocente = docenteSeleccionado?.documento

                                        // Crear curso
                                        coursesViewModel.crearCurso(
                                            grado = grado,
                                            grupo = grupo,
                                            cohorte = cohorteInt,
                                            claveAcceso = claveAcceso,
                                            idInstitucion = idInstitucion,
                                            idDocente = idDocente
                                        ) { success, message ->
                                            if (success) {
                                                // Mostrar diálogo de éxito
                                                showSuccessDialog = true
                                                // Recargar cursos
                                                coursesViewModel.cargarCursos(idInstitucion)
                                            } else {
                                                errorMessage = message ?: "Error al crear el curso"
                                                showErrorDialog = true
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB0C6FF),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB0C6FF),
                            disabledContentColor = Color.White
                        ),
                        enabled = !loading,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Registrar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Snackbar para mensajes
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )

        // Diálogo de éxito
        if (showSuccessDialog) {
            SuccessDialog(
                title = "¡Curso creado!",
                message = "El curso se ha registrado exitosamente.",
                onDismiss = {
                    showSuccessDialog = false
                    navController.popBackStack()
                }
            )
        }

        // Diálogo de error
        if (showErrorDialog) {
            ErrorDialog(
                errorMessage = errorMessage,
                onDismiss = {
                    showErrorDialog = false
                    errorMessage = ""
                }
            )
        }
    }
}