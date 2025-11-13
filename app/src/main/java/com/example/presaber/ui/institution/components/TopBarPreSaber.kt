package com.example.presaber.ui.institution.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.R
import androidx.compose.ui.graphics.Color


@Preview
@Composable
fun TopBarPreSaber() {
    Surface(
        color = Color(0xFFE2E7EE),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Center
        ) {
            // Logo + texto
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pre",
                    color = Color(0xFF1A1B21),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Saber",
                    color = Color(0xFF4A69BD),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Admin",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Ícono de perfil (o logo circular)
            /*Image(
                painter = painterResource(id = R.drawable.logo_icfes), // cambia por tu recurso
                contentDescription = "Logo",
                modifier = Modifier.size(36.dp)
            )*/
        }
    }
}
