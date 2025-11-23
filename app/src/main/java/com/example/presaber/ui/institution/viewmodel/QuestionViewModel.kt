package com.example.presaber.ui.institution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.*
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class QuestionsViewModel : ViewModel() {

    // Estado de la lista de preguntas
    private val _preguntas = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntas: StateFlow<List<Pregunta>> = _preguntas

    // Estado de carga
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Estado de pregunta individual
    private val _pregunta = MutableStateFlow<Pregunta?>(null)
    val pregunta: StateFlow<Pregunta?> = _pregunta

    private val _areas = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _areas

    private val _temas = MutableStateFlow<List<Tema>>(emptyList())
    val temas: StateFlow<List<Tema>> = _temas

    fun cargarAreas() {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api
                _areas.value = api.getAreas()
            } catch (e: Exception) {
                e.printStackTrace()
                _areas.value = emptyList()
            }
        }
    }

    fun cargarTemasPorArea(idArea: Int) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api
                _temas.value = api.getTemasPorArea(idArea)
            } catch (e: Exception) {
                e.printStackTrace()
                _temas.value = emptyList()
            }
        }
    }

    fun crearTema(descripcion: String, idArea: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api
                val body = mapOf("descripcion" to descripcion, "id_area" to idArea)
                api.crearTema(body)
                cargarTemasPorArea(idArea)
                onResult(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, e.message ?: "Error creando tema")
            }
        }
    }

    // Cargar preguntas por área
    fun cargarPreguntasPorArea(idArea: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.getPreguntasPorArea(idArea)
                _preguntas.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                _preguntas.value = emptyList()
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    // Obtener una pregunta individual
    fun obtenerPregunta(idPregunta: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val resp = RetrofitClient.api.getPregunta(idPregunta)
                _pregunta.value = mapRespuestaToPregunta(resp)
            } catch (e: Exception) {
                e.printStackTrace()
                _pregunta.value = null
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun limpiarPregunta() {
        _pregunta.value = null
    }

    // Mapear PreguntaCompletaResponse a Pregunta
    private fun mapRespuestaToPregunta(resp: PreguntaCompletaResponse): Pregunta {
        return Pregunta(
            id_pregunta = resp.id_pregunta,
            enunciado = resp.enunciado,
            nivel_dificultad = resp.nivel_dificultad,
            imagen = resp.imagen,
            id_area = resp.id_area,
            id_tema = resp.id_tema,
            area = resp.area,
            tema = resp.tema,
            opciones = resp.opcions.map { opcionResp ->
                Opcion(
                    id_opcion = opcionResp.id_opcion,
                    texto_opcion = opcionResp.texto_opcion,
                    imagen = opcionResp.imagen,
                    es_correcta = opcionResp.es_correcta
                )
            }
        )
    }

    // Editar pregunta y todas sus opciones
    fun editarPreguntaCompleta(
        pregunta: EditarPreguntaUI,
        opciones: List<EditarOpcionUI>,
        eliminarImagenPregunta: Boolean = false,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val api = RetrofitClient.api

                println("📝 === INICIANDO EDICIÓN DE PREGUNTA ${pregunta.idPregunta} ===")

                // ========== RequestBody básicos ==========
                val enunciadoRB = RequestBody.create("text/plain".toMediaTypeOrNull(), pregunta.enunciado)
                val nivelRB = RequestBody.create("text/plain".toMediaTypeOrNull(), pregunta.nivel)
                val areaRB = RequestBody.create("text/plain".toMediaTypeOrNull(), pregunta.idArea.toString())
                val temaRB = pregunta.idTema?.let {
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString())
                }
                val eliminarImagenRB = RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    if (eliminarImagenPregunta) "true" else "false"
                )

                // ========== Preparar JSON de opciones ==========
                val opcionesJsonArray = JSONArray()
                opciones.forEach { opcion ->
                    val obj = JSONObject()
                    obj.put("id_opcion", opcion.idOpcion)
                    obj.put("texto_opcion", opcion.texto ?: "")
                    obj.put("es_correcta", opcion.esCorrecta)
                    obj.put("eliminar_imagen", opcion.eliminarImagen)
                    opcionesJsonArray.put(obj)

                    println("📋 Opción ${opcion.idOpcion}: texto='${opcion.texto}', correcta=${opcion.esCorrecta}, eliminar=${opcion.eliminarImagen}")
                }

                val opcionesRB = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    opcionesJsonArray.toString()
                )

                // ========== Crear un mapa para retrofit con @PartMap ==========
                val parts = mutableMapOf<String, RequestBody>()
                parts["enunciado"] = enunciadoRB
                parts["nivel_dificultad"] = nivelRB
                parts["id_area"] = areaRB
                if (temaRB != null) parts["id_tema"] = temaRB
                parts["eliminar_imagen"] = eliminarImagenRB
                parts["opciones"] = opcionesRB

                // ========== Preparar TODOS los archivos multipart ==========
                val files = mutableListOf<MultipartBody.Part>()

                // Imagen de la pregunta
                pregunta.imagenNueva?.let { file ->
                    val rb = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    files.add(MultipartBody.Part.createFormData("file", file.name, rb))
                    println("🖼️ Imagen pregunta: ${file.name}")
                }

                // Imágenes de opciones con nombre file_{id_opcion}
                opciones.forEach { opcion ->
                    opcion.imagenNueva?.let { file ->
                        val rb = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        val fieldName = "file_${opcion.idOpcion}"
                        files.add(MultipartBody.Part.createFormData(fieldName, file.name, rb))
                        println("🖼️ Imagen opción ${opcion.idOpcion}: ${file.name} (field: $fieldName)")
                    }
                }

                println("📤 Total archivos: ${files.size}")
                println("🚀 Enviando petición...")

                // ========== Llamar al endpoint ==========
                val response = api.editarPreguntaConOpcionesMultipart(
                    idPregunta = pregunta.idPregunta,
                    parts = parts,
                    files = files
                )

                println("✅ Respuesta exitosa: $response")
                onResult(true, null)

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("❌ HTTP ${e.code()}: $errorBody")

                val mensaje = when (e.code()) {
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos"
                    404 -> "Pregunta no encontrada"
                    400 -> errorBody ?: "Datos inválidos"
                    500 -> "Error del servidor"
                    else -> errorBody ?: "Error desconocido"
                }

                _error.value = mensaje
                onResult(false, mensaje)
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                e.printStackTrace()
                _error.value = e.message
                onResult(false, e.message)
            } finally {
                _loading.value = false
            }
        }
    }

}
