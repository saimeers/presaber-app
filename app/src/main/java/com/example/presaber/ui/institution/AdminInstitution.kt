package com.example.presaber.ui.institution

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.presaber.ui.institution.components.*
import com.example.presaber.ui.layout.AdminLayout

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AdminInstitution() {

    // Control de pantallas:
    // null → lista normal
    // InstitutionDetail → abrir detalle
    var selectedInstitution by remember { mutableStateOf<InstitutionDetail?>(null) }

    // Abrir formulario
    var showForm by remember { mutableStateOf(false) }

    // Estado para la búsqueda
    var searchQuery by remember { mutableStateOf("") }

    // Lista simulada
    val institutions = listOf(
        "Institución Educativa Santo Ángel",
        "Colegio Sagrado Corazón de Jesús",
        "Colegio San Francisco de Sales",
        "Institución Educativa Fe y Alegría",
        "Institución Educativa Nueva Esperanza",
        "Colegio Técnico del Norte"
    ).filter { it.contains(searchQuery, ignoreCase = true) }

    AdminLayout(selectedNavItem = 1) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            //------------------------------------------------------------------
            //  🟦 1) FORMULARIO DE REGISTRO (FULLSCREEN)
            //------------------------------------------------------------------
            AnimatedVisibility(
                visible = showForm,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 }
                ) + fadeIn(tween(250)),
                exit = slideOutVertically(
                    targetOffsetY = { it / 2 }
                ) + fadeOut(tween(250))
            ) {
                RegisterFormInstitution(
                    onFinish = { showForm = false },
                    onCancel = { showForm = false }
                )
            }

            //------------------------------------------------------------------
            //  🟪 2) DETALLE DE INSTITUCIÓN (SLIDE LATERAL)
            //------------------------------------------------------------------
            AnimatedVisibility(
                visible = selectedInstitution != null,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeIn(tween(350)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeOut(tween(350))
            ) {
                selectedInstitution?.let { inst ->
                    InstitutionDetailCard(
                        institution = inst,
                        onBack = { selectedInstitution = null }
                    )
                }
            }

            //------------------------------------------------------------------
            //  🟩 3) LISTA DE INSTITUCIONES (VISTA PRINCIPAL)
            //------------------------------------------------------------------
            AnimatedVisibility(
                visible = !showForm && selectedInstitution == null,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    AddInstitution(onClick = { showForm = true })

                    Spacer(modifier = Modifier.height(16.dp))

                    SearchBarInstitution(
                        onSearch = { query -> searchQuery = query }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (institutions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "No se encontraron instituciones",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    } else {
                        InstitutionList(
                            institutions = institutions,
                            onSelectInstitution = { name ->

                                // Datos fake por ahora, luego vendrán desde Firebase
                                selectedInstitution = InstitutionDetail(
                                    name = name,
                                    email = "correo@institucion.com",
                                    phone = "0000000",
                                    department = "Norte de Santander",
                                    municipality = "Cúcuta",
                                    address = "Dirección ejemplo",
                                    admin = "Juan Pérez"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminInstitutionPreview() {
    CompositionLocalProvider(LocalInspectionMode provides true) {
        AdminInstitution()
    }
}
