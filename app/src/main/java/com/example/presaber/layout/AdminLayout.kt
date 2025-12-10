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

// Definimos los ítems de navegación del Admin
private data class AdminNavItem(
    val index: Int,
    val iconRes: Int? = null,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLayout(
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

    // Lista de navegación para el Administrador
    val navItems = remember {
        listOf(
            AdminNavItem(0, iconVector = Icons.Default.Home, description = "Inicio"),
            AdminNavItem(1, iconRes = R.drawable.icon_institution, description = "Instituciones"),
            AdminNavItem(2, iconRes = R.drawable.icon_grupos, description = "Simulacros"),
            AdminNavItem(3, iconRes = R.drawable.icon_user_settings, description = "Usuarios")
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA), // [OPCIONAL] Color de fondo base para evitar cortes blancos
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("Pre")
                                withStyle(style = SpanStyle(color = Color(0xFF5B7ABD))) { append("Saber") }
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1B21),
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "ADMINISTRADOR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5B7BC6),
                            letterSpacing = 1.sp,
                            lineHeight = 10.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAccountDialog.value = true }) {
                        if (currentUser?.photoUrl != null) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = "Perfil",
                                modifier = Modifier.size(32.dp).clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.icon_user),
                                contentDescription = "Perfil",
                                modifier = Modifier.size(32.dp).clip(CircleShape)
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
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            if (item.iconRes != null) {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.description,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else if (item.iconVector != null) {
                                Icon(
                                    imageVector = item.iconVector,
                                    contentDescription = item.description,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = null,
                        selected = selectedNavItem == item.index,
                        onClick = { onNavItemSelected(item.index) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF5B7BC6),
                            indicatorColor = Color(0xFFD8E2F7),
                            unselectedIconColor = Color.Gray
                        ),
                        alwaysShowLabel = false
                    )
                }
            }
        },
        content = { paddingValues ->
            // --- CORRECCIÓN AQUÍ ---
            // Quitamos .padding(paddingValues) de este Box.
            // Solo pasamos paddingValues hacia abajo (al content).
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                content(paddingValues)
            }
        }
    )

    if (showAccountDialog.value) {
        AccountDialogAdmin(
            usuario = usuario,
            onDismiss = { showAccountDialog.value = false },
            onSignOut = onSignOut
        )
    }
}

@Composable
fun AccountDialogAdmin(
    usuario: Usuario?,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF0F3FF),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.icon_close),
                        contentDescription = "Cerrar",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentUser?.photoUrl != null) {
                    AsyncImage(
                        model = currentUser.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.icon_user_settings),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser?.displayName ?: "Administrador",
                    fontSize = 20.sp,
                    color = Color(0xFF1A1B21),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = currentUser?.email ?: "admin@presaber.com",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color(0xFFB0C4DE).copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Opciones del menú
                AdminAccountOption(icon = R.drawable.icon_user_settings, text = "Configuración Global") {}
                AdminAccountOption(icon = R.drawable.icon_logout, text = "Cerrar sesión") {
                    onSignOut()
                    onDismiss()
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun AdminAccountOption(icon: Int, text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1A1B21))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color(0xFF5B7BC6),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}