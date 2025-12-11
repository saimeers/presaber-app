package com.example.presaber.layout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// --- Contenedor Genérico para las Vistas ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericInfoDialog(
    title: String,
    content: String,
    onBack: () -> Unit
) {
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar Simple
                TopAppBar(
                    title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )

                Divider(color = Color(0xFFEEEEEE))

                // Contenido Scrollable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        text = content,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFF444746),
                        textAlign = TextAlign.Justify
                    )

                    Spacer(Modifier.height(40.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B57CF))
                    ) {
                        Text("Entendido")
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

// --- Contenidos de Texto ---

@Composable
fun PrivacyPolicyContent(): String {
    return """
        POLÍTICA DE PRIVACIDAD
        
        Última actualización: Diciembre 2025
        
        1. INFORMACIÓN QUE RECOPILAMOS
        En PreSaber, recopilamos información necesaria para proporcionar nuestros servicios educativos, incluyendo nombre, correo electrónico, institución educativa y datos de rendimiento académico en simulacros.
        
        2. USO DE LA INFORMACIÓN
        Utilizamos tu información para:
        - Crear y gestionar tu cuenta.
        - Proporcionar resultados de simulacros y análisis de desempeño.
        - Mejorar nuestros servicios educativos.
        - Comunicarnos contigo sobre actualizaciones del servicio.
        
        3. PROTECCIÓN DE DATOS
        Implementamos medidas de seguridad técnicas y organizativas para proteger tus datos personales contra el acceso no autorizado o la pérdida.
        
        4. COMPARTIR INFORMACIÓN
        No vendemos tu información personal a terceros. Solo compartimos datos con tu institución educativa para fines de seguimiento académico.
    """.trimIndent()
}

@Composable
fun TermsOfUseContent(): String {
    return """
        TÉRMINOS DE USO
        
        1. ACEPTACIÓN DE LOS TÉRMINOS
        Al acceder y utilizar la plataforma PreSaber, aceptas estar sujeto a estos términos y condiciones.
        
        2. USO ADECUADO
        Te comprometes a utilizar la plataforma únicamente con fines educativos y de preparación académica. Está prohibido el uso indebido, fraudulento o que intente vulnerar la seguridad del sistema.
        
        3. PROPIEDAD INTELECTUAL
        Todo el contenido, preguntas, simulacros y material educativo presente en la aplicación son propiedad de PreSaber o de sus licenciantes. No está permitida su reproducción sin autorización.
        
        4. CUENTAS DE USUARIO
        Eres responsable de mantener la confidencialidad de tu cuenta y contraseña. Notifícanos inmediatamente sobre cualquier uso no autorizado.
        
        5. MODIFICACIONES
        Nos reservamos el derecho de modificar estos términos en cualquier momento. Las modificaciones serán efectivas inmediatamente después de su publicación.
    """.trimIndent()
}

@Composable
fun DataPrivacyContent(): String {
    return """
        DATOS Y PRIVACIDAD
        
        Gestiona cómo se utilizan tus datos en PreSaber.
        
        ACTIVIDAD EN LA WEB Y APLICACIONES
        Guardamos tu actividad en los simulacros, incluyendo tiempos de respuesta y opciones seleccionadas, para ofrecerte recomendaciones personalizadas de estudio y estadísticas de mejora.
        
        HISTORIAL DE SIMULACROS
        Tu historial completo de pruebas se almacena de forma segura. Puedes solicitar un reporte detallado de tu rendimiento en cualquier momento.
        
        ELIMINACIÓN DE CUENTA
        Si deseas eliminar tu cuenta y todos los datos asociados, por favor contacta al administrador de tu institución o utiliza la opción de soporte técnico. Esta acción es irreversible.
        
        CONTROL DE DATOS
        Tú tienes el control. Tus datos de contacto no son visibles para otros estudiantes, solo para los docentes y directivos de tu institución asignada.
    """.trimIndent()
}