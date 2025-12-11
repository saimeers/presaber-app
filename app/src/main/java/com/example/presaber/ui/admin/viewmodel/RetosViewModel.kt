package com.example.presaber.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RetosViewModel : ViewModel() {

    // --- ESTADOS ---
    private val _areas = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _areas

    private val _retos = MutableStateFlow<List<Reto>>(emptyList())
    val retos: StateFlow<List<Reto>> = _retos

    private val _selectedAreaId = MutableStateFlow<Int>(0)
    val selectedAreaId: StateFlow<Int> = _selectedAreaId

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // Temas para el formulario de creación
    private val _temas = MutableStateFlow<List<TemaConteo>>(emptyList())
    val temas: StateFlow<List<TemaConteo>> = _temas

    // Inicialización: Cargar áreas y luego retos de la primera área
    fun cargarDatosIniciales() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // 1. Cargar Áreas
                val areasResponse = RetrofitClient.api.getAreas() // Asumiendo que devuelve List<Area>
                _areas.value = areasResponse

                // 2. Seleccionar la primera por defecto si existe
                if (areasResponse.isNotEmpty()) {
                    val primerAreaId = areasResponse[0].id_area
                    _selectedAreaId.value = primerAreaId
                    cargarRetosPorArea(primerAreaId)
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar áreas: ${e.message}"
                _loading.value = false
            }
        }
    }

    fun seleccionarArea(idArea: Int) {
        if (_selectedAreaId.value != idArea) {
            _selectedAreaId.value = idArea
            cargarRetosPorArea(idArea)
        }
    }

    private fun cargarRetosPorArea(idArea: Int) {
        viewModelScope.launch {
            _loading.value = true
            _retos.value = emptyList() // Limpiar lista visualmente mientras carga
            try {
                val response = RetrofitClient.api.getRetosPorArea(idArea)
                if (response.success) {
                    _retos.value = response.data
                } else {
                    // Si no hay éxito (o lista vacía controlada por backend), dejar lista vacía
                    _retos.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // No mostrar error intrusivo en la lista, solo vacía o log
            } finally {
                _loading.value = false
            }
        }
    }

    // ... (Mantener funciones cargarTemasPorArea y crearReto igual que antes) ...

    fun cargarTemasPorArea(idArea: Int) {
        viewModelScope.launch {
            // ... (Lógica existente para el formulario)
            try {
                _temas.value = emptyList()
                val response = RetrofitClient.api.obtenerTemasPorArea(idArea)
                if (response.success) _temas.value = response.data
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun crearReto(req: CrearRetoRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.api.crearReto(req)
                if(response.success) {
                    _successMessage.value = "Reto creado"
                    // Recargar la lista si el reto creado es del área actual
                    if(_selectedAreaId.value == req.id_tema /* lógica para saber el área */) {
                        cargarRetosPorArea(_selectedAreaId.value)
                    } else {
                        // O simplemente recargar todo
                        cargarRetosPorArea(_selectedAreaId.value)
                    }
                    onSuccess()
                } else {
                    _error.value = response.message
                }
            } catch(e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}