package com.example.presaber.data.remote

import com.google.gson.annotations.SerializedName

data class Usuario(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    @SerializedName("uid_firebase")
    val uidFirebase: String,
    @SerializedName("id_rol")
    val rol: Int, // 1: Admin, 2: Docente, 3: Estudiante, 4: Director
    @SerializedName("id_institucion")
    val institucion: Int
)

// Enum para roles
enum class UserRole(val id: Int) {
    ADMINISTRADOR(1),
    DOCENTE(2),
    ESTUDIANTE(3),
    DIRECTOR(4);

    companion object {
        fun fromId(id: Int): UserRole? = UserRole.entries.find { it.id == id }
    }
}

data class VerificarCorreoRequest(
    val correo: String
)

data class VerificarCorreoResponse(
    val success: Boolean,
    val existe: Boolean,
    val mensaje: String
)