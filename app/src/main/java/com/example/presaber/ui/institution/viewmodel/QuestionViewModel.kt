package com.example.presaber.ui.institution.viewmodel

import android.util.Log
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

    companion object {
        private const val TAG = "QuestionsViewModel"
    }

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
                Log.d(TAG, "✅ Áreas cargadas: ${_areas.value.size}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando áreas", e)
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
                Log.d(TAG, "✅ Temas cargados para área $idArea: ${_temas.value.size}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando temas para área $idArea", e)
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
                Log.d(TAG, "✅ Tema creado: $descripcion")
                onResult(true, null)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error creando tema", e)
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
                Log.d(TAG, "📥 Cargando preguntas para área: $idArea")
                val response = RetrofitClient.api.getPreguntasPorArea(idArea)
                _preguntas.value = response
                Log.d(TAG, "✅ Preguntas cargadas: ${response.size}")

                response.forEachIndexed { index, pregunta ->
                    val opcionesCount = pregunta.opciones?.size ?: 0
                    Log.d(TAG, "  [$index] ID: ${pregunta.id_pregunta}, Opciones: $opcionesCount")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando preguntas", e)
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

            Log.d(TAG, "════════════════════════════════════════")
            Log.d(TAG, "📥 OBTENIENDO PREGUNTA ID: $idPregunta")
            Log.d(TAG, "════════════════════════════════════════")

            try {
                Log.d(TAG, "🌐 Llamando a API...")
                val resp = RetrofitClient.api.getPregunta(idPregunta)

                Log.d(TAG, "✅ Respuesta recibida del API:")
                Log.d(TAG, "  - ID Pregunta: ${resp.id_pregunta}")
                Log.d(TAG, "  - Enunciado: ${resp.enunciado}")
                Log.d(TAG, "  - Nivel: ${resp.nivel_dificultad}")
                Log.d(TAG, "  - Imagen: ${resp.imagen}")
                Log.d(TAG, "  - ID Área: ${resp.id_area}")
                Log.d(TAG, "  - ID Tema: ${resp.id_tema}")
                Log.d(TAG, "  - Área: ${resp.area}")
                Log.d(TAG, "  - Tema: ${resp.tema}")

                // ✅ VERIFICAR SI OPCIONS O OPCIONES EXISTE
                try {
                    // Intentar acceder al campo usando reflection para ver qué campos tiene
                    val fields = resp.javaClass.declaredFields
                    Log.d(TAG, "🔍 Campos disponibles en PreguntaCompletaResponse:")
                    fields.forEach { field ->
                        field.isAccessible = true
                        val value = field.get(resp)
                        Log.d(TAG, "    - ${field.name}: ${value?.javaClass?.simpleName} = $value")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error inspeccionando campos", e)
                }

                Log.d(TAG, "🔄 Mapeando respuesta a Pregunta...")
                val preguntaMapeada = mapRespuestaToPregunta(resp)

                Log.d(TAG, "✅ Pregunta mapeada:")
                Log.d(TAG, "  - ID: ${preguntaMapeada.id_pregunta}")
                Log.d(TAG, "  - Opciones: ${preguntaMapeada.opciones}")
                Log.d(TAG, "  - Cantidad opciones: ${preguntaMapeada.opciones.size}")

                preguntaMapeada.opciones.forEachIndexed { index, opcion ->
                    Log.d(TAG, "  [Opción $index]")
                    Log.d(TAG, "    - ID: ${opcion.id_opcion}")
                    Log.d(TAG, "    - Texto: ${opcion.texto_opcion}")
                    Log.d(TAG, "    - Es correcta: ${opcion.es_correcta}")
                    Log.d(TAG, "    - Imagen: ${opcion.imagen}")
                }

                _pregunta.value = preguntaMapeada
                Log.d(TAG, "✅ Pregunta asignada al StateFlow")

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "❌ HTTP Error ${e.code()}")
                Log.e(TAG, "   Body: $errorBody")
                _pregunta.value = null
                _error.value = "Error HTTP ${e.code()}: $errorBody"
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error obteniendo pregunta", e)
                Log.e(TAG, "   Tipo: ${e.javaClass.simpleName}")
                Log.e(TAG, "   Mensaje: ${e.message}")
                e.printStackTrace()
                _pregunta.value = null
                _error.value = e.message
            } finally {
                _loading.value = false
                Log.d(TAG, "════════════════════════════════════════")
            }
        }
    }

    fun limpiarPregunta() {
        Log.d(TAG, "🧹 Limpiando pregunta del StateFlow")
        _pregunta.value = null
    }

    // Mapear PreguntaCompletaResponse a Pregunta
    private fun mapRespuestaToPregunta(resp: PreguntaCompletaResponse): Pregunta {
        Log.d(TAG, "🔄 Iniciando mapeo de PreguntaCompletaResponse a Pregunta")

        try {
            // Intentar obtener opciones usando reflection si el campo directo falla
            val opcionesList = try {
                Log.d(TAG, "   Intentando acceder a resp.opciones...")
                resp.opciones
            } catch (e: Exception) {
                Log.e(TAG, "   ❌ Error accediendo a resp.opciones", e)
                try {
                    Log.d(TAG, "   Intentando acceder via reflection...")
                    val field = resp.javaClass.getDeclaredField("opcions")
                    field.isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    field.get(resp) as? List<Opcion> ?: emptyList()
                } catch (e2: Exception) {
                    Log.e(TAG, "   ❌ Error con reflection", e2)
                    emptyList()
                }
            }

            Log.d(TAG, "   Opciones obtenidas: ${opcionesList.size} elementos")

            val preguntaMapeada = Pregunta(
                id_pregunta = resp.id_pregunta,
                enunciado = resp.enunciado,
                nivel_dificultad = resp.nivel_dificultad,
                imagen = resp.imagen,
                id_area = resp.id_area,
                id_tema = resp.id_tema,
                area = resp.area,
                tema = resp.tema,
                opciones = opcionesList
            )

            Log.d(TAG, "✅ Mapeo completado exitosamente")
            return preguntaMapeada

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error crítico en mapeo", e)
            throw e
        }
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

                Log.d(TAG, "📝 === INICIANDO EDICIÓN DE PREGUNTA ${pregunta.idPregunta} ===")

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

                    Log.d(TAG, "📋 Opción ${opcion.idOpcion}: texto='${opcion.texto}', correcta=${opcion.esCorrecta}, eliminar=${opcion.eliminarImagen}")
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
                    Log.d(TAG, "🖼️ Imagen pregunta: ${file.name}")
                }

                // Imágenes de opciones con nombre file_{id_opcion}
                opciones.forEach { opcion ->
                    opcion.imagenNueva?.let { file ->
                        val rb = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        val fieldName = "file_${opcion.idOpcion}"
                        files.add(MultipartBody.Part.createFormData(fieldName, file.name, rb))
                        Log.d(TAG, "🖼️ Imagen opción ${opcion.idOpcion}: ${file.name} (field: $fieldName)")
                    }
                }

                Log.d(TAG, "📤 Total archivos: ${files.size}")
                Log.d(TAG, "🚀 Enviando petición...")

                // ========== Llamar al endpoint ==========
                val response = api.editarPreguntaConOpcionesMultipart(
                    idPregunta = pregunta.idPregunta,
                    parts = parts,
                    files = files
                )

                Log.d(TAG, "✅ Respuesta exitosa: $response")
                onResult(true, null)

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "❌ HTTP ${e.code()}: $errorBody")

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
                Log.e(TAG, "❌ Error: ${e.message}", e)
                _error.value = e.message
                onResult(false, e.message)
            } finally {
                _loading.value = false
            }
        }
    }

}