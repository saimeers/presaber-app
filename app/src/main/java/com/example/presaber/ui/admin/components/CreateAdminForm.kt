package com.example.presaber.ui.admin.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.presaber.data.remote.CrearAdministradorRequest
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.TipoDocumento
import com.example.presaber.ui.auth.components.DatePickerModalInput
import com.example.presaber.utils.ValidationUtils
import com.example.presaber.utils.parseError
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdminForm(
    idInstitucion: Int,
    onCancel: () -> Unit,
    onSuccess: () -> Unit
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

    // Cargar tipos de documento al iniciar
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Registrar Administrador",
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
                modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                        onClick = { selectedTipo = tipo; expandedTipo = false }
                    )
                }
            }
        }

        // Documento
        OutlinedTextField(
            value = documento,
            onValueChange = { documento = it },
            label = { Text("Documento") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1976D2), focusedLabelColor = Color(0xFF1976D2))
        )

        // Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1976D2), focusedLabelColor = Color(0xFF1976D2))
        )

        // Apellido
        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1976D2), focusedLabelColor = Color(0xFF1976D2))
        )

        // Correo (Con validación visual)
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            isError = correo.isNotBlank() && !ValidationUtils.isValidEmail(correo),
            supportingText = {
                if (correo.isNotBlank() && !ValidationUtils.isValidEmail(correo)) {
                    Text("Correo inválido")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1976D2), focusedLabelColor = Color(0xFF1976D2))
        )

        // Teléfono
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1976D2), focusedLabelColor = Color(0xFF1976D2))
        )

        // Fecha Nacimiento
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de nacimiento") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1976D2), focusedLabelColor = Color(0xFF1976D2))
            )
            if (showDatePicker) {
                DatePickerModalInput(
                    onDateSelected = { millis ->
                        fechaNacimiento = millis?.let { ValidationUtils.convertMillisToDate(it) } ?: ""
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mensaje Informativo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Text(
                text = "ℹ️ Se enviará un correo al administrador para establecer su contraseña.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                color = Color(0xFF1565C0)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de Acción
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1976D2))
            ) {
                Text("Cancelar")
            }

            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1976D2)
                    )
                }
            } else {
                Button(
                    onClick = {
                        // VALIDACIONES
                        when {
                            selectedTipo == null -> Toast.makeText(context, "Selecciona un tipo de documento", Toast.LENGTH_SHORT).show()
                            documento.isBlank() -> Toast.makeText(context, "Ingresa el documento", Toast.LENGTH_SHORT).show()
                            nombre.isBlank() -> Toast.makeText(context, "Ingresa el nombre", Toast.LENGTH_SHORT).show()
                            apellido.isBlank() -> Toast.makeText(context, "Ingresa el apellido", Toast.LENGTH_SHORT).show()
                            correo.isBlank() -> Toast.makeText(context, "Ingresa el correo", Toast.LENGTH_SHORT).show()
                            !ValidationUtils.isValidEmail(correo) -> Toast.makeText(context, "Correo inválido", Toast.LENGTH_SHORT).show()
                            telefono.isBlank() -> Toast.makeText(context, "Ingresa el teléfono", Toast.LENGTH_SHORT).show()
                            fechaNacimiento.isBlank() -> Toast.makeText(context, "Selecciona la fecha de nacimiento", Toast.LENGTH_SHORT).show()
                            else -> {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val request = CrearAdministradorRequest(
                                            documento = documento,
                                            nombre = nombre,
                                            apellido = apellido,
                                            correo = correo,
                                            telefono = telefono,
                                            fecha_nacimiento = fechaNacimiento,
                                            id_tipo_documento = selectedTipo!!.id_tipo_documento,
                                            id_institucion = idInstitucion
                                        )

                                        val response = RetrofitClient.api.crearAdministrador(request)

                                        if (response.success) {
                                            // Enviar correo de reseteo de contraseña (Firebase)
                                            FirebaseAuthService.sendPasswordResetEmailSimple(correo)
                                                .onSuccess {
                                                    Toast.makeText(context, "Administrador creado. Correo enviado.", Toast.LENGTH_LONG).show()
                                                }
                                                .onFailure { error ->
                                                    Toast.makeText(context, "Creado, pero falló el envío de correo: ${error.message}", Toast.LENGTH_LONG).show()
                                                }

                                            onSuccess()
                                        } else {
                                            Toast.makeText(context, response.mensaje, Toast.LENGTH_LONG).show()
                                        }

                                    } catch (e: HttpException) {
                                        val errorMsg = parseError(e.response()?.errorBody())?.mensaje ?: "Error HTTP ${e.code()}"
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Registrar")
                }
            }
        }
    }
}