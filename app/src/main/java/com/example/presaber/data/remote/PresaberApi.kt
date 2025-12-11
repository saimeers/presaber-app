package com.example.presaber.data.remote

import com.example.presaber.R
import com.example.presaber.ui.institution.components.teachers.Teacher
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import java.io.File
import retrofit2.Response


data class InstitucionesResponse(
    val success: Boolean,
    val data: List<Institucion>
)

data class Institucion(
    val id_institucion: Int,
    val nombre: String
)

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
    @SerializedName("opciones", alternate = ["opcions"])
    val opciones: List<Opcion> = emptyList()
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
    @SerializedName("opciones", alternate = ["opcions"])
    val opciones: List<Opcion> = emptyList()
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
    suspend fun getInstituciones(): InstitucionesResponse

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

    @POST("api/usuarios/verificar-correo")
    suspend fun verificarCorreo(
        @Body request: VerificarCorreoRequest
    ): VerificarCorreoResponse

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

    @GET("api/usuarios/firebase/{uid_firebase}")
    suspend fun getUsuarioByUidFirebase(
        @Path("uid_firebase") uidFirebase: String
    ): Usuario

    // Crear sala privada
    @POST("api/salas/crear")
    suspend fun crearSala(
        @Body request: CrearSalaRequest
    ): SalaResponse

    // Unirse a sala mediante código
    @POST("api/salas/unirse")
    suspend fun unirseSala(
        @Body request: UnirseSalaRequest
    ): SalaResponse

    // Obtener detalle de sala
    @GET("api/salas/{idSala}")
    suspend fun obtenerSala(
        @Path("idSala") idSala: Int
    ): SalaResponse

    // Obtener preguntas de la sala
    @GET("api/salas/{idSala}/preguntas")
    suspend fun obtenerPreguntasSala(
        @Path("idSala") idSala: Int
    ): PreguntasSalaResponse

    // Guardar respuesta en sala PvP
    @POST("api/salas/respuesta")
    suspend fun guardarRespuestaPvP(
        @Body request: RespuestaPvPRequest
    ): RespuestaPvPResponse

    // Finalizar participación
    @POST("api/salas/finalizar")
    suspend fun finalizarPvP(
        @Body request: FinalizarPvPRequest
    ): FinalizarPvPResponse

    // Obtener resultado final
    @GET("api/salas/{idSala}/resultado")
    suspend fun obtenerResultadoSala(
        @Path("idSala") idSala: Int
    ): ResultadoSalaResponse

    // Obtener progreso en tiempo real
    @GET("api/salas/{idSala}/progreso")
    suspend fun obtenerProgresoSala(
        @Path("idSala") idSala: Int
    ): ProgresoResponse

    // Obtener historial de salas
    @GET("api/salas/historial/{idEstudiante}")
    suspend fun obtenerHistorialSalas(
        @Path("idEstudiante") idEstudiante: String
    ): HistorialSalaResponse

    // Contar preguntas por área y nivel
    @GET("api/preguntas/cantidad")
    suspend fun contarPreguntas(
        @Query("id_area") idArea: Int,
        @Query("nivel_dificultad") nivel: String
    ): ContarPreguntasResponse

    @POST("api/usuarios/docente")
    suspend fun crearDocente(
        @Body request: CrearDocenteRequest
    ): CrearDocenteResponse

    // Crear simulacro grupal
    @POST("api/simulacro-grupal/crear")
    suspend fun crearSimulacro(
        @Body request: CrearSimulacroRequest
    ): SimulacroResponse

    // Unirse a simulacro
    @POST("api/simulacro-grupal/{id}/unirse")
    suspend fun unirseASimulacro(
        @Path("id") idSimulacro: Int,
        @Body body: Map<String, String> // { "id_estudiante": "..." }
    ): SimulacroResponse

    // Iniciar simulacro (Docente)
    @POST("api/simulacro-grupal/{id}/iniciar")
    suspend fun iniciarSimulacro(
        @Path("id") idSimulacro: Int,
        @Body request: IniciarSimulacroRequest
    ): SimulacroResponse

    // Obtener detalle del simulacro
    @GET("api/simulacro-grupal/{id}")
    suspend fun obtenerSimulacro(
        @Path("id") idSimulacro: Int
    ): SimulacroResponse

    // Obtener preguntas del simulacro
    @GET("api/simulacro-grupal/{id}/preguntas")
    suspend fun obtenerPreguntasSimulacro(
        @Path("id") idSimulacro: Int
    ): PreguntasSimulacroResponse

    // Guardar respuesta
    @POST("api/simulacro-grupal/{id}/respuesta")
    suspend fun guardarRespuestaSimulacro(
        @Path("id") idSimulacro: Int,
        @Body request: GuardarRespuestaSimulacroRequest
    ): RespuestaSimulacroResponse

    // Finalizar participación de un estudiante
    @POST("api/simulacro-grupal/{id}/finalizar-participacion")
    suspend fun finalizarParticipacionSimulacro(
        @Path("id") idSimulacro: Int,
        @Body request: FinalizarParticipacionSimulacroRequest
    ): FinalizarParticipacionSimulacroResponse

    // Finalizar simulacro completo (Docente)
    @POST("api/simulacro-grupal/{id}/finalizar")
    suspend fun finalizarSimulacro(
        @Path("id") idSimulacro: Int,
        @Body request: FinalizarSimulacroRequest
    ): ResultadoSimulacroResponse

    // Obtener resultado final
    @GET("api/simulacro-grupal/{id}/resultado")
    suspend fun obtenerResultadoSimulacro(
        @Path("id") idSimulacro: Int
    ): ResultadoSimulacroResponse

    // Obtener progreso en tiempo real
    @GET("api/simulacro-grupal/{id}/progreso")
    suspend fun obtenerProgresoSimulacro(
        @Path("id") idSimulacro: Int
    ): ProgresoSimulacroResponse

    // Obtener simulacros de un curso
    @GET("api/simulacro-grupal/curso/{grado}/{grupo}/{cohorte}/{id_institucion}")
    suspend fun obtenerSimulacrosCurso(
        @Path("grado") grado: String,
        @Path("grupo") grupo: String,
        @Path("cohorte") cohorte: Int,
        @Path("id_institucion") idInstitucion: Int
    ): SimulacrosResumenResponse

    // Obtener historial de un estudiante
    @GET("api/simulacro-grupal/historial/{id_estudiante}")
    suspend fun obtenerHistorialSimulacroEstudiante(
        @Path("id_estudiante") idEstudiante: String
    ): HistorialSimulacroResponse

    // Obtener los cursos de un docente
    @GET("api/usuarios/{documento}/cursos")
    suspend fun obtenerCursosDeUsuario(
        @Path("documento") documento: String
    ): CursosUsuarioResponse

    // Obtener participantes del curso (con foto, nombre y rol)
    @POST("api/curso/participantes")
    suspend fun obtenerParticipantesCurso(
        @Body request: CursoRequest
    ): ParticipantesResponse

    // Obtener promedios por área (de simulacros por secciones)
    @POST("api/curso/promedios")
    suspend fun obtenerPromediosCurso(
        @Body request: CursoRequest
    ): PromediosResponse

    // Obtener ranking de estudiantes (experiencia total)
    @POST("api/curso/ranking")
    suspend fun obtenerRankingCurso(
        @Body request: CursoRequest
    ): RankingResponse

    // Actualizar configuración del curso
    @PATCH("api/curso/configuracion")
    suspend fun actualizarConfiguracionCurso(
        @Body request: ActualizarCursoRequest
    ): ActualizarCursoResponse

    // ==================== SIMULACRO ADMIN ====================
    
    // Obtener todos los simulacros (admin)
    @GET("api/simulacros")
    suspend fun obtenerSimulacrosAdmin(
        @Query("estado") estado: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): SimulacrosAdminResponse

    // Obtener estructura ICFES
    @GET("api/simulacros/estructura-icfes")
    suspend fun obtenerEstructuraICFES(): EstructuraICFESResponse

    // Obtener simulacro por ID
    @GET("api/simulacros/{id_simulacro}")
    suspend fun obtenerSimulacroPorId(
        @Path("id_simulacro") idSimulacro: Int
    ): SimulacroDetalleResponse

    // Crear simulacro (solo estructura)
    @POST("api/simulacros/crear")
    suspend fun crearSimulacroAdmin(
        @Body request: CrearSimulacroGrandeRequest
    ): CrearSimulacroResponse

    // Actualizar simulacro
    @PUT("api/simulacros/{id_simulacro}")
    suspend fun actualizarSimulacroAdmin(
        @Path("id_simulacro") idSimulacro: Int,
        @Body request: ActualizarSimulacroRequest
    ): SimulacroResponseGrande

    // Desactivar simulacro
    @DELETE("api/simulacros/{id_simulacro}")
    suspend fun desactivarSimulacro(
        @Path("id_simulacro") idSimulacro: Int
    ): Response<Unit>

    // Asignar simulacro a cursos
    @POST("api/simulacros/{id_simulacro}/asignar")
    suspend fun asignarSimulacroACursos(
        @Path("id_simulacro") idSimulacro: Int,
        @Body request: AsignarSimulacroRequest
    ): AsignarSimulacroResponse

    // Agregar pregunta a sesión/área
    @POST("api/simulacros/{id_simulacro}/sesiones/{numero_sesion}/areas/{id_area}/preguntas")
    suspend fun agregarPreguntaASesion(
        @Path("id_simulacro") idSimulacro: Int,
        @Path("numero_sesion") numeroSesion: Int,
        @Path("id_area") idArea: Int,
        @Body request: AgregarPreguntaRequest
    ): Response<Unit>

    // Eliminar pregunta de sesión/área
    @DELETE("api/simulacros/{id_simulacro}/sesiones/{numero_sesion}/areas/{id_area}/preguntas/{id_pregunta}")
    suspend fun eliminarPreguntaDeSesion(
        @Path("id_simulacro") idSimulacro: Int,
        @Path("numero_sesion") numeroSesion: Int,
        @Path("id_area") idArea: Int,
        @Path("id_pregunta") idPregunta: Int
    ): Response<Unit>

    // Obtener todos los cursos (para asignación)
    @GET("api/curso/institucion/{id_institucion}")
    suspend fun obtenerCursosPorInstitucionAdmin(
        @Path("id_institucion") idInstitucion: Int
    ): List<CursoResponse>

    // ==================== SIMULACRO ESTUDIANTE ====================
    
    // Obtener último simulacro del estudiante
    @GET("api/simulacros/ultimo/{id_usuario}")
    suspend fun obtenerUltimoSimulacro(
        @Path("id_usuario") idUsuario: String
    ): UltimoSimulacroResponse

    // Obtener simulacros disponibles para el estudiante
    @GET("api/simulacros/disponibles/{id_estudiante}")
    suspend fun obtenerSimulacrosDisponibles(
        @Path("id_estudiante") idEstudiante: String
    ): List<SimulacroDisponible>

    // Obtener preguntas de una sesión para un estudiante
    @GET("api/sesiones/{id_sesion}/estudiante/{id_estudiante}")
    suspend fun obtenerPreguntasSesion(
        @Path("id_sesion") idSesion: Int,
        @Path("id_estudiante") idEstudiante: String
    ): PreguntasSesionResponse

    // Guardar / actualizar respuesta de pregunta
    @POST("api/sesiones/respuesta")
    suspend fun responderPreguntaSesion(
        @Body request: ResponderPreguntaRequest
    ): GuardarRespuestaResponse

    // Finalizar sesión
    @POST("api/sesiones/finalizar")
    suspend fun finalizarSesion(
        @Body request: FinalizarSesionRequest
    ): FinalizarSesionResponse

    // Obtener resultados de sesión
    @GET("api/sesiones/resultado/{id_sesion}/estudiante/{id_estudiante}")
    suspend fun obtenerResultadosSesion(
        @Path("id_sesion") idSesion: Int,
        @Path("id_estudiante") idEstudiante: String
    ): ResultadoSesionResponse

    // Obtener lista simple de instituciones (id y nombre)
    @GET("api/institucion")
    suspend fun obtenerInstituciones(): InstitucionesSimpleResponse

    // Obtener instituciones con información completa (incluye director)
    @GET("api/institucion/completas")
    suspend fun obtenerInstitucionesCompletas(): InstitucionesCompletasResponse

    // Crear institución con director
    @POST("api/institucion")
    suspend fun crearInstitucion(
        @Body request: CrearInstitucionRequest
    ): CrearInstitucionResponse

    // Obtener departamentos de Colombia
    @GET("api/institucion/departamentos")
    suspend fun obtenerDepartamentos(): DepartamentosResponse

    // Obtener municipios de un departamento
    @GET("api/institucion/departamentos/{id}/municipios")
    suspend fun obtenerMunicipios(
        @Path("id") idDepartamento: Int
    ): MunicipiosResponse

    // Crear administrador
    @POST("api/usuarios/administrador")
    suspend fun crearAdministrador(
        @Body request: CrearAdministradorRequest
    ): CrearAdministradorResponse

    // Listar todos los administradores
    @GET("api/usuarios/administradores")
    suspend fun obtenerAdministradores(): AdministradoresResponse

    // Obtener un administrador específico
    @GET("api/usuarios/administrador/{documento}")
    suspend fun obtenerAdministrador(
        @Path("documento") documento: String
    ): AdministradorResponse

    // Obtener solo la racha (rápido, para el header o home)
    @GET("api/estudiantes/{documento}/racha")
    suspend fun obtenerRacha(
        @Path("documento") documento: String
    ): RachaResponse

    // Obtener el perfil completo (estadísticas, gráficos, historial)
    @GET("api/estudiantes/{documento}/perfil")
    suspend fun obtenerPerfil(
        @Path("documento") documento: String
    ): PerfilResponse

    @GET("api/temas/listar/{idArea}")
    suspend fun obtenerTemasPorArea(@Path("idArea") idArea: Int): TemasResponse

    @POST("api/retos/crear")
    suspend fun crearReto(@Body request: CrearRetoRequest): CrearRetoResponse

    @GET("api/simulacros/{id_simulacro}/estudiante/{id_estudiante}/resultados")
    suspend fun obtenerResultadosSimulacro(
        @Path("id_simulacro") idSimulacro: Int,
        @Path("id_estudiante") idEstudiante: String
    ): ResultadoSimulacroGlobalResponse
}

// ==================== DATA MODELS PARA SIMULACRO ADMIN ====================

data class SimulacroAdmin(
    val id_simulacro: Int,
    val nombre: String,
    val descripcion: String?,
    val fecha_creacion: String,
    val estado: Boolean
)

data class SimulacrosAdminResponse(
    val status: String,
    val data: List<SimulacroAdmin>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class EstructuraICFES(
    val orden: Int,
    val nombre: String,
    val areas: List<AreaEstructura>
)

data class AreaEstructura(
    val id_area: Int,
    val nombre: String,
    val cantidad: Int
)

data class EstructuraICFESResponse(
    val status: String,
    val data: List<EstructuraICFES>
)

data class CrearSimulacroGrandeRequest(
    val nombre_simulacro: String,
    val descripcion: String? = null,
    val duracion_sesion_1: Int? = null,
    val duracion_sesion_2: Int? = null,
    val preguntas_sesion1: PreguntasSesion? = null,
    val preguntas_sesion2: PreguntasSesion? = null
)

data class PreguntasSesion(
    val matematicas: List<Int>? = null,
    val lectura_critica: List<Int>? = null,
    val sociales: List<Int>? = null,
    val naturales: List<Int>? = null,
    val ingles: List<Int>? = null
)

data class CrearSimulacroResponse(
    val status: String,
    val mensaje: String,
    val data: SimulacroCreado
)

data class SimulacroCreado(
    val simulacro: SimulacroInfo
)

data class SimulacroInfo(
    val id_simulacro: Int,
    val nombre: String,
    val sesion1_preguntas: Int,
    val sesion2_preguntas: Int,
    val total_preguntas: Int
)

data class ActualizarSimulacroRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val estado: Boolean? = null
)

data class SimulacroResponseGrande(
    val status: String,
    val mensaje: String,
    val data: SimulacroAdmin
)

data class SimulacroDetalleResponse(
    val status: String,
    val data: SimulacroCompleto
)

data class SimulacroCompleto(
    val id_simulacro: Int,
    val nombre: String,
    val descripcion: String?,
    val fecha_creacion: String,
    val estado: Boolean,
    val sesions: List<SesionCompleta>
)

data class SesionCompleta(
    val id_sesion: Int,
    val nombre: String,
    val descripcion: String?,
    val duracion_segundos: Int,
    val orden: Int,
    val sesion_areas: List<SesionAreaCompleta>
)

data class SesionAreaCompleta(
    val id_sesion_area: Int,
    val id_area: Int,
    val orden_area: Int,
    val cantidad_preguntas: Int,
    val area: Area,
    val sesion_preguntas: List<SesionPreguntaCompleta>
)

data class SesionPreguntaCompleta(
    val id_sesion_pregunta: Int,
    val orden_en_sesion: Int,
    val puntaje_base: Double,
    val pregunta: Pregunta
)

data class AsignarSimulacroRequest(
    val asignaciones: List<AsignacionCurso>
)

data class AsignacionCurso(
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val id_institucion: Int,
    val fecha_apertura_s1: String,
    val fecha_cierre_s1: String,
    val fecha_apertura_s2: String,
    val fecha_cierre_s2: String
)

data class AsignarSimulacroResponse(
    val status: String,
    val mensaje: String,
    val data: List<AsignacionResultado>
)

data class AsignacionResultado(
    val id_curso_simulacro: Int,
    val id_simulacro: Int,
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val fecha_apertura_s1: String,
    val fecha_cierre_s1: String,
    val fecha_apertura_s2: String,
    val fecha_cierre_s2: String,
    val actualizado: Boolean
)

data class AgregarPreguntaRequest(
    val id_pregunta: Int,
    val puntaje_base: Double = 0.5
)

// ==================== DATA MODELS PARA SIMULACRO ESTUDIANTE ====================

data class OpcionSesion(
    val id_opcion: Int,
    val texto_opcion: String?,
    val imagen_opcion: String?
)

data class PreguntaSesion(
    val id_sesion_pregunta: Int,
    val id_pregunta: Int,
    val orden_en_sesion: Int,
    val puntaje_base: Double,
    val id_sesion_area: Int,
    val area_id: Int?,
    val area_nombre: String?,
    val enunciado: String?,
    val imagen_url: String?,
    val opciones: List<OpcionSesion>,
    val contestada: Boolean,
    val opcion_seleccionada: Int?
)

data class ProgresoSesionData(
    val ultima_pregunta: Int?,
    val completada: Boolean,
    val puntaje_acumulado: Double,
    val puede_continuar: Boolean
)

data class PreguntasSesionResponse(
    val id_sesion: Int,
    val nombre: String,
    val instrucciones: String?,
    val duracion_segundos: Int,
    val tiempo_usado: Int,
    val tiempo_restante: Int,
    val total_preguntas: Int,
    val preguntas_contestadas: Int,
    val preguntas: List<PreguntaSesion>,
    val progreso: ProgresoSesionData,
    val puedeIngresar: Boolean,
    val mensaje: String? = null
)

data class ResponderPreguntaRequest(
    val id_estudiante: String,
    val id_sesion: Int,
    val id_sesion_pregunta: Int,
    val id_opcion: Int,
    val orden: Int
)

data class GuardarRespuestaResponse(
    val status: String,
    val mensaje: String,
    val guardado: Boolean,
    val cambio: Boolean,
    val total_contestadas: Int,
    val puntaje_acumulado: Double
)

data class FinalizarSesionRequest(
    val id_sesion: Int,
    val id_estudiante: String,
    val tiempo_usado_final: Int? = null
)

data class FinalizarSesionResponse(
    val status: String,
    val mensaje: String,
    val completada: Boolean,
    val puntaje_final: Double,
    val tiempo_total: Int
)

data class ResultadoSesionResponse(
    val disponible: Boolean,
    val mensaje: String? = null,
    val puntaje_obtenido: Double? = null,
    val puntaje_total: Double? = null,
    val correctas: Int? = null,
    val incorrectas: Int? = null,
    val tiempo_total: Int? = null,
    val experiencia_ganada: Int? = null
)

// ==================== DATA MODELS PARA SIMULACRO ESTUDIANTE ====================

// Respuesta del último simulacro
data class UltimoSimulacroResponse(
    val id_simulacro: Int,
    val fecha: String,
    val completado: Boolean,
    val puntaje_total: Double,
    val tiempo_total: Int, // en segundos
    val preguntas_total: Int
)

// Simulacro disponible con sesiones
data class SimulacroDisponible(
    val id_curso_simulacro: Int,
    val id_simulacro: Int,
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val fecha_apertura_s1: String,
    val fecha_cierre_s1: String,
    val fecha_apertura_s2: String,
    val fecha_cierre_s2: String,
    val simulacro: SimulacroDisponibleData
)

data class SimulacroDisponibleData(
    val id_simulacro: Int,
    val nombre: String,
    val sesions: List<SesionDisponible>
)

data class SesionDisponible(
    val id_sesion: Int,
    val nombre: String,
    val descripcion: String?,
    val duracion_segundos: Int,
    val orden: Int,
    val habilitada: Boolean,
    val fecha_apertura: String?,
    val fecha_cierre: String?,
    val completada: Boolean = false
)

data class AreaResultadoSimulacro(
    val id_area: Int,
    val nombre_area: String,
    val puntaje_obtenido: Double,
    val puntaje_maximo: Double,
    val porcentaje: Int
)

data class ResultadoSimulacroGlobalResponse(
    val disponible: Boolean,
    val puntaje_total_obtenido: Double?,
    val puntaje_total_maximo: Double?,
    val fecha_finalizacion: String?,
    val areas: List<AreaResultadoSimulacro>?
)