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
import com.example.presaber.layout.components.AccountDialog
import com.google.firebase.auth.FirebaseAuth

private data class NavItem(
    val index: Int,
    val iconRes: Int? = null,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val description: String
)

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

    val esDocente = usuario?.rol == 2
    val esDirector = usuario?.rol == 4

    // Determinar el texto del rol para mostrar
    val rolTexto = when (usuario?.rol) {
        2 -> "DOCENTE"
        4 -> "DIRECTOR"
        else -> ""
    }

    val navItems = remember(esDocente, esDirector) {
        val items = mutableListOf<NavItem>()
        items.add(NavItem(0, iconVector = Icons.Default.Home, description = "Inicio"))
        if (esDirector) {
            items.add(NavItem(1, iconRes = R.drawable.icon_profesores, description = "Profesores"))
        }
        items.add(NavItem(2, iconRes = R.drawable.icon_pregunta, description = "Preguntas"))
        items.add(NavItem(3, iconRes = R.drawable.icon_grupos, description = "Grupos"))
        items.add(NavItem(4, iconRes = R.drawable.icon_gamificacion, description = "Gamificación"))
        items
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Usamos una columna para apilar Título y Subtítulo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Título Principal
                        Text(
                            text = buildAnnotatedString {
                                append("Pre")
                                withStyle(style = SpanStyle(color = Color(0xFF5B7ABD))) { append("Saber") }
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1B21),
                            lineHeight = 24.sp // Ajuste para que no quede muy separado
                        )

                        // Subtítulo del Rol (Letra pequeñita)
                        if (rolTexto.isNotEmpty()) {
                            Text(
                                text = rolTexto,
                                fontSize = 10.sp, // Tamaño pequeño
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5B7BC6), // Un azul suave o gris (Color.Gray)
                                letterSpacing = 1.sp, // Espaciado elegante
                                lineHeight = 10.sp
                            )
                        }
                    }
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
            onSignOut = onSignOut,
            isInPreview = isInPreview
        )
    }
}