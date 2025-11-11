package com.example.presaber.navigation

import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presaber.ui.auth.Login
import com.example.presaber.ui.home.HomeEstudiante
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val firebaseAuth = FirebaseAuth.getInstance()
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    // Comprobar usuario actual para startDestination
    val currentUser = firebaseAuth.currentUser
    val startDestination = if (currentUser != null) "home" else "login"

    // Observador de estado de autenticación (mantener navegación en sync)
    LaunchedEffect(Unit) {
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            Login(
                onLoginClick = { email, password ->
                    loginErrorMessage = null
                    firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Login correcto -> navegar
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                // Mejor manejo de excepciones por tipo
                                val ex = task.exception
                                val message = when (ex) {
                                    is FirebaseAuthInvalidUserException -> {
                                        // usuario no encontrado / deshabilitado
                                        when (ex.errorCode) {
                                            "ERROR_USER_DISABLED" -> "Usuario deshabilitado"
                                            "ERROR_USER_NOT_FOUND" -> "Correo no registrado"
                                            else -> "Usuario no encontrado"
                                        }
                                    }
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        // credenciales inválidas (contraseña, token)
                                        when (ex.errorCode) {
                                            "ERROR_INVALID_EMAIL" -> "Correo no válido"
                                            "ERROR_WRONG_PASSWORD" -> "Contraseña inválida"
                                            else -> "Credenciales incorrectas"
                                        }
                                    }
                                    is FirebaseNetworkException -> "Error de conexión, verifica tu red"
                                    is FirebaseTooManyRequestsException -> "Demasiados intentos, intenta más tarde"
                                    is FirebaseAuthException -> {
                                        // Fallback a errorCode si está disponible
                                        when (ex.errorCode) {
                                            "ERROR_INVALID_EMAIL" -> "Correo no válido"
                                            "ERROR_USER_NOT_FOUND" -> "Correo no registrado"
                                            "ERROR_WRONG_PASSWORD" -> "Contraseña inválida"
                                            else -> {
                                                Log.e("LoginError", "FirebaseAuthException code=${ex.errorCode} msg=${ex.message}")
                                                "Error al iniciar sesión"
                                            }
                                        }
                                    }
                                    else -> {
                                        Log.e("LoginError", "Exception class=${ex?.javaClass?.simpleName} msg=${ex?.message}")
                                        "Error al iniciar sesión"
                                    }
                                }
                                loginErrorMessage = message
                            }
                        }
                },
                loginError = loginErrorMessage
            )
        }

        composable("home") {
            HomeEstudiante(
                onNavigateToSubject = { subject ->
                    navController.navigate("subject/${subject.title}")
                }
            )
        }

        composable("subject/{subjectId}") { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId")
            // Muestra subject screen si lo necesitas
        }
    }
}
