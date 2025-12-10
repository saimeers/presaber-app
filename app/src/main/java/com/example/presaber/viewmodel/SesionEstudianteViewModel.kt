package com.example.presaber.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.local.SessionLocalCache
import com.example.presaber.data.remote.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.max

data class SesionQuizUiState(
    val loading: Boolean = false,
    val preguntas: List<PreguntaSesion> = emptyList(),
    val currentIndex: Int = 0,
    val nombreSesion: String = "",
    val duracionSegundos: Int = 0,
    val tiempoRestante: Int = 0,
    val puedeIngresar: Boolean = true,
    val mensaje: String? = null,
    val totalPreguntas: Int = 0,
    val contestadas: Int = 0,
    val puntajeAcumulado: Double = 0.0,
    val progresoCompleto: Boolean = false
)

class SesionEstudianteViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SesionQuizUiState())
    val uiState: StateFlow<SesionQuizUiState> = _uiState.asStateFlow()

    private val _resultadoSesion = MutableStateFlow<ResultadoSesionResponse?>(null)
    val resultadoSesion: StateFlow<ResultadoSesionResponse?> = _resultadoSesion.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var timerJob: Job? = null
    private var sesionId: Int = 0
    private var estudianteId: String = ""

    fun cargarSesion(context: Context, idSesion: Int, idEstudiante: String) {
        sesionId = idSesion
        estudianteId = idEstudiante
        timerJob?.cancel()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, mensaje = null)
            _error.value = null
            try {
                val resp = RetrofitClient.api.obtenerPreguntasSesion(idSesion, idEstudiante)
                if (!resp.puedeIngresar) {
                    _uiState.value = SesionQuizUiState(
                        loading = false,
                        puedeIngresar = false,
                        mensaje = resp.mensaje
                    )
                    return@launch
                }
                // aplicar cache offline
                val cacheAnswers = SessionLocalCache.loadAnswers(context, idSesion)
                val cachedIndex = SessionLocalCache.loadIndex(context, idSesion)

                val preguntas = resp.preguntas.map { p ->
                    val cachedOption = cacheAnswers[p.id_sesion_pregunta]
                    if (cachedOption != null) {
                        p.copy(
                            contestada = true,
                            opcion_seleccionada = cachedOption
                        )
                    } else p
                }

                _uiState.value = SesionQuizUiState(
                    loading = false,
                    preguntas = preguntas,
                    currentIndex = cachedIndex ?: resp.progreso.ultima_pregunta?.let { idxFromId(preguntas, it) } ?: 0,
                    nombreSesion = resp.nombre,
                    duracionSegundos = resp.duracion_segundos,
                    tiempoRestante = resp.tiempo_restante,
                    puedeIngresar = true,
                    mensaje = resp.mensaje,
                    totalPreguntas = resp.total_preguntas,
                    contestadas = resp.preguntas_contestadas,
                    puntajeAcumulado = resp.progreso.puntaje_acumulado,
                    progresoCompleto = resp.progreso.completada
                )

                startTimer(resp.tiempo_restante)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar sesión"
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    private fun idxFromId(preguntas: List<PreguntaSesion>, idSesionPregunta: Int): Int {
        val idx = preguntas.indexOfFirst { it.id_sesion_pregunta == idSesionPregunta }
        return if (idx >= 0) idx else 0
    }

    private fun startTimer(start: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var t = start
            while (t > 0) {
                _uiState.value = _uiState.value.copy(tiempoRestante = t)
                kotlinx.coroutines.delay(1000)
                t--
            }
            // tiempo agotado -> finalizar
            finalizeByTime()
        }
    }

    private fun finalizeByTime() {
        viewModelScope.launch {
            try {
                RetrofitClient.api.finalizarSesion(
                    FinalizarSesionRequest(
                        id_sesion = sesionId,
                        id_estudiante = estudianteId,
                        tiempo_usado_final = _uiState.value.duracionSegundos
                    )
                )
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    fun seleccionarOpcion(
        context: Context,
        questionIndex: Int,
        opcionId: Int
    ) {
        val state = _uiState.value
        if (questionIndex !in state.preguntas.indices) return
        val pregunta = state.preguntas[questionIndex]
        val nuevas = state.preguntas.toMutableList()
        nuevas[questionIndex] = pregunta.copy(
            contestada = true,
            opcion_seleccionada = opcionId
        )
        _uiState.value = state.copy(preguntas = nuevas, currentIndex = questionIndex)
        SessionLocalCache.saveAnswer(context, sesionId, pregunta.id_sesion_pregunta, opcionId)
        SessionLocalCache.saveIndex(context, sesionId, questionIndex)

        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.responderPreguntaSesion(
                    ResponderPreguntaRequest(
                        id_estudiante = estudianteId,
                        id_sesion = sesionId,
                        id_sesion_pregunta = pregunta.id_sesion_pregunta,
                        id_opcion = opcionId,
                        orden = pregunta.orden_en_sesion
                    )
                )
                _uiState.value = _uiState.value.copy(
                    contestadas = resp.total_contestadas,
                    puntajeAcumulado = resp.puntaje_acumulado
                )
            } catch (e: Exception) {
                _error.value = "Sin conexión. Respuesta guardada localmente."
            }
        }
    }

    fun irA(index: Int) {
        val clamped = max(0, minOf(index, _uiState.value.preguntas.lastIndex))
        _uiState.value = _uiState.value.copy(currentIndex = clamped)
    }

    fun finalizarSesion(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        timerJob?.cancel()
        viewModelScope.launch {
            try {
                val tiempoUsado = _uiState.value.duracionSegundos - _uiState.value.tiempoRestante
                val resp = RetrofitClient.api.finalizarSesion(
                    FinalizarSesionRequest(
                        id_sesion = sesionId,
                        id_estudiante = estudianteId,
                        tiempo_usado_final = tiempoUsado
                    )
                )
                SessionLocalCache.clearSession(context, sesionId)
                if (resp.completada) {
                    onSuccess()
                } else {
                    onError(resp.mensaje)
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error al finalizar sesión")
            }
        }
    }

    fun cargarResultado(idSesion: Int, idEstudiante: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.obtenerResultadosSesion(idSesion, idEstudiante)
                _resultadoSesion.value = resp
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

