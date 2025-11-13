package com.example.presaber.ui.institution.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun InstitutionFormButtons(
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextLabel: String = "Siguiente"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {

        // --- Botón Atrás (Outlined) ---
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.height(42.dp),
            shape = RoundedCornerShape(50), // cápsula
            border = BorderStroke(1.dp, Color(0xFFD0D5DD)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF344054)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                "Atrás",
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- Botón Siguiente / Registrar ---
        Button(
            onClick = onNext,
            modifier = Modifier.height(42.dp),
            shape = RoundedCornerShape(50), // cápsula
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA9BCFF), // azul claro del Figma
                contentColor = Color(0xFF1A1B21)     // texto oscuro
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                nextLabel,
                fontSize = 15.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstitutionFormButtonsPreview() {
    InstitutionFormButtons(
        onBack = {},
        onNext = {}
    )
}