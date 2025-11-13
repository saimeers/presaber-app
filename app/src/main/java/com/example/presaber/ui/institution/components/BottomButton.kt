package com.example.presaber.ui.institution.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.presaber.ui.theme.PresaberTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomButton(navController: NavController) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.height(48.dp)
        ) {
            // Botón Atrás
            SegmentedButton(
                selected = selectedIndex == 0,
                onClick = {
                    selectedIndex = 0
                    navController.popBackStack()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color(0xFF5B7BC6),
                    activeContentColor = Color.White,
                    inactiveContainerColor = Color(0xFFD2DEFF),
                    inactiveContentColor = Color(0xFF3D5A8F)
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Atrás",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Botón Nueva
            SegmentedButton(
                selected = selectedIndex == 1,
                onClick = {
                    selectedIndex = 1
                    /* TODO: Nueva pregunta */
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color(0xFF5B7BC6),
                    activeContentColor = Color.White,
                    inactiveContainerColor = Color(0xFFD2DEFF),
                    inactiveContentColor = Color(0xFF3D5A8F)
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva pregunta",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Nueva",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Botón Guardar
            SegmentedButton(
                selected = selectedIndex == 2,
                onClick = {
                    selectedIndex = 2
                    /* TODO: Guardar pregunta */
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color(0xFF5B7BC6),
                    activeContentColor = Color.White,
                    inactiveContainerColor = Color(0xFFD2DEFF),
                    inactiveContentColor = Color(0xFF3D5A8F)
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = "Guardar",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Guardar",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BottomButtonPreview() {
    val navController = rememberNavController()
    PresaberTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                BottomButton(navController)
            }
        }
    }
}