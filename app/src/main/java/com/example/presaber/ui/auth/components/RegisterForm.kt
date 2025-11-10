package com.example.presaber.ui.auth.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import com.example.presaber.ui.auth.components.DatePickerModalInput
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.presaber.data.remote.RegistroRequest
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.TipoDocumento
import com.example.presaber.data.remote.VerificarUsuarioRequest
import com.example.presaber.utils.ValidationUtils
import com.example.presaber.utils.PasswordStrength
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterForm(
    idInstitucion: String,
    grado: String,
    grupo: String,
    cohorte: String,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var paso by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }

    var tiposDocumento by remember { mutableStateOf<List<TipoDocumento>>(emptyList()) }
    var selectedTipo by remember { mutableStateOf<TipoDocumento?>(null) }
    var documento by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var expandedTipo by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var isVerifying by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var passwordValidation by remember { mutableStateOf(ValidationUtils.validatePassword("")) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            tiposDocumento = RetrofitClient.api.getTiposDocumento()
            isVerifying = true
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar tipos de documento", Toast.LENGTH_SHORT).show()
        } finally {
            isVerifying = false
        }
    }

    // Actualizar validación de contraseña
    LaunchedEffect(password) {
        passwordValidation = ValidationUtils.validatePassword(password)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

            when (paso) {
                1 -> {
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
                                    onClick = {
                                        selectedTipo = tipo
                                        expandedTipo = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

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

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
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

                    Spacer(Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text("Cancelar")
                        }
                        if (isVerifying) {
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
                                                "Ingresa tu documento",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        correo.isBlank() -> {
                                            Toast.makeText(
                                                context,
                                                "Ingresa tu correo",
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

                                        else -> {
                                            isVerifying = true
                                            scope.launch {
                                                try {
                                                    val request = VerificarUsuarioRequest(
                                                        documento = documento,
                                                        correo = correo,
                                                        id_tipo_documento = selectedTipo!!.id_tipo_documento
                                                    )
                                                    val resp =
                                                        RetrofitClient.api.verificarUsuario(request)
                                                    if (resp.existe) {
                                                        Toast.makeText(
                                                            context,
                                                            resp.mensaje,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Usuario válido, continúa",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        paso = 2
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "Error de conexión",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } finally {
                                                    isVerifying = false
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2)
                                )
                            ) {
                                Text("Verificar")
                            }
                        }
                    }
                }

                2 -> {
                    // PASO 2: Datos personales
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

                    Spacer(Modifier.height(12.dp))

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

                    Spacer(Modifier.height(12.dp))

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

                    Spacer(Modifier.height(12.dp))

                    // DatePicker
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
                                    fechaNacimiento = millis?.let { ValidationUtils.convertMillisToDate(it) } ?: ""
                                    showDatePicker = false
                                },
                                onDismiss = { showDatePicker = false }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { paso = 1 },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text("Atrás")
                        }

                        Button(
                            onClick = {
                                when {
                                    nombre.isBlank() -> Toast.makeText(context, "Ingresa tu nombre", Toast.LENGTH_SHORT).show()
                                    apellido.isBlank() -> Toast.makeText(context, "Ingresa tu apellido", Toast.LENGTH_SHORT).show()
                                    telefono.isBlank() -> Toast.makeText(context, "Ingresa tu teléfono", Toast.LENGTH_SHORT).show()
                                    fechaNacimiento.isBlank() -> Toast.makeText(context, "Selecciona tu fecha de nacimiento", Toast.LENGTH_SHORT).show()
                                    else -> paso = 3
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text("Continuar")
                        }
                    }
                }

                3 -> {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )

                    // Indicador de seguridad
                    if (password.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { when (passwordValidation.strength) {
                                    PasswordStrength.INVALID -> 0.25f
                                    PasswordStrength.WEAK -> 0.33f
                                    PasswordStrength.MEDIUM -> 0.66f
                                    PasswordStrength.STRONG -> 1f
                                } },
                                modifier = Modifier.weight(1f).height(4.dp),
                                color = when (passwordValidation.strength) {
                                    PasswordStrength.INVALID, PasswordStrength.WEAK -> Color.Red
                                    PasswordStrength.MEDIUM -> Color(0xFFFFA726)
                                    PasswordStrength.STRONG -> Color(0xFF66BB6A)
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = passwordValidation.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = when (passwordValidation.strength) {
                                    PasswordStrength.INVALID, PasswordStrength.WEAK -> Color.Red
                                    PasswordStrength.MEDIUM -> Color(0xFFFFA726)
                                    PasswordStrength.STRONG -> Color(0xFF66BB6A)
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmar,
                        onValueChange = { confirmar = it },
                        label = { Text("Confirmar contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        isError = confirmar.isNotBlank() && password != confirmar,
                        supportingText = {
                            if (confirmar.isNotBlank() && password != confirmar) {
                                Text("Las contraseñas no coinciden")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { paso = 2 },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text("Atrás")
                        }

                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color(0xFF1976D2),
                                trackColor = Color(0xFF1976D2).copy(alpha = 0.2f)
                            )
                        } else {
                        Button(
                            onClick = {
                                when {
                                    password.isBlank() -> {
                                        Toast.makeText(context, "Ingresa una contraseña", Toast.LENGTH_SHORT).show()
                                    }
                                    !passwordValidation.isValid -> {
                                        Toast.makeText(context, "La contraseña debe tener mínimo 8 caracteres, mayúsculas, minúsculas y números", Toast.LENGTH_LONG).show()
                                    }
                                    confirmar.isBlank() -> {
                                        Toast.makeText(context, "Confirma tu contraseña", Toast.LENGTH_SHORT).show()
                                    }
                                    password != confirmar -> {
                                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        isVerifying = true
                                        scope.launch {
                                            try {
                                                val nuevoUsuario = RegistroRequest(
                                                    documento = documento,
                                                    nombre = nombre,
                                                    apellido = apellido,
                                                    correo = correo,
                                                    telefono = telefono,
                                                    fecha_nacimiento = fechaNacimiento,
                                                    id_tipo_documento = selectedTipo!!.id_tipo_documento.toString(),
                                                    id_institucion = idInstitucion,
                                                    grado = grado,
                                                    grupo = grupo,
                                                    cohorte = cohorte,
                                                    password = password
                                                )

                                                RetrofitClient.api.registrarUsuario(nuevoUsuario)
                                                Toast.makeText(context, "Registro completado 🎉", Toast.LENGTH_LONG).show()
                                                onCancel()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isVerifying = false
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text("Registrarme")
                        }
                    }
                }
                    }
            }
        }
    }
