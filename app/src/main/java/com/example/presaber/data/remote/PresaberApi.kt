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

data class Reto(
    val id_reto: Int,
    val nombre: String,
    val descripcion: String,
    val nivel_dificultad: String,
    val duracion: String,
    val cantidad_preguntas: Int,
    val imagen: String?,
    val tema: Tema
)

data class Tema(
    val id_tema: Int,
    val descripcion: String
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

data class RetoResponse(
    val success: Boolean,
    val data: List<Reto>
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


data class Pregunta(
    val id_pregunta: Int,
    val enunciado: String?,
    val nivel_dificultad: String?,
    val imagen: String?,
    val id_area: Int,
    val id_tema: Int?,
    val area: Area?,
    val tema: Tema?,
    val opciones: List<Opcion>
)

data class Opcion(
    val id_opcion: Int,
    val texto_opcion: String?,
    val imagen: String?
)

data class RetoInfo(
    val id_reto: Int,
    val nombre: String,
    val descripcion: String,
    val duracion: String,
    val cantidad_preguntas: Int
)

data class PreguntasData(
    val reto: RetoInfo,
    val preguntas: List<Pregunta>
)

data class PreguntasResponse(
    val success: Boolean,
    val data: PreguntasData
)

data class RespuestaRequest(
    val id_estudiante: String,
    val id_reto: Int,
    val id_opcion: Int
)

data class RespuestaResponse(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>
)

data class ResultadoRequest(
    val id_estudiante: String,
    val id_reto: Int,
    val duracion: String
)

data class ResultadoData(
    val id_resultado: Int,
    val preguntas_correctas: Int,
    val total_preguntas: Int,
    val puntaje: String,
    val experiencia: Int,
    val frase_motivadora: String,
    val fecha_realizacion: String,
    val duracion: String
)

data class ResultadoResponse(
    val success: Boolean,
    val message: String,
    val data: ResultadoData
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

    @GET("api/retos/area/{idArea}")
    suspend fun getRetosPorArea(@Path("idArea") idArea: Int): RetoResponse

    @GET("api/retos/{idReto}/preguntas")
    suspend fun getPreguntasPorReto(@Path("idReto") idReto: Int): PreguntasResponse

    @POST("api/retos/respuestas")
    suspend fun postRespuesta(@Body body: RespuestaRequest): RespuestaResponse

    @POST("api/retos/resultado")
    suspend fun postResultado(@Body body: ResultadoRequest): ResultadoResponse
}