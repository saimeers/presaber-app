package com.example.presaber.ui.institution.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.presaber.ui.institution.components.teachers.CreateTeacherForm
import com.example.presaber.ui.theme.PresaberTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeacherScreen(
    navController: NavController,
    idInstitucion: Int
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Docente") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            CreateTeacherForm(
                idInstitucion = idInstitucion.toString(),
                onCancel = { navController.popBackStack() },
                onSuccess = {
                    // Opcional: Podrías recargar la lista de docentes aquí
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTeacherScreenPreview() {
    val navController = rememberNavController()

    PresaberTheme {
        CreateTeacherScreen(
            navController = navController,
            idInstitucion = 1
        )
    }
}
