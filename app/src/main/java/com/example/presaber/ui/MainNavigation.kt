package com.example.presaber.navigation

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

    // Observar cambios de autenticación
    var isUserLoggedIn by remember { mutableStateOf(firebaseAuth.currentUser != null) }

    DisposableEffect(Unit) {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val wasLoggedIn = isUserLoggedIn
            val isNowLoggedIn = auth.currentUser != null

            // Solo navegar si el estado cambió
            if (wasLoggedIn != isNowLoggedIn) {
                isUserLoggedIn = isNowLoggedIn

                if (isNowLoggedIn) {
                    // Usuario inició sesión
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    // Usuario cerró sesión
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        onDispose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    val startDestination = if (isUserLoggedIn) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            Login(
                onLoginClick = { email, password ->
                    loginErrorMessage = null
                    firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                val ex = task.exception
                                val message = when (ex) {
                                    is FirebaseAuthInvalidUserException -> {
                                        when (ex.errorCode) {
                                            "ERROR_USER_DISABLED" -> "Usuario deshabilitado"
                                            "ERROR_USER_NOT_FOUND" -> "Correo no registrado"
                                            else -> "Usuario no encontrado"
                                        }
                                    }
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        when (ex.errorCode) {
                                            "ERROR_INVALID_EMAIL" -> "Correo no válido"
                                            "ERROR_WRONG_PASSWORD" -> "Contraseña inválida"
                                            else -> "Credenciales incorrectas"
                                        }
                                    }
                                    is FirebaseNetworkException -> "Error de conexión"
                                    is FirebaseTooManyRequestsException -> "Demasiados intentos"
                                    else -> "Error al iniciar sesión"
                                }
                                loginErrorMessage = message
                            }
                        }
                },
                loginError = loginErrorMessage
            )
        }

        composable("home") {
            HomeEstudiante()
        }
    }
}