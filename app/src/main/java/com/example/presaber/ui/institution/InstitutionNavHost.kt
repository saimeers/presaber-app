package com.example.presaber.ui.institution

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.questions.SubjectArea
import com.example.presaber.ui.institution.components.questions.LocalNavController
import com.example.presaber.ui.institution.screens.CoursesScreen
import com.example.presaber.ui.institution.screens.CreateCourseScreen
import com.example.presaber.ui.institution.screens.CreateQuestionScreen
import com.example.presaber.ui.institution.screens.EditQuestionScreenWrapper
import com.example.presaber.ui.institution.screens.HomeQuestion
import com.example.presaber.ui.institution.screens.QuestionsScreen
import com.example.presaber.ui.institution.screens.TeachersScreen

@Composable
fun InstitutionNavHost(idInstitucion: Int ) {
    val navController = rememberNavController()
    var selectedNavItem by remember { mutableStateOf(0) }

    CompositionLocalProvider(LocalNavController provides navController) {
        InstitutionLayout(
            selectedNavItem = selectedNavItem,
            onNavItemSelected = { index ->
                selectedNavItem = index // Mantiene compatibilidad
                when (index) {
                    0 -> navController.navigate("homeQuestion") { launchSingleTop = true }
                    1 -> navController.navigate("teachers/$idInstitucion") { launchSingleTop = true }
                    2 -> navController.navigate("HomeQuestion") { launchSingleTop = true }
                    3 -> navController.navigate("CoursesScreen/$idInstitucion") { launchSingleTop = true }
                    4 -> navController.navigate("GamificationScreen") { launchSingleTop = true }
                }
            },
            content = { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = "homeQuestion",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("homeQuestion") {
                        HomeQuestion(onNavigateToSubject = { subject ->
                            navController.navigate("questions/${subject.title}/${subject.imageRes}")
                        })
                    }

                    composable("CreateQuestionScreen") { CreateQuestionScreen() }

                    composable(
                        route = "teachers/{idInstitucion}",
                        arguments = listOf(navArgument("idInstitucion") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idInst = backStackEntry.arguments?.getInt("idInstitucion") ?: 0
                        TeachersScreen(idInstitucion = idInst)
                    }

                    composable(
                        route = "CoursesScreen/{idInstitucion}",
                        arguments = listOf(
                            navArgument("idInstitucion") {
                                type = NavType.IntType
                            }
                        )
                    ) { backStackEntry ->
                        val idInstitucion = backStackEntry.arguments?.getInt("idInstitucion") ?: 1
                        CoursesScreen(
                            idInstitucion = idInstitucion,
                            navController = navController
                        )
                    }
                    
                    composable(
                        route = "CreateCourseScreen/{idInstitucion}",
                        arguments = listOf(
                            navArgument("idInstitucion") {
                                type = NavType.IntType
                            }
                        )
                    ) { backStackEntry ->
                        val idInstitucion = backStackEntry.arguments?.getInt("idInstitucion") ?: 1
                        CreateCourseScreen(
                            navController = navController,
                            idInstitucion = idInstitucion
                        )
                    }

                    composable("GamificationScreen") { /* pantalla de gamificación */ }

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

                    composable(
                        route = "editQuestion/{idPregunta}",
                        arguments = listOf(
                            navArgument("idPregunta") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val idPregunta = backStackEntry.arguments?.getInt("idPregunta")
                        if (idPregunta != null) {
                            EditQuestionScreenWrapper(idPregunta = idPregunta)
                        }
                    }
                }
            }

        )
    }
}
