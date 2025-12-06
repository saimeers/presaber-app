package com.example.presaber.ui.auth.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.presaber.data.firebase.FirebaseAuthService
import com.example.presaber.utils.ValidationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    loginError: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado para el modal de recuperación de contraseña
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryEmail by remember { mutableStateOf("") }
    var isSendingEmail by remember { mutableStateOf(false) }

    // Mostrar el error específico (si lo recibe) como texto rojo debajo del botón
    var localError by remember { mutableStateOf<String?>(null) }

    // Si loginError cambia desde Navigation lo mostramos localmente
    LaunchedEffect(loginError) {
        localError = loginError
    }

    // EMAIL
    OutlinedTextField(
        value = email,
        onValueChange = { input ->
            val cleaned = input.replace("\n", "")
            onEmailChange(cleaned.trimEnd())
        },
        label = { Text("Correo electrónico") },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Email
        ),
        singleLine = true,
        isError = email.isNotBlank() && !ValidationUtils.isValidEmail(email),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (email.isNotBlank() && !ValidationUtils.isValidEmail(email)) Color.Red else Color(0xFF1976D2),
            focusedLabelColor = if (email.isNotBlank() && !ValidationUtils.isValidEmail(email)) Color.Red else Color(0xFF1976D2)
        )
    )

    Spacer(Modifier.height(8.dp))

    // PASSWORD
    OutlinedTextField(
        value = password,
        onValueChange = { input ->
            onPasswordChange(input.replace("\n", ""))
        },
        label = { Text("Contraseña") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1976D2),
            focusedLabelColor = Color(0xFF1976D2)
        )
    )

    Spacer(Modifier.height(4.dp))

    // Texto clickeable para recuperar contraseña
    Text(
        text = "¿Has olvidado tu contraseña?",
        color = Color(0xFF1976D2),
        fontSize = 13.sp,
        textAlign = TextAlign.End,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                recoveryEmail = email // Pre-llenar con el email del campo de login
                showRecoveryDialog = true
            }
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = {
            when {
                email.isBlank() || password.isBlank() -> {
                    Toast.makeText(context, "Completa el correo y la contraseña", Toast.LENGTH_SHORT).show()
                }
                !ValidationUtils.isValidEmail(email) -> {
                    Toast.makeText(context, "Correo no válido", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    onLoginClick()
                }
            }
        },
        modifier = Modifier.fillMaxWidth(0.7f),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
    ) {
        Text("Iniciar sesión")
    }

    // Dialog para recuperación de contraseña
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSendingEmail) {
                    showRecoveryDialog = false
                }
            },
            icon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = Color(0xFF1976D2)
                )
            },
            title = {
                Text(
                    "Recuperar contraseña",
                    color = Color(0xFF1976D2)
                )
            },
            text = {
                Column {
                    Text(
                        "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = recoveryEmail,
                        onValueChange = { recoveryEmail = it.trim() },
                        label = { Text("Correo electrónico") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (ValidationUtils.isValidEmail(recoveryEmail) && !isSendingEmail) {
                                    scope.launch {
                                        isSendingEmail = true
                                        val result = FirebaseAuthService.sendPasswordResetEmailSimple(recoveryEmail)
                                        result.fold(
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "Correo enviado. Revisa tu bandeja de entrada.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                showRecoveryDialog = false
                                                recoveryEmail = ""
                                            },
                                            onFailure = { error ->
                                                val errorMessage = when {
                                                    error.message?.contains("user-not-found", ignoreCase = true) == true ->
                                                        "No existe una cuenta con este correo"
                                                    error.message?.contains("invalid-email", ignoreCase = true) == true ->
                                                        "Correo electrónico inválido"
                                                    error.message?.contains("too-many-requests", ignoreCase = true) == true ->
                                                        "Demasiados intentos. Intenta más tarde"
                                                    else -> "Error: ${error.message}"
                                                }
                                                Toast.makeText(
                                                    context,
                                                    errorMessage,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                        isSendingEmail = false
                                    }
                                }
                            }
                        ),
                        singleLine = true,
                        isError = recoveryEmail.isNotBlank() && !ValidationUtils.isValidEmail(recoveryEmail),
                        supportingText = {
                            if (recoveryEmail.isNotBlank() && !ValidationUtils.isValidEmail(recoveryEmail)) {
                                Text("Correo inválido", color = Color.Red)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        ),
                        enabled = !isSendingEmail
                    )
                }
            },
            confirmButton = {
                if (isSendingEmail) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1976D2),
                        strokeWidth = 2.dp
                    )
                } else {
                    TextButton(
                        onClick = {
                            when {
                                recoveryEmail.isBlank() -> {
                                    Toast.makeText(
                                        context,
                                        "Ingresa tu correo electrónico",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                !ValidationUtils.isValidEmail(recoveryEmail) -> {
                                    Toast.makeText(
                                        context,
                                        "Correo electrónico inválido",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    scope.launch {
                                        isSendingEmail = true
                                        val result = FirebaseAuthService.sendPasswordResetEmailSimple(recoveryEmail)
                                        result.fold(
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "Correo enviado.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                showRecoveryDialog = false
                                                recoveryEmail = ""
                                            },
                                            onFailure = { error ->
                                                val errorMessage = when {
                                                    error.message?.contains("user-not-found", ignoreCase = true) == true ->
                                                        "No existe una cuenta con este correo"
                                                    error.message?.contains("invalid-email", ignoreCase = true) == true ->
                                                        "Correo electrónico inválido"
                                                    error.message?.contains("too-many-requests", ignoreCase = true) == true ->
                                                        "Demasiados intentos. Intenta más tarde"
                                                    else -> "${error.message}"
                                                }
                                                Toast.makeText(
                                                    context,
                                                    errorMessage,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                        isSendingEmail = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF1976D2)
                        )
                    ) {
                        Text("Enviar")
                    }
                }
            },
            dismissButton = {
                if (!isSendingEmail) {
                    TextButton(
                        onClick = {
                            showRecoveryDialog = false
                            recoveryEmail = ""
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}