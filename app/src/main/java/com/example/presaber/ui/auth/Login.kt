package com.example.presaber.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.presaber.R
import com.example.presaber.ui.auth.components.GoogleSignInButton
import com.example.presaber.ui.auth.components.LoginForm
import com.example.presaber.ui.auth.components.RegisterForm
import com.example.presaber.ui.auth.components.VerificarForm
import com.example.presaber.ui.theme.PresaberTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
    onLoginClick: (String, String) -> Unit,
    onGoogleSignInSuccess: (String) -> Unit = {}, // idToken
    loginError: String? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accesoVerificado by remember { mutableStateOf(false) }

    // Datos verificados para registro
    var idInstitucion by remember { mutableStateOf("") }
    var grado by remember { mutableStateOf("") }
    var grupo by remember { mutableStateOf("") }
    var cohorte by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("Login", "Google Sign-In result received: ${result.resultCode}")
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            Log.d("Login", "ID Token obtenido: ${idToken?.take(20)}...")

            if (idToken != null) {
                onGoogleSignInSuccess(idToken)
            } else {
                Toast.makeText(context, "Error al obtener token de Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("Login", "Error en Google Sign-In: ${e.statusCode} - ${e.message}")
            if (e.statusCode != 12501) { // 12501 = Usuario canceló
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Configurar Google Sign-In
    fun initiateGoogleSignIn() {
        Log.d("Login", "Iniciando Google Sign-In...")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut() // Forzar selección de cuenta
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    // Mostrar Toast cuando cambia loginError
    LaunchedEffect(loginError) {
        loginError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_icfes),
                contentDescription = "Logo ICFES",
                modifier = Modifier.size(240.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            SingleChoiceSegmentedButtonRow {
                listOf("Log in", "Sign in").forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, 2),
                        selected = selectedTab == index,
                        onClick = { if (!accesoVerificado) selectedTab = index },
                        enabled = !accesoVerificado || selectedTab == index,
                        label = { Text(label) },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color(0xFFD9DFF6),
                            activeContentColor = Color(0xFF1A1B21),
                            inactiveContainerColor = Color(0xFFF5F6FA),
                            inactiveContentColor = Color(0xFF1A1B21)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                selectedTab == 0 && !accesoVerificado -> {
                    LoginForm(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        onLoginClick = { onLoginClick(email, password) },
                        loginError = loginError
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divisor con texto "o"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = "o",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.Gray
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de Google Sign-In
                    GoogleSignInButton(
                        onClick = { initiateGoogleSignIn() }
                    )
                }
                selectedTab == 1 && !accesoVerificado -> {
                    VerificarForm { idInst, grad, grup, coh ->
                        idInstitucion = idInst
                        grado = grad
                        grupo = grup
                        cohorte = coh
                        accesoVerificado = true
                    }
                }
                accesoVerificado -> {
                    RegisterForm(
                        idInstitucion = idInstitucion,
                        grado = grado,
                        grupo = grupo,
                        cohorte = cohorte,
                        onCancel = { accesoVerificado = false }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    PresaberTheme {
        Login(
            onLoginClick = { _, _ -> },
            onGoogleSignInSuccess = {},
            loginError = null
        )
    }
}