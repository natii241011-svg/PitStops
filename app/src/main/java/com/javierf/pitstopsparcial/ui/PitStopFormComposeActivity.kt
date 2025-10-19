package com.javierf.pitstopsparcial.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javierf.pitstopsparcial.model.PitStop
import com.javierf.pitstopsparcial.theme.PitStopsParcialTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PitStopFormComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PitStopsParcialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PitStopFormScreen(
                        onGuardar = { pitStop ->
                            val resultIntent = Intent()
                            resultIntent.putExtra(PitStopFormActivity.EXTRA_PITSTOP, pitStop)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        },
                        onCancelar = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitStopFormScreen(
    onGuardar: (PitStop) -> Unit,
    onCancelar: () -> Unit
) {
    var piloto by remember { mutableStateOf("Lewis Hamilton") }
    var escuderia by remember { mutableStateOf("Mercedes") }
    var tiempoSegundos by remember { mutableStateOf("") }
    var compuesto by remember { mutableStateOf("Hard") }
    var neumaticosCambiados by remember { mutableStateOf("4") }
    var estado by remember { mutableStateOf("OK") }
    var motivoFallo by remember { mutableStateOf("") }
    var mecanicoPrincipal by remember { mutableStateOf("") }
    var fechaHora by remember { mutableStateOf(LocalDateTime.now()) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(4.dp)
    ) {
        // Header centrado
        Text(
            text = "Registrar Pit Stop",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Formulario
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Piloto
                Text(
                    text = "🏎️ Piloto",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                
                var expandedPiloto by remember { mutableStateOf(false) }
                val pilotos = listOf("Lewis Hamilton", "Max Verstappen", "Charles Leclerc", "Lando Norris", "Fernando Alonso")
                
                ExposedDropdownMenuBox(
                    expanded = expandedPiloto,
                    onExpandedChange = { expandedPiloto = !expandedPiloto }
                ) {
                    OutlinedTextField(
                        value = piloto,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPiloto) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPiloto,
                        onDismissRequest = { expandedPiloto = false }
                    ) {
                        pilotos.forEach { pilotoOption ->
                            DropdownMenuItem(
                                text = { Text(pilotoOption) },
                                onClick = {
                                    piloto = pilotoOption
                                    expandedPiloto = false
                                }
                            )
                        }
                    }
                }

                // Escudería
                Text(
                    text = "🏁 Escudería",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                
                var expandedEscuderia by remember { mutableStateOf(false) }
                val escuderias = listOf("Mercedes", "Red Bull Racing", "Ferrari", "McLaren", "Alpine")
                
                ExposedDropdownMenuBox(
                    expanded = expandedEscuderia,
                    onExpandedChange = { expandedEscuderia = !expandedEscuderia }
                ) {
                    OutlinedTextField(
                        value = escuderia,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscuderia) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEscuderia,
                        onDismissRequest = { expandedEscuderia = false }
                    ) {
                        escuderias.forEach { escuderiaOption ->
                            DropdownMenuItem(
                                text = { Text(escuderiaOption) },
                                onClick = {
                                    escuderia = escuderiaOption
                                    expandedEscuderia = false
                                }
                            )
                        }
                    }
                }

                // Tiempo en segundos
                Text(
                    text = "⏱️ Tiempo (segundos)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                OutlinedTextField(
                    value = tiempoSegundos,
                    onValueChange = { tiempoSegundos = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("Ej: 2.5") }
                )

                // Compuesto
                Text(
                    text = "🛞 Compuesto",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                
                var expandedCompuesto by remember { mutableStateOf(false) }
                val compuestos = listOf("Hard", "Medium", "Soft")
                
                ExposedDropdownMenuBox(
                    expanded = expandedCompuesto,
                    onExpandedChange = { expandedCompuesto = !expandedCompuesto }
                ) {
                    OutlinedTextField(
                        value = compuesto,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCompuesto) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCompuesto,
                        onDismissRequest = { expandedCompuesto = false }
                    ) {
                        compuestos.forEach { compuestoOption ->
                            DropdownMenuItem(
                                text = { Text(compuestoOption) },
                                onClick = {
                                    compuesto = compuestoOption
                                    expandedCompuesto = false
                                }
                            )
                        }
                    }
                }

                // Neumáticos cambiados
                Text(
                    text = "🔧 Neumáticos cambiados",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                OutlinedTextField(
                    value = neumaticosCambiados,
                    onValueChange = { neumaticosCambiados = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Ej: 4") }
                )

                // Estado
                Text(
                    text = "📊 Estado",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                
                var expandedEstado by remember { mutableStateOf(false) }
                val estados = listOf("OK", "Fallido")
                
                ExposedDropdownMenuBox(
                    expanded = expandedEstado,
                    onExpandedChange = { expandedEstado = !expandedEstado }
                ) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEstado,
                        onDismissRequest = { expandedEstado = false }
                    ) {
                        estados.forEach { estadoOption ->
                            DropdownMenuItem(
                                text = { Text(estadoOption) },
                                onClick = {
                                    estado = estadoOption
                                    expandedEstado = false
                                }
                            )
                        }
                    }
                }

                // Motivo fallo (solo si es fallido)
                if (estado == "Fallido") {
                    Text(
                        text = "⚠️ Motivo del fallo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE74C3C)
                    )
                    OutlinedTextField(
                        value = motivoFallo,
                        onValueChange = { motivoFallo = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Describe el problema...") }
                    )
                }

                // Mecánico principal
                Text(
                    text = "👨‍🔧 Mecánico principal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                OutlinedTextField(
                    value = mecanicoPrincipal,
                    onValueChange = { mecanicoPrincipal = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nombre del mecánico") }
                )

                // Fecha y hora
                Text(
                    text = "📅 Fecha y hora",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Fecha
                    OutlinedTextField(
                        value = fechaHora.toLocalDate().toString(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val datePicker = DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            fechaHora = LocalDateTime.of(
                                                LocalDate.of(year, month + 1, dayOfMonth),
                                                fechaHora.toLocalTime()
                                            )
                                        },
                                        fechaHora.year,
                                        fechaHora.monthValue - 1,
                                        fechaHora.dayOfMonth
                                    )
                                    datePicker.show()
                                }
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Fecha") }
                    )

                    // Hora
                    OutlinedTextField(
                        value = String.format("%02d:%02d", fechaHora.hour, fechaHora.minute),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val timePicker = TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            fechaHora = LocalDateTime.of(
                                                fechaHora.toLocalDate(),
                                                LocalTime.of(hourOfDay, minute)
                                            )
                                        },
                                        fechaHora.hour,
                                        fechaHora.minute,
                                        true
                                    )
                                    timePicker.show()
                                }
                            ) {
                                Icon(Icons.Default.List, contentDescription = "Seleccionar hora")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Hora") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onCancelar,
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF95A5A6)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Cancelar",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    if (tiempoSegundos.isNotBlank()) {
                        val pitStop = PitStop(
                            piloto = piloto,
                            escuderia = escuderia,
                            tiempoSegundos = tiempoSegundos.toDouble(),
                            compuesto = compuesto,
                            neumaticosCambiados = neumaticosCambiados.toInt(),
                            estado = estado,
                            motivoFallo = if (estado == "Fallido") motivoFallo else null,
                            mecanicoPrincipal = mecanicoPrincipal,
                            fechaHora = fechaHora
                        )
                        onGuardar(pitStop)
                    }
                },
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Guardar",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
