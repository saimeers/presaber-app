package com.example.presaber.ui.institution.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.presaber.ui.theme.PresaberTheme

@Composable
fun OptionsAnswer(
    opciones: MutableList<String>,
    onChange: (Int, String) -> Unit,
    opcionSeleccionada: Int?,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEachIndexed { index, texto ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { onSelect(index) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    RadioButton(
                        selected = opcionSeleccionada == index,
                        onClick = { onSelect(index) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = texto,
                        onValueChange = { onChange(index, it) },
                        modifier = Modifier.fillMaxWidth(),
                        colors= TextFieldDefaults.colors(
                            // Hacemos que la línea inferior (indicador) sea transparente en todos los estados
                            focusedIndicatorColor = Color.Transparent,    // cuando el campo de texto está enfocado
                            unfocusedIndicatorColor = Color.Transparent,  // cuando no está enfocado
                            disabledIndicatorColor = Color.Transparent,   // cuando está deshabilitado
                            errorIndicatorColor = Color.Transparent,      // cuando hay un error

                            // Opcional: Si también quieres quitar el color de fondo
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent
                        )
                        )
                }
            }
        }
    }
}

@Preview
@Composable

fun OptionsAnswerPreview(){
    PresaberTheme{
        var opciones = remember { mutableStateListOf("Opción 1", "Opción 2", "Opción 3", "Opción 4") }
        var opcionSeleccionada by remember { mutableStateOf<Int?>(null) }

        OptionsAnswer(
            opciones = opciones,
            onChange = { index, valor -> opciones[index] = valor },
            opcionSeleccionada = opcionSeleccionada,
            onSelect = { opcionSeleccionada = it }
        )
    }
}
