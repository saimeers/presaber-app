package com.example.presaber.ui.institution.components.questions

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*

@Composable
fun QuestionStatement(
    enunciado: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    OutlinedTextField(
        value = enunciado,
        onValueChange = onValueChange,
        label = { Text("Enunciado") },
        placeholder = { Text("¿El cero es un entero positivo o negativo?") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}
