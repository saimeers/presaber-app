package com.example.presaber.data.remote

import com.example.presaber.R
import com.example.presaber.ui.institution.components.teachers.Teacher
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import java.io.File
import retrofit2.Response


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
    val imagen: String?,
    val es_correcta: Boolean? = null
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

data class CrearTemaResponse(
    val message: String,
    val tema: Tema
)


data class PreguntaLoteUI(
    val enunciado: String?,
    val nivel: String,
    val idArea: Int,
    val idTema: Int?,
    val imagenPregunta: File?,
    val opciones: List<OpcionLoteUI>
)

data class OpcionLoteUI(
    val texto: String?,
    val esCorrecta: Boolean,
    val imagenOpcion: File?
)

data class EditarPreguntaUI(
    val idPregunta: Int,
    val enunciado: String,
    val nivel: String,
    val idArea: Int,
    val idTema: Int?,
    val imagenNueva: File?
)

data class EditarOpcionUI(
    val idOpcion: Int,
    val texto: String?,
    val esCorrecta: Boolean,
    val imagenNueva: File?,
    val eliminarImagen: Boolean = false
)

data class PreguntaCompletaResponse(
    val id_pregunta: Int,
    val enunciado: String,
    val imagen: String?,
    val nivel_dificultad: String,
    val id_area: Int,
    val id_tema: Int,
    val area: Area,
    val tema: Tema,
    val opcions: List<Opcion>
)

data class TeacherResponse(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val fecha_nacimiento: String,
    val uid_firebase: String?,
    val photoURL: String?,
    val displayName: String?,
    val id_usuario: String? = null // ID del usuario para asociar al curso
){
    fun toTeacher(): Teacher {
        return Teacher(
            name = displayName ?: "$nombre $apellido",
            imageRes = R.drawable.user_profile,
            photoUrl = photoURL
        )
    }
}

data class CursoResponse(
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val clave_acceso: String,
    val habilitado: Boolean,
    val cantidad_estudiantes: Int? = 0,
    val docente: DocenteInfo? = null,
    val institucion: InstitucionInfo? = null
)

data class DocenteInfo(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val nombre_completo: String,
    val correo: String
)

data class InstitucionInfo(
    val id_institucion: Int,
    val nombre: String
)

data class CrearCursoRequest(
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val clave_acceso: String,
    val id_institucion: Int,
    val id_docente: String? = null
)

data class CrearCursoResponse(
    val mensaje: String,
    val data: CursoData
)

data class CursoData(
    val curso: CursoResponse,
    val docente: Any? = null
)

data class ActualizarEstadoRequest(
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val id_institucion: Int,
    val habilitado: Boolean
)

data class ActualizarEstadoResponse(
    val mensaje: String,
    val data: CursoResponse
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

    //Crear Tema

    @POST("api/temas")
    suspend fun crearTema(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Tema

    // Crear pregunta (sin opciones)
    @Multipart
    @POST("api/preguntas/crear")
    suspend fun crearPregunta(
        @Part("enunciado") enunciado: RequestBody,
        @Part("nivel_dificultad") nivelDificultad: RequestBody,
        @Part("id_area") idArea: RequestBody,
        @Part("id_tema") idTema: RequestBody?,
        @Part file: MultipartBody.Part? = null
    ): Pregunta


    // Crear opción
    @Multipart
    @POST("api/opciones")
    suspend fun crearOpcion(
        @Part("texto_opcion") textoOpcion: RequestBody,
        @Part("es_correcta") esCorrecta: RequestBody,
        @Part("id_pregunta") idPregunta: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): Opcion


    @Multipart
    @POST("api/preguntas/preguntas/lote")
    suspend fun crearPreguntasLote(
        @Part("data") data: RequestBody,
        @Part parts: List<MultipartBody.Part>
    ): Response<Map<String, Any>>


    // EDITAR PREGUNTA
    @Multipart
    @PUT("api/preguntas/{id}/opciones")
    suspend fun editarPreguntaConOpciones(
        @Path("id") idPregunta: Int,
        @Part("enunciado") enunciado: RequestBody,
        @Part("nivel_dificultad") nivelDificultad: RequestBody,
        @Part("id_area") idArea: RequestBody,
        @Part("id_tema") idTema: RequestBody?,
        @Part file: MultipartBody.Part? = null,
        @Part("eliminar_imagen") eliminarImagen: RequestBody,
        @Part("opciones") opciones: RequestBody,
        @Part imagenesOpciones: List<MultipartBody.Part>? = null
    ): Pregunta


    @Multipart
    @PUT("api/preguntas/{id}/opciones")
    suspend fun editarPreguntaConOpcionesMultipart(
        @Path("id") idPregunta: Int,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part files: List<MultipartBody.Part>
    ): Pregunta



    @GET("api/preguntas/{id_pregunta}")
    suspend fun getPregunta(@Path("id_pregunta") idPregunta: Int): PreguntaCompletaResponse


    @GET("api/retos/area/{idArea}")
    suspend fun getRetosPorArea(@Path("idArea") idArea: Int): RetoResponse

    @GET("api/retos/{idReto}/preguntas")
    suspend fun getPreguntasPorReto(@Path("idReto") idReto: Int): PreguntasResponse

    @POST("api/retos/respuestas")
    suspend fun postRespuesta(@Body body: RespuestaRequest): RespuestaResponse

    @POST("api/retos/resultado")
    suspend fun postResultado(@Body body: ResultadoRequest): ResultadoResponse

    @GET("api/usuarios/institucion/{id_institucion}")
    suspend fun getDocentes(@Path("id_institucion") idInstitucion: Int): List<TeacherResponse>


    //Cursos

    @GET("api/curso/institucion/{id_institucion}")
    suspend fun getCursosPorInstitucion(
        @Path("id_institucion") idInstitucion: Int
    ): List<CursoResponse>

    // Crear nuevo curso
    @POST("api/curso/crear")
    suspend fun crearCurso(
        @Body request: CrearCursoRequest
    ): CrearCursoResponse


    @PUT("api/curso/actualizar-estado")
    suspend fun actualizarEstadoCurso(
        @Body request: ActualizarEstadoRequest
    ): ActualizarEstadoResponse
}