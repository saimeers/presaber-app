package com.example.presaber.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalInput(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDateMillis
                )

                DatePicker(
                    state = datePickerState,
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

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color(0xFF1A1B21))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { onDateSelected(datePickerState.selectedDateMillis) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF1976D2)
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
