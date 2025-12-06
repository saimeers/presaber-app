package com.example.presaber.utils

import com.google.gson.Gson
import okhttp3.ResponseBody

data class ErrorResponse(
    val success: Boolean,
    val mensaje: String?
)

fun parseError(errorBody: ResponseBody?): ErrorResponse? {
    return try {
        errorBody?.charStream()?.use { reader ->
            Gson().fromJson(reader, ErrorResponse::class.java)
        }
    } catch (e: Exception) {
        null
    }
}
