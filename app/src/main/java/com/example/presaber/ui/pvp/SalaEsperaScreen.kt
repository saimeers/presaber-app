package com.example.presaber.ui.pvp

import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.RetrofitClient
import com.example.presaber.data.remote.SalaPrivada
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// --- Colores y Estilos ---
val TextDark = Color(0xFF1A1B21)
val CodeBlue = Color(0xFF2962FF)
val BackgroundSoft = Color(0xFFFFFFFF)

@Composable
fun SalaEsperaScreen(
    idSala: Int,
    idEstudiante: String,
    onIniciar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sala by remember { mutableStateOf<SalaPrivada?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var copiado by remember { mutableStateOf(false) }

    // --- Lógica de Polling (Igual que antes) ---
    LaunchedEffect(idSala) {
        while (isActive) {
            try {
                val response = RetrofitClient.api.obtenerSala(idSala)
                if (response.success) {
                    sala = response.data
                    if (response.data.estado == "en_curso") {
                        delay(1000)
                        onIniciar()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
            delay(2000)
        }
    }

    if (isLoading || sala == null) {
        PantallaCargaSimple()
        return
    }

    val participantes = sala!!.participantes
    val todoListos = participantes.size >= 2

    // Identificar quién soy yo y quién es el oponente (si existe)
    val soyYo = participantes.find { it.id_estudiante == idEstudiante }
    val oponente = participantes.find { it.id_estudiante != idEstudiante }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        // Fondo decorativo sutil (reusamos blobs para consistencia)
        BackgroundBlobsSutil()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // 1. Título pequeño
            Text(
                text = "Código de la sala",
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            // 2. CÓDIGO GIGANTE (Estilo Píldora)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable {
                        copiarCodigo(context, sala!!.codigo_sala)
                        copiado = true
                        scope.launch { delay(2000); copiado = false }
                    },
                shape = RoundedCornerShape(50.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = sala!!.codigo_sala,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = TextDark
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. Botones de Acción (Iconos limpios)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton(
                    icon = Icons.Rounded.ContentCopy,
                    label = if (copiado) "¡Copiado!" else "Copiar",
                    onClick = {
                        copiarCodigo(context, sala!!.codigo_sala)
                        copiado = true
                        scope.launch { delay(2000); copiado = false }
                    }
                )

                Spacer(Modifier.width(32.dp))

                ActionButton(
                    icon = Icons.Rounded.Share,
                    label = "Compartir",
                    onClick = { compartirCodigo(context, sala!!.codigo_sala, sala!!.area.nombre) }
                )
            }

            Spacer(Modifier.height(60.dp))

            // 4. Área de Jugadores (Avatares)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // MI AVATAR
                JugadorAvatar(
                    url = soyYo?.estudiante?.photoURL,
                    nombre = "Tú",
                    activo = true
                )

                // VS animado o Conector
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(2.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )

                // OPONENTE (O Placeholder animado)
                JugadorAvatar(
                    url = oponente?.estudiante?.photoURL,
                    nombre = oponente?.estudiante?.nombre_completo?.split(" ")?.first() ?: "Esperando...",
                    activo = oponente != null,
                    esPlaceholder = oponente == null
                )
            }

            Spacer(Modifier.weight(1f))

            // 5. Estado del juego
            if (todoListos) {
                Text(
                    text = "¡Jugador encontrado!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text("Iniciando partida...", color = Color.Gray)
            } else {
                LoadingDotsText()
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// --- Componentes UI ---

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFF0F4F8)),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CodeBlue)
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun JugadorAvatar(
    url: String?,
    nombre: String,
    activo: Boolean,
    esPlaceholder: Boolean = false
) {
    // Animación de pulso para el placeholder vacío
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if(esPlaceholder) 1.05f else 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "scale"
    )

    val borderColor = if (activo && !esPlaceholder) CodeBlue else Color.LightGray

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .then(
                    if (esPlaceholder) {
                        Modifier.drawBehind {
                            drawCircle(
                                color = Color.LightGray,
                                style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f))
                            )
                        }
                    } else {
                        Modifier.border(3.dp, borderColor, CircleShape)
                    }
                )
                .padding(4.dp) // Padding entre borde e imagen
                .clip(CircleShape)
                .background(if (esPlaceholder) Color.Transparent else Color.LightGray)
        ) {
            if (activo && !esPlaceholder && url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (esPlaceholder) {
                Icon(
                    imageVector = Icons.Default.Person, // Icono genérico
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(40.dp).align(Alignment.Center)
                )
            } else {
                // Fallback letras
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(nombre.take(1), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = nombre,
            fontSize = 14.sp,
            fontWeight = if(activo) FontWeight.Bold else FontWeight.Normal,
            color = if(activo) TextDark else Color.Gray
        )
    }
}

@Composable
fun LoadingDotsText() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Esperando jugador",
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        // Puntos suspensivos animados simples
        Text(text = ".", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.alpha(if(alpha > 0.3f) 1f else 0f))
        Text(text = ".", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.alpha(if(alpha > 0.6f) 1f else 0f))
        Text(text = ".", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.alpha(if(alpha > 0.9f) 1f else 0f))
    }
}

@Composable
fun BackgroundBlobsSutil() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFE3F2FD).copy(alpha = 0.5f), // Azul muy claro
            radius = size.width * 0.4f,
            center = androidx.compose.ui.geometry.Offset(size.width, 0f)
        )
        drawCircle(
            color = Color(0xFFFFF3E0).copy(alpha = 0.5f), // Naranja muy claro
            radius = size.width * 0.3f,
            center = androidx.compose.ui.geometry.Offset(0f, size.height)
        )
    }
}

@Composable
fun PantallaCargaSimple() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CodeBlue)
    }
}

// --- Funciones de Utilidad (Mantener las existentes) ---
fun copiarCodigo(context: Context, codigo: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Código Sala", codigo)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
}

fun compartirCodigo(context: Context, codigo: String, area: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "¡Únete a mi sala en PreSaber!\nCódigo: $codigo\nÁrea: $area")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir sala"))
}