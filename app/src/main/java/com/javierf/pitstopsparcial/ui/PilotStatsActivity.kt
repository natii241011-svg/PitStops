package com.javierf.pitstopsparcial.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javierf.pitstopsparcial.model.PitStop
import com.javierf.pitstopsparcial.theme.PitStopsParcialTheme

class PilotStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pitStops = intent.getParcelableArrayListExtra<PitStop>("pitstops") ?: arrayListOf()
        val pilotName = intent.getStringExtra("pilotName") ?: ""

        setContent {
            PitStopsParcialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PilotStatsScreen(
                        pilotName = pilotName,
                        pitStops = pitStops,
                        onVolver = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun PilotStatsScreen(
    pilotName: String,
    pitStops: List<PitStop>,
    onVolver: () -> Unit
) {
    val pilotPitStops = pitStops.filter { it.piloto.equals(pilotName, ignoreCase = true) }
    val pilotColor = getPilotColor(pilotName)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header del piloto
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = pilotColor)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = pilotName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = getPilotTeam(pilotName),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (pilotPitStops.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sin datos disponibles",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "No hay pit stops registrados para este piloto",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Estadísticas del piloto en cards horizontales
            val totalPitStops = pilotPitStops.size
            val successfulPitStops = pilotPitStops.filter { it.estado == "OK" }
            val failedPitStops = pilotPitStops.filter { it.estado == "Fallido" }
            val fastestTime = successfulPitStops.minByOrNull { it.tiempoSegundos }?.tiempoSegundos
            val averageTime = if (successfulPitStops.isNotEmpty()) {
                successfulPitStops.map { it.tiempoSegundos }.average()
            } else 0.0
            val successRate = if (totalPitStops > 0) {
                (successfulPitStops.size.toDouble() / totalPitStops.toDouble()) * 100
            } else 0.0

            LazyRow(
                modifier = Modifier.padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatisticCard(
                        icon = "🔢",
                        title = "Total",
                        value = totalPitStops.toString(),
                        subtitle = "Paradas",
                        backgroundColor = Color(0xFF2196F3)
                    )
                }
                item {
                    StatisticCard(
                        icon = "✅",
                        title = "Exitosos",
                        value = successfulPitStops.size.toString(),
                        subtitle = "Correctos",
                        backgroundColor = Color(0xFF4CAF50)
                    )
                }
                item {
                    StatisticCard(
                        icon = "❌",
                        title = "Fallidos",
                        value = failedPitStops.size.toString(),
                        subtitle = "Con problemas",
                        backgroundColor = Color(0xFFF44336)
                    )
                }
                item {
                    StatisticCard(
                        icon = "⚡",
                        title = "Más Rápido",
                        value = if (fastestTime != null) "${fastestTime}s" else "N/A",
                        subtitle = "Mejor tiempo",
                        backgroundColor = Color(0xFFFF9800)
                    )
                }
                item {
                    StatisticCard(
                        icon = "📊",
                        title = "Promedio",
                        value = if (averageTime > 0) "${String.format("%.2f", averageTime)}s" else "N/A",
                        subtitle = "Tiempo medio",
                        backgroundColor = Color(0xFF9C27B0)
                    )
                }
                item {
                    StatisticCard(
                        icon = "🎯",
                        title = "Éxito",
                        value = "${String.format("%.1f", successRate)}%",
                        subtitle = "Tasa de éxito",
                        backgroundColor = Color(0xFF00BCD4)
                    )
                }
            }

            // Historial de pit stops
            Text(
                text = "Historial de Pit Stops",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF800020),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pilotPitStops.sortedByDescending { it.fechaHora }) { pitStop ->
                    EnhancedPilotPitStopCard(pitStop)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVolver,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF800020)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Volver",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF800020)
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun StatisticCard(
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EnhancedPilotPitStopCard(pitStop: PitStop) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${pitStop.tiempoSegundos}s",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF800020)
                    )
                }

                val colorEstado = if (pitStop.estado == "OK") Color(0xFF4CAF50) else Color(0xFFF44336)
                val iconEstado = if (pitStop.estado == "OK") "✅" else "❌"
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = iconEstado,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = pitStop.estado,
                        color = colorEstado,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏎️",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pitStop.escuderia,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pitStop.mecanicoPrincipal,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚫",
                            fontSize = 14.sp,
                            color = getTireColor(pitStop.compuesto)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pitStop.compuesto,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔄",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${pitStop.neumaticosCambiados} neumáticos",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (pitStop.estado == "Fallido" && !pitStop.motivoFallo.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = pitStop.motivoFallo,
                        fontSize = 12.sp,
                        color = Color(0xFFF44336),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

private fun getPilotColor(pilotName: String): Color {
    return when (pilotName) {
        "Lewis Hamilton" -> Color(0xFF00D2BE)
        "Max Verstappen" -> Color(0xFF0600EF)
        "Charles Leclerc" -> Color(0xFFDC143C)
        "Lando Norris" -> Color(0xFFFF8700)
        "Fernando Alonso" -> Color(0xFF006F62)
        else -> Color(0xFF800020)
    }
}

private fun getPilotTeam(pilotName: String): String {
    return when (pilotName) {
        "Lewis Hamilton" -> "Mercedes"
        "Max Verstappen" -> "Red Bull Racing"
        "Charles Leclerc" -> "Ferrari"
        "Lando Norris" -> "McLaren"
        "Fernando Alonso" -> "Aston Martin"
        else -> "Unknown"
    }
}

private fun getTireColor(compuesto: String): Color {
    return when (compuesto.lowercase()) {
        "soft" -> Color(0xFFE91E63)
        "medium" -> Color(0xFFFF9800)
        "hard" -> Color(0xFF607D8B)
        "wet" -> Color(0xFF2196F3)
        "intermediate" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}
