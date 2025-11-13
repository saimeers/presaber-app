package com.example.presaber.ui.institution.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RegisterFormInstitution(
    onFinish: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        RegisterInstitutionHeader()

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(
                        animationSpec = tween(300)
                    ) { it } + fadeIn() togetherWith
                            slideOutHorizontally(
                                animationSpec = tween(300)
                            ) { -it } + fadeOut()
                } else {
                    slideInHorizontally(
                        animationSpec = tween(300)
                    ) { -it } + fadeIn() togetherWith
                            slideOutHorizontally(
                                animationSpec = tween(300)
                            ) { it } + fadeOut()
                }
            },
            label = "FormStepAnim"
        ) { step ->

            when (step) {
                1 -> InstitutionStepBasicScreen(
                    onNext = { currentStep = 2 },
                    onCancel = onCancel
                )

                2 -> InstitutionStepContactScreen(
                    onBack = { currentStep = 1 },
                    onSubmit = onFinish
                )
            }
        }
    }
}

//
// ---------------------------
//   PASO 1 — Validaciones
// ---------------------------
//
@Composable
fun InstitutionStepBasicScreen(
    onNext: () -> Unit,
    onCancel: () -> Unit
) {

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var municipality by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Estados de error
    var showErrors by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Nombre
        InstitutionTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Nombre de la institución."
        )
        if (showErrors && name.isBlank()) ErrorLabel("El nombre es obligatorio")

        Spacer(modifier = Modifier.height(12.dp))

        // Departamento
        InstitutionDropdownField(
            selected = department,
            placeholder = "Departamento",
            options = listOf("Norte de Santander", "Santander", "Cesar"),
            onSelect = { department = it }
        )
        if (showErrors && department.isBlank()) ErrorLabel("Selecciona un departamento")

        Spacer(modifier = Modifier.height(12.dp))

        // Municipio
        InstitutionDropdownField(
            selected = municipality,
            placeholder = "Municipio",
            options = listOf("Cúcuta", "Villa del Rosario", "Los Patios"),
            onSelect = { municipality = it }
        )
        if (showErrors && municipality.isBlank()) ErrorLabel("Selecciona un municipio")

        Spacer(modifier = Modifier.height(12.dp))

        // Dirección
        InstitutionTextField(
            value = address,
            onValueChange = { address = it },
            placeholder = "Dirección"
        )
        if (showErrors && address.isBlank()) ErrorLabel("La dirección es obligatoria")

        Spacer(modifier = Modifier.height(24.dp))

        InstitutionFormButtons(
            onBack = onCancel,
            onNext = {
                showErrors = true
                if (name.isNotBlank() &&
                    department.isNotBlank() &&
                    municipality.isNotBlank() &&
                    address.isNotBlank()
                ) {
                    onNext()
                }
            },
            nextLabel = "Siguiente"
        )
    }
}

//
// ---------------------------
//   PASO 2 — Validaciones
// ---------------------------
//
@Composable
fun InstitutionStepContactScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var admin by remember { mutableStateOf("") }

    var showErrors by remember { mutableStateOf(false) }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPhoneValid(number: String): Boolean {
        return number.matches(Regex("^[0-9]{7,10}\$"))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        InstitutionTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Correo electrónico"
        )
        if (showErrors && !isEmailValid(email)) ErrorLabel("Ingresa un correo válido")

        Spacer(modifier = Modifier.height(12.dp))

        InstitutionTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "Teléfono"
        )
        if (showErrors && !isPhoneValid(phone)) ErrorLabel("Ingresa un número válido (7 a 10 dígitos)")

        Spacer(modifier = Modifier.height(12.dp))

        InstitutionTextField(
            value = admin,
            onValueChange = { admin = it },
            placeholder = "Administrador responsable"
        )
        if (showErrors && admin.isBlank()) ErrorLabel("El nombre del administrador es obligatorio")

        Spacer(modifier = Modifier.height(24.dp))

        InstitutionFormButtons(
            onBack = onBack,
            onNext = {
                showErrors = true
                if (isEmailValid(email) &&
                    isPhoneValid(phone) &&
                    admin.isNotBlank()
                ) {
                    onSubmit()
                }
            },
            nextLabel = "Registrar"
        )
    }
}

//
// ---------------------------
//   COMPONENTE MENSAJE ERROR
// ---------------------------
//
@Composable
fun ErrorLabel(msg: String) {
    Text(
        text = msg,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 2.dp)
    )
}

//
// ---------------------------
//   PREVIEW
// ---------------------------
//
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterFormInstitutionPreview() {
    RegisterFormInstitution()
}
