package com.example.presaber.ui.institution.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.presaber.ui.theme.PresaberTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsHeader(
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    SearchBar(
        query = query,
        onQueryChange = {
            query = it
            onSearch(it)
        },
        onSearch = {
            active = false
            onSearch(query)
        },
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text("Buscar preguntas...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtrar"
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Contenido del menú desplegable (sugerencias opcionales)
        if (query.isNotEmpty()) {
            Text(
                text = "Buscando: \"$query\"",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuestionsHeaderPreview() {
    PresaberTheme {
        QuestionsHeader(
            onSearch = {},
            onFilterClick = {}
        )
    }
}
