package com.example.presaber.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.presaber.data.remote.Reto
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.ResultadoData
import com.example.presaber.data.remote.Usuario
import com.example.presaber.ui.home.components.Quiz
import com.example.presaber.ui.home.components.RetoList
import com.example.presaber.ui.home.components.ResultQuiz
import com.example.presaber.ui.layout.StudentLayout

@Composable
fun HomeEstudiante(
    usuario: Usuario,
    onSignOut: () -> Unit
) {
    var selectedNavItem by remember { mutableStateOf(0) }
    val showAccountDialog = remember { mutableStateOf(false) }

    var selectedArea by remember { mutableStateOf<SubjectArea?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var retos by remember { mutableStateOf<List<Reto>>(emptyList()) }

    var currentReto by remember { mutableStateOf<Reto?>(null) }
    var resultadoFinal by remember { mutableStateOf<ResultadoData?>(null) }

    // Cargar retos cuando cambia el área
    LaunchedEffect(selectedArea) {
        if (selectedArea != null) {
            isLoading = true
            try {
                val response = RetrofitClient.api.getRetosPorArea(selectedArea!!.id)
                if (response.success) {
                    retos = response.data
                } else {
                    retos = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                retos = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    when {
        // Mostrar resultado en pantalla completa
        resultadoFinal != null -> {
            ResultQuiz(
                resultado = resultadoFinal!!,
                onAccept = {
                    resultadoFinal = null
                    currentReto = null
                    selectedArea = null
                }
            )
        }

        // Mostrar quiz en pantalla completa
        currentReto != null -> {
            Quiz(
                reto = currentReto!!,
                idEstudiante = usuario.documento,
                onFinish = { resultado ->
                    resultadoFinal = resultado
                },
                onExit = {
                    currentReto = null
                }
            )
        }

        // Mostrar home normal con layout
        else -> {
            StudentLayout(
                selectedNavItem = selectedNavItem,
                onNavItemSelected = { index -> selectedNavItem = index },
                showAccountDialog = showAccountDialog,
                usuario = usuario,
                onSignOut = onSignOut
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    if (selectedArea != null) {
                        RetoList(
                            area = selectedArea!!,
                            retos = retos,
                            isLoading = isLoading,
                            onBackClick = { selectedArea = null },
                            onStartReto = { reto ->
                                currentReto = reto
                            }
                        )
                    } else {
                        HomeContent(
                            onSubjectClick = { area -> selectedArea = area }
                        )
                    }
                }
            }
        }
    }
}