package com.example.presaber.ui.institution.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.viewmodel.QuestionsViewModel

@Composable
fun EditQuestionScreenWrapper(
    idPregunta: Int,
    viewModel: QuestionsViewModel = viewModel()
) {
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val pregunta by viewModel.pregunta.collectAsState()

    LaunchedEffect(idPregunta) {
        viewModel.obtenerPregunta(idPregunta)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.limpiarPregunta()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading && pregunta == null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Cargando pregunta...")
                }
            }

            error != null && pregunta == null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = { viewModel.obtenerPregunta(idPregunta) }) {
                        Text("Reintentar")
                    }
                }
            }

            pregunta != null -> {
                EditQuestionScreen(
                    pregunta = pregunta!!,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}