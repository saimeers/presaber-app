package com.example.presaber.ui.auth

import com.example.presaber.ui.auth.components.LoginForm
import com.example.presaber.ui.auth.components.VerificarForm
import com.example.presaber.ui.auth.components.RegisterForm
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.presaber.R
import com.example.presaber.ui.theme.PresaberTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(onLoginClick: (String, String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var accesoVerificado by remember { mutableStateOf(false) }

    // Datos verificados que se recordarán para registro
    var idInstitucion by remember { mutableStateOf("") }
    var grado by remember { mutableStateOf("") }
    var grupo by remember { mutableStateOf("") }
    var cohorte by remember { mutableStateOf("") }

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
                contentDescription = "Logo",
                modifier = Modifier.size(240.dp),
                contentScale = ContentScale.Fit
            )

            SingleChoiceSegmentedButtonRow {
                listOf("Log in", "Sign in").forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, 2),
                        selected = selectedTab == index,
                        onClick = { if (!accesoVerificado) selectedTab = index },
                        enabled = !accesoVerificado || selectedTab == index,
                        label = { Text(label) }
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
                        onLoginClick = { onLoginClick(email, password) }
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
        Login(onLoginClick = { _, _ -> })
    }
}
