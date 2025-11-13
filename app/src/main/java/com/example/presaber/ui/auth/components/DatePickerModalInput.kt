package com.example.presaber.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalInput(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled = remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onDateSelected(datePickerState.selectedDateMillis) },
                enabled = confirmEnabled.value
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = Color(0xFFD9DFF6),
                selectedDayContentColor = Color(0xFF1A1B21),
                todayDateBorderColor = Color(0xFF1976D2),
                dayContentColor = Color(0xFF1A1B21),
                weekdayContentColor = Color(0xFF1A1B21),
                todayContentColor = Color(0xFF1976D2),
                yearContentColor = Color(0xFF1A1B21),
                selectedYearContainerColor = Color(0xFFD9DFF6),
                selectedYearContentColor = Color(0xFF1A1B21)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDatePickerModalInput() {
    var showDialog by remember { mutableStateOf(true) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showDialog) {
                DatePickerModalInput(
                    onDateSelected = { selectedDate ->
                        println("Fecha seleccionada: $selectedDate")
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            } else {
                Button(onClick = { showDialog = true }) {
                    Text("Mostrar DatePicker")
                }
            }
        }
    }
}