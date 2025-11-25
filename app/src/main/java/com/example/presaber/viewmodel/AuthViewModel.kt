package com.example.presaber.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    data class Authenticated(val usuario: Usuario) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val firebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AuthViewModel"
    }

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                Log.d(TAG, "Usuario ya autenticado: ${currentUser.uid}")
                loadUserData(currentUser.uid)
            } else {
                Log.d(TAG, "No hay usuario autenticado")
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }

    private suspend fun loadUserData(uid: String) {
        try {
            Log.d(TAG, "Cargando datos del usuario: $uid")
            _authState.value = AuthState.Loading
            val usuario = RetrofitClient.api.getUsuarioByUidFirebase(uid)
            Log.d(TAG, "Usuario cargado exitosamente: ${usuario.nombre}")
            _authState.value = AuthState.Authenticated(usuario)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar usuario: ${e.message}", e)

            // Verificar si es error 404 o usuario no encontrado
            val isUserNotFound = e.message?.contains("404") == true ||
                    e.message?.contains("not found", ignoreCase = true) == true ||
                    e.message?.contains("Usuario no encontrado", ignoreCase = true) == true

            if (isUserNotFound) {
                Log.w(TAG, "Usuario no registrado en BD, cerrando sesión Firebase")
                firebaseAuth.signOut()
                _authState.value = AuthState.Error("No estás registrado. Por favor, regístrate primero.")
            } else {
                _authState.value = AuthState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Intentando login con email: $email")
                firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
                val uid = firebaseAuth.currentUser?.uid
                if (uid != null) {
                    Log.d(TAG, "Login exitoso, UID: $uid")
                    loadUserData(uid)
                    onResult(true, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en login: ${e.message}", e)
                onResult(false, e.message)
            }
        }
    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Autenticando con Google usando ID Token")

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(firebaseCredential).await()

                val uid = firebaseAuth.currentUser?.uid
                Log.d(TAG, "Firebase Auth exitoso, UID: $uid")

                if (uid != null) {
                    try {
                        loadUserData(uid)
                        onResult(true, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Usuario no existe en BD", e)
                        // Usuario autenticado en Firebase pero no existe en BD
                        firebaseAuth.signOut()
                        _authState.value = AuthState.NotAuthenticated
                        onResult(false, "No estás registrado. Por favor, regístrate primero.")
                    }
                } else {
                    Log.e(TAG, "UID es null después de auth")
                    onResult(false, "Error al obtener datos del usuario")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error en Google Sign-In: ${e.message}", e)
                onResult(false, "Error al iniciar sesión con Google: ${e.localizedMessage}")
            }
        }
    }

    fun signOut() {
        Log.d(TAG, "Cerrando sesión")
        firebaseAuth.signOut()
        _authState.value = AuthState.NotAuthenticated
    }
}