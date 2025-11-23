// ============================================
// 1. VIEWMODEL ACTUALIZADO
// ============================================
package com.example.presaber.ui.institution.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.R
import com.example.presaber.data.remote.Area
import com.example.presaber.data.remote.EditarOpcionUI
import com.example.presaber.data.remote.EditarPreguntaUI
import com.example.presaber.data.remote.Opcion
import com.example.presaber.data.remote.Pregunta
import com.example.presaber.data.remote.PreguntaCompletaResponse
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

    private val _uploadProgress = MutableStateFlow<String>("")
    val uploadProgress: StateFlow<String> = _uploadProgress

    // 🆕 Para ocultar el loading cuando recibamos 202
    private val _shouldDismissLoading = MutableStateFlow(false)
    val shouldDismissLoading: StateFlow<Boolean> = _shouldDismissLoading

    companion object {
        private const val CHANNEL_ID = "preguntas_channel"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        cargarAreas()
    }

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

    // Crear canal de notificaciones
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Preguntas"
            val descriptionText = "Notificaciones sobre la creación de preguntas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Mostrar notificación de éxito
    private fun showSuccessNotification(context: Context, numPreguntas: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usa tu propio ícono
            .setContentTitle("✅ Preguntas creadas")
            .setContentText("Se crearon $numPreguntas pregunta(s) exitosamente")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tus $numPreguntas pregunta(s) se han guardado correctamente en la base de datos."))

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // Mostrar notificación de error
    private fun showErrorNotification(context: Context, error: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("❌ Error al crear preguntas")
            .setContentText(error)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(error))

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun crearPreguntasLote(
        context: Context,
        preguntas: List<PreguntaLoteUI>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success.value = false
            _shouldDismissLoading.value = false
            _uploadProgress.value = "Preparando archivos..."

            // Crear canal de notificaciones
            createNotificationChannel(context)

            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()

                val api = RetrofitClient.createApiWithClient(client)

                _uploadProgress.value = "Construyendo petición..."

                val json = buildJsonString(preguntas)
                val dataRB = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    json
                )

                val parts = mutableListOf<MultipartBody.Part>()
                var totalImagenes = 0

                preguntas.forEachIndexed { pIndex, pregunta ->
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

                val response = api.crearPreguntasLote(dataRB, parts)

                when (response.code()) {
                    202 -> {
                        _uploadProgress.value = "Procesando en el servidor..."
                        _shouldDismissLoading.value = true
                        _success.value = true

                        kotlinx.coroutines.delay(3000) // Simular espera de procesamiento
                        showSuccessNotification(context, preguntas.size)

                        onComplete(true, null) // No mostrar Snackbar
                    }
                    201, 200 -> {
                        _uploadProgress.value = "¡Completado!"
                        _success.value = true
                        showSuccessNotification(context, preguntas.size)
                        onComplete(true, null)
                    }
                    else -> {
                        val errorMsg = response.errorBody()?.string() ?: "Error ${response.code()}"
                        _error.value = errorMsg
                        _uploadProgress.value = ""
                        showErrorNotification(context, errorMsg)
                        onComplete(false, errorMsg)
                    }
                }

            } catch (e: java.net.SocketTimeoutException) {
                _uploadProgress.value = ""
                _error.value = "Timeout"
                val errorMsg = "El servidor tardó en responder. Verifica si las preguntas se guardaron."
                showErrorNotification(context, errorMsg)
                onComplete(false, errorMsg)
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadProgress.value = ""
                _error.value = e.message
                showErrorNotification(context, e.message ?: "Error desconocido")
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

    fun resetShouldDismissLoading() {
        _shouldDismissLoading.value = false
    }

}


