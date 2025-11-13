package com.example.presaber.ui.institution.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Buttons() {
    var nivelExpanded by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }

    // 🔽 Tema - autocompletado y agregar
    var temaText by remember { mutableStateOf("") }
    var temaExpanded by remember { mutableStateOf(false) }
    var showTemaInput by remember { mutableStateOf(false) }
    val temasDisponibles = remember {
        mutableStateListOf("Genética", "Ecosistemas", "Energía", "Materia", "Célula")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 📸 Primera fila: Imagen, Nivel, Área
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 📷 Botón de imagen
            ElevatedButton(onClick = { /* TODO: Acción subir imagen */ }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Subir imagen")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Imagen")
            }

            // ⬇️ SplitButton: Nivel
            Box {
                FilledTonalButton(onClick = { nivelExpanded = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Nivel")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Desplegar niveles",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = nivelExpanded,
                    onDismissRequest = { nivelExpanded = false }
                ) {
                    listOf("Bajo", "Medio", "Alto").forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { nivelExpanded = false })
                    }
                }
            }

            // ⬇️ SplitButton: Área
            Box {
                FilledTonalButton(onClick = { areaExpanded = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Área")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Desplegar áreas",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = areaExpanded,
                    onDismissRequest = { areaExpanded = false }
                ) {
                    listOf("Matemáticas", "Ciencias Naturales", "Lenguaje", "Sociales").forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { areaExpanded = false })
                    }
                }
            }
        }

        // 🎯 Selector de tema con búsqueda y botón ➕ en la misma fila
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = temaText,
                    onValueChange = {
                        temaText = it
                        temaExpanded = it.isNotEmpty()
                    },
                    label = { Text("Tema") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { temaExpanded = !temaExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Mostrar temas")
                        }
                    }
                )

                DropdownMenu(
                    expanded = temaExpanded,
                    onDismissRequest = { temaExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val resultados = temasDisponibles.filter {
                        it.contains(temaText, ignoreCase = true)
                    }

                    if (resultados.isNotEmpty()) {
                        resultados.forEach { tema ->
                            DropdownMenuItem(
                                text = { Text(tema) },
                                onClick = {
                                    temaText = tema
                                    temaExpanded = false
                                }
                            )
                        }
                    } else {
                        DropdownMenuItem(
                            text = { Text("➕ Agregar \"$temaText\"") },
                            onClick = {
                                if (temaText.isNotBlank()) {
                                    temasDisponibles.add(temaText)
                                    temaExpanded = false
                                }
                            }
                        )
                    }
                }
            }

            // ➕ Botón redondo justo al lado del selector
            FilledTonalIconButton(
                onClick = { showTemaInput = !showTemaInput },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tema")
            }
        }

        // 📝 Campo para ingresar nuevo tema (solo si se presiona ➕)
        if (showTemaInput) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = temaText,
                    onValueChange = { temaText = it },
                    label = { Text("Nuevo tema") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (temaText.isNotBlank()) {
                        temasDisponibles.add(temaText)
                        showTemaInput = false
                        temaText = ""
                    }
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}
