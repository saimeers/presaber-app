package com.example.presaber.ui.institution

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.presaber.ui.theme.PresaberTheme
import androidx.compose.runtime.CompositionLocalProvider


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuestionScreen() {

    val navController = LocalNavController.current

    var enunciado by remember { mutableStateOf(TextFieldValue("")) }
    val opciones = remember { mutableStateListOf("Opcion 1", "Opcion 2", "Opcion 3", "Opcion 4") }
    val imagenes = remember { mutableStateListOf<String?>(null, null, null, null) }
    var opcionSeleccionada by remember { mutableStateOf<Int?>(null) }

    val showAccountDialog = remember { mutableStateOf(false) }

    InstitutionLayout(
        selectedNavItem = 2,
        onNavItemSelected = {},
        showAccountDialog = showAccountDialog
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 15.dp),
                 verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Crear Pregunta(s)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF485E92),
                textAlign = TextAlign.Center,
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
                imagenes = imagenes,
                opcionSeleccionada = opcionSeleccionada,
                onSelect = { opcionSeleccionada = it },
                onChange = { index, valor -> opciones[index] = valor },
                onUploadImage = { index ->
                    // Aquí lanza selector de imágenes
                }
            )


            // Botones superiores (Imagen, Nivel, Área, Tema)
            Buttons()

            Spacer(modifier = Modifier.height(5.dp))

            // Botones inferiores (Atrás, Nueva, Guardar)
            BottomButton(navController)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateQuestionScreenPreview() {
    val fakeNavController = rememberNavController()
    PresaberTheme{
        CompositionLocalProvider(LocalNavController provides fakeNavController) {
            CreateQuestionScreen()
        }
    }
}
