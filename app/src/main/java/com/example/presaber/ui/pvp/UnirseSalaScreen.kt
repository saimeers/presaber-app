package com.example.presaber.ui.pvp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.UnirseSalaRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

// Reutilizamos los colores del sistema de diseño
private val AccentBlue = Color(0xFF2962FF)
private val ErrorRed = Color(0xFFFF5252)

@Composable
fun UnirseSalaScreen(
    idEstudiante: String,
    codigoCompartido: String? = null,
    onUnido: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var codigo by remember { mutableStateOf(codigoCompartido ?: "") }
    var isJoining by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Lógica de Auto-unirse (Mantenemos tu lógica intacta)
    LaunchedEffect(codigoCompartido) {
        if (!codigoCompartido.isNullOrBlank() && codigoCompartido.length == 7) {
            isJoining = true
            try {
                val response = RetrofitClient.api.unirseSala(
                    UnirseSalaRequest(formatearCodigo(codigoCompartido), idEstudiante)
                )
                if (response.success) {
                    onUnido(response.data.id_sala)
                } else {
                    errorMessage = response.message ?: "Error al unirse"
                }
            } catch (e: HttpException) {
                errorMessage = if (e.code() == 400) "Sala no encontrada" else "Error del servidor: ${e.code()}"
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isJoining = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Fondo Decorativo (Coherencia visual)
        BackgroundBlobsInput()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 2. Header Limpio
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF5F7FA))
                ) {
                    Icon(Icons.Rounded.ArrowBack, "Atrás", tint = TextDark)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ingresa el Código",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Introduce el código que te compartieron\npara unirte a la partida.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(48.dp))

                // 3. Input Moderno (Cajas flotantes)
                CodigoInputModerno(
                    value = codigo,
                    onValueChange = { newValue ->
                        val filtered = newValue.uppercase().filter { it.isLetterOrDigit() }
                        if (filtered.length <= 7) {
                            codigo = filtered
                            errorMessage = null
                        }
                    },
                    focusRequester = focusRequester
                )

                Spacer(Modifier.height(32.dp))

                // 4. Mensaje de Error Animado
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                // 5. Botón de Acción Principal
                Button(
                    onClick = {
                        if (codigo.length != 7) {
                            errorMessage = "El código debe tener 7 caracteres"
                            return@Button
                        }
                        errorMessage = null
                        isJoining = true
                        focusManager.clearFocus()

                        scope.launch {
                            try {
                                val codigoFormateado = formatearCodigo(codigo)
                                val response = RetrofitClient.api.unirseSala(
                                    UnirseSalaRequest(codigoFormateado, idEstudiante)
                                )
                                if (response.success) {
                                    onUnido(response.data.id_sala)
                                } else {
                                    errorMessage = response.message ?: "Error al unirse"
                                }
                            } catch (e: HttpException) {
                                errorMessage = when (e.code()) {
                                    400, 404 -> "Sala no encontrada o expirada"
                                    else -> "Error del servidor: ${e.code()}"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de conexión"
                            } finally {
                                isJoining = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)), // Botón redondeado moderno
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    enabled = !isJoining && codigo.length == 7
                ) {
                    if (isJoining) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            "Unirse a la Sala",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CodigoInputModerno(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { focusRequester.requestFocus() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Grupo 1: ABC
            InputGroup(value, 0, 3)

            Spacer(Modifier.width(12.dp))

            // Guion decorativo
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .background(Color.LightGray, CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            // Grupo 2: 1234
            InputGroup(value, 3, 7)
        }

        // TextField Invisible
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .alpha(0f), // Completamente invisible
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                autoCorrect = false
            ),
            cursorBrush = SolidColor(Color.Transparent)
        )
    }

    // Auto-focus al abrir
    LaunchedEffect(Unit) {
        // Pequeño delay para asegurar que la UI esté lista
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }
}

@Composable
fun InputGroup(value: String, startIndex: Int, endIndex: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in startIndex until endIndex) {
            val char = value.getOrNull(i)
            val isFocused = value.length == i
            val isFilled = char != null

            // Diseño de cada cajita
            Box(
                modifier = Modifier
                    .width(40.dp) // Más ancho para mejor legibilidad
                    .height(56.dp) // Más alto (Estilo moderno)
                    .border(
                        width = if (isFocused || isFilled) 2.dp else 1.5.dp,
                        color = if (isFocused) AccentBlue else if (isFilled) AccentBlue.copy(alpha = 0.5f) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = if (isFocused) AccentBlue.copy(alpha = 0.05f) else Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char?.toString() ?: "",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }
    }
}

// Fondo decorativo similar a las otras pantallas
@Composable
fun BackgroundBlobsInput() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFE3F2FD).copy(alpha = 0.6f),
            radius = size.width * 0.5f,
            center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.1f)
        )
        drawCircle(
            color = Color(0xFFFFF3E0).copy(alpha = 0.5f),
            radius = size.width * 0.3f,
            center = androidx.compose.ui.geometry.Offset(x = 0f, y = size.height * 0.85f)
        )
    }
}

// Utilidad (Sin cambios)
private fun formatearCodigo(codigo: String): String {
    val limpio = codigo.replace("-", "")
    return if (limpio.length >= 4) {
        "${limpio.substring(0, 3)}-${limpio.substring(3)}"
    } else {
        limpio
    }
}

// Extension function para alpha (si no usas Compose 1.7+ donde ya viene mejorado)
fun Modifier.alpha(alpha: Float) = this.then(
    Modifier.drawWithContent() {
        if (alpha > 0) drawContent()
    }
)