package com.example.presaber.ui.institution.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class InstitutionDetail(
    val name: String,
    val email: String,
    val phone: String,
    val department: String,
    val municipality: String,
    val address: String,
    val admin: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InstitutionDetailCard(
    institution: InstitutionDetail,
    onBack: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400)
        ) + fadeIn(tween(400)),
        exit = fadeOut()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF9F2FF)) // color rosado suave del Figma
                    .padding(20.dp),
            ) {

                // ========== Encabezado con avatar =============

                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD9D4FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = institution.name.first().uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B4B8C)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            institution.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )
                        Text(
                            institution.email,
                            fontSize = 14.sp,
                            color = Color(0xFF555555)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ========= Campos con etiquetas ===============

                DetailItem(label = "Telefono", value = institution.phone)
                DetailItem(label = "Departamento", value = institution.department)
                DetailItem(label = "Municipio", value = institution.municipality)
                DetailItem(label = "Dirección", value = institution.address)
                DetailItem(label = "Administrador Responsable", value = institution.admin)

                Spacer(modifier = Modifier.height(20.dp))

                // Botón atrás
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5A72D6),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Atrás")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF222222)
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color(0xFF444444),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InstitutionDetailCardPreview() {
    InstitutionDetailCard(
        institution = InstitutionDetail(
            name = "Institución Educativa Santo Ángel",
            email = "santoangel@santoangel.edu.co",
            phone = "75763449",
            department = "Norte de Santander",
            municipality = "Cúcuta",
            address = "Barrio La Rinconada",
            admin = "Juan Perez"
        ),
        onBack = {}
    )
}
