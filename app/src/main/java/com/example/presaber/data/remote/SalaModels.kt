package com.example.presaber.data.remote

// Request para crear sala
data class CrearSalaRequest(
    val id_estudiante: String,
    val id_area: Int,
    val nivel_dificultad: String,
    val duracion_minutos: Int = 10,
    val cantidad_preguntas: Int = 5
)

// Request para unirse a sala
data class UnirseSalaRequest(
    val codigo_sala: String,
    val id_estudiante: String
)

// Participante de sala
data class ParticipanteSala(
    val id_estudiante: String,
    val posicion: Int,
    val estado_jugador: String,
    val preguntas_respondidas: Int,
    val preguntas_correctas: Int,
    val estudiante: EstudianteSala
)

data class EstudianteSala(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val nombre_completo: String,
    val photoURL: String?
)

// Sala completa
data class SalaPrivada(
    val id_sala: Int,
    val codigo_sala: String,
    val id_creador: String,
    val area: Area,
    val nivel_dificultad: String,
    val duracion_minutos: Int,
    val cantidad_preguntas: Int,
    val estado: String, // esperando, en_curso, finalizada, cancelada
    val fecha_creacion: String,
    val fecha_inicio: String?,
    val expira_en: String,
    val participantes: List<ParticipanteSala>
)

// Response crear/unirse sala
data class SalaResponse(
    val success: Boolean,
    val message: String? = null,
    val data: SalaPrivada
)

// Preguntas de sala
data class PreguntasSalaData(
    val sala: InfoSala,
    val preguntas: List<PreguntaSala>
)

data class InfoSala(
    val id_sala: Int,
    val codigo_sala: String,
    val duracion_minutos: Int,
    val cantidad_preguntas: Int
)

data class PreguntaSala(
    val id_pregunta: Int,
    val enunciado: String?,
    val imagen: String?,
    val nivel_dificultad: String,
    val orden: Int,
    val opciones: List<OpcionSala>
)

data class OpcionSala(
    val id_opcion: Int,
    val texto_opcion: String?,
    val imagen: String?
)

data class PreguntasSalaResponse(
    val success: Boolean,
    val data: PreguntasSalaData
)

// Request respuesta PvP
data class RespuestaPvPRequest(
    val id_sala: Int,
    val id_estudiante: String,
    val id_pregunta: Int,
    val id_opcion: Int,
    val tiempo_respuesta: Int = 0
)

data class RespuestaPvPData(
    val id_estudiante: String,
    val id_pregunta: Int,
    val es_correcta: Boolean,
    val preguntas_respondidas: Int,
    val preguntas_correctas: Int
)

data class RespuestaPvPResponse(
    val success: Boolean,
    val message: String,
    val data: RespuestaPvPData
)

// Request finalizar
data class FinalizarPvPRequest(
    val id_sala: Int,
    val id_estudiante: String,
    val duracion_total: String
)

data class FinalizarPvPData(
    val id_estudiante: String,
    val puntaje_final: String,
    val preguntas_correctas: Int,
    val total_preguntas: Int,
    val experiencia_ganada: Int
)

data class FinalizarPvPResponse(
    val success: Boolean,
    val message: String,
    val data: FinalizarPvPData
)

// Resultado final de sala
data class ResultadoSala(
    val id_sala: Int,
    val codigo_sala: String,
    val area: String,
    val nivel_dificultad: String,
    val duracion_minutos: Int,
    val total_preguntas: Int,
    val estado: String,
    val participantes: List<ParticipanteResultado>
)

data class ParticipanteResultado(
    val posicion: Int,
    val id_estudiante: String,
    val nombre_completo: String,
    val photoURL: String?,
    val preguntas_correctas: Int,
    val puntaje_final: String,
    val experiencia_ganada: Int,
    val es_ganador: Boolean
)

data class ResultadoSalaResponse(
    val success: Boolean,
    val data: ResultadoSala
)

// Progreso en tiempo real
data class ProgresoJugador(
    val id_estudiante: String,
    val nombre: String,
    val photoURL: String?,
    val posicion: Int,
    val preguntas_respondidas: Int,
    val preguntas_correctas: Int,
    val estado: String
)

data class ProgresoResponse(
    val success: Boolean,
    val data: List<ProgresoJugador>
)

// Historial de salas
data class HistorialSala(
    val id_sala: Int,
    val codigo_sala: String,
    val area: String,
    val nivel_dificultad: String,
    val fecha_finalizacion: String,
    val preguntas_correctas: Int,
    val total_preguntas: Int,
    val puntaje_final: String,
    val experiencia_ganada: Int,
    val resultado: String // victoria o derrota
)

data class HistorialSalaResponse(
    val success: Boolean,
    val data: List<HistorialSala>
)

data class ContarPreguntasResponse(
    val ok: Boolean,
    val total: Int
)

