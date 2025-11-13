package com.example.presaber.ui.institution.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.R
import com.example.presaber.ui.theme.PresaberTheme
import com.example.presaber.ui.components.AddCard
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController
import androidx.compose.runtime.CompositionLocalProvider

val LocalNavController = compositionLocalOf<NavController> {
    error("NavController not provided")
}

data class SubjectArea(
    val title: String,
    val description: String,
    val imageRes: Int,
    val cardColor: Color
)

@Composable
fun HomeQuestionContent(
    onSubjectClick: (SubjectArea) -> Unit = {}
) {
    val navController = LocalNavController.current
    var showFabMenu by remember { mutableStateOf(false) }

    val subjects = listOf(
        SubjectArea(
            "Lectura Crítica",
            "Evalua comprensión lectora y análisis de textos",
            R.drawable.img_lectura,
            Color(0xFFE8B959)
        ),
        SubjectArea(
            "Matemáticas",
            "Evalua razonamiento lógico y habilidades matemáticas",
            R.drawable.img_matematicas,
            Color(0xFF5B7ABD)
        ),
        SubjectArea(
            "Ciencias Naturales",
            "Evalua el mundo natural y sus fenómenos",
            R.drawable.img_ciencias,
            Color(0xFF7AB88E)
        ),
        SubjectArea(
            "Ciencias Sociales y Ciudadanas",
            "Evalua la comprensión de la sociedad y el rol como ciudadano",
            R.drawable.img_sociales,
            Color(0xFF9E9E9E)
        ),
        SubjectArea(
            "Inglés",
            "Evalua habilidades comunicativas en inglés",
            R.drawable.img_ingles,
            Color(0xFFE87C7C)
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.Center)
        ) {
            item {
                Text(
                    text = "Banco de Preguntas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF485E92),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth() .padding(bottom = 16.dp, top = 8.dp)
                )
            }

            item {
                AddCard(
                    text = "Agregar preguntas",
                    onClick = { navController.navigate("CreateQuestionScreen") }
                )
            }

            items(subjects) { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = { onSubjectClick(subject) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Espacio adicional al final para que no quede detrás del FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectCard(
    subject: SubjectArea,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Texto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subject.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1B21)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subject.description,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Imagen
            Surface(
                color = subject.cardColor,
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = subject.imageRes),
                    contentDescription = subject.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview
@Composable
fun HomeQuestionContentPreview(){
    PresaberTheme{
        val navController = androidx.navigation.compose.rememberNavController()
        HomeQuestionContent()
    }
}
