package com.example.presaber.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.*
import com.example.presaber.ui.home.components.Quiz
import com.example.presaber.ui.home.components.RetoList
import com.example.presaber.ui.home.components.ResultQuiz
import com.example.presaber.ui.home.screen.StudentCourseDetailScreen
import com.example.presaber.ui.home.screen.StudentProfileScreen
import com.example.presaber.ui.institution.components.questions.SubjectArea as InstitutionSubjectArea
import com.example.presaber.ui.layout.StudentLayout
import com.example.presaber.ui.pvp.*
import com.example.presaber.ui.simulacro.student.*
import com.example.presaber.utils.SimulacroSessionManager
import com.example.presaber.viewmodel.SimulacroEstudianteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ... (Tus sealed classes se mantienen igual) ...
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
    data class EsperandoResultados(val idSimulacro: Int) : SimulacroState()
    data class Podio(val idSimulacro: Int) : SimulacroState()
}

sealed class SimulacroIndividualState {
    object None : SimulacroIndividualState()
    object UltimoSimulacro : SimulacroIndividualState()
    object Disponibles : SimulacroIndividualState()
    data class Sesiones(val simulacro: com.example.presaber.data.remote.SimulacroDisponible) : SimulacroIndividualState()
    data class SesionQuiz(val idSesion: Int, val sesionNombre: String) : SimulacroIndividualState()
    data class SesionResultado(val idSesion: Int) : SimulacroIndividualState()
    data class ResultadoGlobal(val idSimulacro: Int) : SimulacroIndividualState()
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

    var racha by remember { mutableStateOf(0) }

    // --- NUEVO: Estado para saber si estamos viendo el perfil de un compañero ---
    var viewingStudentId by remember { mutableStateOf<String?>(null) }

    // CORRECCIÓN 2: selectedArea ahora usa explícitamente el SubjectArea del paquete actual (ui.home)
    // Esto arregla el "Argument type mismatch" en RetoList
    var selectedArea by remember { mutableStateOf<com.example.presaber.ui.home.SubjectArea?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var retos by remember { mutableStateOf<List<Reto>>(emptyList()) }
    var currentReto by remember { mutableStateOf<Reto?>(null) }
    var resultadoFinal by remember { mutableStateOf<ResultadoData?>(null) }

    var pvpState by remember { mutableStateOf<PvPState>(
        if (codigoSalaCompartido != null) PvPState.UnirseSala(codigoSalaCompartido) else PvPState.Home
    ) }
    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }

    var simulacroState by remember { mutableStateOf<SimulacroState>(SimulacroState.None) }
    var simulacroIndividualState by remember { mutableStateOf<SimulacroIndividualState>(SimulacroIndividualState.None) }

    val simulacroViewModel: SimulacroEstudianteViewModel = viewModel()
    val scope = rememberCoroutineScope()
    var simulacroSeleccionado by remember { mutableStateOf<com.example.presaber.data.remote.SimulacroDisponible?>(null) }

    LaunchedEffect(selectedNavItem) {
        if (selectedNavItem != 3) viewingStudentId = null
    }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.obtenerRacha(usuario.documento)
            if (response.success) {
                racha = response.data.rachaVictorias
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
                // CORRECCIÓN 3: Resolvemos el ID a partir del título, ya que el objeto visual no tiene ID
                val areaId = getAreaId(selectedArea!!.title)
                val response = RetrofitClient.api.getRetosPorArea(areaId)
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
        // ... (Simulacro Individual flows - unchanged) ...
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
                idEstudiante = usuario.documento,
                simulacro = state.simulacro,
                onBack = { simulacroIndividualState = SimulacroIndividualState.Disponibles },
                onComenzarSesion = { idSesion, _ ->
                    simulacroIndividualState = SimulacroIndividualState.SesionQuiz(
                        idSesion = idSesion,
                        sesionNombre = state.simulacro.simulacro.sesions.find { it.id_sesion == idSesion }?.nombre ?: ""
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
                    val simulacro = simulacroSeleccionado
                    if (simulacro != null) {
                        // Verificar si es la última sesión
                        val lastSession = simulacro.simulacro.sesions.maxByOrNull { it.orden }
                        if (lastSession?.id_sesion == state.idSesion) {
                            // Es la última -> Ir a Resultado Global
                            simulacroIndividualState = SimulacroIndividualState.ResultadoGlobal(simulacro.simulacro.id_simulacro)
                        } else {
                            // No es la última -> Volver a la lista (refresh via Disponibles)
                            simulacroIndividualState = SimulacroIndividualState.Disponibles
                        }
                    } else {
                        simulacroIndividualState = SimulacroIndividualState.Disponibles
                    }
                }
            )
        }
        
        simulacroIndividualState is SimulacroIndividualState.ResultadoGlobal -> {
            val state = simulacroIndividualState as SimulacroIndividualState.ResultadoGlobal
            com.example.presaber.ui.simulacro.student.SimulacroResultadoGlobalScreen(
                idSimulacro = state.idSimulacro,
                usuario = usuario,
                onContinuar = {
                    // Finalizar flujo, volver a home
                    simulacroIndividualState = SimulacroIndividualState.None
                }
            )
        }

        // --- FLUJO SIMULACRO GRUPAL (Igual) ---

        simulacroState is SimulacroState.Unirse -> {
            UnirseSimulacroScreen(
                usuario = usuario,
                onNavigateToWaitingRoom = { id -> simulacroState = SimulacroState.SalaEspera(id) },
                onVerResultados = { id -> simulacroState = SimulacroState.Podio(id) },
                onBack = { simulacroState = SimulacroState.None }
            )
        }
        simulacroState is SimulacroState.SalaEspera -> {
            val state = simulacroState as SimulacroState.SalaEspera
            SalaEsperaEstudianteScreen(
                idSimulacro = state.idSimulacro,
                onStartQuiz = { simulacroState = SimulacroState.Quiz(state.idSimulacro) }
            )
        }
        simulacroState is SimulacroState.Quiz -> {
            val state = simulacroState as SimulacroState.Quiz
            SimulacroQuizScreen(
                idSimulacro = state.idSimulacro,
                idEstudiante = usuario.documento,
                onQuizFinished = { simulacroState = SimulacroState.EsperandoResultados(state.idSimulacro) }
            )
        }
        simulacroState is SimulacroState.EsperandoResultados -> {
            val state = simulacroState as SimulacroState.EsperandoResultados
            ResultadoEstudianteScreen(
                idSimulacro = state.idSimulacro,
                idEstudiante = usuario.documento,
                onSimulacroFinalizado = { simulacroState = SimulacroState.Podio(state.idSimulacro) }
            )
        }
        simulacroState is SimulacroState.Podio -> {
            val state = simulacroState as SimulacroState.Podio
            SimulacroPodioScreen(
                idSimulacro = state.idSimulacro,
                onAceptar = { simulacroState = SimulacroState.None }
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
                onFinish = { resultado -> resultadoFinal = resultado },
                onExit = { currentReto = null }
            )
        }

        // --- FLUJO PVP ---
        selectedNavItem == 2 -> {
            if (pvpState is PvPState.Home) {
                StudentLayout(
                    selectedNavItem = selectedNavItem,
                    onNavItemSelected = { index -> selectedNavItem = index },
                    showAccountDialog = showAccountDialog,
                    usuario = usuario,
                    racha = racha,
                    onSignOut = onSignOut
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        PvPHomeScreen(
                            idEstudiante = usuario.documento,
                            onCrearSala = { pvpState = PvPState.CrearSala },
                            onUnirseSala = { pvpState = PvPState.UnirseSala() }
                        )
                    }
                }
            } else {
                when (val state = pvpState) {
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
                    else -> {}
                }
            }
        }

        // --- HOME NORMAL (MENU) Y OTRAS PESTAÑAS ---
        else -> {
            StudentLayout(
                selectedNavItem = selectedNavItem,
                onNavItemSelected = { index ->
                    selectedNavItem = index
                    if (index == 2) pvpState = PvPState.Home
                },
                showAccountDialog = showAccountDialog,
                racha = racha,
                usuario = usuario,
                onSignOut = onSignOut
            ) { paddingValues ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                ) {
                    when (selectedNavItem) {
                        0 -> {
                            if (selectedArea != null) {
                                // CORRECCIÓN 4: Pasamos el objeto del tipo correcto a RetoList
                                RetoList(
                                    area = selectedArea!!,
                                    retos = retos,
                                    isLoading = isLoading,
                                    onBackClick = { selectedArea = null },
                                    onStartReto = { reto -> currentReto = reto }
                                )
                            } else {
                                HomeContent(
                                    onSubjectClick = { area ->
                                        selectedArea = com.example.presaber.ui.home.SubjectArea(
                                            id = area.id,
                                            title = area.title,
                                            description = area.description,
                                            imageRes = area.imageRes,
                                            cardColor = area.cardColor
                                        )
                                    },
                                    onUnirseSimulacroClick = {
                                        simulacroState = SimulacroState.Unirse
                                    },
                                    onSimulacroClick = {
                                        simulacroViewModel.cargarUltimoSimulacro(usuario.documento)
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
                        1 -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("IA - Próximamente") }

                        // 3. GRUPOS (Integración de CourseDetail y Profile)
                        3 -> {
                            if (viewingStudentId == null) {
                                // Muestra el curso
                                StudentCourseDetailScreen(
                                    grado = usuario.grado,
                                    grupo = usuario.grupo,
                                    cohorte = usuario.cohorte,
                                    idInstitucion = usuario.institucion,
                                    onBack = { selectedNavItem = 0 },
                                    onStudentClick = { studentId ->
                                        // Ir al perfil del compañero
                                        viewingStudentId = studentId
                                    }
                                )
                            } else {
                                // Muestra el perfil del compañero
                                StudentProfileScreen(
                                    studentId = viewingStudentId!!,
                                    onBack = { viewingStudentId = null } // Volver al curso
                                )
                            }
                        }

                        // 4. MI PERFIL
                        4 -> {
                            StudentProfileScreen(
                                studentId = usuario.documento, // Mi propio perfil
                                onBack = { selectedNavItem = 0 }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper para convertir nombre a ID (ya que el objeto visual no tiene ID)
fun getAreaId(title: String): Int {
    return when(title) {
        "Lectura Crítica" -> 1
        "Matemáticas" -> 2
        "Ciencias Naturales" -> 3
        "Ciencias Sociales y Ciudadanas" -> 4
        "Inglés" -> 5
        else -> 0
    }
}