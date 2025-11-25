package com.example.presaber.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.data.remote.Usuario
import com.example.presaber.ui.layout.AccountDialog
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionLayout(
    selectedNavItem: Int = 0,
    onNavItemSelected: (Int) -> Unit = {},
    showAccountDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    usuario: Usuario? = null,
    onSignOut: () -> Unit = {},
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
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFE2E7EE),
                tonalElevation = 0.dp
            ) {
                // Inicio
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Inicio",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selected = selectedNavItem == 0,
                    onClick = { onNavItemSelected(0) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5B7BC6),
                        indicatorColor = Color(0xFFD8E2F7)
                    ),
                    label = null
                )

                // Profesores
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.icon_profesores),
                            contentDescription = "Profesores",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selected = selectedNavItem == 1,
                    onClick = { onNavItemSelected(1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5B7BC6),
                        indicatorColor = Color(0xFFD8E2F7)
                    ),
                    label = null
                )

                // Preguntas
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.icon_pregunta),
                            contentDescription = "Preguntas",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selected = selectedNavItem == 2,
                    onClick = { onNavItemSelected(2) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5B7BC6),
                        indicatorColor = Color(0xFFD8E2F7)
                    ),
                    label = null
                )

                // Grupos
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.icon_grupos),
                            contentDescription = "Grupos",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selected = selectedNavItem == 3,
                    onClick = { onNavItemSelected(3) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5B7BC6),
                        indicatorColor = Color(0xFFD8E2F7)
                    ),
                    label = null
                )

                // Gamificación
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.icon_gamificacion),
                            contentDescription = "Gamificación",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selected = selectedNavItem == 4,
                    onClick = { onNavItemSelected(4) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5B7BC6),
                        indicatorColor = Color(0xFFD8E2F7)
                    ),
                    label = null
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                content(paddingValues)
            }
        }
    )

    if (showAccountDialog.value) {
        AccountDialog(
            usuario = usuario,
            onDismiss = { showAccountDialog.value = false },
            onSignOut = onSignOut
        )
    }
}