package com.example.presaber.ui.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presaber.data.remote.Usuario
import com.example.presaber.ui.admin.simulacro.*
import com.example.presaber.ui.institution.screens.CreateQuestionScreen
import com.example.presaber.layout.AdminLayout

@Composable
fun AdminNavHost(
    usuario: Usuario
) {
    val navController = rememberNavController()
    var selectedNavItem by remember { mutableStateOf(0) }

    AdminLayout(
        selectedNavItem = selectedNavItem,
        onNavItemSelected = { index ->
            selectedNavItem = index
            when (index) {
                0 -> navController.navigate("home") {
                    launchSingleTop = true
                }
                1 -> navController.navigate("instituciones") {
                    launchSingleTop = true
                }
                2 -> navController.navigate("simulacros") {
                    launchSingleTop = true
                }
                3 -> navController.navigate("usuario") {
                    launchSingleTop = true
                }
            }
        },
        usuario = usuario,
        content = { paddingValues ->  // ⭐ Recibe paddingValues del AdminLayout
            NavHost(
                navController = navController,
                startDestination = "simulacros",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // ⭐ CLAVE: Respeta el espacio del TopBar y BottomBar
            ) {
                // ==========================================
                // PANTALLA: Home
                // ==========================================
                composable("home") {
                    // TODO: Implementar HomeScreen
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.Text("Home - Por implementar")
                    }
                }

                // ==========================================
                // PANTALLA: Instituciones
                // ==========================================
                composable("instituciones") {
                    // TODO: Implementar InstitucionesScreen
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.Text("Instituciones - Por implementar")
                    }
                }

                // ==========================================
                // PANTALLA: Lista de simulacros
                // ==========================================
                composable("simulacros") {
                    SimulacroListScreen(
                        onCrearSimulacro = {
                            navController.navigate("crearSimulacro/titulo")
                        },
                        onAsignarSimulacro = { idSimulacro ->
                            navController.navigate("asignarSimulacro/$idSimulacro")
                        }
                    )
                }

                // ==========================================
                // PANTALLA: Usuario/Configuración
                // ==========================================
                composable("usuario") {
                    // TODO: Implementar UsuarioScreen
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.Text("Usuario - Por implementar")
                    }
                }

                // ==========================================
                // CREAR SIMULACRO - PASO 1: Título
                // ==========================================
                composable("crearSimulacro/titulo") {
                    CreateSimulacroTitleScreen(
                        onBack = {
                            navController.popBackStack()
                        },
                        onSimulacroCreado = { idSimulacro ->
                            navController.navigate("crearSimulacro/sesion/$idSimulacro/1") {
                                popUpTo("simulacros")
                            }
                        }
                    )
                }

                // ==========================================
                // CREAR SIMULACRO - PASO 2 y 3: Sesiones 1 y 2
                // ==========================================
                composable(
                    route = "crearSimulacro/sesion/{idSimulacro}/{numeroSesion}",
                    arguments = listOf(
                        navArgument("idSimulacro") { type = NavType.IntType },
                        navArgument("numeroSesion") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0
                    val numeroSesion = backStackEntry.arguments?.getInt("numeroSesion") ?: 1

                    CreateSimulacroSessionScreen(
                        idSimulacro = idSimulacro,
                        numeroSesion = numeroSesion,
                        onBack = {
                            if (numeroSesion == 1) {
                                // Si es sesión 1, volver al título
                                navController.popBackStack()
                            } else {
                                // Si es sesión 2, volver a sesión 1
                                navController.navigate("crearSimulacro/sesion/$idSimulacro/1") {
                                    popUpTo("crearSimulacro/sesion/$idSimulacro/1") {
                                        inclusive = true
                                    }
                                }
                            }
                        },
                        onNext = {
                            if (numeroSesion == 1) {
                                // Pasar a sesión 2
                                navController.navigate("crearSimulacro/sesion/$idSimulacro/2")
                            } else {
                                // Finalizar y ir a asignar
                                navController.navigate("asignarSimulacro/$idSimulacro") {
                                    popUpTo("simulacros") {
                                        inclusive = false
                                    }
                                }
                            }
                        },
                        onNavigateToCreateQuestion = { idArea ->
                            navController.navigate("crearPregunta/$idArea/$idSimulacro/$numeroSesion")
                        },
                        onNavigateToQuestionBank = { idArea ->
                            navController.navigate("bancoPreguntas/$idArea/$idSimulacro/$numeroSesion")
                        }
                    )
                }

                // ==========================================
                // ASIGNAR SIMULACRO A GRUPOS/INSTITUCIONES
                // ==========================================
                composable(
                    route = "asignarSimulacro/{idSimulacro}",
                    arguments = listOf(
                        navArgument("idSimulacro") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0

                    AssignSimulacroScreen(
                        idSimulacro = idSimulacro,
                        onBack = {
                            navController.popBackStack()
                        },
                        onSave = {
                            // Volver a la lista de simulacros
                            navController.navigate("simulacros") {
                                popUpTo("simulacros") {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }

                // ==========================================
                // CREAR PREGUNTA NUEVA
                // ==========================================
                composable(
                    route = "crearPregunta/{idArea}/{idSimulacro}/{numeroSesion}",
                    arguments = listOf(
                        navArgument("idArea") { type = NavType.IntType },
                        navArgument("idSimulacro") { type = NavType.IntType },
                        navArgument("numeroSesion") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val idArea = backStackEntry.arguments?.getInt("idArea") ?: 0
                    val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0
                    val numeroSesion = backStackEntry.arguments?.getInt("numeroSesion") ?: 1

                    CreateQuestionScreenWrapper(
                        idArea = idArea,
                        idSimulacro = idSimulacro,
                        numeroSesion = numeroSesion,
                        onQuestionCreated = { pregunta ->
                            // Agregar pregunta al simulacro después de crearla
                            val viewModel: SimulacroAdminViewModel = SimulacroAdminViewModel()

                            viewModel.agregarPreguntaASesion(
                                idSimulacro = idSimulacro,
                                numeroSesion = numeroSesion,
                                idArea = idArea,
                                idPregunta = pregunta.id_pregunta,
                                puntajeBase = 0.5,
                                onSuccess = {
                                    // Volver a la pantalla de sesión
                                    navController.popBackStack()
                                },
                                onError = { error ->
                                    // Error manejado por el ViewModel
                                    // Podrías mostrar un Toast o Snackbar aquí
                                }
                            )
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // ==========================================
                // BANCO DE PREGUNTAS (Seleccionar existentes)
                // ==========================================
                composable(
                    route = "bancoPreguntas/{idArea}/{idSimulacro}/{numeroSesion}",
                    arguments = listOf(
                        navArgument("idArea") { type = NavType.IntType },
                        navArgument("idSimulacro") { type = NavType.IntType },
                        navArgument("numeroSesion") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val idArea = backStackEntry.arguments?.getInt("idArea") ?: 0
                    val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0
                    val numeroSesion = backStackEntry.arguments?.getInt("numeroSesion") ?: 1

                    QuestionBankScreen(
                        idArea = idArea,
                        idSimulacro = idSimulacro,
                        numeroSesion = numeroSesion,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    )
}

/**
 * Wrapper para CreateQuestionScreen con lógica adicional de admin
 */
@Composable
fun CreateQuestionScreenWrapper(
    idArea: Int,
    idSimulacro: Int,
    numeroSesion: Int,
    onQuestionCreated: (Pregunta) -> Unit,
    onBack: () -> Unit
) {
    // Aquí puedes agregar lógica específica del admin antes de mostrar CreateQuestionScreen
    CreateQuestionScreen(
//        idArea = idArea,
//        onQuestionCreated = { pregunta ->
//            onQuestionCreated(pregunta)
//        },
//        onBack = onBack
    )
}

/**
 * Data class para Pregunta (ajustar según tu modelo)
 */
data class Pregunta(
    val id_pregunta: Int,
    val enunciado: String,
    val id_area: Int
    // ... otros campos
)