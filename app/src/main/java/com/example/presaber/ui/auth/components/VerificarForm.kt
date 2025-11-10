package com.example.presaber.ui.auth.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

    LaunchedEffect(Unit) {
        try {
            instituciones = RetrofitClient.api.getInstituciones()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar instituciones", Toast.LENGTH_SHORT).show()
        }
    }

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
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(expanded = expandedInstitucion, onDismissRequest = { expandedInstitucion = false }) {
            instituciones.forEach { institucion ->
                DropdownMenuItem(
                    text = { Text(institucion.nombre) },
                    onClick = {
                        selectedInstitucion = institucion
                        expandedInstitucion = false
                        scope.launch {
                            try {
                                cursos = RetrofitClient.api.getCursos(institucion.id_institucion)
                            } catch (e: Exception) {
                                cursos = emptyList()
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
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(expanded = expandedCurso, onDismissRequest = { expandedCurso = false }) {
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
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = {
            selectedInstitucion?.let { institucion ->
                val (grado, grupo) = selectedCurso?.nombre?.chunked(2) ?: listOf("", "")
                val body = VerificacionRequest(
                    id_institucion = institucion.id_institucion.toString(),
                    grado = grado,
                    grupo = grupo,
                    cohorte = currentYear,
                    clave_acceso = clave
                )

                scope.launch {
                    try {
                        val resp = RetrofitClient.api.verificarCurso(body)
                        if (resp.valido) {
                            Toast.makeText(context, "Verificación exitosa 🎉", Toast.LENGTH_SHORT).show()
                            onVerificado(
                                institucion.id_institucion.toString(),
                                grado,
                                grupo,
                                currentYear
                            )
                        } else {
                            Toast.makeText(context, "Clave o curso inválido", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        Text("Verificar acceso")
    }
}
