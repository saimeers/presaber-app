package com.example.presaber.ui.institution

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presaber.layout.InstitutionLayout
import com.example.presaber.ui.institution.components.questions.BottomButton
import com.example.presaber.ui.institution.components.questions.Buttons
import com.example.presaber.ui.institution.components.questions.LocalNavController
import com.example.presaber.ui.institution.components.questions.OptionsAnswer
import com.example.presaber.ui.institution.components.questions.QuestionStatement
import com.example.presaber.ui.institution.viewmodel.CreateQuestionViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import com.example.presaber.data.remote.PreguntaLoteUI
import com.example.presaber.data.remote.OpcionLoteUI
import com.example.presaber.data.remote.Tema
import com.example.presaber.data.remote.Area
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

// --- UI model que mantiene el estado por cada pregunta en la UI ---
data class PreguntaUI(
    val id: String = java.util.UUID.randomUUID().toString(),
    var enunciado: TextFieldValue = TextFieldValue(""),
    val opciones: MutableList<String> = mutableStateListOf("", "", "", ""),
    val imagenesOpciones: MutableList<File?> = mutableStateListOf(null, null, null, null),
    var imagenPregunta: File? = null,
    var opcionCorrecta: Int? = null,
    var areaId: Int? = null,
    var temaId: Int? = null,
    var nivel: String? = null,
    val temasDisponibles: MutableList<Tema> = mutableStateListOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuestionScreen(
    viewModel: CreateQuestionViewModel = viewModel()
) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val loading by viewModel.loading.collectAsState()
    val areas by viewModel.areas.collectAsState()
    val temasGlobales by viewModel.temas.collectAsState()

    // 🆕 Observar el progreso de subida
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    val context = LocalContext.current

    val preguntas = remember { mutableStateListOf(PreguntaUI()) }
    val scrollState = rememberScrollState()

    // Estados para recomposición forzada cuando se carguen imágenes
    var refreshTrigger by remember { mutableStateOf(0) }

    var lastQuestionIdForQuestionImage by remember { mutableStateOf<String?>(null) }
    var lastQuestionIdForOptionImage by remember { mutableStateOf<String?>(null) }
    var lastOptionIndexForImage by remember { mutableStateOf<Int?>(null) }

    val pickQuestionImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val qId = lastQuestionIdForQuestionImage
            val preguntaIndex = preguntas.indexOfFirst { it.id == qId }
            if (preguntaIndex != -1) {
                preguntas[preguntaIndex].imagenPregunta = uriToFile(context, it, "pregunta_${System.currentTimeMillis()}.jpg")
                refreshTrigger++ // Forzar recomposición
            }
        }
    }

    val pickOptionImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val qId = lastQuestionIdForOptionImage
            val oIdx = lastOptionIndexForImage
            val preguntaIndex = preguntas.indexOfFirst { it.id == qId }
            if (preguntaIndex != -1 && oIdx != null && oIdx in preguntas[preguntaIndex].imagenesOpciones.indices) {
                preguntas[preguntaIndex].imagenesOpciones[oIdx] = uriToFile(context, it, "opcion_${System.currentTimeMillis()}.jpg")
                refreshTrigger++ // Forzar recomposición
            }
        }
    }

    // Observar cambios en temas globales y actualizar las preguntas correspondientes
    LaunchedEffect(temasGlobales) {
        preguntas.forEach { pregunta ->
            if (pregunta.areaId != null) {
                pregunta.temasDisponibles.clear()
                pregunta.temasDisponibles.addAll(temasGlobales)
            }
        }
    }

    fun validarTodas(): Pair<Boolean, String?> {
        if (preguntas.isEmpty()) return Pair(false, "No hay preguntas para guardar")
        preguntas.forEachIndexed { idx, p ->
            if (p.enunciado.text.isBlank() && p.imagenPregunta == null) {
                return Pair(false, "Pregunta ${idx + 1}: falta enunciado o imagen")
            }
            val optsValidas = p.opciones.filter { it.isNotBlank() }
            if (optsValidas.size < 2) {
                return Pair(false, "Pregunta ${idx + 1}: necesita al menos 2 opciones con texto")
            }
            if (p.opcionCorrecta == null) {
                return Pair(false, "Pregunta ${idx + 1}: debes seleccionar la opción correcta")
            }
            if (p.areaId == null) {
                return Pair(false, "Pregunta ${idx + 1}: debes seleccionar un área")
            }
            if (p.temaId == null) {
                return Pair(false, "Pregunta ${idx + 1}: debes seleccionar un tema")
            }
        }
        return Pair(true, null)
    }

    InstitutionLayout(selectedNavItem = 2, onNavItemSelected = {}, showAccountDialog = remember { mutableStateOf(false) }) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Crear Pregunta(s)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF485E92),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                preguntas.forEachIndexed { qIndex, preguntaUI ->
                    key(preguntaUI.id) {
                        PreguntaItem(
                            index = qIndex,
                            pregunta = preguntaUI,
                            areas = areas,
                            refreshTrigger = refreshTrigger,
                            onUpdateEnunciado = { newValue ->
                                preguntas[qIndex] = preguntas[qIndex].copy(enunciado = newValue)
                            },
                            onAddOption = {
                                preguntaUI.opciones.add("")
                                preguntaUI.imagenesOpciones.add(null)
                                scope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
                            },
                            onDeleteOption = { optIndex ->
                                if (preguntaUI.opciones.size > 2) {
                                    preguntaUI.opciones.removeAt(optIndex)
                                    preguntaUI.imagenesOpciones.removeAt(optIndex)
                                    if (preguntaUI.opcionCorrecta != null) {
                                        if (preguntaUI.opcionCorrecta == optIndex) preguntaUI.opcionCorrecta = null
                                        else if (preguntaUI.opcionCorrecta!! > optIndex) preguntaUI.opcionCorrecta = preguntaUI.opcionCorrecta!! - 1
                                    }
                                    refreshTrigger++
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Debe quedar al menos 2 opciones") }
                                }
                            },
                            onPickQuestionImage = {
                                lastQuestionIdForQuestionImage = preguntaUI.id
                                pickQuestionImageLauncher.launch("image/*")
                            },
                            onPickOptionImage = { optIndex ->
                                lastQuestionIdForOptionImage = preguntaUI.id
                                lastOptionIndexForImage = optIndex
                                pickOptionImageLauncher.launch("image/*")
                            },
                            onSelectArea = { areaId ->
                                preguntaUI.areaId = areaId
                                preguntaUI.temaId = null
                                preguntaUI.temasDisponibles.clear()
                                viewModel.cargarTemasPorArea(areaId)
                                refreshTrigger++
                            },
                            onSelectTema = { temaId ->
                                preguntaUI.temaId = temaId
                                refreshTrigger++
                            },
                            onSelectNivel = { nivel ->
                                preguntaUI.nivel = nivel
                                refreshTrigger++
                            },
                            onSelectCorrect = { optIndex ->
                                preguntas[qIndex] = preguntas[qIndex].copy(opcionCorrecta = optIndex)
                                refreshTrigger++
                            },
                            onDeleteQuestion = {
                                if (preguntas.size > 1) {
                                    preguntas.removeAt(qIndex)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Debe quedar al menos 1 pregunta")
                                    }
                                }
                                scope.launch { scrollState.animateScrollTo(0) }
                            },
                            onCreateTema = { descripcion, areaId, callback ->
                                viewModel.crearTema(descripcion, areaId) { success, errorMsg ->
                                    if (success) {
                                        viewModel.cargarTemasPorArea(areaId)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Tema creado exitosamente")
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(errorMsg ?: "Error al crear tema")
                                        }
                                    }
                                    callback(success, errorMsg)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                BottomButton(
                    onBack = { navController.popBackStack() },
                    onNew = {
                        preguntas.add(PreguntaUI())
                        scope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
                    },
                    onSave = {
                        val (ok, msg) = validarTodas()
                        if (!ok) {
                            scope.launch { snackbarHostState.showSnackbar(msg ?: "Error de validación") }
                            return@BottomButton
                        }

                        val preguntasLote = preguntas.map { p ->
                            PreguntaLoteUI(
                                enunciado = p.enunciado.text.ifBlank { null },
                                nivel = p.nivel ?: "Medio",
                                idArea = p.areaId ?: 0,
                                idTema = p.temaId,
                                imagenPregunta = p.imagenPregunta,
                                opciones = p.opciones.mapIndexed { oi, txt ->
                                    OpcionLoteUI(
                                        texto = txt.ifBlank { null },
                                        esCorrecta = (p.opcionCorrecta == oi),
                                        imagenOpcion = p.imagenesOpciones.getOrNull(oi)
                                    )
                                }.filter { it.texto != null || it.imagenOpcion != null }
                            )
                        }

                        viewModel.crearPreguntasLote(preguntasLote) { success, errorMsg ->
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar(
                                        errorMsg ?: "Preguntas creadas correctamente"
                                    )
                                    preguntas.clear()
                                    preguntas.add(PreguntaUI())
                                    scrollState.animateScrollTo(0)
                                } else {
                                    snackbarHostState.showSnackbar(errorMsg ?: "Error creando preguntas")
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 🆕 INDICADOR DE CARGA CON PROGRESO
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .wrapContentHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color(0xFF485E92),
                                strokeWidth = 4.dp
                            )

                            if (uploadProgress.isNotEmpty()) {
                                Text(
                                    text = uploadProgress,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF485E92),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Text(
                                text = "No cierres la aplicación",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Este proceso puede tomar algunos minutos dependiendo del número de imágenes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

@Composable
fun PreguntaItem(
    index: Int,
    pregunta: PreguntaUI,
    areas: List<Area>,
    refreshTrigger: Int,
    onUpdateEnunciado: (TextFieldValue) -> Unit,
    onAddOption: () -> Unit,
    onDeleteOption: (Int) -> Unit,
    onPickQuestionImage: () -> Unit,
    onPickOptionImage: (Int) -> Unit,
    onSelectArea: (Int) -> Unit,
    onSelectTema: (Int) -> Unit,
    onSelectNivel: (String) -> Unit,
    onSelectCorrect: (Int) -> Unit,
    onDeleteQuestion: () -> Unit,
    onCreateTema: (String, Int, (Boolean, String?) -> Unit) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Pregunta ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onDeleteQuestion) {
                    Text("Eliminar", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            QuestionStatement(
                enunciado = pregunta.enunciado,
                onValueChange = onUpdateEnunciado
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sincronizar las imágenes de opciones cada vez que cambie refreshTrigger
            val imagenesPath = remember(refreshTrigger, pregunta.imagenesOpciones.size) {
                mutableStateListOf<String?>().apply {
                    clear()
                    addAll(pregunta.imagenesOpciones.map { it?.path })
                }
            }

            OptionsAnswer(
                opciones = pregunta.opciones,
                imagenes = imagenesPath,
                opcionSeleccionada = pregunta.opcionCorrecta,
                onSelect = onSelectCorrect,
                onChange = { idx, new -> pregunta.opciones[idx] = new },
                onUploadImage = { optIdx -> onPickOptionImage(optIdx) },
                onAddOption = onAddOption,
                onDeleteOption = onDeleteOption
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Usar key para forzar recomposición cuando cambian los estados
            key(pregunta.areaId, pregunta.temaId, pregunta.nivel, pregunta.imagenPregunta, refreshTrigger) {
                Buttons(
                    areas = areas,
                    temas = pregunta.temasDisponibles,
                    selectedAreaId = pregunta.areaId,
                    onSelectArea = onSelectArea,
                    selectedTemaId = pregunta.temaId,
                    onSelectTema = onSelectTema,
                    onCreateTema = onCreateTema,
                    onPickQuestionImage = onPickQuestionImage,
                    onPickOptionImage = { optIdx -> onPickOptionImage(optIdx) },
                    selectedNivel = pregunta.nivel,
                    onSelectNivel = onSelectNivel,
                    hasQuestionImage = pregunta.imagenPregunta != null,
                    optionImagesStatus = pregunta.imagenesOpciones.map { it != null }
                )
            }

            // INDICADORES VISUALES DE IMÁGENES CARGADAS
            if (pregunta.imagenPregunta != null || pregunta.imagenesOpciones.any { it != null }) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pregunta.imagenPregunta != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text("✓ Imagen de pregunta") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                labelColor = Color(0xFF2E7D32)
                            )
                        )
                    }

                    val imagenesOpcionesCargadas = pregunta.imagenesOpciones.count { it != null }
                    if (imagenesOpcionesCargadas > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("✓ $imagenesOpcionesCargadas imagen(es) de opción") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF2196F3).copy(alpha = 0.2f),
                                labelColor = Color(0xFF1565C0)
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun uriToFile(context: android.content.Context, uri: Uri, fileName: String): File {
    val inputStream: InputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("No se pudo abrir el uri")
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { output ->
        inputStream.copyTo(output)
    }
    inputStream.close()
    return file
}