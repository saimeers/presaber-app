package com.example.presaber.ui.home.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.presaber.data.remote.Opcion
import com.example.presaber.data.remote.Pregunta

@Composable
fun QuestionCard(
    pregunta: Pregunta,
    selectedOptionId: Int?,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3F6F8))
            .padding(16.dp)
            .animateContentSize(tween(300))
    ) {
        // Enunciado
        if (!pregunta.enunciado.isNullOrBlank()) {
            Text(
                text = pregunta.enunciado,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Imagen de la pregunta
        if (!pregunta.imagen.isNullOrEmpty()) {
            AsyncImage(
                model = pregunta.imagen,
                contentDescription = "Imagen pregunta",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Opciones
        pregunta.opciones.forEach { opcion ->
            OptionRow(
                opcion = opcion,
                selected = selectedOptionId == opcion.id_opcion,
                onClick = { onSelect(opcion.id_opcion) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun OptionRow(opcion: Opcion, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Texto de la opción
            if (!opcion.texto_opcion.isNullOrBlank()) {
                Text(
                    text = opcion.texto_opcion,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Imagen de la opción
            if (!opcion.imagen.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = opcion.imagen,
                    contentDescription = "Imagen opción",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}