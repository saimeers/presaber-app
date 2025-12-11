package com.example.presaber.ui.layout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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

private data class StudentNavItem(
    val index: Int,
    val iconRes: Int? = null,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLayout(
    selectedNavItem: Int = 0,
    onNavItemSelected: (Int) -> Unit = {},
    showAccountDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    usuario: Usuario? = null,
    racha: Int = 0,
    onSignOut: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val isInPreview = LocalInspectionMode.current
    val firebaseAuth = if (!isInPreview) FirebaseAuth.getInstance() else null
    val currentUser = firebaseAuth?.currentUser

    val navItems = remember {
        listOf(
            StudentNavItem(0, iconVector = Icons.Default.Home, description = "Inicio"),
            StudentNavItem(1, iconRes = R.drawable.icon_ai, description = "IA"),
            StudentNavItem(2, iconRes = R.drawable.icon_pvp, description = "PvP"),
            StudentNavItem(3, iconVector = Icons.Default.Group, description = "Grupos"),
            StudentNavItem(4, iconVector = Icons.Default.Person, description = "Perfil")
        )
    }

    Scaffold(
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
                                withStyle(style = SpanStyle(color = Color(0xFF5B7ABD))) {
                                    append("Saber")
                                }
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1B21)
                        )
                        Text(
                            text = "ESTUDIANTE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5B7BC6),
                            letterSpacing = 1.sp,
                            lineHeight = 10.sp
                        )
                    }
                },
                navigationIcon = {
                    // Reemplazamos el IconButton simple por nuestro componente animado
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        StreakBadge(count = racha)
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
                modifier = Modifier.fillMaxSize()
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

/**
 * Componente Animado para la Racha
 */
@Composable
fun StreakBadge(count: Int) {
    // 1. Animación de "respiración" para el fuego (Escala de 1.0 a 1.2)
    val infiniteTransition = rememberInfiniteTransition(label = "fire_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    // Gradiente para el borde (Opcional, para que se vea más pro)
    val borderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF9800), Color(0xFFFF5722))
    )

    Surface(
        shape = RoundedCornerShape(50), // Forma de píldora
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier
            .height(36.dp) // Altura controlada
            .wrapContentWidth()
            .clickable { /* Acción al clicar racha si deseas */ },
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Icono de Fuego Animado
            Icon(
// Definición de ítems para el estudiante
                painter = painterResource(id = R.drawable.fire),
                contentDescription = "Racha",
                tint = Color(0xFFFF5722), // Naranja Fuego
                modifier = Modifier
                    .size(20.dp)
                    .scale(if (count > 0) scale else 1.0f) // Solo anima si hay racha > 0
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Número Animado
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    // El número nuevo entra deslizándose desde arriba, el viejo sale hacia abajo
                    if (targetState > initialState) {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> height } + fadeOut()
                    }
                }, label = "count_animation"
            ) { targetCount ->
                Text(
                    text = "$targetCount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (targetCount > 0) Color(0xFFFF5722) else Color.Gray
                )
            }
        }
    }
}