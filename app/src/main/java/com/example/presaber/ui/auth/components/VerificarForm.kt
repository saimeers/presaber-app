package com.example.presaber.ui.auth.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.presaber.data.remote.Curso
import com.example.presaber.data.remote.Institucion
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.VerificacionRequest
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificarForm(onVerificado: (String, String, String, String) -> Unit) {
    var instituciones by remember { mutableStateOf<List<Institucion>>(emptyList()) }
    var cursos by remember { mutableStateOf<List<Curso>>(emptyList()) }
    var selectedInstitucion by remember { mutableStateOf<Institucion?>(null) }
    var selectedCurso by remember { mutableStateOf<Curso?>(null) }
    var clave by remember { mutableStateOf("") }
    var expandedInstitucion by remember { mutableStateOf(false) }
    var expandedCurso by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

    LaunchedEffect(instituciones.isEmpty()) {
        if (instituciones.isEmpty()) {
            try {
                instituciones = RetrofitClient.api.getInstituciones()
                isVerifying = true
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Toast.makeText(context, "Error al cargar instituciones", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isVerifying = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            ExposedDropdownMenuBox(
                expanded = expandedInstitucion,
                onExpandedChange = { expandedInstitucion = !expandedInstitucion }
            ) {
                OutlinedTextField(
                    value = selectedInstitucion?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Institución") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedInstitucion) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        focusedLabelColor = Color(0xFF1976D2)
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedInstitucion,
                    onDismissRequest = { expandedInstitucion = false }
                ) {
                    instituciones.forEach { institucion ->
                        DropdownMenuItem(
                            text = { Text(institucion.nombre) },
                            onClick = {
                                selectedInstitucion = institucion
                                expandedInstitucion = false
                                selectedCurso = null
                                scope.launch {
                                    try {
                                        cursos = RetrofitClient.api.getCursos(institucion.id_institucion)
                                    } catch (e: Exception) {
                                        cursos = emptyList()
                                        Toast.makeText(context, "Error al cargar cursos", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (selectedInstitucion != null) {
                ExposedDropdownMenuBox(
                    expanded = expandedCurso,
                    onExpandedChange = { expandedCurso = !expandedCurso }
                ) {
                    OutlinedTextField(
                        value = selectedCurso?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Curso") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCurso) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCurso,
                        onDismissRequest = { expandedCurso = false }
                    ) {
                        cursos.forEach { curso ->
                            DropdownMenuItem(
                                text = { Text(curso.nombre) },
                                onClick = {
                                    selectedCurso = curso
                                    expandedCurso = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Clave de acceso") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    focusedLabelColor = Color(0xFF1976D2)
                )
            )

            Spacer(Modifier.height(24.dp))

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
                            selectedInstitucion == null -> {
                                Toast.makeText(context, "Selecciona una institución", Toast.LENGTH_SHORT).show()
                            }
                            selectedCurso == null -> {
                                Toast.makeText(context, "Selecciona un curso", Toast.LENGTH_SHORT).show()
                            }
                            clave.isBlank() -> {
                                Toast.makeText(context, "Ingresa la clave de acceso", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                isVerifying = true
                                val (grado, grupo) = selectedCurso!!.nombre.chunked(2)
                                val body = VerificacionRequest(
                                    id_institucion = selectedInstitucion!!.id_institucion.toString(),
                                    grado = grado,
                                    grupo = grupo,
                                    cohorte = currentYear,
                                    clave_acceso = clave
                                )

                                scope.launch {
                                    try {
                                        val resp = RetrofitClient.api.verificarCurso(body)
                                        if (resp.valido) {
                                            Toast.makeText(
                                                context,
                                                "Verificación exitosa 🎉",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onVerificado(
                                                selectedInstitucion!!.id_institucion.toString(),
                                                grado,
                                                grupo,
                                                currentYear
                                            )
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Clave o curso inválido",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Verificar")
                }
            }
        }
    }
