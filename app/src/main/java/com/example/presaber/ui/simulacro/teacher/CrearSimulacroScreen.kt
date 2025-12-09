package com.example.presaber.ui.simulacro.teacher

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.CrearSimulacroRequest
import com.example.presaber.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

@Composable
fun CrearSimulacroScreen(
    idDocente: String,
    grado: String,
    grupo: String,
    cohorte: Int,
    idInstitucion: Int,
    onSimulacroCreado: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Necesario para el Toast
    val scrollState = rememberScrollState()

    var cantidadPreguntas by remember { mutableStateOf(10) }
    var duracionMinutos by remember { mutableStateOf(10) }

    // Ya no usamos errorMessage en texto, sino Toast, pero mantenemos la variable por si acaso
    var isCreating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(16.dp)
    ) {
        // 1. Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Atrás", tint = Color(0xFF1A1B21))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Crear simulacro",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1B21)
            )
        }

        // 2. Contenido Scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(8.dp))

            // Cantidad de preguntas
            Text("Cantidad de preguntas", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                text = "Mínimo 10 preguntas",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    onClick = { if (cantidadPreguntas > 10) cantidadPreguntas-- },
                    enabled = cantidadPreguntas > 10,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = Color(0xFF4A6FA5),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (cantidadPreguntas > 10) Color(0xFF4A6FA5) else Color.LightGray
                    )
                ) {
                    Text("−", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4A6FA5).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$cantidadPreguntas",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A6FA5)
                        )
                        Text(
                            text = "preguntas",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedIconButton(
                    onClick = { cantidadPreguntas++ },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = Color(0xFF4A6FA5)
                    ),
                    border = BorderStroke(width = 2.dp, color = Color(0xFF4A6FA5))
                ) {
                    Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Duración
            Text("Duración", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                text = "Mínimo 10 minutos",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4A6FA5).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$duracionMinutos",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A6FA5)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (duracionMinutos == 1) "minuto" else "minutos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A6FA5).copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Slider(
                        value = duracionMinutos.toFloat(),
                        onValueChange = { duracionMinutos = it.toInt() },
                        valueRange = 10f..120f,
                        steps = 109,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4A6FA5),
                            activeTrackColor = Color(0xFF4A6FA5),
                            inactiveTrackColor = Color(0xFF4A6FA5).copy(alpha = 0.2f)
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10 min", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Text("120 min", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // 3. Footer (Info + Botón)
        Column {
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "💡 Las preguntas se seleccionarán aleatoriamente de todas las áreas",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = Color(0xFF795548)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Botón crear con manejo de errores mejorado
            Button(
                onClick = {
                    isCreating = true
                    scope.launch {
                        try {
                            val res = RetrofitClient.api.crearSimulacro(
                                CrearSimulacroRequest(
                                    id_docente = idDocente,
                                    grado = grado,
                                    grupo = grupo,
                                    cohorte = cohorte,
                                    id_institucion = idInstitucion,
                                    cantidad_preguntas = cantidadPreguntas,
                                    duracion_minutos = duracionMinutos
                                )
                            )

                            if (res.success) {
                                Toast.makeText(context, "Simulacro creado con éxito", Toast.LENGTH_SHORT).show()
                                onSimulacroCreado(res.data.id_simulacro)
                            } else {
                                // Caso raro donde es 200 pero success false
                                Toast.makeText(context, res.message ?: "Error desconocido", Toast.LENGTH_LONG).show()
                            }

                        } catch (e: HttpException) {
                            // AQUÍ ESTÁ LA MAGIA: Capturamos el 400 y leemos el JSON
                            val errorBody = e.response()?.errorBody()?.string()
                            val mensajeError = try {
                                // Parseamos el JSON que manda tu Node: { "success": false, "error": "mensaje..." }
                                val jsonObject = JSONObject(errorBody ?: "")
                                jsonObject.optString("error", "Error en la solicitud")
                            } catch (parseException: Exception) {
                                "Error de conexión con el servidor"
                            }

                            // Mostramos el mensaje exacto del backend ("No hay preguntas suficientes...")
                            Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()

                        } catch (e: Exception) {
                            // Otros errores (internet, etc)
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isCreating = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isCreating,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Crear simulacro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}