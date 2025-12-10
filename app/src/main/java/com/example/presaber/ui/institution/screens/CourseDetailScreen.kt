package com.example.presaber.ui.institution.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.presaber.R
import com.example.presaber.data.remote.*
import kotlinx.coroutines.launch

private val PrimaryBlue = Color(0xFF5B7BC6)
private val TextDark = Color(0xFF1A1B21)
private val TextGray = Color(0xFF757575)
private val Gold = Color(0xFFFFD700)
private val Silver = Color(0xFFC0C0C0)
private val Bronze = Color(0xFFCD7F32)
private val FireGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFFFFC107))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    grado: String,
    grupo: String,
    cohorte: Int,
    idInstitucion: Int,
    userRole: Int,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Participantes", "Ranking", "Configuración")

    var participantes by remember { mutableStateOf<List<ParticipanteCurso>>(emptyList()) }
    var ranking by remember { mutableStateOf<List<EstudianteRanking>>(emptyList()) }
    var claveAcceso by remember { mutableStateOf("") }

    // Estado para docentes
    var docentesList by remember { mutableStateOf<List<TeacherResponse>>(emptyList()) }
    var docenteActualId by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val request = CursoRequest(grado, grupo, cohorte, idInstitucion)

            // 1. Participantes
            val respPart = RetrofitClient.api.obtenerParticipantesCurso(request)
            if (respPart.success) participantes = respPart.data

            // 2. Ranking
            val respRank = RetrofitClient.api.obtenerRankingCurso(request)
            if (respRank.success) ranking = respRank.data

            // 3. Cargar Docentes SOLO si es Director (Rol 4) para optimizar
            if (userRole == 4) {
                docentesList = RetrofitClient.api.getDocentes(idInstitucion)
            }

            // 4. Identificar al docente actual (buscando en participantes)
            val docenteEncontrado = participantes.find { it.rol.id == 2 }
            docenteActualId = docenteEncontrado?.documento

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Grupo $grado-$grupo", fontSize = 14.sp, color = TextGray)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Atrás", tint = TextDark)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = PrimaryBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryBlue
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            } else {
                when (selectedTab) {
                    0 -> ParticipantesTab(participantes, searchQuery) { searchQuery = it }
                    1 -> RankingTab(ranking)
                    2 -> ConfiguracionTab(
                        grado, grupo, cohorte, idInstitucion,
                        claveInicial = claveAcceso,
                        listaDocentes = docentesList,
                        docenteInicialId = docenteActualId,
                        userRole = userRole,
                        onSaveSuccess = onBack
                    )
                }
            }
        }
    }
}

// ... (Tabs de Participantes y Ranking se mantienen igual) ...
@Composable
fun ParticipantesTab(participantes: List<ParticipanteCurso>, searchQuery: String, onSearchChange: (String) -> Unit) {
    val filteredList = participantes.filter { it.nombre_completo.contains(searchQuery, ignoreCase = true) }
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery, onValueChange = onSearchChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { items(filteredList) { p -> ParticipanteCard(p) } }
    }
}

@Composable
fun RankingTab(ranking: List<EstudianteRanking>) {
    if (ranking.isEmpty()) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Aún no hay datos de ranking", color = TextGray) } }
    else { LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(ranking) { estudiante -> RankingCard(estudiante) } } }
}

// --- TAB CONFIGURACIÓN CON LÓGICA DE ROLES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionTab(
    grado: String, grupo: String, cohorte: Int, idInstitucion: Int,
    claveInicial: String,
    listaDocentes: List<TeacherResponse>,
    docenteInicialId: String?,
    userRole: Int, // Recibimos el rol
    onSaveSuccess: () -> Unit
) {
    var clave by remember { mutableStateOf(claveInicial) }
    var docenteSeleccionado by remember { mutableStateOf(listaDocentes.find { it.documento == docenteInicialId }) }

    // Si no es director, buscamos el nombre del docente actual manualmente (ya que listaDocentes puede estar vacía para docentes)
    // O mostramos "Docente Asignado" genérico si no tenemos el dato.
    // Para simplificar, si es docente, mostramos el campo bloqueado.

    var expandedDocente by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isDirector = userRole == 4 // Director = 4

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize()
    ) {
        Text("Editar Curso", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
        Spacer(Modifier.height(8.dp))

        // Texto dinámico según permisos
        val descripcion = if (isDirector)
            "Modifica la clave de acceso o reasigna el docente del curso."
        else
            "Modifica la clave de acceso para los estudiantes."

        Text(text = descripcion, fontSize = 14.sp, color = TextGray)

        Spacer(Modifier.height(32.dp))

        // 1. Campo Clave (Disponible para AMBOS)
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Clave de Acceso") },
            placeholder = { Text("Ej: MAT-11-A") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { Icon(Icons.Default.Edit, null, tint = PrimaryBlue) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                focusedLabelColor = PrimaryBlue
            ),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        // 2. Selector de Docente (Solo editable para DIRECTOR)
        if (isDirector) {
            ExposedDropdownMenuBox(
                expanded = expandedDocente,
                onExpandedChange = { expandedDocente = !expandedDocente }
            ) {
                OutlinedTextField(
                    value = docenteSeleccionado?.let { "${it.nombre} ${it.apellido}" } ?: "Sin docente asignado",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Docente Titular") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryBlue) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedDocente,
                    onDismissRequest = { expandedDocente = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin docente", color = TextGray) },
                        onClick = { docenteSeleccionado = null; expandedDocente = false }
                    )
                    listaDocentes.forEach { docente ->
                        DropdownMenuItem(
                            text = { Text("${docente.nombre} ${docente.apellido}") },
                            onClick = { docenteSeleccionado = docente; expandedDocente = false }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = if (docenteInicialId != null) "Docente Asignado" else "Sin docente",
                onValueChange = {},
                readOnly = true,
                enabled = false, // Deshabilitado visualmente
                label = { Text("Docente Titular") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledLabelColor = Color.Gray,
                    disabledTextColor = TextDark
                )
            )
        }

        Spacer(Modifier.weight(1f))

        // Botón Guardar
        Button(
            onClick = {
                isSaving = true
                scope.launch {
                    try {
                        val request = ActualizarCursoRequest(
                            grado, grupo, cohorte, idInstitucion,
                            clave_acceso = clave,
                            habilitado = null,
                            // Solo enviamos el ID del docente si es Director. Si no, enviamos null para que no se cambie.
                            id_docente = if (isDirector) docenteSeleccionado?.documento else null
                        )

                        val response = RetrofitClient.api.actualizarConfiguracionCurso(request)

                        if (response.success) {
                            Toast.makeText(context, "Curso actualizado correctamente", Toast.LENGTH_SHORT).show()
                            onSaveSuccess()
                        } else {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isSaving = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Guardar Cambios", fontSize = 16.sp)
            }
        }
    }
}

// ... (Componentes auxiliares ParticipanteCard, RankingCard, Avatar se mantienen igual) ...
// Asegúrate de copiarlos del mensaje anterior si no los tienes en este archivo.
@Composable
fun ParticipanteCard(p: ParticipanteCurso) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Avatar(p.photoURL, p.nombre)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(p.nombre_completo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Text(p.rol.descripcion, fontSize = 14.sp, color = TextGray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@Composable
fun RankingCard(estudiante: EstudianteRanking) {
    val colorPosicion = when(estudiante.posicion) { 1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> Color.Transparent }
    val primerNombre = estudiante.nombre_completo.split(" ").firstOrNull() ?: "Estudiante"
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (estudiante.posicion <= 3) Icon(Icons.Rounded.EmojiEvents, null, tint = colorPosicion, modifier = Modifier.size(32.dp))
            else Text("#${estudiante.posicion}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextGray, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            Spacer(Modifier.width(12.dp)); Avatar(estudiante.photoURL, estudiante.nombre_completo); Spacer(Modifier.width(12.dp))
            Text(primerNombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                if (estudiante.ultimo_puntaje_simulacro > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.icon_destellos), null, modifier = Modifier.size(16.dp), tint = Color.Unspecified)
                        Spacer(Modifier.width(4.dp))
                        Text("${estudiante.ultimo_puntaje_simulacro} pts", style = TextStyle(brush = FireGradient, fontWeight = FontWeight.Black, fontSize = 14.sp))
                    }
                } else {
                    Text("¿?", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFCFD8DC), modifier = Modifier.padding(end = 4.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text("${estudiante.experiencia_total} XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun Avatar(url: String?, nombre: String) {
    if (url != null) AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop)
    else Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) { Text(nombre.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 20.sp) }
}