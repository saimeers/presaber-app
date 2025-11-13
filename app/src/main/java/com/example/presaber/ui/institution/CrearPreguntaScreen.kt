package com.example.presaber.ui.institution

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.*
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPreguntaScreen(navController: NavController) {
    var enunciado by remember { mutableStateOf(TextFieldValue("")) }
    var opciones = remember { mutableStateListOf("Opcion 1", "Opcion 2", "Opcion 3", "Opcion 4") }
    var opcionSeleccionada by remember { mutableStateOf<Int?>(null) }

    InstitutionLayout(
        selectedNavItem = 2,
        onNavItemSelected = {},
        showAccountDialog = remember { mutableStateOf(false) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crear Pregunta(s)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF485E92),
                textAlign = TextAlign.Center,
                fontSize = 6.em,
                modifier = Modifier.fillMaxWidth() .padding(bottom = 16.dp, top = 8.dp)
            )
            // Campo de enunciado
            QuestionStatement(
                enunciado = enunciado,
                onValueChange = { enunciado = it }
            )

            // Opciones de respuesta
            OptionsAnswer(
                opciones = opciones,
                onChange = { index, valor -> opciones[index] = valor },
                opcionSeleccionada = opcionSeleccionada,
                onSelect = { opcionSeleccionada = it }
            )

            // Botones superiores (Imagen, Nivel, Área, Tema)
            Buttons()

            Spacer(modifier = Modifier.height(8.dp))

            // Botones inferiores (Atrás, Nueva, Guardar)
            BottomButton(navController)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CrearPreguntaScreenPreview() {
    // Creamos un NavController falso solo para el preview
    val fakeNavController = rememberNavController()

    CrearPreguntaScreen(navController = fakeNavController)
}
