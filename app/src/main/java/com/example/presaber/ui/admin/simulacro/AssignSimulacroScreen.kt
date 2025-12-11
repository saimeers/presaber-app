package com.example.presaber.ui.admin.simulacro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.*
import com.example.presaber.ui.admin.viewmodel.SimulacroAdminViewModel

@Composable
fun AssignSimulacroScreen(
    idSimulacro: Int,
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: SimulacroAdminViewModel = viewModel()
) {
    val instituciones by viewModel.instituciones.collectAsState()
    val cursosPorInstitucion by viewModel.cursosPorInstitucion.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedInstituciones by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedCursos by remember { mutableStateOf<Map<Int, Set<String>>>(emptyMap()) }
    var expandedInstituciones by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showInstitucionDropdown by remember { mutableStateOf(false) }
    var showCursoDropdown by remember { mutableStateOf(false) }
    var selectedInstitucionText by remember { mutableStateOf("") }
    var selectedCursoText by remember { mutableStateOf("") }
    
    // Fechas para sesión 1
    var fechaAperturaS1 by remember { mutableStateOf("") }
    var fechaCierreS1 by remember { mutableStateOf("") }
    
    // Fechas para sesión 2
    var fechaAperturaS2 by remember { mutableStateOf("") }
    var fechaCierreS2 by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarInstituciones()
    }

    LaunchedEffect(selectedInstituciones) {
        selectedInstituciones.forEach { idInst ->
            if (!cursosPorInstitucion.containsKey(idInst)) {
                viewModel.cargarCursosPorInstitucion(idInst)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Título "Asignar simulacro" - Azul
        Text(
            text = "Asignar simulacro",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Campo Institución
            item {
                OutlinedTextField(
                    value = selectedInstitucionText,
                    onValueChange = { },
                    label = { Text("Institución") },
                    placeholder = { Text("Institución") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInstitucionDropdown = !showInstitucionDropdown },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showInstitucionDropdown = !showInstitucionDropdown }) {
                            Icon(
                                Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF757575)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                // Dropdown de instituciones
                if (showInstitucionDropdown) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Opción "Seleccionar todas"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedInstituciones.size == instituciones.size) {
                                            selectedInstituciones = emptySet()
                                            selectedCursos = emptyMap()
                                            selectedInstitucionText = ""
                                        } else {
                                            selectedInstituciones = instituciones.map { it.id_institucion }.toSet()
                                            selectedInstitucionText = "Todas las instituciones"
                                            instituciones.forEach { inst ->
                                                cursosPorInstitucion[inst.id_institucion]?.forEach { curso ->
                                                    selectedCursos = selectedCursos.toMutableMap().apply {
                                                        put(inst.id_institucion, (get(inst.id_institucion) ?: emptySet()) + "${curso.grado}${curso.grupo}${curso.cohorte}")
                                                    }
                                                }
                                            }
                                        }
                                        showInstitucionDropdown = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (selectedInstituciones.size == instituciones.size) Icons.Rounded.CheckBox
                                    else Icons.Rounded.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = Color(0xFF5685FF)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = if (selectedInstituciones.size == instituciones.size) "Deseleccionar todas" else "Seleccionar todas",
                                    fontSize = 14.sp,
                                    color = Color(0xFF5685FF)
                                )
                            }

                            instituciones.forEach { institucion ->
                                val isSelected = selectedInstituciones.contains(institucion.id_institucion)
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) {
                                                selectedInstituciones = selectedInstituciones - institucion.id_institucion
                                                selectedCursos = selectedCursos.toMutableMap().apply {
                                                    remove(institucion.id_institucion)
                                                }
                                            } else {
                                                selectedInstituciones = selectedInstituciones + institucion.id_institucion
                                            }
                                            selectedInstitucionText = when {
                                                selectedInstituciones.isEmpty() -> ""
                                                selectedInstituciones.size == 1 -> {
                                                    instituciones.find { it.id_institucion == selectedInstituciones.first() }?.nombre ?: ""
                                                }
                                                selectedInstituciones.size == instituciones.size -> "Todas las instituciones"
                                                else -> "${selectedInstituciones.size} instituciones seleccionadas"
                                            }
                                            selectedCursoText = updateCursoText(selectedCursos, cursosPorInstitucion, selectedInstituciones)
                                            showInstitucionDropdown = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (isSelected) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF4CAF50) else Color(0xFF757575)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = institucion.nombre,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1A1B21)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Campo Curso
            item {
                OutlinedTextField(
                    value = selectedCursoText,
                    onValueChange = { },
                    label = { Text("Curso") },
                    placeholder = { Text("Curso") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            if (selectedInstituciones.isNotEmpty()) {
                                showCursoDropdown = !showCursoDropdown
                            }
                        },
                    enabled = selectedInstituciones.isNotEmpty(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { 
                            if (selectedInstituciones.isNotEmpty()) {
                                showCursoDropdown = !showCursoDropdown
                            }
                        }) {
                            Icon(
                                Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF757575)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                // Dropdown de cursos (mostrar cursos de todas las instituciones seleccionadas)
                if (showCursoDropdown && selectedInstituciones.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            selectedInstituciones.forEach { idInst ->
                                val cursos = cursosPorInstitucion[idInst] ?: emptyList()
                                val cursosSeleccionados = selectedCursos[idInst] ?: emptySet()
                                
                                if (cursos.isNotEmpty()) {
                                    // Opción "Seleccionar todos los cursos" de esta institución
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (cursosSeleccionados.size == cursos.size) {
                                                    selectedCursos = selectedCursos.toMutableMap().apply {
                                                        put(idInst, emptySet())
                                                    }
                                                } else {
                                                    selectedCursos = selectedCursos.toMutableMap().apply {
                                                        put(idInst, cursos.map { "${it.grado}${it.grupo}${it.cohorte}" }.toSet())
                                                    }
                                                }
                                                    selectedCursoText = updateCursoText(selectedCursos, cursosPorInstitucion, selectedInstituciones)
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (cursosSeleccionados.size == cursos.size) Icons.Rounded.CheckBox
                                            else Icons.Rounded.CheckBoxOutlineBlank,
                                            contentDescription = null,
                                            tint = Color(0xFF5685FF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (cursosSeleccionados.size == cursos.size) "Deseleccionar todos" else "Seleccionar todos",
                                            fontSize = 12.sp,
                                            color = Color(0xFF5685FF)
                                        )
                                    }

                                    cursos.forEach { curso ->
                                        val cursoId = "${curso.grado}${curso.grupo}${curso.cohorte}"
                                        val cursoSeleccionado = cursosSeleccionados.contains(cursoId)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCursos = selectedCursos.toMutableMap().apply {
                                                        val cursosActuales = get(idInst) ?: emptySet()
                                                        put(
                                                            idInst,
                                                            if (cursoSeleccionado) {
                                                                cursosActuales - cursoId
                                                            } else {
                                                                cursosActuales + cursoId
                                                            }
                                                        )
                                                    }
                                                    selectedCursoText = updateCursoText(selectedCursos, cursosPorInstitucion, selectedInstituciones)
                                                }
                                                .padding(vertical = 8.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (cursoSeleccionado) Icons.Rounded.CheckBox
                                                else Icons.Rounded.CheckBoxOutlineBlank,
                                                contentDescription = null,
                                                tint = if (cursoSeleccionado) Color(0xFF4CAF50) else Color(0xFF757575),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "${curso.grado}${curso.grupo}",
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fechas Sesión 1
            item {
                Text(
                    text = "Sesión 1",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1B21),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = fechaAperturaS1,
                    onValueChange = { fechaAperturaS1 = it },
                    label = { Text("Fecha apertura sesión 1 (Fecha y hora)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = fechaCierreS1,
                    onValueChange = { fechaCierreS1 = it },
                    label = { Text("Fecha cierre sesión 1 (Fecha y hora)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // Fechas Sesión 2
            item {
                Text(
                    text = "Sesión 2",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1B21),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = fechaAperturaS2,
                    onValueChange = { fechaAperturaS2 = it },
                    label = { Text("Fecha apertura sesión 2 (Fecha y hora)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = fechaCierreS2,
                    onValueChange = { fechaCierreS2 = it },
                    label = { Text("Fecha cierre sesión 2 (Fecha y hora)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }
        }

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón Atrás - Azul sólido
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5685FF)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Atrás", fontSize = 16.sp, color = Color.White)
            }

            // Botón Guardar - Gris claro
            Button(
                onClick = {
                    // Preparar asignaciones
                    val asignaciones = mutableListOf<AsignacionCurso>()
                    
                    selectedCursos.forEach { (idInstitucion, cursosIds) ->
                        val cursos = cursosPorInstitucion[idInstitucion] ?: emptyList()
                        cursosIds.forEach { cursoId ->
                            val curso = cursos.find {
                                "${it.grado}${it.grupo}${it.cohorte}" == cursoId
                            }
                            
                            curso?.let {
                                asignaciones.add(
                                    AsignacionCurso(
                                        grado = it.grado,
                                        grupo = it.grupo,
                                        cohorte = it.cohorte,
                                        id_institucion = idInstitucion,
                                        fecha_apertura_s1 = fechaAperturaS1,
                                        fecha_cierre_s1 = fechaCierreS1,
                                        fecha_apertura_s2 = fechaAperturaS2,
                                        fecha_cierre_s2 = fechaCierreS2
                                    )
                                )
                            }
                        }
                    }
                    
                    if (asignaciones.isNotEmpty()) {
                        viewModel.asignarSimulacroACursos(
                            idSimulacro,
                            asignaciones,
                            onSuccess = onSave,
                            onError = { error ->
                                // Error ya está en el StateFlow
                            }
                        )
                    }
                },
                enabled = selectedCursos.isNotEmpty() && 
                         fechaAperturaS1.isNotBlank() && 
                         fechaCierreS1.isNotBlank() &&
                         fechaAperturaS2.isNotBlank() && 
                         fechaCierreS2.isNotBlank() &&
                         !loading,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E9E9E),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar", fontSize = 16.sp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun updateCursoText(
    selectedCursos: Map<Int, Set<String>>,
    cursosPorInstitucion: Map<Int, List<CursoResponse>>,
    selectedInstituciones: Set<Int>
): String {
    val totalCursos = selectedCursos.values.sumOf { it.size }
    val totalDisponibles = selectedInstituciones.sumOf { idInst ->
        cursosPorInstitucion[idInst]?.size ?: 0
    }
    
    return when {
        totalCursos == 0 -> ""
        totalCursos == totalDisponibles -> "Todos los cursos"
        totalCursos == 1 -> {
            // Encontrar el curso único
            selectedCursos.forEach { (idInst, cursosIds) ->
                cursosIds.forEach { cursoId ->
                    val curso = cursosPorInstitucion[idInst]?.find {
                        "${it.grado}${it.grupo}${it.cohorte}" == cursoId
                    }
                    curso?.let {
                        return "${it.grado}${it.grupo}"
                    }
                }
            }
            ""
        }
        else -> "$totalCursos cursos seleccionados"
    }
}
