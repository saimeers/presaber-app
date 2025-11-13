package com.example.presaber.ui.institution.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionDropdownField(
    selected: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        // CAMPO PRINCIPAL
        TextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 15.sp,
                    color = Color(0xFF6A6A6A)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF3A3A3A)
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 1.3.dp,
                    color = Color(0xFFA9AEC1),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // LISTA DESPLEGABLE
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .border(1.dp, Color(0xFFA9AEC1), RoundedCornerShape(8.dp))
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item, fontSize = 15.sp, color = Color.Black) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstitutionDropdownFieldPreview() {
    var selected by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        InstitutionDropdownField(
            selected = selected,
            placeholder = "Departamento",
            options = listOf("Norte de Santander", "Antioquia", "Cundinamarca"),
            onSelect = { selected = it }
        )
    }
}
