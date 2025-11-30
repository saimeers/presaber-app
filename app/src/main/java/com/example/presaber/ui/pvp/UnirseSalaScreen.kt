package com.example.presaber.ui.pvp


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.UnirseSalaRequest
import com.example.presaber.ui.theme.PresaberTheme
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

    // Auto unirse si viene de link compartido
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
                errorMessage = if (e.code() == 400) {
                    "Sala no encontrada"
                } else {
                    "Error del servidor: ${e.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isJoining = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Atrás", tint = Color(0xFF1A1B21))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Únete a una sala",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21)
            )
        }

        Spacer(Modifier.height(40.dp))

        // Input código
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Ingresa el código de la sala",
                    fontSize = 16.sp,
                    color = Color(0xFF5F6368),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Campo de código con formato ABC-1234
                CodigoField(
                    value = codigo,
                    onValueChange = { newValue ->
                        // Filtrar solo letras y números, máximo 7 caracteres
                        val filtered = newValue.uppercase().filter { it.isLetterOrDigit() }
                        if (filtered.length <= 7) {
                            codigo = filtered
                            errorMessage = null
                        }
                    },
                    focusRequester = focusRequester,
                    onDone = {
                        focusManager.clearFocus()
                    }
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Formato: ABC-1234",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFC62828),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = !isJoining
            ) {
                Text("Atrás")
            }

            Button(
                onClick = {
                    if (codigo.length != 7) {
                        errorMessage = "Código incompleto. Formato: ABC-1234"
                        return@Button
                    }

                    errorMessage = null
                    isJoining = true

                    scope.launch {
                        try {
                            val codigoFormateado = formatearCodigo(codigo)
                            val response = RetrofitClient.api.unirseSala(
                                UnirseSalaRequest(codigoFormateado, idEstudiante)
                            )

                            if (response.success) {
                                onUnido(response.data.id_sala)
                            } else {
                                errorMessage = response.message ?: "Error al unirse a la sala"
                            }
                        } catch (e: HttpException) {
                            errorMessage = when (e.code()) {
                                400 -> "Sala no encontrada"
                                404 -> "Sala no encontrada"
                                else -> "Error del servidor: ${e.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error de conexión: ${e.message}"
                        } finally {
                            isJoining = false
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF4A261)
                ),
                enabled = !isJoining && codigo.length == 7
            ) {
                if (isJoining) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Unirse")
                }
            }
        }
    }
}

@Composable
fun CodigoField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { focusRequester.requestFocus() },
        contentAlignment = Alignment.Center
    ) {
        // Visualización de las cajas (3 letras + 4 números = 7 cajas + guion)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Primera parte: 3 cajas (ABC)
            for (i in 0 until 3) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(48.dp)
                        .border(
                            width = 2.dp,
                            color = if (i < value.length)
                                Color(0xFF4A6FA5)
                            else
                                Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.getOrNull(i)?.toString() ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1B21)
                    )
                }

                if (i < 2) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            // Separador guion
            Text(
                text = "-",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Segunda parte: 4 cajas (1234)
            for (i in 3 until 7) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(48.dp)
                        .border(
                            width = 2.dp,
                            color = if (i < value.length)
                                Color(0xFF4A6FA5)
                            else
                                Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.getOrNull(i)?.toString() ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1B21)
                    )
                }

                if (i < 6) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        // TextField invisible para capturar el input
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .offset(y = (-9999).dp),
            textStyle = TextStyle(
                color = Color.Transparent,
                fontSize = 1.sp
            ),
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                autoCorrect = false
            )
        )
    }

    // Auto-focus al entrar
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// Función auxiliar para formatear el código ABC1234 -> ABC-1234
private fun formatearCodigo(codigo: String): String {
    val limpio = codigo.replace("-", "")
    return if (limpio.length >= 4) {
        "${limpio.substring(0, 3)}-${limpio.substring(3)}"
    } else {
        limpio
    }
}
