package com.example.presaber.ui.institution.components.questions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Buttons(
    areas: List<com.example.presaber.data.remote.Area>,
    temas: List<com.example.presaber.data.remote.Tema>,
    selectedAreaId: Int?,
    onSelectArea: (Int) -> Unit,
    selectedTemaId: Int?,
    onSelectTema: (Int) -> Unit,
    onCreateTema: (String, Int, (Boolean, String?) -> Unit) -> Unit,
    onPickQuestionImage: () -> Unit,
    onPickOptionImage: (Int) -> Unit,
    selectedNivel: String?,
    onSelectNivel: (String) -> Unit,
    hasQuestionImage: Boolean = false,
    optionImagesStatus: List<Boolean> = emptyList()
) {
    var nivelExpanded by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }
    var temaExpanded by remember { mutableStateOf(false) }
    var showTemaInput by remember { mutableStateOf(false) }

    // Sincronizar temaText cuando cambia selectedTemaId o temas
    var temaText by remember(selectedTemaId, temas.size) {
        mutableStateOf(temas.firstOrNull { it.id_tema == selectedTemaId }?.descripcion ?: "")
    }

    // 🎨 TU PALETA DE COLORES ORIGINAL
    val colorInactivo = Color(0xFFD2DEFF)
    val colorActivo = Color(0xFF5B7BC6)
    val colorActivoOscuro = Color(0xFF3D5A8F)
    val textoInactivo = Color(0xFF3D5A8F)
    val textoActivo = Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 📸 FILA 1: Imagen – Nivel – Área
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // 📷 Botón imagen con indicador visual
            Button(
                onClick = onPickQuestionImage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasQuestionImage) colorActivoOscuro else colorInactivo,
                    contentColor = if (hasQuestionImage) textoActivo else textoInactivo
                )
            ) {
                if (hasQuestionImage) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Imagen cargada")
                } else {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Subir imagen")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (hasQuestionImage) "✓ Cargada" else "Imagen")
            }

            // ⬇️ Nivel
            Box {
                Button(
                    onClick = { nivelExpanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedNivel != null) colorActivo else colorInactivo,
                        contentColor = if (selectedNivel != null) textoActivo else textoInactivo
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedNivel ?: "Nivel")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = nivelExpanded,
                    onDismissRequest = { nivelExpanded = false }
                ) {
                    listOf("Bajo", "Medio", "Alto").forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                nivelExpanded = false
                                onSelectNivel(it)
                            }
                        )
                    }
                }
            }

            // ⬇️ Área
            Box {
                Button(
                    onClick = { areaExpanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAreaId != null) colorActivo else colorInactivo,
                        contentColor = if (selectedAreaId != null) textoActivo else textoInactivo
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(areas.firstOrNull { it.id_area == selectedAreaId }?.nombre ?: "Área")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = areaExpanded,
                    onDismissRequest = { areaExpanded = false }
                ) {
                    areas.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area.nombre) },
                            onClick = {
                                areaExpanded = false
                                onSelectArea(area.id_area)
                            }
                        )
                    }
                }
            }
        }

        // 🎯 FILA 2: Selector tema + botón redondo azul
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
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
                    enabled = selectedAreaId != null, // Solo habilitado si hay área seleccionada
                    trailingIcon = {
                        IconButton(
                            onClick = { if (selectedAreaId != null) temaExpanded = !temaExpanded }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorActivo,
                        focusedLabelColor = colorActivo,
                        cursorColor = colorActivo,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray
                    )
                )

                DropdownMenu(
                    expanded = temaExpanded && selectedAreaId != null,
                    onDismissRequest = { temaExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val resultados = temas.filter {
                        it.descripcion.contains(temaText, ignoreCase = true)
                    }

                    if (resultados.isNotEmpty()) {
                        resultados.forEach { tema ->
                            DropdownMenuItem(
                                text = { Text(tema.descripcion) },
                                onClick = {
                                    temaText = tema.descripcion
                                    temaExpanded = false
                                    onSelectTema(tema.id_tema)
                                }
                            )
                        }
                    } else {
                        DropdownMenuItem(
                            text = { Text("➕ Agregar \"$temaText\"") },
                            onClick = {
                                if (temaText.isNotBlank() && selectedAreaId != null) {
                                    onCreateTema(temaText, selectedAreaId) { ok, _ ->
                                        if (ok) {
                                            temaExpanded = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // ➕ Botón redondo azul
            IconButton(
                onClick = {
                    if (selectedAreaId != null) {
                        showTemaInput = !showTemaInput
                    }
                },
                enabled = selectedAreaId != null,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (selectedAreaId != null) colorInactivo else Color.LightGray,
                    contentColor = if (selectedAreaId != null) textoInactivo else Color.Gray
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tema")
            }
        }

        // Mensaje si no hay área seleccionada
        if (selectedAreaId == null) {
            Text(
                text = "Selecciona un área primero para elegir tema",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Campo nuevo tema
        if (showTemaInput && selectedAreaId != null) {
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
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorActivo,
                        focusedLabelColor = colorActivo,
                        cursorColor = colorActivo
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (temaText.isNotBlank() && selectedAreaId != null) {
                            onCreateTema(temaText, selectedAreaId) { ok, _ ->
                                if (ok) {
                                    temaText = ""
                                    showTemaInput = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorActivo,
                        contentColor = textoActivo
                    )
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ButtonsPreview() {

    val areasFake = listOf(
        com.example.presaber.data.remote.Area(id_area = 1, nombre = "Ciencias Naturales"),
        com.example.presaber.data.remote.Area(id_area = 2, nombre = "Matemáticas"),
        com.example.presaber.data.remote.Area(id_area = 3, nombre = "Lenguaje")
    )

    val temasFake = listOf(
        com.example.presaber.data.remote.Tema(id_tema = 1, descripcion = "Genética"),
        com.example.presaber.data.remote.Tema(id_tema = 2, descripcion = "Ecosistemas"),
        com.example.presaber.data.remote.Tema(id_tema = 3, descripcion = "Química básica")
    )

    var selectedArea by remember { mutableStateOf<Int?>(null) }
    var selectedTema by remember { mutableStateOf<Int?>(null) }
    var selectedNivel by remember { mutableStateOf<String?>(null) }
    var hasImage by remember { mutableStateOf(false) }

    Column {
        Buttons(
            areas = areasFake,
            temas = temasFake,
            selectedAreaId = selectedArea,
            onSelectArea = { selectedArea = it },
            selectedTemaId = selectedTema,
            onSelectTema = { selectedTema = it },
            onCreateTema = { nuevoTema, areaId, callback ->
                callback(true, null)
            },
            onPickQuestionImage = { hasImage = !hasImage },
            onPickOptionImage = { },
            selectedNivel = selectedNivel,
            onSelectNivel = { selectedNivel = it },
            hasQuestionImage = hasImage,
            optionImagesStatus = listOf(true, false, false, false)
        )

    }
}