package com.example.presaber.ui.institution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.Area
import com.example.presaber.data.remote.Tema
import com.example.presaber.data.repository.PreguntaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CreateQuestionViewModel(
    private val repository: PreguntaRepository
) : ViewModel() {

    // Estado de áreas
    private val _areas = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _areas.asStateFlow()

    // Estado de temas filtrados por área
    private val _temas = MutableStateFlow<List<Tema>>(emptyList())
    val temas: StateFlow<List<Tema>> = _temas.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Estado de éxito
    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    init {
        cargarAreas()
    }

    // Cargar áreas al iniciar
    fun cargarAreas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val areasResponse = repository.getAreas()
                _areas.value = areasResponse
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar áreas: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Cargar temas cuando se selecciona un área
    fun cargarTemasPorArea(idArea: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val temasResponse = repository.getTemasPorArea(idArea)
                _temas.value = temasResponse
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar temas: ${e.message}"
                _temas.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Crear pregunta
    fun crearPregunta(
        enunciado: String,
        nivelDificultad: String,
        idArea: Int,
        idTema: Int?,
        opciones: List<String>,
        respuestaCorrecta: Int,
        file: File?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _success.value = false
            try {
                repository.crearPregunta(
                    enunciado = enunciado,
                    nivelDificultad = nivelDificultad,
                    idArea = idArea,
                    idTema = idTema,
                    opciones = opciones,
                    respuestaCorrecta = respuestaCorrecta,
                    file = file
                )
                _success.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al crear pregunta: ${e.message}"
                _success.value = false
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Limpiar mensajes
    fun clearMessages() {
        _error.value = null
        _success.value = false
    }
}