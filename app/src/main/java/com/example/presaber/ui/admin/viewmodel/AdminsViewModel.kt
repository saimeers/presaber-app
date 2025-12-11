package com.example.presaber.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.Administrador
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminsViewModel : ViewModel() {

    private val _admins = MutableStateFlow<List<Administrador>>(emptyList())
    val admins: StateFlow<List<Administrador>> = _admins

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarAdministradores() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.obtenerAdministradores()
                if (response.success) {
                    _admins.value = response.data
                } else {
                    _error.value = "No se pudieron cargar los administradores"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}