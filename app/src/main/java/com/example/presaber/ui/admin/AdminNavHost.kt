package com.example.presaber.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    // Sincronizar el ítem seleccionado con la ruta actual
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry.destination.route) {
                "home" -> selectedNavItem = 0
                "instituciones" -> selectedNavItem = 1
                "simulacros" -> selectedNavItem = 2
                "usuario" -> selectedNavItem = 3
            }
        }
    }

    AdminLayout(
        selectedNavItem = selectedNavItem,
        onNavItemSelected = { index ->
            selectedNavItem = index
            when (index) {
                0 -> navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
                1 -> navController.navigate("instituciones") {
                    popUpTo("home")
                    launchSingleTop = true
                }
                2 -> navController.navigate("simulacros") {
                    popUpTo("home")
                    launchSingleTop = true
                }
                3 -> navController.navigate("usuario") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            }
        },
        usuario = usuario,
        content = { paddingValues ->
            // 1. El NavHost debe llenar el tamaño disponible
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ==========================================
                // PANTALLA: Home (Dashboard)
                // ==========================================
                composable("home") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Dashboard Admin - Próximamente")
                    }
                }

                // ==========================================
                // PANTALLA: Instituciones
                // ==========================================
                composable("instituciones") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Gestión de Instituciones")
                    }
                }

                // ==========================================
                // PANTALLA: Simulacros
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
                // PANTALLA: Usuario
                // ==========================================
                composable("usuario") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Perfil de Administrador")
                    }
                }

                // ... (Rutas de Crear Simulacro, Asignar, Preguntas se mantienen igual) ...

                composable("crearSimulacro/titulo") {
                    CreateSimulacroTitleScreen(
                        onBack = { navController.popBackStack() },
                        onSimulacroCreado = { id ->
                            navController.navigate("crearSimulacro/sesion/$id/1")
                        }
                    )
                }

                // ... (Resto de rutas copiadas de tu código original) ...

                composable(
                    route = "crearSimulacro/sesion/{idSimulacro}/{numeroSesion}",
                    arguments = listOf(navArgument("idSimulacro") { type = NavType.IntType }, navArgument("numeroSesion") { type = NavType.IntType })
                ) { backStackEntry ->
                    val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0
                    val numeroSesion = backStackEntry.arguments?.getInt("numeroSesion") ?: 1
                    CreateSimulacroSessionScreen(
                        idSimulacro = idSimulacro,
                        numeroSesion = numeroSesion,
                        onBack = { navController.popBackStack() },
                        onNext = {
                            if (numeroSesion == 1) navController.navigate("crearSimulacro/sesion/$idSimulacro/2")
                            else navController.navigate("asignarSimulacro/$idSimulacro")
                        },
                        onNavigateToCreateQuestion = { idArea -> navController.navigate("crearPregunta/$idArea/$idSimulacro/$numeroSesion") },
                        onNavigateToQuestionBank = { idArea -> navController.navigate("bancoPreguntas/$idArea/$idSimulacro/$numeroSesion") }
                    )
                }

                composable(
                    route = "asignarSimulacro/{idSimulacro}",
                    arguments = listOf(navArgument("idSimulacro") { type = NavType.IntType })
                ) { backStackEntry ->
                    val idSimulacro = backStackEntry.arguments?.getInt("idSimulacro") ?: 0
                    AssignSimulacroScreen(
                        idSimulacro = idSimulacro,
                        onBack = { navController.popBackStack() },
                        onSave = { navController.navigate("simulacros") { popUpTo("simulacros") { inclusive = true } } }
                    )
                }

                // Rutas de Preguntas... (Mantener igual)
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