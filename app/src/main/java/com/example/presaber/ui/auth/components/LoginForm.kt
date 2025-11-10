package com.example.presaber.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.utils.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Correo electrónico") },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Email
        ),
        modifier = Modifier.fillMaxWidth(),
        isError = email.isNotBlank() && !ValidationUtils.isValidEmail(email),
        supportingText = {
            if (email.isNotBlank() && !ValidationUtils.isValidEmail(email)) {
                Text("Correo inválido")
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1976D2),
            focusedLabelColor = Color(0xFF1976D2)
        )
    )

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Contraseña") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1976D2),
            focusedLabelColor = Color(0xFF1976D2)
        )
    )

    Spacer(Modifier.height(24.dp))

    Text(
        text = "¿Has olvidado tu contraseña?",
        color = Color.Gray,
        fontSize = 13.sp,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth() )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onLoginClick,
        modifier = Modifier.fillMaxWidth(0.7f),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1976D2)
        )
    ) {
        Text("Iniciar sesión")
    }
}
