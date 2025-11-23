package com.example.presaber.ui.institution.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.presaber.data.remote.*
import com.example.presaber.ui.institution.components.questions.LocalNavController
import com.example.presaber.ui.institution.viewmodel.QuestionsViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionScreen(
    pregunta: Pregunta,
    viewModel: QuestionsViewModel,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Estado de guardado
    var isGuardando by remember { mutableStateOf(false) }

    // Estados de la pregunta
    var enunciado by remember { mutableStateOf(pregunta.enunciado ?: "") }
    var nivel by remember { mutableStateOf(pregunta.nivel_dificultad ?: "") }
    var selectedAreaId by remember { mutableStateOf(pregunta.id_area) }
    var selectedTemaId by remember { mutableStateOf(pregunta.id_tema) }

    var imagenFile by remember { mutableStateOf<File?>(null) }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var eliminarImagenPregunta by remember { mutableStateOf(false) }

    // Observar StateFlows del ViewModel
    val areas by viewModel.areas.collectAsState()
    val temas by viewModel.temas.collectAsState()

    // Cargar áreas al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarAreas()
        selectedAreaId?.let { areaId ->
            viewModel.cargarTemasPorArea(areaId)
        }
    }

    // Cargar temas cuando cambia el área seleccionada
    LaunchedEffect(selectedAreaId) {
        selectedAreaId?.let { areaId ->
            viewModel.cargarTemasPorArea(areaId)
        }
    }

    // Launcher para imagen de pregunta
    val pickQuestionImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        pickedImageUri = uri
        eliminarImagenPregunta = false
        uri?.let {
            val input = context.contentResolver.openInputStream(it)
            val tempFile = File(context.cacheDir, "img_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { output -> input?.copyTo(output) }
            imagenFile = tempFile
        }
    }

    // Estado de opciones
    var opcionesState = remember {
        mutableStateListOf<OpcionEditState>().apply {
            addAll(pregunta.opciones.map {
                OpcionEditState(
                    opcionOriginal = it,
                    texto = it.texto_opcion ?: "",
                    esCorrecta = it.es_correcta == true,
                    imagenActual = it.imagen,
                    imagenNueva = null,
                    imagenUri = null,
                    eliminarImagen = false
                )
            })
        }
    }

    // Paleta de colores
    val colorInactivo = Color(0xFFD2DEFF)
    val colorActivo = Color(0xFF5B7BC6)
    val colorActivoOscuro = Color(0xFF3D5A8F)
    val textoInactivo = Color(0xFF3D5A8F)
    val textoActivo = Color.White

    if (isGuardando) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = colorActivo)
                    Text(
                        text = "Guardando cambios...",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorActivo
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorActivo
                        )
                    }
                    Text(
                        text = "Editar Pregunta",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorActivo,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp)) // Balance visual
                }
            }

            // Enunciado
            item {
                OutlinedTextField(
                    value = enunciado,
                    onValueChange = { enunciado = it },
                    label = { Text("Enunciado de la pregunta") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorActivo,
                        focusedLabelColor = colorActivo,
                        cursorColor = colorActivo
                    )
                )
            }

            // Imagen de la pregunta con diseño mejorado
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            // Imagen nueva seleccionada
                            pickedImageUri != null -> {
                                AsyncImage(
                                    model = pickedImageUri,
                                    contentDescription = "Imagen nueva",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Imagen existente y no eliminada
                            !pregunta.imagen.isNullOrEmpty() && !eliminarImagenPregunta -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(pregunta.imagen)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Imagen actual",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Sin imagen - mostrar icono
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Sin imagen",
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sin imagen",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botones de imagen
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { pickQuestionImageLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorActivo,
                            contentColor = textoActivo
                        )
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (pickedImageUri != null || !pregunta.imagen.isNullOrEmpty()) "Cambiar" else "Subir")
                    }

                    if (!pregunta.imagen.isNullOrEmpty() || pickedImageUri != null) {
                        OutlinedButton(
                            onClick = {
                                pickedImageUri = null
                                imagenFile = null
                                eliminarImagenPregunta = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    }
                }
            }

            // Selectores: Nivel, Área, Tema
            item {
                SelectorsSection(
                    nivel = nivel,
                    onNivelSelected = { nivel = it },
                    areas = areas,
                    selectedAreaId = selectedAreaId,
                    onAreaSelected = {
                        selectedAreaId = it
                        selectedTemaId = null
                        viewModel.cargarTemasPorArea(it)
                    },
                    temas = temas,
                    selectedTemaId = selectedTemaId,
                    onTemaSelected = { selectedTemaId = it },
                    onCreateTema = { descripcion, areaId ->
                        viewModel.crearTema(descripcion, areaId) { success, message ->
                            if (success) {
                                // El tema se cargará automáticamente por el LaunchedEffect
                                scope.launch {
                                    snackbarHostState.showSnackbar("Tema creado exitosamente")
                                }
                                // Seleccionar el nuevo tema (será el último de la lista recargada)
                                scope.launch {
                                    kotlinx.coroutines.delay(300)
                                    selectedTemaId = temas.lastOrNull()?.id_tema
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message ?: "Error al crear tema")
                                }
                            }
                        }
                    },
                    colorActivo = colorActivo,
                    colorInactivo = colorInactivo,
                    textoActivo = textoActivo,
                    textoInactivo = textoInactivo
                )
            }

            // Título de opciones
            item {
                Text(
                    text = "Opciones de respuesta",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorActivo
                )
            }

            // Lista de opciones
            itemsIndexed(opcionesState) { index, opcionState ->
                EditOptionItem(
                    opcionState = opcionState,
                    onTextoChange = { opcionesState[index] = opcionState.copy(texto = it) },
                    onCorrectaChange = {
                        opcionesState.replaceAll { op ->
                            op.copy(esCorrecta = op.opcionOriginal.id_opcion == opcionState.opcionOriginal.id_opcion)
                        }
                    },
                    onImagePicked = { uri, file ->
                        opcionesState[index] = opcionState.copy(
                            imagenUri = uri,
                            imagenNueva = file,
                            eliminarImagen = false
                        )
                    },
                    onImageDeleted = {
                        opcionesState[index] = opcionState.copy(
                            imagenUri = null,
                            imagenNueva = null,
                            eliminarImagen = true
                        )
                    },
                    colorActivo = colorActivo
                )
            }

            // Botón guardar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Cancelar
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = !isGuardando,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorActivo
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Atrás", style = MaterialTheme.typography.titleMedium)
                    }

                    // Botón Guardar
                    Button(
                        onClick = {
                            // Validaciones
                            if (enunciado.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("El enunciado es requerido")
                                }
                                return@Button
                            }
                            if (nivel.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Selecciona un nivel de dificultad")
                                }
                                return@Button
                            }
                            if (selectedAreaId == null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Selecciona un área")
                                }
                                return@Button
                            }
                            if (!opcionesState.any { it.esCorrecta }) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Debes marcar una opción como correcta")
                                }
                                return@Button
                            }

                            isGuardando = true

                            val preguntaData = EditarPreguntaUI(
                                idPregunta = pregunta.id_pregunta,
                                enunciado = enunciado,
                                nivel = nivel,
                                idArea = selectedAreaId!!,
                                idTema = selectedTemaId,
                                imagenNueva = imagenFile
                            )

                            val opcionesData = opcionesState.map { op ->
                                EditarOpcionUI(
                                    idOpcion = op.opcionOriginal.id_opcion,
                                    texto = op.texto,
                                    esCorrecta = op.esCorrecta,
                                    imagenNueva = op.imagenNueva,
                                    eliminarImagen = op.eliminarImagen
                                )
                            }

                            // Log para debug
                            println("=== DEBUG EDITAR PREGUNTA ===")
                            println("Pregunta ID: ${pregunta.id_pregunta}")
                            println("Enunciado: $enunciado")
                            println("Nivel: $nivel")
                            println("Área: $selectedAreaId")
                            println("Tema: $selectedTemaId")
                            println("Eliminar imagen pregunta: $eliminarImagenPregunta")
                            println("Imagen nueva pregunta: ${imagenFile?.name}")
                            opcionesData.forEachIndexed { index, op ->
                                println("Opción $index - ID: ${op.idOpcion}, Texto: ${op.texto}, Correcta: ${op.esCorrecta}, Imagen nueva: ${op.imagenNueva?.name}, Eliminar: ${op.eliminarImagen}")
                            }
                            println("=============================")

                            viewModel.editarPreguntaCompleta(
                                pregunta = preguntaData,
                                opciones = opcionesData,
                                eliminarImagenPregunta = eliminarImagenPregunta
                            ) { success, message ->
                                isGuardando = false

                                if (success) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Cambios guardados exitosamente")
                                        kotlinx.coroutines.delay(100)

                                        // ✅ Limpiar el estado de la pregunta en el ViewModel
                                        viewModel.limpiarPregunta()

                                        // ✅ Navegar de vuelta a QuestionsScreen
                                        navController.popBackStack()
                                    }
                                } else {
                                    val errorMsg = message ?: "Error al guardar cambios"
                                    println("ERROR AL GUARDAR: $errorMsg")
                                    scope.launch {
                                        snackbarHostState.showSnackbar("❌ $errorMsg")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = !isGuardando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorActivoOscuro,
                            contentColor = textoActivo
                        )
                    ) {
                        if (isGuardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = textoActivo,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // Espaciado final
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// Data class para manejar el estado de cada opción
data class OpcionEditState(
    val opcionOriginal: Opcion,
    val texto: String,
    val esCorrecta: Boolean,
    val imagenActual: String?,
    val imagenNueva: File?,
    val imagenUri: Uri?,
    val eliminarImagen: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorsSection(
    nivel: String,
    onNivelSelected: (String) -> Unit,
    areas: List<Area>,
    selectedAreaId: Int?,
    onAreaSelected: (Int) -> Unit,
    temas: List<Tema>,
    selectedTemaId: Int?,
    onTemaSelected: (Int) -> Unit,
    onCreateTema: (String, Int) -> Unit,
    colorActivo: Color,
    colorInactivo: Color,
    textoActivo: Color,
    textoInactivo: Color
) {
    var nivelExpanded by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }
    var temaExpanded by remember { mutableStateOf(false) }
    var showTemaInput by remember { mutableStateOf(false) }
    var temaText by remember(selectedTemaId, temas.size) {
        mutableStateOf(temas.firstOrNull { it.id_tema == selectedTemaId }?.descripcion ?: "")
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Primera fila: Nivel y Área
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nivel
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { nivelExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (nivel.isNotBlank()) colorActivo else colorInactivo,
                        contentColor = if (nivel.isNotBlank()) textoActivo else textoInactivo
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (nivel.isNotBlank()) nivel else "Nivel")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = nivelExpanded,
                    onDismissRequest = { nivelExpanded = false }
                ) {
                    listOf("Bajo", "Medio", "Alto").forEach { nivelOption ->
                        DropdownMenuItem(
                            text = { Text(nivelOption) },
                            onClick = {
                                nivelExpanded = false
                                onNivelSelected(nivelOption)
                            }
                        )
                    }
                }
            }

            // Área
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { areaExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAreaId != null) colorActivo else colorInactivo,
                        contentColor = if (selectedAreaId != null) textoActivo else textoInactivo
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = areas.firstOrNull { it.id_area == selectedAreaId }?.nombre ?: "Área",
                            maxLines = 1
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = areaExpanded,
                    onDismissRequest = { areaExpanded = false }
                ) {
                    areas.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area.nombre) },
                            onClick = {
                                areaExpanded = false
                                onAreaSelected(area.id_area)
                            }
                        )
                    }
                }
            }
        }

        // Segunda fila: Tema con botón de agregar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = temaText,
                    onValueChange = {
                        temaText = it
                        temaExpanded = it.isNotEmpty()
                    },
                    label = { Text("Tema") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAreaId != null,
                    trailingIcon = {
                        IconButton(
                            onClick = { if (selectedAreaId != null) temaExpanded = !temaExpanded }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorActivo,
                        focusedLabelColor = colorActivo,
                        cursorColor = colorActivo
                    )
                )

                DropdownMenu(
                    expanded = temaExpanded && selectedAreaId != null,
                    onDismissRequest = { temaExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val resultados = temas.filter {
                        it.descripcion.contains(temaText, ignoreCase = true)
                    }

                    if (resultados.isNotEmpty()) {
                        resultados.forEach { tema ->
                            DropdownMenuItem(
                                text = { Text(tema.descripcion) },
                                onClick = {
                                    temaText = tema.descripcion
                                    temaExpanded = false
                                    onTemaSelected(tema.id_tema)
                                }
                            )
                        }
                    } else if (temaText.isNotBlank()) {
                        DropdownMenuItem(
                            text = { Text("➕ Agregar \"$temaText\"") },
                            onClick = {
                                selectedAreaId?.let { areaId ->
                                    onCreateTema(temaText, areaId)
                                    temaExpanded = false
                                }
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = { showTemaInput = !showTemaInput },
                enabled = selectedAreaId != null,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (selectedAreaId != null) colorInactivo else Color.LightGray,
                    contentColor = if (selectedAreaId != null) textoInactivo else Color.Gray
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tema")
            }
        }

        if (selectedAreaId == null) {
            Text(
                text = "Selecciona un área primero para elegir tema",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Campo para agregar nuevo tema
        if (showTemaInput && selectedAreaId != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var nuevoTemaText by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = nuevoTemaText,
                    onValueChange = { nuevoTemaText = it },
                    label = { Text("Nuevo tema") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorActivo,
                        focusedLabelColor = colorActivo,
                        cursorColor = colorActivo
                    )
                )

                Button(
                    onClick = {
                        if (nuevoTemaText.isNotBlank() && selectedAreaId != null) {
                            onCreateTema(nuevoTemaText, selectedAreaId)
                            nuevoTemaText = ""
                            showTemaInput = false
                        }
                    },
                    enabled = nuevoTemaText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorActivo,
                        contentColor = textoActivo
                    )
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
fun EditOptionItem(
    opcionState: OpcionEditState,
    onTextoChange: (String) -> Unit,
    onCorrectaChange: () -> Unit,
    onImagePicked: (Uri, File) -> Unit,
    onImageDeleted: () -> Unit,
    colorActivo: Color
) {
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val input = context.contentResolver.openInputStream(it)
            val tempFile = File(context.cacheDir, "op_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { output -> input?.copyTo(output) }
            onImagePicked(uri, tempFile)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (opcionState.esCorrecta)
                colorActivo.copy(alpha = 0.1f) else Color.White
        ),
        border = if (opcionState.esCorrecta)
            androidx.compose.foundation.BorderStroke(2.dp, colorActivo) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Campo de texto
            OutlinedTextField(
                value = opcionState.texto,
                onValueChange = onTextoChange,
                label = { Text("Texto de la opción") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorActivo,
                    focusedLabelColor = colorActivo,
                    cursorColor = colorActivo
                )
            )

            // Radio button para marcar correcta
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = opcionState.esCorrecta,
                    onClick = onCorrectaChange,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = colorActivo
                    )
                )
                Text(
                    text = "Respuesta correcta",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Sección de imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        // Imagen nueva
                        opcionState.imagenUri != null -> {
                            AsyncImage(
                                model = opcionState.imagenUri,
                                contentDescription = "Imagen nueva",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Imagen existente y no eliminada
                        !opcionState.imagenActual.isNullOrEmpty() && !opcionState.eliminarImagen -> {
                            AsyncImage(
                                model = opcionState.imagenActual,
                                contentDescription = "Imagen actual",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Sin imagen
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Sin imagen",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sin imagen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Botones de imagen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorActivo,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (opcionState.imagenUri != null || !opcionState.imagenActual.isNullOrEmpty())
                            "Cambiar" else "Subir",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (opcionState.imagenUri != null ||
                    (!opcionState.imagenActual.isNullOrEmpty() && !opcionState.eliminarImagen)) {
                    OutlinedButton(
                        onClick = onImageDeleted,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}