package com.example.presaber.ui.institution

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presaber.ui.institution.components.SubjectArea

@Composable
fun InstitutionNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "homeQuestion"
    ) {
        // Pantalla principal (lista de áreas)
        composable("homeQuestion") {
            HomeQuestion(
                onNavigateToSubject = { subject: SubjectArea ->
                    navController.navigate(
                        "questions/${subject.title}/${subject.imageRes}"
                    )
                }
            )
        }

        // Pantalla de preguntas por área
        composable(
            route = "questions/{areaName}/{areaIcon}",
            arguments = listOf(
                navArgument("areaName") { type = NavType.StringType },
                navArgument("areaIcon") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val areaName = backStackEntry.arguments?.getString("areaName") ?: "Área"
            val areaIcon = backStackEntry.arguments?.getInt("areaIcon") ?: 0

            // 🔹 Asignar ID según el nombre del área
            val idArea = when (areaName) {
                "Lectura Crítica" -> 1
                "Matemáticas" -> 2
                "Ciencias Naturales" -> 3
                "Ciencias Sociales y Ciudadanas" -> 4
                "Inglés" -> 5
                else -> 0
            }

            // Llamamos correctamente al composable existente
            QuestionsScreen(
                idArea = idArea,
                areaName = areaName,
                areaIcon = areaIcon
            )
        }
    }
}
