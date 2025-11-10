package com.example.presaber.ui.auth.components

import android.widget.Toast
import com.google.gson.Gson
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.presaber.data.remote.RegistroRequest
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.TipoDocumento
import com.example.presaber.data.remote.VerificarUsuarioRequest
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

    var tiposDocumento by remember { mutableStateOf<List<TipoDocumento>>(emptyList()) }
    var selectedTipo by remember { mutableStateOf<TipoDocumento?>(null) }
    var documento by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var expandedTipo by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            tiposDocumento = RetrofitClient.api.getTiposDocumento()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar tipos de documento", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {

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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
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
                OutlinedTextField(value = documento, onValueChange = { documento = it }, label = { Text("Documento") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onCancel) { Text("Cancelar") }
                    Button(onClick = {
                        if (selectedTipo == null || documento.isBlank() || correo.isBlank()) {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            try {
                                val request = VerificarUsuarioRequest(
                                    documento = documento,
                                    correo = correo,
                                    id_tipo_documento = selectedTipo!!.id_tipo_documento
                                )
                                val resp = RetrofitClient.api.verificarUsuario(request)
                                if (resp.existe) {
                                    Toast.makeText(context, resp.mensaje, Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Usuario válido, continúa", Toast.LENGTH_SHORT).show()
                                    paso = 2
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Verificar")
                    }
                }
            }

            2 -> {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = fechaNacimiento, onValueChange = { fechaNacimiento = it }, label = { Text("Fecha de nacimiento (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                Button(onClick = { paso = 3 }, modifier = Modifier.fillMaxWidth(0.7f)) { Text("Continuar") }
            }

            3 -> {
                var jsonPreview by remember { mutableStateOf<String?>(null) }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmar,
                    onValueChange = { confirmar = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (password != confirmar) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
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

                                // Convertimos el objeto a JSON
                                val json = com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(nuevoUsuario)

                                // Lo mostramos en pantalla
                                jsonPreview = json

                                // También lo imprimimos en Logcat por si deseas verlo allí
                                android.util.Log.d("JSON_ENVIADO", json)

                                // Llamada real al backend
                                RetrofitClient.api.registrarUsuario(nuevoUsuario)
                                Toast.makeText(context, "Registro completado 🎉", Toast.LENGTH_LONG).show()
                                onCancel()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al registrar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Registrar usuario")
                }

                // 🔍 Muestra el JSON debajo del botón
                jsonPreview?.let { json ->
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = json,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
