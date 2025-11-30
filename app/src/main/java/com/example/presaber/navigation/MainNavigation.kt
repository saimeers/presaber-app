package com.example.presaber.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.UserRole
import com.example.presaber.ui.auth.Login
import com.example.presaber.ui.home.HomeEstudiante
import com.example.presaber.ui.institution.InstitutionNavHost
import com.example.presaber.viewmodel.AuthState
import com.example.presaber.viewmodel.AuthViewModel

@Composable
fun MainNavigation(codigoSalaCompartido: String? = null) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Guardar el código para usarlo después del login
    var codigoPendiente by remember { mutableStateOf(codigoSalaCompartido) }

    when (val state = authState) {
        is AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is AuthState.NotAuthenticated -> {
            LoginScreen(
                authViewModel = authViewModel,
                codigoSalaCompartido = codigoPendiente
            )
        }

        is AuthState.Authenticated -> {
            RoleBasedNavigation(
                usuario = state.usuario,
                codigoSalaCompartido = codigoPendiente,
                onSignOut = {
                    codigoPendiente = null // Limpiar al cerrar sesión
                    authViewModel.signOut()
                }
            )
        }

        is AuthState.Error -> {
            LoginScreen(
                authViewModel = authViewModel,
                codigoSalaCompartido = codigoPendiente
            )
        }
    }
}

@Composable
private fun LoginScreen(
    authViewModel: AuthViewModel,
    codigoSalaCompartido: String? = null
) {
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    // Mostrar mensaje si viene de un deep link
    LaunchedEffect(codigoSalaCompartido) {
        if (codigoSalaCompartido != null) {
            loginErrorMessage = "Inicia sesión para unirte a la sala $codigoSalaCompartido"
        }
    }

    Login(
        onLoginClick = { email, password ->
            loginErrorMessage = null
            authViewModel.signIn(email, password) { success, error ->
                if (!success) {
                    loginErrorMessage = error ?: "Error al iniciar sesión"
                }
            }
        },
        onGoogleSignInSuccess = { idToken ->
            loginErrorMessage = null
            authViewModel.signInWithGoogle(idToken) { success, error ->
                if (!success && error != null) {
                    loginErrorMessage = error
                }
            }
        },
        loginError = loginErrorMessage
    )
}

@Composable
private fun RoleBasedNavigation(
    usuario: com.example.presaber.data.remote.Usuario,
    codigoSalaCompartido: String? = null,
    onSignOut: () -> Unit
) {
    when (UserRole.fromId(usuario.rol)) {

        UserRole.ESTUDIANTE -> {
            HomeEstudiante(
                usuario = usuario,
                codigoSalaCompartido = codigoSalaCompartido,
                onSignOut = onSignOut
            )
        }

        UserRole.DIRECTOR,
        UserRole.DOCENTE -> {
            // Si es director/docente y viene con código de sala, mostrar mensaje
            if (codigoSalaCompartido != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Los enlaces de sala PvP son solo para estudiantes")
                }
            } else {
                InstitutionNavHost(
                    idInstitucion = usuario.institucion,
                    usuario = usuario,
                    onSignOut = onSignOut
                )
            }
        }

        UserRole.ADMINISTRADOR -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Panel de Administrador - En desarrollo")
            }
        }

        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Rol no reconocido")
            }
        }
    }
}