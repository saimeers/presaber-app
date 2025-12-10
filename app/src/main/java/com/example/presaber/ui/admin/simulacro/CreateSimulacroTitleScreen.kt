package com.example.presaber.ui.admin.simulacro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateSimulacroTitleScreen(
    onBack: () -> Unit,
    onSimulacroCreado: (Int) -> Unit,
    viewModel: SimulacroAdminViewModel = viewModel()
) {
    var titulo by remember { mutableStateOf("") }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Título "Crear Simulacro" - Azul oscuro, grande y bold
        Text(
            text = "Crear Simulacro",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Campo de texto con label flotante
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { 
                Text(
                    "Título",
                    color = Color(0xFF757575)
                ) 
            },
            placeholder = { 
                Text(
                    "Dale un título al simulacro",
                    color = Color(0xFFBDBDBD)
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1565C0),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Botones en la parte inferior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón Atrás - Azul sólido
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5685FF)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Atrás", fontSize = 16.sp, color = Color.White)
            }

            // Botón Guardar - Gris claro
            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        viewModel.crearSimulacro(
                            titulo,
                            onSuccess = { idSimulacro ->
                                onSimulacroCreado(idSimulacro)
                            },
                            onError = { error ->
                                // Error ya está en el StateFlow
                            }
                        )
                    }
                },
                enabled = titulo.isNotBlank() && !loading,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E9E9E),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar", fontSize = 16.sp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
