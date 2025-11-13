package com.example.presaber.ui.institution.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SearchBarInstitution(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }

    OutlinedTextField(
        value = query,
        onValueChange = {
            query = it
            onSearch(it.text)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                text = "Buscar institución",
                color = Color(0xFF3A3843),
                fontSize = 15.sp,
                textAlign = TextAlign.Start
            )
        },
        trailingIcon = { // 🔍 Icono a la derecha
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar institución",
                tint = Color(0xFF3A3843)
            )
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedContainerColor = Color(0xFFE8E6EF),
            unfocusedContainerColor = Color(0xFFE8E6EF)
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SearchBarInstitutionPreview() {
    MaterialTheme {
        Surface(color = Color(0xFFF8F8FA)) {
            SearchBarInstitution(
                modifier = Modifier.padding(16.dp),
                onSearch = {}
            )
        }
    }
}
