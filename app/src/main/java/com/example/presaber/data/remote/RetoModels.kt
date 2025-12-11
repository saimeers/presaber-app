package com.example.presaber.data.remote

import com.google.gson.annotations.SerializedName

data class CrearRetoRequest(
    val nombre: String,
    val descripcion: String,
    val nivel_dificultad: String, // "Bajo", "Medio", "Alto"
    val duracion: String, // "00:30:00"
    val cantidad_preguntas: Int,
    val id_tema: Int
)

data class CrearRetoResponse(
    val success: Boolean,
    val message: String,
    val reto: RetoData? // Define RetoData con los campos que devuelve el create
)

data class RetoData(
    @SerializedName("id_reto") val idReto: Int,
    val nombre: String,
    val descripcion: String,
    @SerializedName("nivel_dificultad") val nivelDificultad: String,
    val duracion: String, // Viene como "HH:MM:SS"
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    @SerializedName("cantidad_preguntas") val cantidadPreguntas: Int,
    @SerializedName("id_tema") val idTema: Int
)

data class TemaConteo(
    val id_tema: Int,
    val descripcion: String,
    val total_preguntas: Int
)

data class TemasResponse(
    val success: Boolean,
    val data: List<TemaConteo>
)