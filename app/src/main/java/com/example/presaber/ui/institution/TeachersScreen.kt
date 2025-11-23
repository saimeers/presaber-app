package com.example.presaber.ui.institution


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.TeachersContent
import com.example.presaber.ui.institution.components.teachers.teacherList
import com.example.presaber.ui.institution.components.teachers.Teacher
import com.example.presaber.ui.theme.PresaberTheme


@Composable
fun TeachersScreen(
    onAddTeacher: () -> Unit = {},
    onTeacherClick: (Teacher) -> Unit = {}
) {
    var selectedNavItem by remember { mutableStateOf(1) }
    val showAccountDialog = remember { mutableStateOf(false) }

    InstitutionLayout(
        selectedNavItem = selectedNavItem,
        onNavItemSelected = { selectedNavItem = it },
        showAccountDialog = showAccountDialog
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            TeachersContent(
                teachers = teacherList,
                onAddTeacher = onAddTeacher,
                onTeacherClick = onTeacherClick
            )
        }
    }
}

// OPCIÓN 1: Preview simple (sólo el componente)
@Preview(showBackground = true, name = "Vista Simple")
@Composable
fun TeachersScreenPreview() {
    PresaberTheme {
        TeachersScreen()
    }
}

