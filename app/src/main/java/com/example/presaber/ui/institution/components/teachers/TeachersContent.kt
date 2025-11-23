package com.example.presaber.ui.institution.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.presaber.ui.components.AddCard
import com.example.presaber.ui.institution.components.teachers.Teacher
import com.example.presaber.ui.institution.components.teachers.TeacherItem
import com.example.presaber.ui.institution.components.teachers.teacherList
import com.example.presaber.ui.theme.PresaberTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachersContent(
    teachers: List<Teacher>,
    onAddTeacher: () -> Unit,
    onTeacherClick: (Teacher) -> Unit
) {
    var search by remember { mutableStateOf("")}

    Column(modifier = Modifier.padding(16.dp)) {

        // Tarjeta "Agregar profesor"
        AddCard(
            text = "Agregar profesor",
            onClick = onAddTeacher
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth().shadow(2.dp, shape = MaterialTheme.shapes.extraLarge)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.extraLarge
                ),
            placeholder = { Text("Buscar profesor…") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            },
            trailingIcon = {
                Icon(Icons.Default.FilterList, contentDescription = "Filtro")
            },
            shape = MaterialTheme.shapes.extraLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,

                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,

                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = Color.Gray,
                unfocusedLeadingIconColor = Color.Gray,
                focusedTrailingIconColor = Color.Gray,
                unfocusedTrailingIconColor = Color.Gray,
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        val filtered = teachers.filter {
            it.name.contains(search, ignoreCase = true)
        }

        LazyColumn {
            items(filtered) { teacher ->
                TeacherItem(teacher = teacher, onClick = { onTeacherClick(teacher) })
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TeachersContentPreview() {
    PresaberTheme {
        TeachersContent(
            teachers = teacherList,
            onAddTeacher = {},
            onTeacherClick = {}
        )
    }
}