package com.example.presaber.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SimulacroDisponible
import com.example.presaber.data.remote.UltimoSimulacroResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

open class SimulacroEstudianteViewModel : ViewModel() {
    private val _ultimoSimulacro = MutableStateFlow<UltimoSimulacroResponse?>(null)
    val ultimoSimulacro: StateFlow<UltimoSimulacroResponse?> = _ultimoSimulacro.asStateFlow()

    private val _simulacrosDisponibles = MutableStateFlow<List<SimulacroDisponible>>(emptyList())
    val simulacrosDisponibles: StateFlow<List<SimulacroDisponible>> = _simulacrosDisponibles.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    open fun cargarUltimoSimulacro(idUsuario: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.obtenerUltimoSimulacro(idUsuario)
                _ultimoSimulacro.value = response
            } catch (e: HttpException) {
                // Si es 404, no hay último simulacro (no es un error)
                if (e.code() == 404) {
                    _ultimoSimulacro.value = null
                    _error.value = null
                } else {
                    _error.value = e.message ?: "Error al cargar último simulacro"
                    _ultimoSimulacro.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar último simulacro"
                _ultimoSimulacro.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    open fun cargarSimulacrosDisponibles(idEstudiante: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.obtenerSimulacrosDisponibles(idEstudiante)
                _simulacrosDisponibles.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar simulacros disponibles"
                _simulacrosDisponibles.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}

