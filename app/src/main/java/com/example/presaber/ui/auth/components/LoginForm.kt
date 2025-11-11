package com.example.presaber.ui.auth.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.presaber.utils.ValidationUtils

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

    // Mostrar el error específico (si lo recibe) como texto rojo debajo del botón
    // y también mostramos Toast en Login() — pero aquí lo dejamos visible en UI.
    var localError by remember { mutableStateOf<String?>(null) }

    // Si loginError cambia desde Navigation lo mostramos localmente
    LaunchedEffect(loginError) {
        localError = loginError
    }

    // EMAIL
    OutlinedTextField(
        value = email,
        onValueChange = { input ->
            // quitar saltos/newlines y trim final, impedir espacios en medio? se permite espacio interno si usuario lo pone accidente no deseado
            // aquí eliminamos solo saltos y evitamos espacios al principio/final
            val cleaned = input.replace("\n", "")
            onEmailChange(cleaned.trimEnd())
        },
        label = { Text("Correo electrónico") },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
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
            // impedir salto de línea dentro de password
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

    Text(
        text = "¿Has olvidado tu contraseña?",
        color = Color.Gray,
        fontSize = 13.sp,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = {
            // validaciones UI local
            when {
                email.isBlank() || password.isBlank() -> {
                    Toast.makeText(context, "Completa el correo y la contraseña", Toast.LENGTH_SHORT).show()
                }
                !ValidationUtils.isValidEmail(email) -> {
                    // marcamos borde rojo (isError ya lo hace) y mostramos toast
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
}
