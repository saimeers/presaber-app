package com.example.presaber.data.remote

data class CursoRequest(
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val id_institucion: Int
)

data class ActualizarCursoRequest(
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val id_institucion: Int,
    val clave_acceso: String? = null,
    val id_docente: String? = null,
    val habilitado: Boolean? = null
)

// Participantes
data class ParticipanteCurso(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val nombre_completo: String,
    val correo: String,
    val photoURL: String?,
    val rol: RolParticipante
)

data class RolParticipante(
    val id: Int,
    val descripcion: String
)

data class ParticipantesResponse(
    val success: Boolean,
    val data: List<ParticipanteCurso>
)

// Promedios por área
data class PromedioArea(
    val id_area: Int,
    val nombre_area: String,
    val promedio: Double
)

data class PromediosResponse(
    val success: Boolean,
    val data: List<PromedioArea>
)

// Ranking
data class EstudianteRanking(
    val posicion: Int,
    val documento: String,
    val nombre_completo: String,
    val photoURL: String?,
    val experiencia_total: Int,
    val ultimo_puntaje_simulacro: Int
)

data class RankingResponse(
    val success: Boolean,
    val data: List<EstudianteRanking>
)

// Configuración actualizada
data class CursoActualizado(
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val clave_acceso: String,
    val id_docente: String,
    val habilitado: Boolean
)

data class ActualizarCursoResponse(
    val success: Boolean,
    val message: String,
    val data: CursoActualizado
)