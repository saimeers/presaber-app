package com.example.presaber.ui.institution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.Pregunta
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestionsViewModel : ViewModel() {

    private val _preguntas = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntas: StateFlow<List<Pregunta>> = _preguntas

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun cargarPreguntasPorArea(idArea: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = RetrofitClient.api.getPreguntasPorArea(idArea)
                _preguntas.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                _preguntas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}
