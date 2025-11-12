package com.example.presaber.ui.institution

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.presaber.ui.institution.components.*
import com.example.presaber.ui.layout.AdminLayout

@Composable
fun AdminInstitution() {
    // Estado para la búsqueda
    var searchQuery by remember { mutableStateOf("") }

    // Lista simulada (filtrada según el texto)
    val institutions = listOf(
        "Institución Educativa Santo Ángel",
        "Colegio Sagrado Corazón de Jesús",
        "Colegio San Francisco de Sales",
        "Institución Educativa Fe y Alegría",
        "Institución Educativa Nueva Esperanza",
        "Colegio Técnico del Norte"
    ).filter { it.contains(searchQuery, ignoreCase = true) }

    AdminLayout(selectedNavItem = 1) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Botón agregar institución
            AddInstitution()

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de búsqueda
            SearchBarInstitution(
                onSearch = { query -> searchQuery = query }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de instituciones
            if (institutions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No se encontraron instituciones",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                InstitutionList(
                    institutions = institutions,
                    onSelectInstitution = { name ->
                        println("Seleccionó: $name")
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminInstitutionPreview() {
    CompositionLocalProvider(LocalInspectionMode provides true) {
        // Simula entorno sin Firebase
        AdminInstitution()
    }
}
