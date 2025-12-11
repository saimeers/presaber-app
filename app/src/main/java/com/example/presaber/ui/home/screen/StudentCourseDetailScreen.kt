package com.example.presaber.ui.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

// Colores
private val PrimaryBlue = Color(0xFF5B7BC6)
private val TextDark = Color(0xFF1A1B21)
private val TextGray = Color(0xFF757575)
private val Gold = Color(0xFFFFD700)
private val Silver = Color(0xFFC0C0C0)
private val Bronze = Color(0xFFCD7F32)

// Gradiente de Fuego
private val FireGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFFFFC107))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCourseDetailScreen(
    grado: String,
    grupo: String,
    cohorte: Int,
    idInstitucion: Int,
    onBack: () -> Unit,
    onStudentClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Participantes", "Ranking")
    var participantes by remember { mutableStateOf<List<ParticipanteCurso>>(emptyList()) }
    var ranking by remember { mutableStateOf<List<EstudianteRanking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val request = CursoRequest(grado, grupo, cohorte, idInstitucion)
            val respPart = RetrofitClient.api.obtenerParticipantesCurso(request)
            if (respPart.success) participantes = respPart.data
            val respRank = RetrofitClient.api.obtenerRankingCurso(request)
            if (respRank.success) ranking = respRank.data
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = PrimaryBlue,
                    indicator = { tabPositions -> TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = PrimaryBlue) }
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
                    0 -> ParticipantesTab(participantes, searchQuery, { searchQuery = it }, onStudentClick)
                    1 -> RankingTab(ranking)
                }
            }
        }
    }
}

// --- TABS (Reutilizados del diseño de Docente) ---

@Composable
fun ParticipantesTab(
    participantes: List<ParticipanteCurso>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onStudentClick: (String) -> Unit
) {
    val filteredList = participantes.filter { it.nombre_completo.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery, onValueChange = onSearchChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar compañero...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredList) { p ->
                // Only allow clicking if role is student (id=3). Teachers usually shouldn't be clicked to see a student profile.
                val isStudent = p.rol.id == 3
                ParticipanteCard(p, onClick = { if (isStudent) onStudentClick(p.documento) })
            }
        }
    }
}

@Composable
fun RankingTab(ranking: List<EstudianteRanking>) {
    if (ranking.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Aún no hay datos de ranking", color = TextGray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ranking) { estudiante ->
                RankingCard(estudiante)
            }
        }
    }
}

// --- COMPONENTES AUXILIARES (Idénticos al de Docente) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipanteCard(p: ParticipanteCurso, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        onClick = onClick
    ) {
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
    val colorPosicion = when(estudiante.posicion) {
        1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> Color.Transparent
    }

    val primerNombre = estudiante.nombre_completo.split(" ").firstOrNull() ?: "Estudiante"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición (Izquierda)
            if (estudiante.posicion <= 3) {
                Icon(Icons.Rounded.EmojiEvents, null, tint = colorPosicion, modifier = Modifier.size(32.dp))
            } else {
                // Si es un número grande (>9), lo ponemos en columna
                if (estudiante.posicion > 9) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(32.dp)
                    ) {
                        Text("#", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray)
                        Text("${estudiante.posicion}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextGray)
                    }
                } else {
                    Text("#${estudiante.posicion}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextGray, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.width(12.dp))
            Avatar(estudiante.photoURL, estudiante.nombre_completo)
            Spacer(Modifier.width(12.dp))

            Text(
                text = primerNombre,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )

            // Columna de Stats (Derecha)
            Column(horizontalAlignment = Alignment.End) {
                // Último puntaje
                if (estudiante.ultimo_puntaje_simulacro > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.icon_destellos),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${estudiante.ultimo_puntaje_simulacro} pts",
                            style = TextStyle(
                                brush = FireGradient,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        )
                    }
                } else {
                    Text(
                        text = "¿?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFCFD8DC),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${estudiante.experiencia_total} XP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun Avatar(url: String?, nombre: String) {
    if (url != null) {
        AsyncImage(
            model = url, contentDescription = null,
            modifier = Modifier.size(48.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(nombre.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 20.sp)
        }
    }
}