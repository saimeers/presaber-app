package com.example.presaber.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.*
import com.example.presaber.ui.home.components.Quiz
import com.example.presaber.ui.home.components.RetoList
import com.example.presaber.ui.home.components.ResultQuiz
import com.example.presaber.ui.layout.StudentLayout
import com.example.presaber.ui.pvp.*
import com.example.presaber.ui.simulacro.student.*
import com.example.presaber.utils.SimulacroSessionManager
import com.example.presaber.viewmodel.SimulacroEstudianteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class PvPState {
    object Home : PvPState()
    object CrearSala : PvPState()
    data class UnirseSala(val codigoCompartido: String? = null) : PvPState()
    data class SalaEspera(val idSala: Int) : PvPState()
    data class QuizPvP(val idSala: Int) : PvPState()
    data class ResultadoPvP(val idSala: Int) : PvPState()
}

sealed class SimulacroState {
    object None : SimulacroState()
    object Unirse : SimulacroState()
    data class SalaEspera(val idSimulacro: Int) : SimulacroState()
    data class Quiz(val idSimulacro: Int) : SimulacroState()

    // NUEVO: Estado intermedio donde se ve el progreso de los demás
    data class EsperandoResultados(val idSimulacro: Int) : SimulacroState()

    // Estado final: Podio
    data class Podio(val idSimulacro: Int) : SimulacroState()
}

sealed class SimulacroIndividualState {
    object None : SimulacroIndividualState()
    object UltimoSimulacro : SimulacroIndividualState()
    object Disponibles : SimulacroIndividualState()
    data class Sesiones(val simulacro: com.example.presaber.data.remote.SimulacroDisponible) : SimulacroIndividualState()
    data class SesionQuiz(val idSesion: Int, val sesionNombre: String) : SimulacroIndividualState()
    data class SesionResultado(val idSesion: Int) : SimulacroIndividualState()
}

@Composable
fun HomeEstudiante(
    usuario: Usuario,
    codigoSalaCompartido: String? = null,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var selectedNavItem by remember { mutableStateOf(0) }
    val showAccountDialog = remember { mutableStateOf(false) }

    // Estados Retos normales
    var selectedArea by remember { mutableStateOf<SubjectArea?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var retos by remember { mutableStateOf<List<Reto>>(emptyList()) }
    var currentReto by remember { mutableStateOf<Reto?>(null) }
    var resultadoFinal by remember { mutableStateOf<ResultadoData?>(null) }

    // Estados PvP
    var pvpState by remember { mutableStateOf<PvPState>(
        if (codigoSalaCompartido != null) PvPState.UnirseSala(codigoSalaCompartido) else PvPState.Home
    ) }
    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }

    // Estado Simulacro Grupal
    var simulacroState by remember { mutableStateOf<SimulacroState>(SimulacroState.None) }

    // Estado Simulacro Individual (ICFES)
    var simulacroIndividualState by remember { mutableStateOf<SimulacroIndividualState>(SimulacroIndividualState.None) }
    val simulacroViewModel: SimulacroEstudianteViewModel = viewModel()
    val scope = rememberCoroutineScope()
    var simulacroSeleccionado by remember { mutableStateOf<com.example.presaber.data.remote.SimulacroDisponible?>(null) }

    // Recuperar sesión activa si se cerró la app
    LaunchedEffect(Unit) {
        val activeSimulacroId = SimulacroSessionManager.getActiveSimulacroId(context)
        if (activeSimulacroId != null) {
            simulacroState = SimulacroState.Quiz(activeSimulacroId)
        }

        try {
            areas = RetrofitClient.api.getAreas()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(selectedArea) {
        if (selectedArea != null && selectedNavItem == 0) {
            isLoading = true
            try {
                val response = RetrofitClient.api.getRetosPorArea(selectedArea!!.id)
                retos = if (response.success) response.data else emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                retos = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // MANEJO DE VISTAS
    when {
        // --- FLUJO SIMULACRO INDIVIDUAL (ICFES) ---
        simulacroIndividualState is SimulacroIndividualState.UltimoSimulacro -> {
            com.example.presaber.ui.simulacro.student.UltimoSimulacroScreen(
                idUsuario = usuario.documento,
                onBack = { simulacroIndividualState = SimulacroIndividualState.None },
                onComenzar = { simulacroIndividualState = SimulacroIndividualState.Disponibles }
            )
        }

        simulacroIndividualState is SimulacroIndividualState.Disponibles -> {
            com.example.presaber.ui.simulacro.student.SimulacrosDisponiblesScreen(
                idEstudiante = usuario.documento,
                onBack = { simulacroIndividualState = SimulacroIndividualState.None },
                onSimulacroSelected = { simulacro ->
                    simulacroSeleccionado = simulacro
                    simulacroIndividualState = SimulacroIndividualState.Sesiones(simulacro)
                }
            )
        }

        simulacroIndividualState is SimulacroIndividualState.Sesiones -> {
            val state = simulacroIndividualState as SimulacroIndividualState.Sesiones
            com.example.presaber.ui.simulacro.student.SimulacroSesionesScreen(
                simulacro = state.simulacro,
                onBack = { simulacroIndividualState = SimulacroIndividualState.Disponibles },
                onComenzarSesion = { idSesion, _ ->
                    simulacroIndividualState = SimulacroIndividualState.SesionQuiz(
                        idSesion = idSesion,
                        sesionNombre = state.simulacro.simulacro.nombre
                    )
                }
            )
        }

        simulacroIndividualState is SimulacroIndividualState.SesionQuiz -> {
            val state = simulacroIndividualState as SimulacroIndividualState.SesionQuiz
            com.example.presaber.ui.simulacro.student.SesionQuizScreen(
                idSesion = state.idSesion,
                idEstudiante = usuario.documento,
                sesionNombre = state.sesionNombre,
                onBack = {
                    simulacroIndividualState = simulacroSeleccionado?.let {
                        SimulacroIndividualState.Sesiones(it)
                    } ?: SimulacroIndividualState.Disponibles
                },
                onFinish = {
                    simulacroIndividualState = SimulacroIndividualState.SesionResultado(state.idSesion)
                }
            )
        }

        simulacroIndividualState is SimulacroIndividualState.SesionResultado -> {
            val state = simulacroIndividualState as SimulacroIndividualState.SesionResultado
            com.example.presaber.ui.simulacro.student.SesionResultadoScreen(
                idSesion = state.idSesion,
                idEstudiante = usuario.documento,
                onAceptar = {
                    simulacroIndividualState = SimulacroIndividualState.None
                }
            )
        }

        // --- FLUJO SIMULACRO GRUPAL ---

        // 1. Pantalla de Selección/Historial
        simulacroState is SimulacroState.Unirse -> {
            UnirseSimulacroScreen(
                usuario = usuario,
                onNavigateToWaitingRoom = { id ->
                    simulacroState = SimulacroState.SalaEspera(id)
                },
                onVerResultados = { id ->
                    // Si viene del historial, va directo al Podio
                    simulacroState = SimulacroState.Podio(id)
                },
                onBack = { simulacroState = SimulacroState.None }
            )
        }

        // 2. Sala de Espera (Antes de iniciar)
        simulacroState is SimulacroState.SalaEspera -> {
            val state = simulacroState as SimulacroState.SalaEspera
            SalaEsperaEstudianteScreen(
                idSimulacro = state.idSimulacro,
                onStartQuiz = {
                    simulacroState = SimulacroState.Quiz(state.idSimulacro)
                }
            )
        }

        // 3. Quiz Activo
        simulacroState is SimulacroState.Quiz -> {
            val state = simulacroState as SimulacroState.Quiz
            SimulacroQuizScreen(
                idSimulacro = state.idSimulacro,
                idEstudiante = usuario.documento,
                onQuizFinished = {
                    // Al terminar, va a la sala de espera de resultados (Progreso Clase)
                    simulacroState = SimulacroState.EsperandoResultados(state.idSimulacro)
                }
            )
        }

        // 4. Esperando Resultados (Progreso de la clase en tiempo real)
        simulacroState is SimulacroState.EsperandoResultados -> {
            val state = simulacroState as SimulacroState.EsperandoResultados
            ResultadoEstudianteScreen(
                idSimulacro = state.idSimulacro,
                idEstudiante = usuario.documento,
                onSimulacroFinalizado = {
                    // Cuando el docente finaliza, vamos al Podio
                    simulacroState = SimulacroState.Podio(state.idSimulacro)
                }
            )
        }

        // 5. Podio Final (Confeti y ganadores)
        simulacroState is SimulacroState.Podio -> {
            val state = simulacroState as SimulacroState.Podio
            // Usamos la pantalla de Podio que creaste para el estudiante
            SimulacroPodioScreen(
                idSimulacro = state.idSimulacro,
                onAceptar = {
                    simulacroState = SimulacroState.None
                }
            )
        }

        // --- RETOS NORMALES ---
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

        // --- FLUJO PVP ---
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
                        onSalaCreada = { idSala -> pvpState = PvPState.SalaEspera(idSala) },
                        onBack = { pvpState = PvPState.Home }
                    )
                }
                is PvPState.UnirseSala -> {
                    UnirseSalaScreen(
                        idEstudiante = usuario.documento,
                        codigoCompartido = state.codigoCompartido,
                        onUnido = { idSala -> pvpState = PvPState.QuizPvP(idSala) },
                        onBack = { pvpState = PvPState.Home }
                    )
                }
                is PvPState.SalaEspera -> {
                    SalaEsperaScreen(
                        idSala = state.idSala,
                        idEstudiante = usuario.documento,
                        onIniciar = { pvpState = PvPState.QuizPvP(state.idSala) }
                    )
                }
                is PvPState.QuizPvP -> {
                    QuizPvPScreen(
                        idSala = state.idSala,
                        idEstudiante = usuario.documento,
                        onFinish = { pvpState = PvPState.ResultadoPvP(state.idSala) }
                    )
                }
                is PvPState.ResultadoPvP -> {
                    ResultadoPvPScreen(
                        idSala = state.idSala,
                        idEstudiante = usuario.documento,
                        onAceptar = { pvpState = PvPState.Home }
                    )
                }
            }
        }

        // --- HOME NORMAL (MENU) ---
        else -> {
            StudentLayout(
                selectedNavItem = selectedNavItem,
                onNavItemSelected = { index ->
                    selectedNavItem = index
                    if (index == 2) pvpState = PvPState.Home
                },
                showAccountDialog = showAccountDialog,
                usuario = usuario,
                onSignOut = onSignOut
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedNavItem) {
                        0 -> {
                            if (selectedArea != null) {
                                RetoList(
                                    area = selectedArea!!,
                                    retos = retos,
                                    isLoading = isLoading,
                                    onBackClick = { selectedArea = null },
                                    onStartReto = { reto -> currentReto = reto }
                                )
                            } else {
                                HomeContent(
                                    onSubjectClick = { area -> selectedArea = area },
                                    // Al hacer click en "Unirse", cambiamos al estado del Simulacro
                                    onUnirseSimulacroClick = {
                                        simulacroState = SimulacroState.Unirse
                                    },
                                    // Al hacer click en "Simulacro", cargamos el último simulacro o mostramos disponibles
                                    onSimulacroClick = {
                                        simulacroViewModel.cargarUltimoSimulacro(usuario.documento)
                                        // Esperamos un momento para que se cargue, luego decidimos qué mostrar
                                        scope.launch {
                                            delay(500)
                                            val ultimo = simulacroViewModel.ultimoSimulacro.value
                                            if (ultimo != null) {
                                                simulacroIndividualState = SimulacroIndividualState.UltimoSimulacro
                                            } else {
                                                simulacroIndividualState = SimulacroIndividualState.Disponibles
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        1 -> Box {}
                        2 -> Box {}
                        3 -> Box {}
                        4 -> Box {}
                    }
                }
            }
        }
    }
}