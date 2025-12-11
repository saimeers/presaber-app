package com.example.presaber.ui.admin.simulacro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.*
import com.example.presaber.ui.admin.viewmodel.SimulacroAdminViewModel
import com.example.presaber.ui.institution.viewmodel.QuestionsViewModel

@Composable
fun QuestionBankScreen(
    idArea: Int,
    idSimulacro: Int,
    numeroSesion: Int,
    onBack: () -> Unit,
    viewModel: SimulacroAdminViewModel = viewModel(),
    questionsViewModel: QuestionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val preguntas by questionsViewModel.preguntas.collectAsState()
    val loading by questionsViewModel.loading.collectAsState()
    val loadingAdd by viewModel.loading.collectAsState()

    LaunchedEffect(idArea) {
        questionsViewModel.cargarPreguntasPorArea(idArea)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Atrás")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Banco de Preguntas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(preguntas) { pregunta ->
                    QuestionBankItem(
                        pregunta = pregunta,
                        onSelect = {
                            viewModel.agregarPreguntaASesion(
                                idSimulacro = idSimulacro,
                                numeroSesion = numeroSesion,
                                idArea = idArea,
                                idPregunta = pregunta.id_pregunta,
                                puntajeBase = 0.5,
                                onSuccess = {
                                    onBack()
                                },
                                onError = { error ->
                                    // Error manejado por el ViewModel
                                }
                            )
                        },
                        isLoading = loadingAdd
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionBankItem(
    pregunta: Pregunta,
    onSelect: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pregunta.enunciado ?: "Sin enunciado",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Nivel: ${pregunta.nivel_dificultad ?: "N/A"}",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Seleccionar",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

