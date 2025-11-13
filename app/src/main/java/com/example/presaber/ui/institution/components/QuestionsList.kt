package com.example.presaber.ui.institution.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.presaber.ui.theme.PresaberTheme

data class QuestionItem(
    val id: Int,
    val title: String,
    val tema: String,
    val nivel: String,
    val imagen:String
)

@Composable
fun QuestionsList(
    questions: List<QuestionItem>,
    selectedId: Int?,
    onSelect: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(questions) { question ->
            QuestionCard(
                title = question.title,
                tema = question.tema,
                nivel = question.nivel,
                imagen= question.imagen,
                isSelected = question.id == selectedId,
                onClick = { onSelect(question.id) }
            )
        }
    }
}

@Preview
@Composable
fun QuestionListView(){
    PresaberTheme{
        val sampleQuestions = listOf(
            QuestionItem(id = 1, title = "Pregunta de Álgebra", tema = "Matemáticas", nivel = "Fácil", imagen=""),
            QuestionItem(id = 2, title = "Análisis de Poema", tema = "Lectura Crítica", nivel = "Medio", imagen=""),
            QuestionItem(id = 3, title = "Leyes de Newton", tema = "Ciencias", nivel = "Difícil", imagen=""),
            QuestionItem(id = 4, title = "Verbo 'To Be'", tema = "Inglés", nivel = "Fácil", imagen="")
        )

        // 2. Llama a tu componente con los datos de prueba.
        QuestionsList(
            questions = sampleQuestions,   // Pasa la lista de prueba
            selectedId = 2,                // Simula que la pregunta con id=2 está seleccionada
            onSelect = {}                  // Pasa una función vacía para el clic
        )
    }
}
