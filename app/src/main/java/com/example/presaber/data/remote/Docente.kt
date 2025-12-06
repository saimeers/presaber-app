package com.example.presaber.data.remote
import com.google.gson.annotations.SerializedName

data class CrearDocenteRequest(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val fecha_nacimiento: String, // Formato: "YYYY-MM-DD"
    val id_tipo_documento: String,
    val id_institucion: String
)

data class CrearDocenteResponse(
    val success: Boolean,
    val data: DocenteData?,
    val mensaje: String
)

data class DocenteData(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String?,
    val fecha_nacimiento: String?,
    val id_tipo_documento: Int,
    val id_rol: Int,
    val id_institucion: Int,
    val uid_firebase: String
)

data class ReenviarCorreoRequest(
    val correo: String
)

data class GenericResponse(
    val success: Boolean,
    val mensaje: String
)