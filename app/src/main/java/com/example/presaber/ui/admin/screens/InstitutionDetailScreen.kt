package com.example.presaber.ui.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presaber.data.remote.InstitucionCompleta
import com.example.presaber.data.remote.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionDetailScreen(
    idInstitucion: Int,
    onBack: () -> Unit
) {
    var institucion by remember { mutableStateOf<InstitucionCompleta?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar detalles (Podrías pasar el objeto completo, pero por ID es más seguro para deep links)
    LaunchedEffect(idInstitucion) {
        try {
            // Nota: Usamos obtenerInstitucionesCompletas y filtramos, o un endpoint específico si lo tienes.
            // Aquí asumiremos que buscamos en la lista completa por simplicidad o un endpoint getById
            val response = RetrofitClient.api.obtenerInstitucionesCompletas()
            if (response.success) {
                institucion = response.data.find { it.id_institucion == idInstitucion }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle Institución") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF5B7BC6)
                )
            } else if (institucion == null) {
                Text("Institución no encontrada", modifier = Modifier.align(Alignment.Center))
            } else {
                val inst = institucion!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Institución
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE3F2FD),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Business,
                                        null,
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = inst.nombre,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1B21),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // Sección Ubicación y Contacto
                    DetailSection(title = "Ubicación y Contacto") {
                        DetailRow(Icons.Rounded.LocationCity, "Departamento", inst.departamento)
                        DetailRow(Icons.Default.Map, "Municipio", inst.municipio)
                        DetailRow(Icons.Default.Place, "Dirección", inst.direccion)
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
                        DetailRow(Icons.Default.Email, "Correo", inst.correo)
                        DetailRow(Icons.Default.Phone, "Teléfono", inst.telefono)
                    }

                    // Sección Director
                    DetailSection(title = "Director Encargado") {
                        if (inst.director != null) {
                            DetailRow(Icons.Default.Person, "Nombre", inst.director.nombre_completo)
                            DetailRow(Icons.Default.Badge, "Documento", inst.director.documento)
                            DetailRow(Icons.Default.Email, "Correo", inst.director.correo)
                            DetailRow(Icons.Default.Phone, "Teléfono", inst.director.telefono)
                        } else {
                            Text(
                                "No hay director asignado",
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5B7BC6),
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, color = Color(0xFF1A1B21))
        }
    }
}