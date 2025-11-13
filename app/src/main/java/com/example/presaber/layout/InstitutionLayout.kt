package com.example.presaber.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.ui.layout.AccountDialog
import com.example.presaber.ui.theme.PresaberTheme
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionLayout(
    selectedNavItem: Int = 0,
    onNavItemSelected: (Int) -> Unit = {},
    showAccountDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    content: @Composable (PaddingValues) -> Unit
) {
    val isInPreview = LocalInspectionMode.current

    val currentUser = if (!isInPreview) {
        try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: IllegalStateException) {
            null
        }
    } else null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = buildAnnotatedString {
                            append("Pre")
                            withStyle(style = SpanStyle(color = Color(0xFF5B7ABD))) { append("Saber") }
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1B21)
                    )
                },
                actions = {
                    IconButton(onClick = { showAccountDialog.value = true }) {
                        if (currentUser?.photoUrl != null) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.icon_user),
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFE2E7EE)
                )
            )
        },
        bottomBar = { /* tu barra igual */ },
        content = content
    )

    if (showAccountDialog.value) {
        AccountDialog(onDismiss = { showAccountDialog.value = false })
    }
}

@Preview
@Composable
fun InstitutionLayoutPreview(){
    PresaberTheme {
        InstitutionLayout(
            selectedNavItem = 0,
            onNavItemSelected = {},
            showAccountDialog = remember { mutableStateOf(false) }, // Crea un estado de prueba
            content = { paddingValues ->
                // Pasa un contenido de ejemplo que use el padding del Scaffold
                Text(
                    text = "Este es el contenido de la vista previa",
                    modifier = Modifier.padding(paddingValues)
                )
            }
        )
    }
}
