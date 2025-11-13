package com.example.presaber.ui.institution.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.ui.theme.PresaberTheme
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun QuestionCard(
    title: String,
    tema: String,
    nivel: String,
    imagen: String? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFA9C9FF) else Color(0xFFE2E7EE)
    val textColor = if (isSelected) Color.Black else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onClick() }
            .heightIn(min = 125.dp)
        ,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {

                    if (!imagen.isNullOrBlank()) {
                        AsyncImage(
                            model = imagen,
                            contentDescription = "Imagen de la pregunta",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Category, // Puedes cambiarlo
                            contentDescription = "Icono de tema",
                            tint = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tema,
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph, // Ícono para nivel
                            contentDescription = "Icono de nivel",
                            tint = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nivel,
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = textColor
                )
            }
        }
}



@Preview
@Composable
fun QuestionCardPreview(){
    PresaberTheme{
        QuestionCard(
            title = "Prueba",
            tema= "Prueba",
            nivel = "Nivel Medio",
            isSelected = false
        ) {

        }
    }
}
