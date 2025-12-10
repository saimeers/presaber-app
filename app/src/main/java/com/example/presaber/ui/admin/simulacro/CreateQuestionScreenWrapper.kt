package com.example.presaber.ui.admin.simulacro

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.ui.institution.screens.CreateQuestionScreen
import com.example.presaber.ui.institution.viewmodel.CreateQuestionViewModel
import com.example.presaber.ui.institution.components.questions.LocalNavController

@Composable
fun CreateQuestionScreenWrapper(
    idArea: Int,
    idSimulacro: Int,
    numeroSesion: Int,
    onQuestionCreated: (com.example.presaber.data.remote.Pregunta) -> Unit
) {
    val viewModel: CreateQuestionViewModel = viewModel()
    val navController = LocalNavController.current

    // Observar cuando se crea una pregunta exitosamente
    val success by viewModel.success.collectAsState()
    
    LaunchedEffect(success) {
        if (success) {
            // Cuando se crea exitosamente, volver a la sesión
            // La pregunta recién creada estará disponible en el banco de preguntas
            navController.popBackStack()
        }
    }

    CreateQuestionScreen(viewModel = viewModel)
}

