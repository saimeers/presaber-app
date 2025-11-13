package com.example.presaber.ui.institution.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InstitutionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 15.sp,
                color = Color(0xFF6A6A6A) // hint gris oscuro
            )
        },
        textStyle = TextStyle(
            color = Color(0xFF222222),
            fontSize = 16.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.3.dp,
                color = Color(0xFFA9AEC1), // borde gris azulado
                shape = RoundedCornerShape(12.dp)
            ),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun InstitutionTextFieldPreview() {
    var text by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(16.dp)) {
        InstitutionTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Nombre de la institución."
        )
    }
}
