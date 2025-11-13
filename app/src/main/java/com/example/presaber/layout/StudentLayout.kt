package com.example.presaber.ui.layout

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLayout(
    selectedNavItem: Int = 0,
    onNavItemSelected: (Int) -> Unit = {},
    showAccountDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    content: @Composable (PaddingValues) -> Unit
) {
    val isInPreview = LocalInspectionMode.current
    val firebaseAuth = if (!isInPreview) FirebaseAuth.getInstance() else null
    val currentUser = firebaseAuth?.currentUser

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
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
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Acción del ícono leading */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.fire),
                            contentDescription = "Fire Icon",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
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
                modifier = Modifier.height(64.dp)
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(Icons.Default.Home, null, modifier = Modifier.size(20.dp))
                    },
                    selected = selectedNavItem == 0,
                    onClick = { onNavItemSelected(0) }
                )
                NavigationBarItem(
                    icon = {
                        Icon(painterResource(R.drawable.icon_ai), null, modifier = Modifier.size(20.dp))
                    },
                    selected = selectedNavItem == 1,
                    onClick = { onNavItemSelected(1) }
                )
                NavigationBarItem(
                    icon = {
                        Icon(painterResource(R.drawable.icon_pvp), null, modifier = Modifier.size(20.dp))
                    },
                    selected = selectedNavItem == 2,
                    onClick = { onNavItemSelected(2) }
                )
                NavigationBarItem(
                    icon = {
                        Icon(Icons.Default.Group, null, modifier = Modifier.size(20.dp))
                    },
                    selected = selectedNavItem == 3,
                    onClick = { onNavItemSelected(3) }
                )
                NavigationBarItem(
                    icon = {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
                    },
                    selected = selectedNavItem == 4,
                    onClick = { onNavItemSelected(4) }
                )
            }
        },
        content = content
    )

    // Account Management Dialog
    if (showAccountDialog.value) {
        AccountDialog(
            onDismiss = { showAccountDialog.value = false },
            isInPreview = isInPreview // ✅ Pásalo aquí
        )
    }
}

@Composable
fun AccountDialog(onDismiss: () -> Unit, isInPreview: Boolean = false) {
    val firebaseAuth = if (!isInPreview) FirebaseAuth.getInstance() else null
    val currentUser = firebaseAuth?.currentUser

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFE9F0FB),
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
                    painter = painterResource(R.drawable.icon_google),
                    contentDescription = "Logo",
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
                        painter = painterResource(id = R.drawable.icon_user),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser?.displayName ?: "Saimer Saavedra",
                    fontSize = 20.sp,
                    color = Color(0xFF1A1B21),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = currentUser?.email ?: "saimer@example.com",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color(0xFFB0C4DE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    AccountOption(
                        icon = R.drawable.icon_config,
                        text = "Configuración",
                        onClick = { /* TODO */ }
                    )
                    AccountOption(
                        icon = R.drawable.icon_logout,
                        text = "Cerrar sesión",
                        onClick = {
                            firebaseAuth?.signOut()
                            onDismiss()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFB0C4DE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { /* Política */ }) {
                        Text("Política de privacidad", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun AccountOption(icon: Int, text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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