package com.example.presaber.ui.institution.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InstitutionList(
    institutions: List<String>,
    onSelectInstitution: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(institutions) { name ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectInstitution(name) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F3FF)
                ),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = name,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }

        item {
            // 🔢 Paginador con espaciado y alineación tipo Figma
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 12.dp), // más cerca del nav bar
                horizontalArrangement = Arrangement.Center
            ) {
                val activePageColor = Color(0xFF5677F5)
                val inactiveTextColor = Color(0xFF3A3843)

                repeat(5) { index ->
                    val isSelected = index == 0

                    Text(
                        text = (index + 1).toString(),
                        color = if (isSelected) Color.White else inactiveTextColor,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .background(
                                color = if (isSelected) activePageColor else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstitutionListPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            InstitutionList(
                institutions = listOf(
                    "Institución Educativa Santo Ángel",
                    "Colegio Sagrado Corazón de Jesús",
                    "Colegio San Francisco de Sales",
                    "Institución Educativa Fe y Alegría",
                    "Institución educativa xxx xxxx"
                ),
                onSelectInstitution = {}
            )
        }
    }
}
