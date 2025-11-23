package com.example.presaber.ui.institution.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.questions.HomeQuestionContent
import com.example.presaber.ui.institution.components.questions.SubjectArea
import com.example.presaber.ui.theme.PresaberTheme

@Composable
fun HomeQuestion(
    onNavigateToSubject: (SubjectArea) -> Unit = {}
) {
    var selectedNavItem by remember { mutableStateOf(2) } // Por ejemplo, 2 si este es el ítem de Preguntas
    val showAccountDialog = remember { mutableStateOf(false) }


    Box(modifier = Modifier) {
        HomeQuestionContent(
            onSubjectClick = { subject ->
                onNavigateToSubject(subject)
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomeQuestion() {
    PresaberTheme {
        HomeQuestion()
    }
}