package com.example.presaber.data.firebase

import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.VerificarCorreoRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object FirebaseAuthService {

    private val auth = FirebaseAuth.getInstance()

    /**
     * Verifica si el correo existe en la BD y envía correo de recuperación
     */
    suspend fun sendPasswordResetEmailSimple(email: String): Result<String> {
        return try {
            // 1. Primero verificar en el backend si el correo existe
            val verificacion = RetrofitClient.api.verificarCorreo(
                VerificarCorreoRequest(correo = email)
            )

            if (!verificacion.existe) {
                return Result.failure(
                    Exception("No existe una cuenta registrada con este correo electrónico")
                )
            }

            // 2. Si existe, enviar el correo de recuperación
            auth.sendPasswordResetEmail(email).await()

            Result.success("Correo enviado exitosamente")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Versión con ActionCodeSettings (si la necesitas)
     */
    suspend fun sendPasswordResetEmail(email: String): Result<String> {
        return try {
            // 1. Verificar en el backend primero
            val verificacion = RetrofitClient.api.verificarCorreo(
                VerificarCorreoRequest(correo = email)
            )

            if (!verificacion.existe) {
                return Result.failure(
                    Exception("No existe una cuenta registrada con este correo electrónico")
                )
            }

            // 2. Configuración para el correo de recuperación
            val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                .setUrl("https://sishub-639f8.firebaseapp.com/__/auth/action")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    "com.example.presaber",
                    true,
                    null
                )
                .build()

            // 3. Enviar correo usando Firebase Client SDK
            auth.sendPasswordResetEmail(email, actionCodeSettings).await()

            Result.success("Correo enviado exitosamente")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}