package com.example.presaber.ui.institution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.ActualizarEstadoRequest
import com.example.presaber.data.remote.CrearCursoRequest
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.ui.institution.screens.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoursesViewModel : ViewModel() {

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos: StateFlow<List<Curso>> = _cursos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Contador para forzar recomposición
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    private var currentIdInstitucion: Int? = null

    fun setIdInstitucion(idInstitucion: Int) {
        currentIdInstitucion = idInstitucion
    }

    fun cargarCursos(idInstitucion: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.getCursosPorInstitucion(idInstitucion)

                _cursos.value = response.map { cursoResponse ->
                    Curso(
                        id = "${cursoResponse.grado}-${cursoResponse.grupo}-${cursoResponse.cohorte}",
                        grado = cursoResponse.grado,
                        grupo = cursoResponse.grupo,
                        cohorte = cursoResponse.cohorte,
                        claveAcceso = cursoResponse.clave_acceso,
                        habilitado = cursoResponse.habilitado,
                        cantidadEstudiantes = cursoResponse.cantidad_estudiantes ?: 0,
                        nombreDocente = cursoResponse.docente?.nombre_completo,
                        fotoDocente = null
                    )
                }

                println("Total cursos cargados: ${_cursos.value.size}")

            } catch (e: Exception) {
                println("Error al cargar cursos: ${e.message}")
                e.printStackTrace()
                _error.value = "Error al cargar cursos: ${e.message}"
                _cursos.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun crearCurso(
        grado: String,
        grupo: String,
        cohorte: Int,
        claveAcceso: String,
        idInstitucion: Int,
        idDocente: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val request = CrearCursoRequest(
                    grado = grado,
                    grupo = grupo,
                    cohorte = cohorte,
                    clave_acceso = claveAcceso,
                    id_institucion = idInstitucion,
                    id_docente = idDocente
                )

                val response = RetrofitClient.api.crearCurso(request)
                println("Curso creado: ${response.mensaje}")
                onResult(true, response.mensaje)

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("Error HTTP al crear curso: $errorBody")
                _error.value = errorBody ?: e.message
                onResult(false, errorBody ?: e.message)
            } catch (e: Exception) {
                println("Error al crear curso: ${e.message}")
                e.printStackTrace()
                _error.value = e.message
                onResult(false, e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleHabilitado(curso: Curso, idInstitucion: Int) {
        viewModelScope.launch {
            val estadoOriginal = curso.habilitado
            val nuevoEstado = !estadoOriginal
            
            try {
                println("TOGGLE - Curso ${curso.id}: ${estadoOriginal} -> ${nuevoEstado}")

                _cursos.value = _cursos.value.map { cursoActual ->
                    if (cursoActual.id == curso.id) {
                        val actualizado = cursoActual.copy(habilitado = nuevoEstado)
                        println("   Optimista: ${actualizado.id} = ${actualizado.habilitado}")
                        actualizado
                    } else {
                        cursoActual
                    }
                }.toList()
                _refreshTrigger.value += 1

                val request = ActualizarEstadoRequest(
                    grado = curso.grado,
                    grupo = curso.grupo,
                    cohorte = curso.cohorte,
                    id_institucion = idInstitucion,
                    habilitado = nuevoEstado
                )

                println("  Enviando request: habilitado=${nuevoEstado}")
                val response = RetrofitClient.api.actualizarEstadoCurso(request)
                val cursoActualizado = response.data
                println("  Backend respondió: habilitado=${cursoActualizado.habilitado}")

                // SIEMPRE sincronizar con la respuesta del backend
                _cursos.value = _cursos.value.map { cursoActual ->
                    if (cursoActual.id == curso.id) {
                        val sincronizado = cursoActual.copy(habilitado = cursoActualizado.habilitado)
                        println("   Sincronizado: ${sincronizado.id} = ${sincronizado.habilitado}")
                        sincronizado
                    } else {
                        cursoActual
                    }
                }.toList()
                _refreshTrigger.value += 1

                // Verificación final
                val cursoFinal = _cursos.value.find { it.id == curso.id }
                println("  FINAL: ${cursoFinal?.id} = ${cursoFinal?.habilitado}")

            } catch (e: retrofit2.HttpException) {
                println(" Error HTTP: ${e.code()} - ${e.message()}")
                val errorBody = e.response()?.errorBody()?.string()
                println(" Error body: $errorBody")
                
                // Revertir al estado original en caso de error
                _cursos.value = _cursos.value.map { cursoActual ->
                    if (cursoActual.id == curso.id) {
                        val revertido = cursoActual.copy(habilitado = estadoOriginal)
                        println(" Revertido: ${revertido.id} = ${revertido.habilitado}")
                        revertido
                    } else {
                        cursoActual
                    }
                }.toList()
                _refreshTrigger.value += 1
                
                _error.value = errorBody ?: e.message()
            } catch (e: Exception) {
                println("  Error general: ${e.message}")
                e.printStackTrace()
                
                // Revertir al estado original en caso de error
                _cursos.value = _cursos.value.map { cursoActual ->
                    if (cursoActual.id == curso.id) {
                        val revertido = cursoActual.copy(habilitado = estadoOriginal)
                        println(" Revertido: ${revertido.id} = ${revertido.habilitado}")
                        revertido
                    } else {
                        cursoActual
                    }
                }.toList()
                _refreshTrigger.value += 1
                
                _error.value = e.message
            }
        }
    }
}