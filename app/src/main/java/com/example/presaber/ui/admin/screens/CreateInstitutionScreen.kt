package com.example.presaber.ui.admin.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.*
import com.example.presaber.ui.admin.viewmodel.InstitutionsViewModel
import com.example.presaber.ui.auth.components.DatePickerModalInput
import com.example.presaber.utils.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInstitutionScreen(
    onBack: () -> Unit,
    viewModel: InstitutionsViewModel = viewModel()
) {
    val context = LocalContext.current
    val loading by viewModel.loading.collectAsState()

    // Datos maestros
    val departamentos by viewModel.departamentos.collectAsState()
    val municipios by viewModel.municipios.collectAsState()
    val tiposDocumento by viewModel.tiposDocumento.collectAsState()

    // --- ESTADOS DEL FORMULARIO INSTITUCIÓN ---
    var nombreInst by remember { mutableStateOf("") }
    var direccionInst by remember { mutableStateOf("") }
    var correoInst by remember { mutableStateOf("") }
    var telefonoInst by remember { mutableStateOf("") }

    var selectedDept by remember { mutableStateOf<Departamento?>(null) }
    var selectedMuni by remember { mutableStateOf<Municipio?>(null) }

    var expandedDept by remember { mutableStateOf(false) }
    var expandedMuni by remember { mutableStateOf(false) }

    // --- ESTADOS DEL FORMULARIO DIRECTOR ---
    var docDirector by remember { mutableStateOf("") }
    var nombreDirector by remember { mutableStateOf("") }
    var apellidoDirector by remember { mutableStateOf("") }
    var correoDirector by remember { mutableStateOf("") }
    var telDirector by remember { mutableStateOf("") }
    var fechaNacDirector by remember { mutableStateOf("") }
    var selectedTipoDoc by remember { mutableStateOf<TipoDocumento?>(null) }

    var expandedTipoDoc by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarDatosMaestros()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Institución") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ================= DATOS INSTITUCIÓN =================
                SectionHeader("Datos de la Institución", Icons.Default.Business)

                CardForm {
                    // Nombre
                    OutlinedTextField(
                        value = nombreInst, onValueChange = { nombreInst = it },
                        label = { Text("Nombre Institución") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    // Departamento
                    ExposedDropdownMenuBox(
                        expanded = expandedDept,
                        onExpandedChange = { expandedDept = !expandedDept }
                    ) {
                        OutlinedTextField(
                            value = selectedDept?.nombre ?: "",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Departamento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDept) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDept,
                            onDismissRequest = { expandedDept = false }
                        ) {
                            departamentos.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept.nombre) },
                                    onClick = {
                                        selectedDept = dept
                                        selectedMuni = null // Resetear municipio
                                        viewModel.cargarMunicipios(dept.id)
                                        expandedDept = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Municipio
                    ExposedDropdownMenuBox(
                        expanded = expandedMuni,
                        onExpandedChange = { if(selectedDept != null) expandedMuni = !expandedMuni }
                    ) {
                        OutlinedTextField(
                            value = selectedMuni?.nombre ?: "",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Municipio") },
                            enabled = selectedDept != null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMuni) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            placeholder = { Text(if(selectedDept == null) "Seleccione departamento primero" else "") }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMuni,
                            onDismissRequest = { expandedMuni = false }
                        ) {
                            municipios.forEach { muni ->
                                DropdownMenuItem(
                                    text = { Text(muni.nombre) },
                                    onClick = { selectedMuni = muni; expandedMuni = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = direccionInst, onValueChange = { direccionInst = it },
                        label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = correoInst, onValueChange = { correoInst = it },
                        label = { Text("Correo Institucional") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = telefonoInst, onValueChange = { telefonoInst = it },
                        label = { Text("Teléfono Institución") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true
                    )
                }

                // ================= DATOS DIRECTOR =================
                SectionHeader("Datos del Director", Icons.Default.Person)

                CardForm {
                    // Tipo Documento
                    ExposedDropdownMenuBox(
                        expanded = expandedTipoDoc,
                        onExpandedChange = { expandedTipoDoc = !expandedTipoDoc }
                    ) {
                        OutlinedTextField(
                            value = selectedTipoDoc?.descripcion ?: "",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Tipo de Documento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTipoDoc) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipoDoc,
                            onDismissRequest = { expandedTipoDoc = false }
                        ) {
                            tiposDocumento.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo.descripcion) },
                                    onClick = { selectedTipoDoc = tipo; expandedTipoDoc = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = docDirector, onValueChange = { docDirector = it },
                        label = { Text("Documento") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombreDirector, onValueChange = { nombreDirector = it },
                        label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apellidoDirector, onValueChange = { apellidoDirector = it },
                        label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = correoDirector, onValueChange = { correoDirector = it },
                        label = { Text("Correo Director") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = correoDirector.isNotBlank() && !ValidationUtils.isValidEmail(correoDirector),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = telDirector, onValueChange = { telDirector = it },
                        label = { Text("Celular Director") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    // Fecha Nacimiento
                    Box {
                        OutlinedTextField(
                            value = fechaNacDirector, onValueChange = {}, readOnly = true,
                            label = { Text("Fecha Nacimiento") },
                            trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (showDatePicker) {
                            DatePickerModalInput(
                                onDateSelected = {
                                    fechaNacDirector = it?.let { ValidationUtils.convertMillisToDate(it) } ?: ""
                                    showDatePicker = false
                                },
                                onDismiss = { showDatePicker = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // BOTÓN CREAR
                Button(
                    onClick = {
                        if (validateFields(nombreInst, selectedDept, selectedMuni, correoInst, docDirector, correoDirector, selectedTipoDoc)) {
                            val request = CrearInstitucionRequest(
                                nombre = nombreInst,
                                departamento = selectedDept!!.nombre,
                                municipio = selectedMuni!!.nombre,
                                direccion = direccionInst,
                                correo = correoInst,
                                telefono = telefonoInst,
                                director_documento = docDirector,
                                director_nombre = nombreDirector,
                                director_apellido = apellidoDirector,
                                director_correo = correoDirector,
                                director_telefono = telDirector,
                                director_fecha_nacimiento = fechaNacDirector,
                                director_id_tipo_documento = selectedTipoDoc!!.id_tipo_documento
                            )

                            viewModel.crearInstitucion(
                                request = request,
                                onSuccess = {
                                    Toast.makeText(context, "Institución creada y correo enviado al director.", Toast.LENGTH_LONG).show()
                                    onBack()
                                },
                                onError = { errorMsg ->
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Por favor complete los campos obligatorios correctamente", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Registrar Institución", fontSize = 16.sp)
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF1976D2))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
    }
}

@Composable
fun CardForm(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

fun validateFields(
    nombreInst: String, dept: Departamento?, muni: Municipio?, correoInst: String,
    docDirector: String, correoDirector: String, tipoDoc: TipoDocumento?
): Boolean {
    return nombreInst.isNotBlank() && dept != null && muni != null &&
            correoInst.isNotBlank() && ValidationUtils.isValidEmail(correoInst) &&
            docDirector.isNotBlank() && correoDirector.isNotBlank() &&
            ValidationUtils.isValidEmail(correoDirector) && tipoDoc != null
}