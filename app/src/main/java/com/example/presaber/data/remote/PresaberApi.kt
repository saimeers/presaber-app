package com.example.presaber.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
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

data class Area(
    val id_area: Int,
    val nombre: String
)

data class Tema(
    val id_tema: Int?,
    val descripcion: String?
)

data class Pregunta(
    val id_pregunta: Int,
    val enunciado: String?,
    val nivel_dificultad: String?,
    val imagen: String?,
    val id_area: Int,
    val id_tema: Int?,
    val area: Area?,
    val tema: Tema?
)

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

    // Áreas
    @GET("api/areas")
    suspend fun getAreas(): List<Area>

    // Preguntas por área
    @GET("api/preguntas/area/{id_area}")
    suspend fun getPreguntasPorArea(@Path("id_area") idArea: Int): List<Pregunta>

    // Temas por área
    @GET("api/temas/area/{id_area}")
    suspend fun getTemasPorArea(@Path("id_area") idArea: Int): List<Tema>

    // Crear pregunta con imagen (Multipart)
    @Multipart
    @POST("api/preguntas/crear")
    suspend fun crearPregunta(
        @Part("enunciado") enunciado: RequestBody,
        @Part("nivel_dificultad") nivelDificultad: RequestBody,
        @Part("id_area") idArea: RequestBody,
        @Part("id_tema") idTema: RequestBody?,
        @Part("opciones") opciones: RequestBody,
        @Part("respuesta_correcta") respuestaCorrecta: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): Pregunta
}


