package com.example.presaber.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.presaber.data.remote.*
import com.example.presaber.ui.home.components.Quiz
import com.example.presaber.ui.home.components.RetoList
import com.example.presaber.ui.home.components.ResultQuiz
import com.example.presaber.ui.layout.StudentLayout
import com.example.presaber.ui.pvp.*

// Estados de navegación PvP
sealed class PvPState {
    object Home : PvPState()
    object CrearSala : PvPState()
    data class UnirseSala(val codigoCompartido: String? = null) : PvPState()
    data class SalaEspera(val idSala: Int) : PvPState()
    data class QuizPvP(val idSala: Int) : PvPState()
    data class ResultadoPvP(val idSala: Int) : PvPState()
}

@Composable
fun HomeEstudiante(
    usuario: Usuario,
    codigoSalaCompartido: String? = null, // Para deep linking
    onSignOut: () -> Unit
) {
    var selectedNavItem by remember { mutableStateOf(0) }
    val showAccountDialog = remember { mutableStateOf(false) }

    // Estados para Retos normales
    var selectedArea by remember { mutableStateOf<SubjectArea?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var retos by remember { mutableStateOf<List<Reto>>(emptyList()) }
    var currentReto by remember { mutableStateOf<Reto?>(null) }
    var resultadoFinal by remember { mutableStateOf<ResultadoData?>(null) }

    // Estados para PvP
    var pvpState by remember { mutableStateOf<PvPState>(
        if (codigoSalaCompartido != null)
            PvPState.UnirseSala(codigoSalaCompartido)
        else
            PvPState.Home
    ) }
    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }

    // Cargar áreas para PvP
    LaunchedEffect(Unit) {
        try {
            areas = RetrofitClient.api.getAreas()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Cargar retos cuando cambia el área (modo normal)
    LaunchedEffect(selectedArea) {
        if (selectedArea != null && selectedNavItem == 0) {
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
        // Mostrar resultado reto normal en pantalla completa
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

        // Mostrar quiz normal en pantalla completa
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

        // Pantallas PvP (selectedNavItem == 2)
        selectedNavItem == 2 -> {
            when (val state = pvpState) {
                is PvPState.Home -> {
                    StudentLayout(
                        selectedNavItem = selectedNavItem,
                        onNavItemSelected = { index -> selectedNavItem = index },
                        showAccountDialog = showAccountDialog,
                        usuario = usuario,
                        onSignOut = onSignOut
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            PvPHomeScreen(
                                idEstudiante = usuario.documento,
                                onCrearSala = { pvpState = PvPState.CrearSala },
                                onUnirseSala = { pvpState = PvPState.UnirseSala() }
                            )
                        }
                    }
                }

                is PvPState.CrearSala -> {
                    CrearSalaScreen(
                        idEstudiante = usuario.documento,
                        areas = areas,
                        onSalaCreada = { idSala ->
                            pvpState = PvPState.SalaEspera(idSala)
                        },
                        onBack = { pvpState = PvPState.Home }
                    )
                }

                is PvPState.UnirseSala -> {
                    UnirseSalaScreen(
                        idEstudiante = usuario.documento,
                        codigoCompartido = state.codigoCompartido,
                        onUnido = { idSala ->
                            pvpState = PvPState.QuizPvP(idSala)
                        },
                        onBack = { pvpState = PvPState.Home }
                    )
                }

                is PvPState.SalaEspera -> {
                    SalaEsperaScreen(
                        idSala = state.idSala,
                        idEstudiante = usuario.documento,
                        onIniciar = {
                            pvpState = PvPState.QuizPvP(state.idSala)
                        }
                    )
                }

                is PvPState.QuizPvP -> {
                    QuizPvPScreen(
                        idSala = state.idSala,
                        idEstudiante = usuario.documento,
                        onFinish = {
                            pvpState = PvPState.ResultadoPvP(state.idSala)
                        }
                    )
                }

                is PvPState.ResultadoPvP -> {
                    ResultadoPvPScreen(
                        idSala = state.idSala,
                        idEstudiante = usuario.documento,
                        onAceptar = {
                            pvpState = PvPState.Home
                        }
                    )
                }
            }
        }

        // Home normal con layout
        else -> {
            StudentLayout(
                selectedNavItem = selectedNavItem,
                onNavItemSelected = { index ->
                    selectedNavItem = index
                    // Reset PvP state cuando cambia de tab
                    if (index == 2) {
                        pvpState = PvPState.Home
                    }
                },
                showAccountDialog = showAccountDialog,
                usuario = usuario,
                onSignOut = onSignOut
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedNavItem) {
                        0 -> {
                            // Tab Home - Retos normales
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
                        1 -> {
                            // Tab IA - Implementar después
                            Box {}
                        }
                        2 -> {
                            // Tab PvP - Ya manejado arriba
                            Box {}
                        }
                        3 -> {
                            // Tab Grupos - Implementar después
                            Box {}
                        }
                        4 -> {
                            // Tab Perfil - Implementar después
                            Box {}
                        }
                    }
                }
            }
        }
    }
}