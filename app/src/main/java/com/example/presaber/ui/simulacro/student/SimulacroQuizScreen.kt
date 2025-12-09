package com.example.presaber.ui.simulacro.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.*
import com.example.presaber.utils.SimulacroSessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colores para el Vs
private val MyColor = Color(0xFF5685FF)
private val RivalColor = Color(0xFFFF7043) // Naranja rojizo para el rival

@Composable
fun SimulacroQuizScreen(
    idSimulacro: Int,
    idEstudiante: String,
    onQuizFinished: () -> Unit
) {
    // Bloquear el botón atrás
    BackHandler { }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var preguntas by remember { mutableStateOf<List<PreguntaSimulacro>>(emptyList()) }

    var rival by remember { mutableStateOf<ProgresoSimulacro?>(null) }

    var currentIndex by remember {
        mutableStateOf(SimulacroSessionManager.getCurrentIndex(context))
    }

    var selectedOptionId by remember { mutableStateOf<Int?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // 1. Cargar Preguntas
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.obtenerPreguntasSimulacro(idSimulacro)
            if (response.success) {
                preguntas = response.data.preguntas
                if (currentIndex >= preguntas.size && preguntas.isNotEmpty()) {
                    onQuizFinished()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // 2. Polling inteligente de Rival
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val res = RetrofitClient.api.obtenerProgresoSimulacro(idSimulacro)
                if (res.success) {
                    val listaProgreso = res.data

                    if (rival == null) {
                        // Si no tengo rival, busco uno aleatorio que no sea yo
                        val posiblesRivales = listaProgreso.filter { it.id_estudiante != idEstudiante }
                        if (posiblesRivales.isNotEmpty()) {
                            rival = posiblesRivales.random()
                        }
                    } else {
                        // Si ya tengo rival, actualizo SOLO sus datos (para que la barra se mueva)
                        val rivalActualizado = listaProgreso.find { it.id_estudiante == rival!!.id_estudiante }
                        if (rivalActualizado != null) {
                            rival = rivalActualizado
                        }
                    }
                }
            } catch (e: Exception) { }
            delay(4000) // Actualizar cada 4s
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MyColor) }
        return
    }

    if (currentIndex >= preguntas.size) return

    val preguntaActual = preguntas[currentIndex]
    val totalPreguntas = preguntas.size

    Scaffold(
        topBar = {
            // HEADER TIPO JUEGO (TÚ vs RIVAL)
            RivalHeader(
                miProgresoActual = currentIndex,
                rival = rival,
                totalPreguntas = totalPreguntas
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (selectedOptionId == null) return@Button
                    isSubmitting = true
                    scope.launch {
                        try {
                            RetrofitClient.api.guardarRespuestaSimulacro(
                                idSimulacro,
                                GuardarRespuestaSimulacroRequest(
                                    id_simulacro = idSimulacro,
                                    id_estudiante = idEstudiante,
                                    id_pregunta = preguntaActual.id_pregunta,
                                    id_opcion = selectedOptionId!!,
                                    tiempo_respuesta = 0
                                )
                            )

                            val nextIndex = currentIndex + 1
                            currentIndex = nextIndex
                            selectedOptionId = null
                            SimulacroSessionManager.saveCurrentProgress(context, nextIndex)
                            scrollState.scrollTo(0)

                            if (nextIndex >= preguntas.size) {
                                RetrofitClient.api.finalizarParticipacionSimulacro(
                                    idSimulacro,
                                    FinalizarParticipacionSimulacroRequest(idSimulacro, idEstudiante)
                                )
                                SimulacroSessionManager.clearSession(context)
                                onQuizFinished()
                            }
                        } catch (e: Exception) {
                            // Manejo de error silencioso o Toast
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MyColor),
                enabled = selectedOptionId != null && !isSubmitting
            ) {
                if(isSubmitting) CircularProgressIndicator(color = Color.White) else Text("Siguiente")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(8.dp))

            // --- PREGUNTA ---
            Text(
                text = "Pregunta ${currentIndex + 1} de $totalPreguntas",
                color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = preguntaActual.enunciado ?: "Sin enunciado",
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1B21),
                lineHeight = 24.sp
            )

            if (preguntaActual.imagen != null) {
                Spacer(Modifier.height(16.dp))
                AsyncImage(
                    model = preguntaActual.imagen,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- OPCIONES ---
            preguntaActual.opciones.forEach { opcion ->
                val isSelected = selectedOptionId == opcion.id_opcion
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { selectedOptionId = opcion.id_opcion },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MyColor.copy(alpha = 0.1f) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MyColor else Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedOptionId = opcion.id_opcion },
                            colors = RadioButtonDefaults.colors(selectedColor = MyColor)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = opcion.texto_opcion ?: "Ver imagen",
                            fontSize = 16.sp,
                            color = if(isSelected) MyColor else Color.Black
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTE DE HEADER VS ---
@Composable
fun RivalHeader(
    miProgresoActual: Int,
    rival: ProgresoSimulacro?,
    totalPreguntas: Int
) {
    val rivalProgreso = rival?.preguntas_respondidas ?: 0

    // Animaciones para las barras
    val miPorcentaje by animateFloatAsState(
        targetValue = if (totalPreguntas > 0) miProgresoActual.toFloat() / totalPreguntas else 0f,
        label = "miProgreso"
    )

    val rivalPorcentaje by animateFloatAsState(
        targetValue = if (totalPreguntas > 0) rivalProgreso.toFloat() / totalPreguntas else 0f,
        label = "rivalProgreso"
    )

    Card(
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LADO IZQUIERDO: YO
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Placeholder o Imagen
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MyColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("YO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MyColor)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Tú", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$miProgresoActual/$totalPreguntas", fontSize = 12.sp, color = MyColor, fontWeight = FontWeight.Bold)
                    }
                }

                // CENTRO: VS
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                }

                // LADO DERECHO: RIVAL
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = rival?.nombre?.split(" ")?.firstOrNull() ?: "Buscando...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$rivalProgreso/$totalPreguntas",
                            fontSize = 12.sp,
                            color = RivalColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))

                    if (rival?.photoURL != null) {
                        AsyncImage(
                            model = rival.photoURL,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, RivalColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if(rival != null) RivalColor.copy(alpha = 0.2f) else Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rival?.nombre?.take(1) ?: "?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if(rival != null) RivalColor else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // REDISEÑO DE BARRAS (Estilo Carrera, más claro)
            Spacer(Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Mi Barra
                LinearProgressIndicator(
                    progress = { miPorcentaje },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MyColor,
                    trackColor = MyColor.copy(alpha = 0.1f),
                )
                // Barra Rival
                LinearProgressIndicator(
                    progress = { rivalPorcentaje },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = RivalColor,
                    trackColor = RivalColor.copy(alpha = 0.1f),
                )
            }
        }
    }
}