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

    // Variable para almacenar el id de institución
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

                println("Respuesta del backend: $response")

                // Mapear la respuesta del backend a nuestro modelo UI
                _cursos.value = response.map { cursoResponse ->
                    val curso = Curso(
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

                    println("Curso mapeado: ${curso.grado}-${curso.grupo}, Docente: ${curso.nombreDocente}, Estudiantes: ${curso.cantidadEstudiantes}")

                    curso
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
        idDocente: Int? = null,
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
            try {
                val request = ActualizarEstadoRequest(
                    grado = curso.grado,
                    grupo = curso.grupo,
                    cohorte = curso.cohorte,
                    id_institucion = idInstitucion,
                    habilitado = !curso.habilitado
                )

                val response = RetrofitClient.api.actualizarEstadoCurso(request)

                // Actualizar localmente
                _cursos.value = _cursos.value.map {
                    if (it.id == curso.id) {
                        it.copy(habilitado = response.habilitado)
                    } else {
                        it
                    }
                }

                println("Estado actualizado para curso ${curso.id}: ${response.habilitado}")

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("Error HTTP al cambiar estado: $errorBody")
                _error.value = errorBody ?: e.message
            } catch (e: Exception) {
                println("Error al cambiar estado: ${e.message}")
                e.printStackTrace()
                _error.value = e.message
            }
        }
    }

}



