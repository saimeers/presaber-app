package com.example.presaber.ui.institution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.ui.institution.components.teachers.Teacher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeachersViewModel : ViewModel() {

    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadTeachers(idInstitucion: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.getDocentes(idInstitucion)
                _teachers.value = response.map { it.toTeacher() }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message ?: "Error cargando docentes"
            } finally {
                _loading.value = false
            }
        }
    }
}
