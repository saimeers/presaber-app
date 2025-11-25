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
fun MainNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is AuthState.NotAuthenticated -> {
            LoginScreen(authViewModel)
        }

        is AuthState.Authenticated -> {
            RoleBasedNavigation(
                usuario = state.usuario,
                onSignOut = { authViewModel.signOut() }
            )
        }

        is AuthState.Error -> {
            // Si hay error, mostrar login de nuevo
            LoginScreen(authViewModel)
        }
    }
}

@Composable
private fun LoginScreen(authViewModel: AuthViewModel) {
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

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
    onSignOut: () -> Unit
) {
    when (UserRole.fromId(usuario.rol)) {
        UserRole.ESTUDIANTE -> {
            HomeEstudiante(
                usuario = usuario,
                onSignOut = onSignOut
            )
        }

        UserRole.DIRECTOR -> {
            InstitutionNavHost(
                idInstitucion = usuario.institucion,
                usuario = usuario,
                onSignOut = onSignOut
            )
        }

        UserRole.DOCENTE -> {
            InstitutionNavHost(
                idInstitucion = usuario.institucion,
                usuario = usuario,
                onSignOut = onSignOut
            )
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