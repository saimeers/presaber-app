package com.example.presaber.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.firebase.FirebaseAuthService
import com.example.presaber.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class InstitutionsViewModel : ViewModel() {

    private val _instituciones = MutableStateFlow<List<InstitucionCompleta>>(emptyList())
    val instituciones: StateFlow<List<InstitucionCompleta>> = _instituciones

    private val _departamentos = MutableStateFlow<List<Departamento>>(emptyList())
    val departamentos: StateFlow<List<Departamento>> = _departamentos

    private val _municipios = MutableStateFlow<List<Municipio>>(emptyList())
    val municipios: StateFlow<List<Municipio>> = _municipios

    private val _tiposDocumento = MutableStateFlow<List<TipoDocumento>>(emptyList())
    val tiposDocumento: StateFlow<List<TipoDocumento>> = _tiposDocumento

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Cargar lista completa
    fun cargarInstituciones() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.api.obtenerInstitucionesCompletas()
                if (response.success) {
                    _instituciones.value = response.data
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar instituciones: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Cargar datos maestros para el formulario
    fun cargarDatosMaestros() {
        viewModelScope.launch {
            try {
                val deptResponse = RetrofitClient.api.obtenerDepartamentos()
                if (deptResponse.success) _departamentos.value = deptResponse.data

                _tiposDocumento.value = RetrofitClient.api.getTiposDocumento()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Cargar municipios cuando se elige un departamento
    fun cargarMunicipios(idDepartamento: Int) {
        viewModelScope.launch {
            try {
                // Limpiamos la lista anterior para evitar confusiones visuales
                _municipios.value = emptyList()
                val response = RetrofitClient.api.obtenerMunicipios(idDepartamento)
                if (response.success) {
                    _municipios.value = response.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun crearInstitucion(
        request: CrearInstitucionRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.api.crearInstitucion(request)

                if (response.success) {
                    FirebaseAuthService.sendPasswordResetEmailSimple(request.director_correo)
                        .onSuccess {
                            // Todo perfecto
                            cargarInstituciones() // Recargar lista
                            onSuccess()
                        }
                        .onFailure {
                            // Se creó en BD pero falló el correo
                            onSuccess() // Consideramos éxito pero avisamos?
                            // Podrías pasar un mensaje de advertencia aquí
                        }
                } else {
                    onError(response.mensaje)
                }

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val mensaje = try {
                    val json = JSONObject(errorBody ?: "")
                    json.optString("error", "Error en la solicitud")
                } catch (ex: Exception) {
                    "Error de servidor: ${e.code()}"
                }
                onError(mensaje)
            } catch (e: Exception) {
                onError("Error inesperado: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }
}