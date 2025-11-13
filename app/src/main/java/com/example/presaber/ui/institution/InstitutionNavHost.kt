package com.example.presaber.ui.institution

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presaber.ui.institution.components.SubjectArea
import com.example.presaber.ui.institution.components.LocalNavController  // 🔹 IMPORTA ESTO
import com.example.presaber.ui.institution.CreateQuestionScreen

@Composable
fun InstitutionNavHost() {
    val navController = rememberNavController()

    // 🔹 ENVUELVE TODO CON CompositionLocalProvider
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = "homeQuestion"
        ) {
            composable("homeQuestion") {
                HomeQuestion(
                    onNavigateToSubject = { subject: SubjectArea ->
                        navController.navigate(
                            "questions/${subject.title}/${subject.imageRes}"
                        )
                    }
                )
            }

            // 🔹 DESCOMENTA Y QUITA EL PARÁMETRO navController
            composable("CreateQuestionScreen") {
                CreateQuestionScreen()  // Sin parámetros
            }

            composable(
                route = "questions/{areaName}/{areaIcon}",
                arguments = listOf(
                    navArgument("areaName") { type = NavType.StringType },
                    navArgument("areaIcon") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val areaName = backStackEntry.arguments?.getString("areaName") ?: "Área"
                val areaIcon = backStackEntry.arguments?.getInt("areaIcon") ?: 0

                val idArea = when (areaName) {
                    "Lectura Crítica" -> 1
                    "Matemáticas" -> 2
                    "Ciencias Naturales" -> 3
                    "Ciencias Sociales y Ciudadanas" -> 4
                    "Inglés" -> 5
                    else -> 0
                }

                QuestionsScreen(
                    idArea = idArea,
                    areaName = areaName,
                    areaIcon = areaIcon
                )
            }
        }
    }
}