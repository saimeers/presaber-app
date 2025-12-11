package com.example.presaber.data.remote

import com.google.gson.annotations.SerializedName


data class RachaResponse(
    val success: Boolean,
    val data: RachaData
)

data class RachaData(
    @SerializedName("racha_victorias") val rachaVictorias: Int
)

data class PerfilResponse(
    val success: Boolean,
    val data: PerfilData
)

data class PerfilData(
    val estudiante: EstudiantePerfil,
    val resumen: ResumenPerfil,
    val areas: List<AreaEstadistica>,
    val historial: List<ItemHistorial>
)

data class EstudiantePerfil(
    val documento: String,
    val nombre: String,
    val apellido: String,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    val photoURL: String?,
    val institucion: String?
)

data class ResumenPerfil(
    @SerializedName("racha_victorias") val rachaVictorias: Int,
    @SerializedName("experiencia_total") val experienciaTotal: Int,
    @SerializedName("modulos_resueltos") val modulosResueltos: Int,
    @SerializedName("ultimo_puntaje_simulacro") val ultimoPuntajeSimulacro: Int // O Double si manejas decimales
)

data class AreaEstadistica(
    @SerializedName("id_area") val idArea: Int,
    val nombre: String,
    val correctas: Int,
    val incorrectas: Int,
    val total: Int,
    val porcentaje: Int
)

data class ItemHistorial(
    val tipo: String,   // "Grupal" o "Sala"
    val estado: String, // "Victoria" o "Derrota"
    val titulo: String,
    val correctas: Int,
    val total: Int,
    val fecha: String   // Viene como fecha ISO o string del backend
)