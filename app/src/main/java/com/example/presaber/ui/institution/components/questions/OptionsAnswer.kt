package com.example.presaber.ui.institution.components.questions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.presaber.ui.theme.PresaberTheme

@Composable
fun OptionsAnswer(
    opciones: MutableList<String>,
    imagenes: MutableList<String?>,
    opcionSeleccionada: Int?,
    onSelect: (Int) -> Unit,
    onChange: (Int, String) -> Unit,
    onUploadImage: (Int) -> Unit,
    onAddOption: () -> Unit,
    onDeleteOption: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        opciones.forEachIndexed { index, texto ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (opcionSeleccionada == index)
                        Color(0xFFCFDCFF).copy(alpha = 0.15f)
                    else
                        Color.White
                ),
                onClick = { onSelect(index) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 🔘 Radio de selección
                    RadioButton(
                        selected = opcionSeleccionada == index,
                        onClick = { onSelect(index) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF5B7BC6)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // ✏️ Campo de texto editable con placeholder
                    TextField(
                        value = texto,
                        onValueChange = { onChange(index, it) },
                        placeholder = { Text("Opción ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 🖼️ Botón de imagen (se oscurece cuando hay imagen)
                    IconButton(
                        onClick = { onUploadImage(index) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (imagenes.getOrNull(index) != null)
                                Color(0xFF3D5A8F) // Color oscuro cuando hay imagen
                            else
                                Color(0xFFD2DEFF), // Color claro cuando no hay
                            contentColor = if (imagenes.getOrNull(index) != null)
                                Color.White
                            else
                                Color(0xFF3D5A8F)
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = if (imagenes.getOrNull(index) != null)
                                "Imagen cargada"
                            else
                                "Agregar imagen"
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delete button (solo si hay más de 2 opciones)
                    if (opciones.size > 2) {
                        IconButton(onClick = { onDeleteOption(index) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar opción")
                        }
                    }
                }
            }
        }
        OutlinedButton(
            onClick = { onAddOption() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar opción")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OptionsAnswerPreview() {
    val opciones = remember { mutableStateListOf("", "", "Opción 3", "Opción 4") }
    val imagenes = remember { mutableStateListOf<String?>(null, "imagen.jpg", null, null) }
    var seleccionada by remember { mutableStateOf<Int?>(1) }

    PresaberTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OptionsAnswer(
                opciones = opciones,
                imagenes = imagenes,
                opcionSeleccionada = seleccionada,
                onSelect = { seleccionada = it },
                onChange = { index, valor -> opciones[index] = valor },
                onUploadImage = { index ->
                    // Simular carga de imagen
                    imagenes[index] = if (imagenes[index] == null) "imagen_$index.jpg" else null
                },
                onAddOption = {},
                onDeleteOption = {}
            )
        }
    }
}