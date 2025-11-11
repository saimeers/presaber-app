package com.example.presaber.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.presaber.ui.layout.StudentLayout
import com.example.presaber.ui.theme.PresaberTheme

@Composable
fun HomeEstudiante(
    onNavigateToSubject: (SubjectArea) -> Unit = {}
) {
    var selectedNavItem by remember { mutableStateOf(0) }
    val showAccountDialog = remember { mutableStateOf(false) }

    StudentLayout(
        selectedNavItem = selectedNavItem,
        onNavItemSelected = { index ->
            selectedNavItem = index
            // Aquí puedes manejar la navegación a otras pantallas
            when (index) {
                0 -> { /* Ya estamos en Home */ }
                1 -> { /* Navegar a AI */ }
                2 -> { /* Navegar a PVP */ }
                3 -> { /* Navegar a Groups */ }
                4 -> { /* Navegar a Profile */ }
            }
        },
        showAccountDialog = showAccountDialog
    ) { paddingValues ->
        // El contenido del home con el padding del Scaffold
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            HomeContent(
                onSubjectClick = { subject ->
                    onNavigateToSubject(subject)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    PresaberTheme {
        HomeEstudiante()
    }
}