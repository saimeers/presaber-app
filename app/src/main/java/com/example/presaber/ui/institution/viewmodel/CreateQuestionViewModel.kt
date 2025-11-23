package com.example.presaber.ui.institution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.Area
import com.example.presaber.data.remote.PreguntaLoteUI
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.Tema
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

class CreateQuestionViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _areas = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _areas

    private val _temas = MutableStateFlow<List<Tema>>(emptyList())
    val temas: StateFlow<List<Tema>> = _temas

    // 🆕 Para mostrar progreso al usuario
    private val _uploadProgress = MutableStateFlow<String>("")
    val uploadProgress: StateFlow<String> = _uploadProgress

    init {
        cargarAreas()
    }

    // Cargar áreas desde API
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

    // Cargar temas según area
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

    // Crear tema nuevo (envía { descripcion, id_area })
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

    fun crearPreguntaConOpciones(
        enunciado: String,
        nivel: String,
        idArea: Int,
        idTema: Int?,
        opciones: List<String>,
        correctIndex: Int,
        imagenPregunta: java.io.File?,
        imagenesOpciones: List<java.io.File?>,
        onComplete: (Boolean, String?) -> Unit
    ) {

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success.value = false
            try {

                val api = RetrofitClient.api

                // Crear Pregunta
                val enunciadoRB = RequestBody.create("text/plain".toMediaTypeOrNull(), enunciado)
                val nivelRB = RequestBody.create("text/plain".toMediaTypeOrNull(), nivel)
                val areaRB = RequestBody.create("text/plain".toMediaTypeOrNull(), idArea.toString())
                val temaRB = idTema?.let {
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString())
                }

                val imagenPart = imagenPregunta?.let {
                    val rb = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                    MultipartBody.Part.createFormData("file", it.name, rb)
                }

                val preguntaCreated = api.crearPregunta(
                    enunciado = enunciadoRB,
                    nivelDificultad = nivelRB,
                    idArea = areaRB,
                    idTema = temaRB,
                    file = imagenPart
                )

                val idPregunta = preguntaCreated.id_pregunta

                // ---------- PASO 2: Crear opciones ----------
                for (i in opciones.indices) {

                    val textoRB =
                        RequestBody.create("text/plain".toMediaTypeOrNull(), opciones[i])

                    val correctaRB = RequestBody.create(
                        "text/plain".toMediaTypeOrNull(),
                        if (i == correctIndex) "true" else "false"
                    )

                    val idPreguntaRB = RequestBody.create(
                        "text/plain".toMediaTypeOrNull(),
                        idPregunta.toString()
                    )

                    val imgOpcionPart = imagenesOpciones[i]?.let { file ->
                        val rb = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        MultipartBody.Part.createFormData("file", file.name, rb)
                    }

                    api.crearOpcion(
                        textoOpcion = textoRB,
                        esCorrecta = correctaRB,
                        idPregunta = idPreguntaRB,
                        file = imgOpcionPart
                    )
                }

                _success.value = true
                onComplete(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al crear pregunta y opciones"
                onComplete(false, e.message ?: "Error")
            } finally {
                _loading.value = false
            }
        }
    }


    fun crearPreguntasLote(
        preguntas: List<PreguntaLoteUI>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success.value = false
            _uploadProgress.value = "Preparando archivos..."

            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()


                val api = RetrofitClient.createApiWithClient(client)

                _uploadProgress.value = "Construyendo petición..."

                // 1. Convertir preguntas a JSON
                val json = buildJsonString(preguntas)
                val dataRB = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    json
                )

                val parts = mutableListOf<MultipartBody.Part>()
                var totalImagenes = 0

                preguntas.forEachIndexed { pIndex, pregunta ->
                    // Imagen de la pregunta
                    pregunta.imagenPregunta?.let { file ->
                        val rb = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        val part = MultipartBody.Part.createFormData(
                            "pregunta_${pIndex}",
                            file.name,
                            rb
                        )
                        parts.add(part)
                        totalImagenes++
                    }

                    // Imágenes de las opciones
                    pregunta.opciones.forEachIndexed { oIndex, opcion ->
                        opcion.imagenOpcion?.let { file ->
                            val rb = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                            val part = MultipartBody.Part.createFormData(
                                "opcion_${pIndex}_${oIndex}",
                                file.name,
                                rb
                            )
                            parts.add(part)
                            totalImagenes++
                        }
                    }
                }

                _uploadProgress.value = "Enviando ${preguntas.size} preguntas con $totalImagenes imágenes..."

                // 3. Hacer petición
                val response = api.crearPreguntasLote(dataRB, parts)

                when (response.code()) {
                    202 -> {
                        _uploadProgress.value = "Procesando en el servidor..."
                        _success.value = true
                        onComplete(
                            true,
                            "Las preguntas se están procesando. Esto puede tomar unos minutos."
                        )
                    }
                    201, 200 -> {
                        _uploadProgress.value = "¡Completado!"
                        _success.value = true
                        onComplete(true, null)
                    }
                    else -> {

                        val errorMsg = response.errorBody()?.string() ?: "Error ${response.code()}"
                        _error.value = errorMsg
                        _uploadProgress.value = ""
                        onComplete(false, errorMsg)
                    }
                }

            } catch (e: java.net.SocketTimeoutException) {
                _uploadProgress.value = ""
                _error.value = "Timeout: El proceso tardó más de lo esperado"
                onComplete(
                    false,
                    "El servidor tardó en responder. Las preguntas podrían haberse guardado correctamente. Verifica en la lista de preguntas."
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadProgress.value = ""
                _error.value = e.message
                onComplete(false, e.message ?: "Error desconocido")
            } finally {
                _loading.value = false
            }
        }
    }

    private fun buildJsonString(preguntas: List<PreguntaLoteUI>): String {
        val root = mapOf(
            "preguntas" to preguntas.map { p ->
                mapOf(
                    "enunciado" to p.enunciado,
                    "nivel_dificultad" to p.nivel,
                    "id_area" to p.idArea,
                    "id_tema" to p.idTema,
                    "opciones" to p.opciones.map { o ->
                        mapOf(
                            "texto_opcion" to o.texto,
                            "es_correcta" to o.esCorrecta
                        )
                    }
                )
            }
        )

        return Gson().toJson(root)
    }
}