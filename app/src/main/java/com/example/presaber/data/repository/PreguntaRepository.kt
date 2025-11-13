package com.example.presaber.data.repository

import com.example.presaber.data.remote.PresaberApi
import com.example.presaber.data.remote.Pregunta
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class PreguntaRepository(private val api: PresaberApi) {

    suspend fun getAreas() = api.getAreas()

    suspend fun getTemasPorArea(idArea: Int) = api.getTemasPorArea(idArea)

    suspend fun crearPregunta(
        enunciado: String,
        nivelDificultad: String,
        idArea: Int,
        idTema: Int?,
        opciones: List<String>,
        respuestaCorrecta: Int,
        file: File?
    ): Pregunta {
        val enunciadoPart = RequestBody.create("text/plain".toMediaTypeOrNull(), enunciado)
        val nivelPart = RequestBody.create("text/plain".toMediaTypeOrNull(), nivelDificultad)
        val areaPart = RequestBody.create("text/plain".toMediaTypeOrNull(), idArea.toString())
        val temaPart = idTema?.let {
            RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString())
        }

        // Convertir opciones a JSON array string
        val opcionesJson = opciones.joinToString(",", "[", "]") { "\"$it\"" }
        val opcionesPart = RequestBody.create("application/json".toMediaTypeOrNull(), opcionesJson)
        val respuestaCorrectaPart = RequestBody.create("text/plain".toMediaTypeOrNull(), respuestaCorrecta.toString())

        val filePart = file?.let {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
            MultipartBody.Part.createFormData("file", it.name, requestFile)
        }

        return api.crearPregunta(enunciadoPart, nivelPart, areaPart, temaPart, opcionesPart, respuestaCorrectaPart, filePart)
    }
}
