package com.example.presaber.ui.institution.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.R
import com.example.presaber.data.remote.Pregunta
import com.example.presaber.ui.institution.components.*
import com.example.presaber.ui.theme.PresaberTheme
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.questions.LocalNavController
import com.example.presaber.ui.institution.components.questions.QuestionItem
import com.example.presaber.ui.institution.components.questions.QuestionsHeader
import com.example.presaber.ui.institution.components.questions.QuestionsList
import com.example.presaber.ui.institution.viewmodel.QuestionsViewModel

@Composable
fun QuestionsScreen(
    idArea: Int,
    areaName: String,
    areaIcon: Int,
    viewModel: QuestionsViewModel = viewModel(),
) {
    val navController = LocalNavController.current
    // Observar el flujo de preguntas y loading
    val preguntas by viewModel.preguntas.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var filteredQuestions by remember { mutableStateOf<List<Pregunta>>(emptyList()) }

    // Cargar preguntas al entrar
    LaunchedEffect(idArea) {
        viewModel.cargarPreguntasPorArea(idArea)
    }

    // Actualizar la lista cuando cambian las preguntas
    LaunchedEffect(preguntas) {
        filteredQuestions = preguntas
    }

    // Layout principal

        Column(modifier = Modifier) {

            // Encabezado del área
            AreaHeader(nombreArea = areaName, icono = areaIcon)

            // Barra de búsqueda
            QuestionsHeader(
                onSearch = { query ->
                    filteredQuestions = if (query.isBlank()) {
                        preguntas
                    } else {
                        preguntas.filter {
                            it.enunciado?.contains(query, ignoreCase = true) == true
                        }
                    }
                },
                onFilterClick = { println("Filtro presionado") }
            )

            // Mostrar indicador de carga
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                //  Mostrar lista de preguntas
                QuestionsList(
                    questions = filteredQuestions.map {
                        QuestionItem(
                            id = it.id_pregunta,
                            title = it.enunciado ?: "(Sin enunciado)",
                            tema = "Tema: ${it.tema?.descripcion ?: "Sin tema"} ",
                            nivel = it.nivel_dificultad ?: "Sin nivel",
                            imagen = it.imagen ?: ""
                        )
                    },
                    selectedId = null,
                    onSelect = { idPregunta ->
                        navController.navigate("editQuestion/$idPregunta")
                    }
                )
            }
        }
    }


@Preview(showBackground = true)
@Composable
fun PreviewQuestionsScreen() {
    PresaberTheme {
        QuestionsScreen(
            idArea = 2,
            areaName = "Matemáticas",
            areaIcon = R.drawable.img_matematicas
        )
    }
}
