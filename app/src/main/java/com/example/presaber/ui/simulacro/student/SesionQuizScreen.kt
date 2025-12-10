package com.example.presaber.ui.simulacro.student

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.data.remote.PreguntaSesion
import com.example.presaber.viewmodel.SesionEstudianteViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesionQuizScreen(
    idSesion: Int,
    idEstudiante: String,
    sesionNombre: String,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    viewModel: SesionEstudianteViewModel = viewModel()
){
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idSesion, idEstudiante) {
        viewModel.cargarSesion(context, idSesion, idEstudiante)
    }

    var showFinishConfirm by remember { mutableStateOf(false) }
    var currentArea by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.currentIndex) {
        val q = uiState.preguntas.getOrNull(uiState.currentIndex)
        val area = q?.area_nombre
        if (area != null && area != currentArea) {
            currentArea = area
            Toast.makeText(context, "Área: $area", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sesionNombre, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F6FB))
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF5685FF))
            }
            return@Scaffold
        }

        if (!uiState.puedeIngresar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(uiState.mensaje ?: "No puedes ingresar a esta sesión", fontWeight = FontWeight.SemiBold)
                    Button(onClick = onBack) { Text("Volver") }
                }
            }
            return@Scaffold
        }

        val preguntaActual = uiState.preguntas.getOrNull(uiState.currentIndex)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderTimer(
                tiempoRestante = uiState.tiempoRestante,
                totalPreguntas = uiState.totalPreguntas,
                contestadas = uiState.contestadas,
                area = preguntaActual?.area_nombre
            )

            if (preguntaActual != null) {
                QuestionCard(
                    pregunta = preguntaActual,
                    onOptionSelected = { optionId ->
                        viewModel.seleccionarOpcion(context, uiState.currentIndex, optionId)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { viewModel.irA(uiState.currentIndex - 1) },
                    enabled = uiState.currentIndex > 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.NavigateBefore, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Anterior")
                }

                val isLast = uiState.currentIndex >= uiState.preguntas.lastIndex
                Button(
                    onClick = {
                        if (isLast) {
                            showFinishConfirm = true
                        } else {
                            viewModel.irA(uiState.currentIndex + 1)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLast) Color(0xFFEF5350) else Color(0xFF5685FF)
                    )
                ) {
                    Text(if (isLast) "Terminar" else "Siguiente")
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (isLast) Icons.Rounded.CheckCircle else Icons.Rounded.NavigateNext,
                        contentDescription = null
                    )
                }
            }
        }
    }

    if (showFinishConfirm) {
        AlertDialog(
            onDismissRequest = { showFinishConfirm = false },
            title = { Text("Finalizar sesión") },
            text = { Text("¿Estás seguro de finalizar esta sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showFinishConfirm = false
                    viewModel.finalizarSesion(
                        context,
                        onSuccess = onFinish,
                        onError = { msg ->
                            scope.launch {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    error?.let {
        LaunchedEffect(it) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun HeaderTimer(
    tiempoRestante: Int,
    totalPreguntas: Int,
    contestadas: Int,
    area: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6FB)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = area ?: "Área",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1B21)
                )
                Text(
                    text = "$contestadas / $totalPreguntas preguntas",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFDEE9FF))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(formatTimer(tiempoRestante), fontWeight = FontWeight.Bold, color = Color(0xFF2C3E7A))
            }
        }
    }
}

private fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

@Composable
private fun QuestionCard(
    pregunta: PreguntaSesion,
    onOptionSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F6FB)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = pregunta.enunciado ?: "Pregunta",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1B21)
            )

            pregunta.opciones.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionSelected(opcion.id_opcion) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = opcion.id_opcion == pregunta.opcion_seleccionada,
                        onClick = { onOptionSelected(opcion.id_opcion) }
                    )
                    Text(
                        text = opcion.texto_opcion ?: "Opción",
                        fontSize = 15.sp,
                        color = Color(0xFF1A1B21)
                    )
                }
            }
        }
    }
}

