package com.example.presaber.ui.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.Departamento
import com.example.presaber.data.remote.InstitucionCompleta
import com.example.presaber.data.remote.Municipio
import com.example.presaber.ui.admin.viewmodel.InstitutionsViewModel
import com.example.presaber.ui.admin.components.AddCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionsListScreen(
    onAddInstitution: () -> Unit,
    onInstitutionClick: (Int) -> Unit, // Callback para navegar al detalle
    viewModel: InstitutionsViewModel = viewModel()
) {
    val instituciones by viewModel.instituciones.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Datos maestros para el filtro
    val departamentos by viewModel.departamentos.collectAsState()
    val municipios by viewModel.municipios.collectAsState()

    // --- ESTADOS DE FILTRO ---
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filtros aplicados
    var selectedDept by remember { mutableStateOf<Departamento?>(null) }
    var selectedMunicipios by remember { mutableStateOf<List<Municipio>>(emptyList()) }

    // --- LÓGICA DE FILTRADO ---
    val listaFiltrada = remember(instituciones, searchQuery, selectedDept, selectedMunicipios) {
        instituciones.filter { inst ->
            // 1. Filtro Texto (Nombre)
            val matchText = searchQuery.isEmpty() || inst.nombre.contains(searchQuery, ignoreCase = true)

            // 2. Filtro Departamento
            val matchDept = selectedDept == null || inst.departamento.equals(selectedDept!!.nombre, ignoreCase = true)

            // 3. Filtro Municipio (Múltiples)
            val matchMuni = selectedMunicipios.isEmpty() || selectedMunicipios.any {
                it.nombre.equals(inst.municipio, ignoreCase = true)
            }

            matchText && matchDept && matchMuni
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarInstituciones()
        viewModel.cargarDatosMaestros() // Asegurarnos de tener departamentos
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER Y BUSCADOR ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text("Instituciones", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1A1B21))
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Barra de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar nombre...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF5B7BC6)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5B7BC6),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.width(8.dp))

                    // Botón de Filtros
                    val isFilterActive = selectedDept != null
                    FilledTonalIconButton(
                        onClick = { showFilterDialog = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isFilterActive) Color(0xFF5B7BC6) else Color(0xFFF0F4FF),
                            contentColor = if (isFilterActive) Color.White else Color(0xFF5B7BC6)
                        )
                    ) {
                        Icon(Icons.Rounded.FilterList, contentDescription = "Filtros")
                    }
                }

                // Mostrar chips de filtros activos
                if (selectedDept != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = {
                                selectedDept = null
                                selectedMunicipios = emptyList() // Limpiar municipios al quitar depto
                            },
                            label = { Text(selectedDept!!.nombre) },
                            trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                        )
                        if (selectedMunicipios.isNotEmpty()) {
                            Text(
                                "+ ${selectedMunicipios.size} municipios",
                                fontSize = 12.sp, color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }

            // --- LISTA ---
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    AddCard(text = "Registrar nueva institución", onClick = onAddInstitution)
                }

                if (loading) {
                    item { Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF5B7BC6)) } }
                } else if (listaFiltrada.isEmpty()) {
                    item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text("No se encontraron instituciones", color = Color.Gray) } }
                } else {
                    items(listaFiltrada) { inst ->
                        InstitucionCard(
                            institucion = inst,
                            onClick = { onInstitutionClick(inst.id_institucion) }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGO DE FILTROS ---
    if (showFilterDialog) {
        FilterInstitutionDialog(
            departamentos = departamentos,
            municipios = municipios,
            currentDept = selectedDept,
            currentMunis = selectedMunicipios,
            onDismiss = { showFilterDialog = false },
            onApply = { dept, munis ->
                selectedDept = dept
                selectedMunicipios = munis
                showFilterDialog = false
            },
            onDeptSelected = { dept ->
                viewModel.cargarMunicipios(dept.id)
            }
        )
    }
}

// Dialogo Avanzado de Filtros
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterInstitutionDialog(
    departamentos: List<Departamento>,
    municipios: List<Municipio>,
    currentDept: Departamento?,
    currentMunis: List<Municipio>,
    onDismiss: () -> Unit,
    onApply: (Departamento?, List<Municipio>) -> Unit,
    onDeptSelected: (Departamento) -> Unit // Para cargar municipios
) {
    // Estados locales temporales para el dialogo
    var tempDept by remember { mutableStateOf(currentDept) }
    var tempMunis by remember { mutableStateOf(currentMunis) }
    var deptExpanded by remember { mutableStateOf(false) }

    // Cargar municipios si ya hay depto seleccionado al abrir
    LaunchedEffect(currentDept) {
        if (currentDept != null && municipios.isEmpty()) {
            onDeptSelected(currentDept)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Filtrar Instituciones") },
        text = {
            Column {
                // Selector Departamento
                Text("Departamento", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B7BC6))
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = deptExpanded,
                    onExpandedChange = { deptExpanded = !deptExpanded }
                ) {
                    OutlinedTextField(
                        value = tempDept?.nombre ?: "Todos",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5B7BC6),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                tempDept = null
                                tempMunis = emptyList()
                                deptExpanded = false
                            }
                        )
                        departamentos.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept.nombre) },
                                onClick = {
                                    tempDept = dept
                                    tempMunis = emptyList() // Reset municipios al cambiar depto
                                    onDeptSelected(dept) // Cargar municipios
                                    deptExpanded = false
                                }
                            )
                        }
                    }
                }

                // Selector Municipios (Multi-Select)
                if (tempDept != null) {
                    Spacer(Modifier.height(16.dp))
                    Text("Municipios (Selección múltiple)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B7BC6))
                    Spacer(Modifier.height(8.dp))

                    if (municipios.isEmpty()) {
                        Text("Cargando municipios...", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        // FlowRow para chips múltiples
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            municipios.forEach { muni ->
                                val isSelected = tempMunis.any { it.id == muni.id }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        tempMunis = if (isSelected) {
                                            tempMunis.filter { it.id != muni.id }
                                        } else {
                                            tempMunis + muni
                                        }
                                    },
                                    label = { Text(muni.nombre) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE3F2FD),
                                        selectedLabelColor = Color(0xFF1976D2)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(tempDept, tempMunis) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B7BC6))
            ) {
                Text("Aplicar Filtros")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        }
    )
}

@Composable
fun InstitucionCard(
    institucion: InstitucionCompleta,
    onClick: () -> Unit // Nuevo parámetro de click
) {
    Card(
        onClick = onClick, // Habilitar click
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.LocationCity, null, tint = Color(0xFF1976D2))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = institucion.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1B21),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${institucion.municipio}, ${institucion.departamento}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(12.dp))

            // Info Director
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Director: ${institucion.director?.nombre_completo ?: "Sin asignar"}",
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = institucion.correo,
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
            }
        }
    }
}