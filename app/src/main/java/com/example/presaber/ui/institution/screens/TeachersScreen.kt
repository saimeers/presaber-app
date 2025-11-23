package com.example.presaber.ui.institution.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.TeachersContent
import com.example.presaber.ui.institution.components.teachers.teacherList
import com.example.presaber.ui.institution.components.teachers.Teacher
import com.example.presaber.ui.institution.viewmodel.TeachersViewModel
import com.example.presaber.ui.theme.PresaberTheme


@Composable
fun TeachersScreen(
    idInstitucion: Int,
    onAddTeacher: () -> Unit = {},
    onTeacherClick: (Teacher) -> Unit = {}
) {
    val viewModel: TeachersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val teachers by viewModel.teachers.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(idInstitucion) {
        viewModel.loadTeachers(idInstitucion)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error != null -> Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> TeachersContent(
                teachers = teachers,
                onAddTeacher = onAddTeacher,
                onTeacherClick = onTeacherClick
            )
        }
    }
}




// OPCIÓN 1: Preview simple (sólo el componente)
//@Preview(showBackground = true, name = "Vista Simple")
//@Composable
//fun TeachersScreenPreview() {
//    PresaberTheme {
//        TeachersScreen()
//    }
//}

