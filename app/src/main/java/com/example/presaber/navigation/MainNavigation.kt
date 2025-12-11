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
import com.example.presaber.ui.admin.AdminNavHost
import com.example.presaber.ui.auth.Login
import com.example.presaber.ui.components.LoadingScreen
import com.example.presaber.ui.components.WelcomeScreen
import com.example.presaber.ui.home.HomeEstudiante
import com.example.presaber.ui.institution.InstitutionNavHost
import com.example.presaber.viewmodel.AuthState
import com.example.presaber.viewmodel.AuthViewModel

@Composable
fun MainNavigation(codigoSalaCompartido: String? = null) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    var showWelcomeScreen by remember { mutableStateOf(true) }

    // Guardar el código para usarlo después del login
    var codigoPendiente by remember { mutableStateOf(codigoSalaCompartido) }

    when (val state = authState) {
        is AuthState.Loading -> {
            // Tu pantalla de carga profesional se mantiene aquí
            LoadingScreen()
        }

        is AuthState.NotAuthenticated -> {
            if (showWelcomeScreen) {
                // Si es la primera vez (o decidimos mostrarla), mostramos la bienvenida
                WelcomeScreen(
                    onNavigateToLogin = {
                        // Cuando termina la animación, ocultamos la bienvenida
                        showWelcomeScreen = false
                        // Y opcionalmente, marcamos en DataStore que ya no es la primera vez
                    }
                )
            } else {
                // Si ya pasaron la bienvenida, mostramos el Login
                LoginScreen(
                    authViewModel = authViewModel,
                    codigoSalaCompartido = codigoPendiente
                )
            }
        }

        is AuthState.Authenticated -> {
            // Si ya están autenticados, no mostramos bienvenida, vamos directo al rol
            RoleBasedNavigation(
                usuario = state.usuario,
                codigoSalaCompartido = codigoPendiente,
                onSignOut = {
                    codigoPendiente = null
                    authViewModel.signOut()
                    // Opcional: al cerrar sesión, ¿quieres volver a mostrar la bienvenida?
                    // showWelcomeScreen = true
                }
            )
        }

        is AuthState.Error -> {
            // Puedes decidir si mostrar bienvenida o login directo en caso de error inicial
            if (showWelcomeScreen) {
                WelcomeScreen(onNavigateToLogin = { showWelcomeScreen = false })
            } else {
                LoginScreen(
                    authViewModel = authViewModel,
                    codigoSalaCompartido = codigoPendiente
                )
            }
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
            AdminNavHost(
                usuario = usuario,
                onSignOut = onSignOut
            )
        }


//        UserRole.ADMINISTRADOR -> {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text("Panel de Administrador - En desarrollo")
//            }
//        }

        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Rol no reconocido")
            }
        }
    }
}