package com.example.presaber.ui.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLayout(
    selectedNavItem: Int = 0,
    onNavItemSelected: (Int) -> Unit = {},
    showAccountDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    content: @Composable (PaddingValues) -> Unit
) {
    // Detecta si es un modo Preview
    val isInPreview = LocalInspectionMode.current

    // Solo usa Firebase si NO es Preview
    val currentUser = if (!isInPreview) {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        } catch (e: IllegalStateException) {
            null
        }
    } else {
        null // 👈 evita inicializar Firebase en Preview
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Texto "PreSaber Admin" con tamaños diferentes
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF1A1B21), fontSize = 22.sp)) {
                                append("Pre")
                            }
                            withStyle(SpanStyle(color = Color(0xFF5B7ABD), fontSize = 22.sp)) {
                                append("Saber")
                            }
                            append(" ") // pequeño espacio antes de Admin
                            withStyle(SpanStyle(color = Color(0xFF3F5D96), fontSize = 16.sp)) {
                                append("Admin")
                            }
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // Fondo elíptico con el ícono centrado
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .width(44.dp)
                            .height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_user),
                            contentDescription = "Usuario",
                            tint = Color(0xFF1A1B21),
                            modifier = Modifier.size(28.dp)
                        )
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
                tonalElevation = 0.dp,
                modifier = Modifier.height(64.dp)
            ) {
                val items = listOf(
                    Triple(R.drawable.icon_home, "Inicio", 0),
                    Triple(R.drawable.icon_institution, "Instituciones", 1),
                    Triple(R.drawable.icon_user_settings, "Configuración", 2)
                )

                items.forEach { (icon, description, index) ->
                    val isSelected = selectedNavItem == index

                    // Animaciones de color y escala
                    val animatedColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF1A1B21) else Color(0xFF5C5F66),
                        label = "iconColor"
                    )
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
                        label = "iconScale"
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = description,
                                tint = animatedColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(animatedScale)
                            )
                        },
                        selected = isSelected,
                        onClick = { onNavItemSelected(index) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color(0xFF1A1B21),
                            unselectedIconColor = Color(0xFF5C5F66)
                        )
                    )
                }
            }
        }
        ,
        content = content
    )

    if (showAccountDialog.value) {
        AccountDialogAdmin(onDismiss = { showAccountDialog.value = false })
    }
}

@Composable
fun AccountDialogAdmin(onDismiss: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF0F3FF),
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.icon_close),
                        contentDescription = "Cerrar"
                    )
                }
                Image(
                    painter = painterResource(R.drawable.icon_institution),
                    contentDescription = "Logo Admin",
                    modifier = Modifier.height(28.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentUser?.photoUrl != null) {
                    AsyncImage(
                        model = currentUser.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.icon_user_settings),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(84.dp)
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
                    text = currentUser?.email ?: "admin@example.com",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color(0xFFB0C4DE), thickness = 1.dp)

                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    AdminAccountOption(icon = R.drawable.icon_user_settings, text = "Configuración") {}
                    AdminAccountOption(icon = R.drawable.icon_logout, text = "Cerrar sesión") {
                        FirebaseAuth.getInstance().signOut()
                        onDismiss()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFB0C4DE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
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
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = Color.Black, fontSize = 15.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminLayoutPreview() {
    AdminLayout(
        selectedNavItem = 1,
        onNavItemSelected = {},
        showAccountDialog = remember { mutableStateOf(false) }
    ) { paddingValues ->
        // Aquí colocas un contenido de ejemplo solo para que se vea algo en la preview
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vista de Administrador",
                fontSize = 18.sp,
                color = Color(0xFF1A1B21)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aquí se mostrará el contenido de cada pantalla",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}


