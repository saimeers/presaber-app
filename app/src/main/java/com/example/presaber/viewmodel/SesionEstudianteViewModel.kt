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

    private val _resultadoGlobal = MutableStateFlow<ResultadoSimulacroGlobalResponse?>(null)
    val resultadoGlobal: StateFlow<ResultadoSimulacroGlobalResponse?> = _resultadoGlobal.asStateFlow()

    private var timerJob: Job? = null
    private var sesionId: Int = 0
    private var estudianteId: String = ""
    private var appContext: Context? = null

    fun cargarSesion(context: Context, idSesion: Int, idEstudiante: String) {
        sesionId = idSesion
        estudianteId = idEstudiante
        appContext = context.applicationContext
        timerJob?.cancel()
        viewModelScope.launch {
            // Resetear estado completamente para evitar mostrar datos de la sesión anterior
            _uiState.value = SesionQuizUiState(loading = true)
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
                val cachedTimer = SessionLocalCache.loadTimer(context, idSesion)

                val preguntas = resp.preguntas.map { p ->
                    val cachedOption = cacheAnswers[p.id_sesion_pregunta]
                    if (cachedOption != null) {
                        p.copy(
                            contestada = true,
                            opcion_seleccionada = cachedOption
                        )
                    } else p
                }

                // Calcular contestadas localmente
                val contestadasLocal = preguntas.count { it.contestada }

                android.util.Log.d("SesionVM", "===== CARGA INICIAL =====")
                android.util.Log.d("SesionVM", "Total preguntas: ${preguntas.size}")
                android.util.Log.d("SesionVM", "Contestadas (local): $contestadasLocal")
                android.util.Log.d("SesionVM", "Contestadas (servidor): ${resp.preguntas_contestadas}")
                android.util.Log.d("SesionVM", "========================")

                _uiState.value = SesionQuizUiState(
                    loading = false,
                    preguntas = preguntas,
                    currentIndex = cachedIndex ?: resp.progreso.ultima_pregunta?.let { idPregunta ->
                        // Buscar por id_pregunta en lugar de id_sesion_pregunta
                        preguntas.indexOfFirst { it.id_pregunta == idPregunta }
                    }?.takeIf { it >= 0 } ?: 0,
                    nombreSesion = resp.nombre,
                    duracionSegundos = resp.duracion_segundos,
                    tiempoRestante = cachedTimer?.let { minOf(it, resp.tiempo_restante) } ?: resp.tiempo_restante,
                    puedeIngresar = true,
                    mensaje = resp.mensaje,
                    totalPreguntas = resp.total_preguntas,
                    contestadas = contestadasLocal, //  Usar contador local
                    puntajeAcumulado = resp.progreso.puntaje_acumulado,
                    progresoCompleto = resp.progreso.completada
                )

                val startingTime = cachedTimer?.let { minOf(it, resp.tiempo_restante) } ?: resp.tiempo_restante
                startTimer(startingTime)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar sesión"
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    private fun startTimer(start: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var t = start
            while (t > 0) {
                // SOLO actualizar tiempo, no tocar nada más del estado
                val currentState = _uiState.value
                _uiState.value = currentState.copy(tiempoRestante = t)

                appContext?.let { SessionLocalCache.saveTimer(it, sesionId, t) }
                delay(1000)
                t--
            }
            _uiState.value = _uiState.value.copy(tiempoRestante = 0)
            appContext?.let { SessionLocalCache.saveTimer(it, sesionId, 0) }
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
                appContext?.let { SessionLocalCache.clearTimer(it, sesionId) }
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

        // Verificar si es respuesta nueva o cambio
        val esCambio = pregunta.contestada && pregunta.opcion_seleccionada != opcionId
        val esNueva = !pregunta.contestada

        // Log para debug
        android.util.Log.d("SesionVM", "Seleccionando opción - Es nueva: $esNueva, Es cambio: $esCambio")
        android.util.Log.d("SesionVM", "Contador ANTES: ${state.contestadas}")

        val nuevas = state.preguntas.toMutableList()
        nuevas[questionIndex] = pregunta.copy(
            contestada = true,
            opcion_seleccionada = opcionId
        )

        //  Actualizar contador localmente SIEMPRE basado en las preguntas
        val contestadasActual = nuevas.count { it.contestada }

        android.util.Log.d("SesionVM", "Contador DESPUÉS: $contestadasActual")
        android.util.Log.d("SesionVM", "Total preguntas: ${nuevas.size}, Contestadas: $contestadasActual")

        _uiState.value = state.copy(
            preguntas = nuevas,
            currentIndex = questionIndex,
            contestadas = contestadasActual
        )

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

                android.util.Log.d("SesionVM", "Respuesta del servidor - total_contestadas: ${resp.total_contestadas}, puntaje: ${resp.puntaje_acumulado}")

                // SOLO actualizar puntaje, NO el contador
                _uiState.value = _uiState.value.copy(
                    puntajeAcumulado = resp.puntaje_acumulado
                )
            } catch (e: Exception) {
                android.util.Log.e("SesionVM", "Error al guardar: ${e.message}")
                val mensaje = when {
                    e.message?.contains("Unable to resolve host") == true ||
                            e.message?.contains("timeout") == true ||
                            e.message?.contains("Failed to connect") == true ->
                        "Sin conexión. Respuesta guardada localmente."
                    else ->
                        "Error al guardar respuesta: ${e.message}. Guardada localmente."
                }
                _error.value = mensaje
            }
        }
    }

    fun irA(index: Int) {
        val clamped = max(0, minOf(index, _uiState.value.preguntas.lastIndex))

        android.util.Log.d("SesionVM", "irA($index) -> $clamped, contestadas actual: ${_uiState.value.contestadas}")

        // NO cambiar el contador, solo el índice
        _uiState.value = _uiState.value.copy(currentIndex = clamped)
        appContext?.let { SessionLocalCache.saveIndex(it, sesionId, clamped) }
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
                SessionLocalCache.clearTimer(context, sesionId)
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

    fun cargarResultadoGlobal(idSimulacro: Int, idEstudiante: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            try {
                val resp = RetrofitClient.api.obtenerResultadosSimulacro(idSimulacro, idEstudiante)
                _resultadoGlobal.value = resp
                _uiState.value = _uiState.value.copy(loading = false)
            } catch (e: Exception) {
                _error.value = e.message
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}