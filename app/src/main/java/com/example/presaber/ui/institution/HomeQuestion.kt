package com.example.presaber.ui.institution

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.HomeQuestionContent
import com.example.presaber.ui.institution.components.SubjectArea
import com.example.presaber.ui.theme.PresaberTheme

@Composable
fun HomeQuestion(
    onNavigateToSubject: (SubjectArea) -> Unit = {}
) {
    var selectedNavItem by remember { mutableStateOf(2) } // Por ejemplo, 2 si este es el ítem de Preguntas
    val showAccountDialog = remember { mutableStateOf(false) }

    InstitutionLayout(
        selectedNavItem = selectedNavItem,
        onNavItemSelected = { index ->
            selectedNavItem = index
            // Aquí puedes manejar navegación según el índice
            when (index) {
                0 -> { /* Ir a inicio */ }
                1 -> { /* Ir a profesores */ }
                2 -> { /* Banco de preguntas (actual) */ }
                3 -> { /* Ir a grupos */ }
                4 -> { /* Ir a configuración/gamificación */ }
            }
        },
        showAccountDialog = showAccountDialog
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            HomeQuestionContent(
                onSubjectClick = { subject ->
                    onNavigateToSubject(subject)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeQuestion() {
    PresaberTheme {
        HomeQuestion()
    }
}
