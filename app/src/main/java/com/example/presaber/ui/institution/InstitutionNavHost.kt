package com.example.presaber.ui.institution

import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.presaber.data.remote.Usuario
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.questions.LocalNavController
import com.example.presaber.ui.institution.screens.*

@Composable
fun InstitutionNavHost(
    idInstitucion: Int,
    usuario: Usuario,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    var selectedNavItem by remember { mutableStateOf(0) }

    CompositionLocalProvider(LocalNavController provides navController) {
        InstitutionLayout(
            selectedNavItem = selectedNavItem,
            onNavItemSelected = { index ->
                selectedNavItem = index
                when (index) {
                    0 -> navController.navigate("homeQuestion") { launchSingleTop = true }
                    1 -> navController.navigate("teachers/$idInstitucion") { launchSingleTop = true }
                    2 -> navController.navigate("homeQuestion") { launchSingleTop = true }
                    3 -> navController.navigate("courses/$idInstitucion") { launchSingleTop = true }
                    4 -> navController.navigate("gamification") { launchSingleTop = true }
                }
            },
            usuario = usuario,
            onSignOut = onSignOut,
            content = { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = "homeQuestion",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("homeQuestion") {
                        HomeQuestion(
                            onNavigateToSubject = { subject ->
                                navController.navigate("questions/${subject.title}/${subject.imageRes}")
                            }
                        )
                    }

                    composable("CreateQuestionScreen") {
                        CreateQuestionScreen()
                    }

                    composable(
                        route = "teachers/{idInstitucion}",
                        arguments = listOf(navArgument("idInstitucion") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idInst = backStackEntry.arguments?.getInt("idInstitucion") ?: idInstitucion
                        TeachersScreen(idInstitucion = idInst)
                    }

                    composable(
                        route = "courses/{idInstitucion}",
                        arguments = listOf(navArgument("idInstitucion") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idInst = backStackEntry.arguments?.getInt("idInstitucion") ?: idInstitucion
                        CoursesScreen(
                            idInstitucion = idInst,
                            navController = navController
                        )
                    }

                    composable(
                        route = "CreateCourseScreen/{idInstitucion}",
                        arguments = listOf(navArgument("idInstitucion") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idInst = backStackEntry.arguments?.getInt("idInstitucion") ?: idInstitucion
                        CreateCourseScreen(
                            navController = navController,
                            idInstitucion = idInst
                        )
                    }

                    composable("gamification") {
                        // Pantalla de gamificación
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

                    composable(
                        route = "editQuestion/{idPregunta}",
                        arguments = listOf(navArgument("idPregunta") { type = NavType.IntType })
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