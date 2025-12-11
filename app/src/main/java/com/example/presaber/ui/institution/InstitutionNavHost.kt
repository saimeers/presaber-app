package com.example.presaber.ui.institution

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.Usuario
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.questions.LocalNavController
import com.example.presaber.ui.institution.screens.*
import com.example.presaber.ui.home.screen.StudentProfileScreen
import com.example.presaber.ui.simulacro.*
import com.example.presaber.ui.simulacro.teacher.CrearSimulacroScreen
import com.example.presaber.ui.simulacro.teacher.SimulacroEsperaScreen
import com.example.presaber.ui.simulacro.teacher.SimulacroHomeScreen
import com.example.presaber.ui.simulacro.teacher.SimulacroPodioScreen
import com.example.presaber.ui.simulacro.teacher.SimulacroProgresoScreen

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
                    // ==================== PANTALLAS EXISTENTES ====================

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
                        TeachersScreen(
                            idInstitucion = idInst,
                            onAddTeacher = {
                                navController.navigate("CreateTeacherScreen/$idInst")
                            }
                        )
                    }

                    composable(
                        route = "CreateTeacherScreen/{idInstitucion}",
                        arguments = listOf(navArgument("idInstitucion") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idInst = backStackEntry.arguments?.getInt("idInstitucion") ?: idInstitucion
                        CreateTeacherScreen(
                            navController = navController,
                            idInstitucion = idInst
                        )
                    }

                    composable(
                        route = "courses/{idInstitucion}",
                        arguments = listOf(navArgument("idInstitucion") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idInst = backStackEntry.arguments?.getInt("idInstitucion") ?: idInstitucion
                        CoursesScreen(
                            idInstitucion = idInst,
                            usuario = usuario,
                            navController = navController
                        )
                    }

                    // --- CAMBIO 1: Manejar el click en estudiante ---
                    composable(
                        route = "courseDetail/{grado}/{grupo}/{cohorte}/{idInstitucion}",
                        arguments = listOf(
                            navArgument("grado") { type = NavType.StringType },
                            navArgument("grupo") { type = NavType.StringType },
                            navArgument("cohorte") { type = NavType.IntType },
                            navArgument("idInstitucion") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val grado = backStackEntry.arguments?.getString("grado") ?: ""
                        val grupo = backStackEntry.arguments?.getString("grupo") ?: ""
                        val cohorte = backStackEntry.arguments?.getInt("cohorte") ?: 0
                        val idInst = backStackEntry.arguments?.getInt("idInstitucion") ?: 0

                        CourseDetailScreen(
                            grado = grado,
                            grupo = grupo,
                            cohorte = cohorte,
                            idInstitucion = idInst,
                            userRole = usuario.rol,
                            onBack = { navController.popBackStack() },
                            onStudentClick = { studentId -> // Navegar al perfil
                                navController.navigate("studentProfile/$studentId")
                            }
                        )
                    }

                    // --- CAMBIO 2: Agregar ruta para el perfil ---
                    composable(
                        route = "studentProfile/{studentId}",
                        arguments = listOf(navArgument("studentId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("studentId") ?: ""
                        StudentProfileScreen(
                            studentId = id,
                            onBack = { navController.popBackStack() }
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

                    // ==================== GAMIFICACIÓN / SIMULACRO ====================

                    composable("gamification") {
                        SimulacroHomeScreen(
                            idDocente = usuario.documento,
                            onCrearSimulacro = { cursoSeleccionado ->
                                navController.navigate("crearSimulacro/${cursoSeleccionado.grado}/${cursoSeleccionado.grupo}/${cursoSeleccionado.cohorte}/${cursoSeleccionado.idInstitucion}")
                            },
                            onVerSimulacro = { idSimulacro ->
                                navController.navigate("simulacroDetalle/$idSimulacro")
                            }
                        )
                    }

                    composable(
                        route = "crearSimulacro/{grado}/{grupo}/{cohorte}/{idInstitucion}",
                        arguments = listOf(
                            navArgument("grado") { type = NavType.StringType },
                            navArgument("grupo") { type = NavType.StringType },
                            navArgument("cohorte") { type = NavType.IntType },
                            navArgument("idInstitucion") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val grado = backStackEntry.arguments?.getString("grado") ?: ""
                        val grupo = backStackEntry.arguments?.getString("grupo") ?: ""
                        val cohorte = backStackEntry.arguments?.getInt("cohorte") ?: 0
                        val idInst = backStackEntry.arguments?.getInt("idInstitucion") ?: 0

                        CrearSimulacroScreen(
                            idDocente = usuario.documento,
                            grado = grado,
                            grupo = grupo,
                            cohorte = cohorte,
                            idInstitucion = idInst,
                            onSimulacroCreado = { idSimulacro ->
                                navController.navigate("simulacroEspera/$idSimulacro") {
                                    popUpTo("gamification")
                                }
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "simulacroEspera/{idSimulacro}",
                        arguments = listOf(navArgument("idSimulacro") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0
                        SimulacroEsperaScreen(
                            idSimulacro = idSimulacro,
                            idDocente = usuario.documento,
                            onIniciar = {
                                navController.navigate("simulacroProgreso/$idSimulacro") {
                                    popUpTo("gamification")
                                }
                            }
                        )
                    }

                    composable(
                        route = "simulacroProgreso/{idSimulacro}",
                        arguments = listOf(navArgument("idSimulacro") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0
                        SimulacroProgresoScreen (
                            idSimulacro = idSimulacro,
                            idDocente = usuario.documento,
                            onFinalizar = {
                                navController.navigate("simulacroPodio/$idSimulacro") {
                                    popUpTo("gamification")
                                }
                            }
                        )
                    }

                    composable(
                        route = "simulacroPodio/{idSimulacro}",
                        arguments = listOf(navArgument("idSimulacro") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0
                        SimulacroPodioScreen(
                            idSimulacro = idSimulacro,
                            onAceptar = {
                                navController.navigate("gamification") {
                                    popUpTo("gamification") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "simulacroDetalle/{idSimulacro}",
                        arguments = listOf(navArgument("idSimulacro") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0

                        LaunchedEffect(idSimulacro) {
                            try {
                                val response = RetrofitClient.api.obtenerSimulacro(idSimulacro)
                                if (response.success) {
                                    when (response.data.estado) {
                                        "esperando" -> navController.navigate("simulacroEspera/$idSimulacro") {
                                            popUpTo("gamification")
                                        }
                                        "en_curso" -> navController.navigate("simulacroProgreso/$idSimulacro") {
                                            popUpTo("gamification")
                                        }
                                        "finalizado" -> navController.navigate("simulacroPodio/$idSimulacro") {
                                            popUpTo("gamification")
                                        }
                                        else -> navController.popBackStack()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                navController.popBackStack()
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        )
    }
}