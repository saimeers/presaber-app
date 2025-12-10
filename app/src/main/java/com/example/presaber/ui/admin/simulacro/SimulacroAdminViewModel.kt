package com.example.presaber.ui.admin.simulacro

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.*
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class SimulacroAdminViewModel : ViewModel() {

    companion object {
        private const val TAG = "SimulacroAdminViewModel"
    }

    // Estado de lista de simulacros
    val _simulacros = MutableStateFlow<List<SimulacroAdmin>>(emptyList())
    val simulacros: StateFlow<List<SimulacroAdmin>> = _simulacros

    // Estado de carga
    val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Estado de error
    val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Estado de estructura ICFES
    private val _estructuraICFES = MutableStateFlow<List<EstructuraICFES>>(emptyList())
    val estructuraICFES: StateFlow<List<EstructuraICFES>> = _estructuraICFES

    // Estado de simulacro actual (para edición)
    private val _simulacroActual = MutableStateFlow<SimulacroCompleto?>(null)
    val simulacroActual: StateFlow<SimulacroCompleto?> = _simulacroActual

    // Estado de instituciones y cursos
    private val _instituciones = MutableStateFlow<List<Institucion>>(emptyList())
    val instituciones: StateFlow<List<Institucion>> = _instituciones

    private val _cursosPorInstitucion = MutableStateFlow<Map<Int, List<CursoResponse>>>(emptyMap())
    val cursosPorInstitucion: StateFlow<Map<Int, List<CursoResponse>>> = _cursosPorInstitucion

    open fun cargarSimulacros() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.obtenerSimulacrosAdmin()
                if (response.status == "ok") {
                    _simulacros.value = response.data
                } else {
                    _error.value = "Error al cargar simulacros"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando simulacros", e)
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _loading.value = false
            }
        }
    }

    open fun cargarEstructuraICFES() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.obtenerEstructuraICFES()
                if (response.status == "ok") {
                    _estructuraICFES.value = response.data
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando estructura ICFES", e)
            }
        }
    }

    open fun crearSimulacro(
        nombre: String,
        descripcion: String? = null,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val request = CrearSimulacroGrandeRequest(
                    nombre_simulacro = nombre,
                    descripcion = descripcion
                )
                val response = RetrofitClient.api.crearSimulacroAdmin(request)
                if (response.status == "ok") {
                    val idSimulacro = response.data.simulacro.id_simulacro
                    cargarSimulacros() // Recargar lista
                    onSuccess(idSimulacro)
                } else {
                    val errorMsg = "Error al crear simulacro"
                    _error.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando simulacro", e)
                val errorMsg = e.message ?: "Error desconocido"
                _error.value = errorMsg
                onError(errorMsg)
            } finally {
                _loading.value = false
            }
        }
    }

    fun cargarSimulacroPorId(idSimulacro: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.obtenerSimulacroPorId(idSimulacro)
                if (response.status == "ok") {
                    _simulacroActual.value = response.data
                } else {
                    _error.value = "Error al cargar simulacro"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando simulacro", e)
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _loading.value = false
            }
        }
    }

    fun agregarPreguntaASesion(
        idSimulacro: Int,
        numeroSesion: Int,
        idArea: Int,
        idPregunta: Int,
        puntajeBase: Double = 0.5,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = AgregarPreguntaRequest(idPregunta, puntajeBase)
                val response = RetrofitClient.api.agregarPreguntaASesion(
                    idSimulacro, numeroSesion, idArea, request
                )
                if (response.isSuccessful) {
                    cargarSimulacroPorId(idSimulacro) // Recargar
                    onSuccess()
                } else {
                    onError("Error al agregar pregunta")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error agregando pregunta", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    fun eliminarPreguntaDeSesion(
        idSimulacro: Int,
        numeroSesion: Int,
        idArea: Int,
        idPregunta: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.eliminarPreguntaDeSesion(
                    idSimulacro, numeroSesion, idArea, idPregunta
                )
                if (response.isSuccessful) {
                    cargarSimulacroPorId(idSimulacro) // Recargar
                    onSuccess()
                } else {
                    onError("Error al eliminar pregunta")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando pregunta", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    fun cargarInstituciones() {
        viewModelScope.launch {
            try {
                _instituciones.value = RetrofitClient.api.getInstituciones()
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando instituciones", e)
            }
        }
    }

    fun cargarCursosPorInstitucion(idInstitucion: Int) {
        viewModelScope.launch {
            try {
                val cursos = RetrofitClient.api.obtenerCursosPorInstitucionAdmin(idInstitucion)
                _cursosPorInstitucion.value = _cursosPorInstitucion.value.toMutableMap().apply {
                    put(idInstitucion, cursos)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando cursos", e)
            }
        }
    }

    fun asignarSimulacroACursos(
        idSimulacro: Int,
        asignaciones: List<AsignacionCurso>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val request = AsignarSimulacroRequest(asignaciones)
                val response = RetrofitClient.api.asignarSimulacroACursos(idSimulacro, request)
                if (response.status == "ok") {
                    onSuccess()
                } else {
                    onError("Error al asignar simulacro")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error asignando simulacro", e)
                onError(e.message ?: "Error desconocido")
            } finally {
                _loading.value = false
            }
        }
    }

    open fun actualizarEstadoSimulacro(idSimulacro: Int, estado: Boolean) {
        viewModelScope.launch {
            try {
                val request = ActualizarSimulacroRequest(estado = estado)
                RetrofitClient.api.actualizarSimulacroAdmin(idSimulacro, request)
                cargarSimulacros() // Recargar lista
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando estado", e)
            }
        }
    }
}

