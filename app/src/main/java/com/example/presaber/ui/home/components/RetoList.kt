package com.example.presaber.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.presaber.data.remote.Reto
import com.example.presaber.ui.home.SubjectArea

@Composable
fun RetoList(
    area: SubjectArea,
    retos: List<Reto>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onStartReto: (Reto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBackClick) {
                Text("← Volver", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = area.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(retos) { reto ->
                    // Aquí pasamos el callback al componente
                    StackedCardReto(
                        reto = reto,
                        onStart = { onStartReto(reto) }
                    )
                }
            }
        }
    }
}
