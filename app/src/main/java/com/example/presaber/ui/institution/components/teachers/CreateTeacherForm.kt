package com.example.presaber.ui.institution.components.teachers

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.presaber.data.firebase.FirebaseAuthService
import com.example.presaber.data.remote.CrearDocenteRequest
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.TipoDocumento
import com.example.presaber.ui.auth.components.DatePickerModalInput
import com.example.presaber.utils.ValidationUtils
import com.example.presaber.utils.parseError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeacherForm(
    idInstitucion: String,
    onCancel: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var tiposDocumento by remember { mutableStateOf<List<TipoDocumento>>(emptyList()) }

    // Campos del formulario
    var selectedTipo by remember { mutableStateOf<TipoDocumento?>(null) }
    var documento by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }

    var expandedTipo by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Cargar tipos de documento
    LaunchedEffect(Unit) {
        try {
            tiposDocumento = RetrofitClient.api.getTiposDocumento()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar tipos de documento", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Registrar Nuevo Docente",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF1976D2)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tipo de documento
        ExposedDropdownMenuBox(
            expanded = expandedTipo,
            onExpandedChange = { expandedTipo = !expandedTipo }
        ) {
            OutlinedTextField(
                value = selectedTipo?.descripcion ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de documento") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTipo) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    focusedLabelColor = Color(0xFF1976D2)
                )
            )

            ExposedDropdownMenu(
                expanded = expandedTipo,
                onDismissRequest = { expandedTipo = false }
            ) {
                tiposDocumento.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.descripcion) },
                        onClick = {
                            selectedTipo = tipo
                            expandedTipo = false
                        }
                    )
                }
            }
        }

        // Documento
        OutlinedTextField(
            value = documento,
            onValueChange = { documento = it },
            label = { Text("Documento") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1976D2),
                focusedLabelColor = Color(0xFF1976D2)
            )
        )

        // Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1976D2),
                focusedLabelColor = Color(0xFF1976D2)
            )
        )

        // Apellido
        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1976D2),
                focusedLabelColor = Color(0xFF1976D2)
            )
        )

        // Correo
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico institucional") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth(),
            isError = correo.isNotBlank() && !ValidationUtils.isValidEmail(correo),
            supportingText = {
                if (correo.isNotBlank() && !ValidationUtils.isValidEmail(correo)) {
                    Text("Correo inválido")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1976D2),
                focusedLabelColor = Color(0xFF1976D2)
            )
        )

        // Teléfono
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1976D2),
                focusedLabelColor = Color(0xFF1976D2)
            )
        )

        // Fecha de nacimiento
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de nacimiento") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = !showDatePicker }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    focusedLabelColor = Color(0xFF1976D2)
                )
            )

            if (showDatePicker) {
                DatePickerModalInput(
                    onDateSelected = { millis ->
                        fechaNacimiento = millis?.let {
                            ValidationUtils.convertMillisToDate(it)
                        } ?: ""
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mensaje informativo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = "ℹ️ Se enviará un correo electrónico al docente para que establezca su contraseña.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                color = Color(0xFF1976D2)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botones
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1976D2)
                )
            ) {
                Text("Cancelar")
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF1976D2),
                    trackColor = Color(0xFF1976D2).copy(alpha = 0.2f)
                )
            } else {
                Button(
                    onClick = {
                        when {
                            selectedTipo == null -> {
                                Toast.makeText(
                                    context,
                                    "Selecciona un tipo de documento",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            documento.isBlank() -> {
                                Toast.makeText(
                                    context,
                                    "Ingresa el documento",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            nombre.isBlank() -> {
                                Toast.makeText(
                                    context,
                                    "Ingresa el nombre",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            apellido.isBlank() -> {
                                Toast.makeText(
                                    context,
                                    "Ingresa el apellido",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            correo.isBlank() -> {
                                Toast.makeText(
                                    context,
                                    "Ingresa el correo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            !ValidationUtils.isValidEmail(correo) -> {
                                Toast.makeText(
                                    context,
                                    "Correo electrónico inválido",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            telefono.isBlank() -> {
                                Toast.makeText(
                                    context,
                                    "Ingresa el teléfono",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            fechaNacimiento.isBlank() -> {
                                Toast.makeText(
                                    context,
                                    "Selecciona la fecha de nacimiento",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val request = CrearDocenteRequest(
                                            documento = documento,
                                            nombre = nombre,
                                            apellido = apellido,
                                            correo = correo,
                                            telefono = telefono,
                                            fecha_nacimiento = fechaNacimiento,
                                            id_tipo_documento = selectedTipo!!.id_tipo_documento.toString(),
                                            id_institucion = idInstitucion
                                        )

                                        val crearResponse = RetrofitClient.api.crearDocente(request)

                                        if (crearResponse.success) {

                                            val emailResult = FirebaseAuthService.sendPasswordResetEmailSimple(correo)

                                            emailResult.fold(
                                                onSuccess = {
                                                    Toast.makeText(
                                                        context,
                                                        "Docente registrado.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                },
                                                onFailure = { error ->
                                                    Toast.makeText(
                                                        context,
                                                        "Docente registrado, pero no se pudo enviar correo: ${error.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )

                                            onSuccess()
                                            onCancel()

                                        } else {
                                            // ERROR 200 pero success=false
                                            Toast.makeText(
                                                context,
                                                crearResponse.mensaje ?: "Error desconocido",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                    } catch (e: retrofit2.HttpException) {

                                        val body = e.response()?.errorBody()
                                        val error = parseError(body)

                                        Toast.makeText(
                                            context,
                                            error?.mensaje ?: "Error HTTP ${e.code()}",
                                            Toast.LENGTH_LONG
                                        ).show()

                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Error inesperado: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("Registrar Docente")
                }
            }
        }
    }
}