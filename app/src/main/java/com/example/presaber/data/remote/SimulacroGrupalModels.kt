package com.example.presaber.data.remote

// ========== REQUEST ==========

data class CrearSimulacroRequest(
    val id_docente: String,
    val grado: String,
    val grupo: String,
    val cohorte: Int,
    val id_institucion: Int,
    val cantidad_preguntas: Int,
    val duracion_minutos: Int
)

data class IniciarSimulacroRequest(
    val id_docente: String
)

data class FinalizarSimulacroRequest(
    val id_docente: String
)

data class GuardarRespuestaSimulacroRequest(
    val id_simulacro: Int,
    val id_estudiante: String,
    val id_pregunta: Int,
    val id_opcion: Int,
    val tiempo_respuesta: Int
)

data class FinalizarParticipacionSimulacroRequest(
    val id_simulacro: Int,
    val id_estudiante: String
)

// ========== RESPONSE DATA ==========

data class SimulacroGrupal(
    val id_simulacro: Int,
    val docente: DocenteSimulacro,
    val curso: CursoSimulacro,
    val cantidad_preguntas: Int,
    val duracion_minutos: Int,
    val estado: String, // esperando, en_curso, finalizado, cancelado
    val fecha_creacion: String,
    val fecha_inicio: String?,
    val fecha_finalizacion: String?,
    val participantes: List<ParticipanteSimulacro>
)

data class DocenteSimulacro(
    val documento: String,
    val nombre: String,
    val apellido: String
)

data class CursoSimulacro(
    val grado: String,
    val grupo: String,
    val cohorte: Int
)

data class ParticipanteSimulacro(
    val id_estudiante: String,
    val estado_jugador: String, // esperando, jugando, finalizado, abandonó
    val preguntas_respondidas: Int,
    val preguntas_correctas: Int,
    val posicion_final: Int?,
    val estudiante: EstudianteSimulacro
)

data class EstudianteSimulacro(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val nombre_completo: String,
    val photoURL: String?
)

// ========== PREGUNTAS ==========

data class PreguntasSimulacroData(
    val simulacro: InfoSimulacro,
    val preguntas: List<PreguntaSimulacro>
)

data class InfoSimulacro(
    val id_simulacro: Int,
    val duracion_minutos: Int,
    val cantidad_preguntas: Int
)

data class PreguntaSimulacro(
    val id_pregunta: Int,
    val enunciado: String?,
    val imagen: String?,
    val nivel_dificultad: String,
    val area: String,
    val orden: Int,
    val opciones: List<OpcionSimulacro>
)

data class OpcionSimulacro(
    val id_opcion: Int,
    val texto_opcion: String?,
    val imagen: String?
)

// ========== PROGRESO ==========

data class ProgresoSimulacro(
    val id_estudiante: String,
    val nombre: String,
    val photoURL: String?,
    val preguntas_respondidas: Int,
    val preguntas_correctas: Int,
    val estado: String
)

// ========== RESULTADO ==========

data class ResultadoSimulacro(
    val id_simulacro: Int,
    val docente: DocenteResultado,
    val curso: CursoSimulacro,
    val duracion_minutos: Int,
    val total_preguntas: Int,
    val estado: String,
    val fecha_inicio: String?,
    val fecha_finalizacion: String?,
    val participantes: List<ParticipanteResultadoSimulacro>
)

data class DocenteResultado(
    val nombre: String
)

data class ParticipanteResultadoSimulacro(
    val posicion: Int,
    val id_estudiante: String,
    val nombre_completo: String,
    val photoURL: String?,
    val preguntas_correctas: Int,
    val puntaje_final: String,
    val experiencia_ganada: Int,
    val en_podio: Boolean
)

// ========== HISTORIAL ==========

data class HistorialSimulacro(
    val id_simulacro: Int,
    val docente: String,
    val fecha_finalizacion: String,
    val preguntas_correctas: Int,
    val total_preguntas: Int,
    val puntaje_final: String,
    val experiencia_ganada: Int,
    val posicion_final: Int?
)

// ========== LISTA DE SIMULACROS ==========

data class SimulacroResumen(
    val id_simulacro: Int,
    val docente: String,
    val cantidad_preguntas: Int,
    val duracion_minutos: Int,
    val estado: String,
    val fecha_creacion: String,
    val fecha_inicio: String?
)

// ========== API RESPONSES ==========

data class SimulacroResponse(
    val success: Boolean,
    val message: String? = null,
    val data: SimulacroGrupal
)

data class PreguntasSimulacroResponse(
    val success: Boolean,
    val data: PreguntasSimulacroData
)

data class ProgresoSimulacroResponse(
    val success: Boolean,
    val data: List<ProgresoSimulacro>
)

data class ResultadoSimulacroResponse(
    val success: Boolean,
    val data: ResultadoSimulacro
)

data class HistorialSimulacroResponse(
    val success: Boolean,
    val data: List<HistorialSimulacro>
)

data class SimulacrosResumenResponse(
    val success: Boolean,
    val data: List<SimulacroResumen>
)

data class RespuestaSimulacroData(
    val id_estudiante: String,
    val id_pregunta: Int,
    val es_correcta: Boolean,
    val preguntas_respondidas: Int,
    val preguntas_correctas: Int
)

data class RespuestaSimulacroResponse(
    val success: Boolean,
    val message: String,
    val data: RespuestaSimulacroData
)

data class FinalizarParticipacionSimulacroData(
    val id_estudiante: String,
    val puntaje_final: String,
    val preguntas_correctas: Int,
    val total_preguntas: Int,
    val experiencia_ganada: Int
)

data class FinalizarParticipacionSimulacroResponse(
    val success: Boolean,
    val message: String,
    val data: FinalizarParticipacionSimulacroData
)