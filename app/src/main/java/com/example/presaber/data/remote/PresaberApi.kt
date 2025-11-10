package com.example.presaber.data.remote

import retrofit2.http.*

data class Institucion(val id_institucion: Int, val nombre: String)
data class Curso(val id: String, val nombre: String)
data class VerificacionRequest(
    val id_institucion: String,
    val grado: String,
    val grupo: String,
    val cohorte: String,
    val clave_acceso: String
)

data class TipoDocumento(val id_tipo_documento: Int, val descripcion: String)

data class VerificarUsuarioRequest(
    val documento: String,
    val id_tipo_documento: Int,
    val correo: String
)

data class VerificarUsuarioResponse(
    val existe: Boolean,
    val mensaje: String
)

data class RegistroRequest(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val fecha_nacimiento: String,
    val id_tipo_documento: String,
    val id_institucion: String,
    val grado: String,
    val grupo: String,
    val cohorte: String,
    val password: String
)
data class VerificacionResponse(val valido: Boolean)

interface PresaberApi {
    @GET("api/institucion")
    suspend fun getInstituciones(): List<Institucion>

    @GET("api/curso/curso/institucion/{id_institucion}")
    suspend fun getCursos(@Path("id_institucion") id: Int): List<Curso>

    @POST("api/curso/verificar")
    suspend fun verificarCurso(@Body body: VerificacionRequest): VerificacionResponse

    @GET("api/tipos-documento")
    suspend fun getTiposDocumento(): List<TipoDocumento>

    @POST("api/usuarios/usuario/verificar")
    suspend fun verificarUsuario(@Body body: VerificarUsuarioRequest): VerificarUsuarioResponse

    @POST("api/usuarios/")
    suspend fun registrarUsuario(@Body body: RegistroRequest)
}


