package com.example.presaber.ui.institution.components.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCard(
    grado: String,
    grupo: String,
    cohorte: Int,
    claveAcceso: String,
    habilitado: Boolean,
    cantidadEstudiantes: Int,
    nombreDocente: String?,
    fotoDocente: String?,
    onToggleHabilitado: () -> Unit,
    showSwitch: Boolean = true // Para controlar si se muestra el switch según el rol
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAccessKeyDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header del curso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Información principal
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Badge de grado
                        Surface(
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Grupo $grado-$grupo",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5B7BC6),
                                    fontSize = 18.sp
                                )
                            )
                        }

                        // Estado badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (habilitado)
                                Color(0xFF4CAF50).copy(alpha = 0.15f)
                            else
                                Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (habilitado) Color(0xFF4CAF50)
                                            else Color(0xFFFF9800)
                                        )
                                )
                                Text(
                                    text = if (habilitado) "Activo" else "Inactivo",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (habilitado) Color(0xFF2E7D32)
                                        else Color(0xFFE65100),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cohorte
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cohorte $cohorte",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Switch de habilitado (solo si showSwitch es true)
                if (showSwitch) {
                    Switch(
                        checked = habilitado,
                        onCheckedChange = { onToggleHabilitado() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFBDBDBD)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            Spacer(modifier = Modifier.height(16.dp))

            // Información secundaria
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Docente
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoDocente != null) {
                            AsyncImage(
                                model = fotoDocente,
                                contentDescription = "Foto docente",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF5B7BC6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = nombreDocente ?: "Sin docente",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (nombreDocente != null) Color(0xFF333333) else Color(0xFF9E9E9E),
                                fontWeight = if (nombreDocente != null) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "Docente",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF9E9E9E),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Estudiantes
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color(0xFF5B7BC6),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$cantidadEstudiantes",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ver clave de acceso
            OutlinedButton(
                onClick = { showAccessKeyDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF5B7BC6)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.5.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver clave de acceso",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }

    // Diálogo para mostrar la clave de acceso
    if (showAccessKeyDialog) {
        AlertDialog(
            onDismissRequest = { showAccessKeyDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = Color(0xFFE3F2FD),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFF5B7BC6),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Clave de acceso",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF485E92)
                    )
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Grupo $grado°$grupo - Cohorte $cohorte",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF666666)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Text(
                            text = claveAcceso,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5B7BC6),
                                letterSpacing = 2.sp
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAccessKeyDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B7BC6),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Cerrar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        )
    }
}