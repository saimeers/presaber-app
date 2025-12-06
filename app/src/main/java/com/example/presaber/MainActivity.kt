package com.example.presaber

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.presaber.navigation.MainNavigation
import com.example.presaber.ui.theme.PresaberTheme
import android.content.pm.ActivityInfo

class MainActivity : ComponentActivity() {

    private var codigoSalaCompartido by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Procesar deep link inicial
        handleIntent(intent)

        setContent {
            PresaberTheme {
                MainNavigation(
                    codigoSalaCompartido = codigoSalaCompartido
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Importante: actualizar el intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val data: Uri? = intent.data

        if (data != null && data.scheme == "presaber" && data.host == "sala") {
            val codigo = data.pathSegments?.lastOrNull()

            if (codigo != null) {
                // Limpiar el código y formatear si es necesario
                val codigoLimpio = codigo.replace("-", "").uppercase()

                // Validar formato: 3 letras + 4 números = 7 caracteres
                if (codigoLimpio.length == 7 &&
                    codigoLimpio.substring(0, 3).all { it.isLetter() } &&
                    codigoLimpio.substring(3).all { it.isDigit() }) {

                    // Formatear como ABC-1234
                    codigoSalaCompartido = "${codigoLimpio.substring(0, 3)}-${codigoLimpio.substring(3)}"
                }
            }
        }
    }
}