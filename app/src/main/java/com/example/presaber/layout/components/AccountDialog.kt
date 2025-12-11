package com.example.presaber.layout.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.data.remote.Usuario
import com.google.firebase.auth.FirebaseAuth

// Colores estilo Google (Material You - Blue/Grey tint)
private val GoogleSurface = Color(0xFFF7F9FC)
private val GoogleOnSurface = Color(0xFF1F1F1F)
private val GoogleOutline = Color(0xFF747775)

@Composable
fun AccountDialog(
    usuario: Usuario?,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    isInPreview: Boolean = false
) {
    val firebaseAuth = if (!isInPreview) FirebaseAuth.getInstance() else null
    val currentUser = firebaseAuth?.currentUser

    // Estados para navegar a las sub-pantallas
    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showDataPrivacy by remember { mutableStateOf(false) }

    // Si alguna sub-pantalla está activa, mostramos esa en lugar del diálogo principal
    if (showPrivacy) {
        GenericInfoDialog(
            title = "Política de Privacidad",
            content = PrivacyPolicyContent(),
            onBack = { showPrivacy = false }
        )
        return
    }

    if (showTerms) {
        GenericInfoDialog(
            title = "Términos de Uso",
            content = TermsOfUseContent(),
            onBack = { showTerms = false }
        )
        return
    }

    if (showDataPrivacy) {
        GenericInfoDialog(
            title = "Datos y Privacidad",
            content = DataPrivacyContent(),
            onBack = { showDataPrivacy = false }
        )
        return
    }

    // Diálogo Principal
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Para controlar el ancho
    ) {
        Surface(
            modifier = Modifier
                .width(360.dp) // Ancho similar al de Google
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = GoogleSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Header: Botón Cerrar y Logo ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = GoogleOnSurface)
                    }
                    Spacer(Modifier.weight(1f))
                    // Logo de la app o Google (Manteniendo tu recurso)
                    Icon(
                        painter = painterResource(id = R.drawable.icon_google), // O tu logo
                        contentDescription = "Logo",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }

                Spacer(Modifier.height(8.dp))

                // --- Información del Usuario ---
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                ) {
                    if (currentUser?.photoUrl != null) {
                        AsyncImage(
                            model = currentUser.photoUrl,
                            contentDescription = "Foto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.icon_user),
                            contentDescription = "Foto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = usuario?.let { "${it.nombre} ${it.apellido}" }
                        ?: currentUser?.displayName ?: "Usuario",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoogleOnSurface
                )

                Text(
                    text = usuario?.correo ?: currentUser?.email ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(16.dp))

                // Botón "Gestionar tu cuenta" (Estilo Google)
                OutlinedButton(
                    onClick = { /* Acción futura */ },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoogleOutline.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoogleOnSurface)
                ) {
                    Text("Gestionar tu cuenta PreSaber")
                }

                Spacer(Modifier.height(16.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 1.dp)
                Spacer(Modifier.height(8.dp))

                // --- Opciones de Menú ---

                // 1. Datos y Privacidad
                MenuOptionItem(
                    icon = Icons.Outlined.Security,
                    text = "Datos y privacidad",
                    onClick = { showDataPrivacy = true }
                )

                // 2. Cerrar Sesión
                MenuOptionItem(
                    icon = Icons.Outlined.Logout,
                    text = "Cerrar sesión",
                    onClick = {
                        onSignOut()
                        onDismiss()
                    }
                )

                Spacer(Modifier.height(16.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 1.dp)

                // --- Footer: Políticas y Términos ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Política de privacidad",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { showPrivacy = true }
                            .padding(4.dp)
                    )

                    Text(text = " • ", fontSize = 11.sp, color = Color.Gray)

                    Text(
                        text = "Términos de uso",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { showTerms = true }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MenuOptionItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = Color(0xFF444746) // Gris oscuro Google
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = GoogleOnSurface,
            fontWeight = FontWeight.Medium
        )
    }
}