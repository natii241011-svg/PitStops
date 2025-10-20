package com.javierf.pitstopsparcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.javierf.pitstopsparcial.ui.PitStopFormActivity
import com.javierf.pitstopsparcial.ui.PitStopFormComposeActivity
import com.javierf.pitstopsparcial.ui.PitStopListActivity
import com.javierf.pitstopsparcial.ui.components.ProfessionalBarChart
import com.javierf.pitstopsparcial.ui.PilotStatsActivity
import com.javierf.pitstopsparcial.theme.PitStopsParcialTheme
import java.time.LocalDateTime
class MainActivity : ComponentActivity() {
    private val pitStops = mutableListOf<PitStop>()
    private var statisticsUpdateTrigger by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PitStopsParcialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        onRegistrarClick = {
                            val intent = Intent(this, PitStopFormComposeActivity::class.java)
                            launcher.launch(intent)
                        },
                        onVerListadoClick = {
                            val intent = Intent(this, PitStopListActivity::class.java)
                            intent.putParcelableArrayListExtra(
                                "pitstops",
                                ArrayList(pitStops)
                            )
                            listLauncher.launch(intent)
                        },
                        pitStops = pitStops,
                        statisticsUpdateTrigger = statisticsUpdateTrigger
                    )
                }
            }
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val pit = result.data?.getParcelableExtra<PitStop>(PitStopFormActivity.EXTRA_PITSTOP)
            if (pit != null) {
                pitStops.add(pit)
                Toast.makeText(
                    this,
                    "✅ Nuevo pit stop registrado: ${pit.piloto} • ${pit.tiempoSegundos}s • ${pit.estado}",
                    Toast.LENGTH_LONG
                ).show()

                updateStatistics()
            }
        }
    }

    private val listLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedPitStops =
                result.data?.getParcelableArrayListExtra<PitStop>("updated_pitstops")
            if (updatedPitStops != null) {
                pitStops.clear()
                pitStops.addAll(updatedPitStops)
                updateStatistics()
                Toast.makeText(
                    this,
                    "🔄 Lista actualizada - Se eliminaron registros",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateStatistics() {
        statisticsUpdateTrigger++
    }

    private fun getFastestPitStop(): PitStop? {
        return pitStops.filter { it.estado == "OK" }.minByOrNull { it.tiempoSegundos }
    }

    private fun getAverageTime(): Double {
        val successfulPitStops = pitStops.filter { it.estado == "OK" }
        return if (successfulPitStops.isNotEmpty()) {
            successfulPitStops.map { it.tiempoSegundos }.average()
        } else {
            0.0
        }
    }

    private fun getTotalPitStops(): Int {
        return pitStops.size
    }


    @Composable
    private fun HomeScreen(
        onRegistrarClick: () -> Unit,
        onVerListadoClick: () -> Unit,
        pitStops: List<PitStop>,
        statisticsUpdateTrigger: Int
    ) {
        var refreshKey by remember { mutableStateOf(0) }
        var showUpdateMessage by remember { mutableStateOf(false) }
        var lastUpdateTime by remember { mutableStateOf("") }


        LaunchedEffect(statisticsUpdateTrigger) {
            if (statisticsUpdateTrigger > 0) {
                refreshKey = refreshKey + 1
                lastUpdateTime =
                    java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                showUpdateMessage = true
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Resumen de Pit Stops",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )


            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val fastestPitStop = pitStops.minByOrNull { it.tiempoSegundos }
                        val averageTime = if (pitStops.isNotEmpty()) {
                            pitStops.map { it.tiempoSegundos }.average()
                        } else 0.0
                        val totalPitStops = pitStops.size


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (lastUpdateTime.isNotEmpty()) {
                                Text(
                                    text = "📊 Actualizado: $lastUpdateTime",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7F8C8D),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "🔄 ${pitStops.size} registros",
                                fontSize = 12.sp,
                                color = Color(0xFF3498DB),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        StatisticRow(
                            icon = "🏎️",
                            text = "Pit stop más rápido: ${if (fastestPitStop != null) "${fastestPitStop.tiempoSegundos} s" else "N/A"}"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StatisticRow(
                            icon = "⏱️",
                            text = "Promedio de tiempos: ${
                                if (averageTime > 0) String.format(
                                    "%.2f",
                                    averageTime
                                ) else "N/A"
                            } s"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StatisticRow(
                            icon = "🔢",
                            text = "Total de paradas: $totalPitStops"
                        )
                    }
                }


                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Últimos pit stops",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C3E50)
                            )

                            Button(
                                onClick = {

                                    refreshKey = refreshKey + 1
                                    showUpdateMessage = true
                                    lastUpdateTime = java.text.SimpleDateFormat(
                                        "HH:mm:ss",
                                        java.util.Locale.getDefault()
                                    ).format(java.util.Date())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF3498DB
                                    )
                                ),
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔄 Actualizar",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ProfessionalBarChart(
                            pitStops = pitStops.takeLast(5) // Mostrar los últimos 5 pit stops
                        )


                        if (pitStops.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "📋 Detalles de los Últimos Pit Stops",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2C3E50),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    pitStops.takeLast(5).forEachIndexed { index, pitStop ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFDC143C),
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text(
                                                    text = pitStop.piloto,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF2C3E50),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }


                                            Text(
                                                text = "${pitStop.tiempoSegundos}s",
                                                fontSize = 12.sp,
                                                color = Color(0xFFDC143C),
                                                fontWeight = FontWeight.Bold
                                            )


                                            val estadoColor =
                                                if (pitStop.estado == "OK") Color(0xFF27AE60) else Color(
                                                    0xFFE74C3C
                                                )
                                            val estadoIcon =
                                                if (pitStop.estado == "OK") "✅" else "❌"

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = estadoIcon,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )
                                                Text(
                                                    text = pitStop.estado,
                                                    fontSize = 10.sp,
                                                    color = estadoColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        if (showUpdateMessage) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFD5F4E6))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✅",
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "Estadísticas y gráfico actualizados con los últimos datos registrados",
                                        fontSize = 12.sp,
                                        color = Color(0xFF27AE60),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }


            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRegistrarClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "Registrar Pit Stop",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onVerListadoClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34495E)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "Ver Listado",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
    private fun StatisticRow(
        icon: String,
        text: String
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = text,
                fontSize = 18.sp,
                color = Color(0xFF2C3E50),
                fontWeight = FontWeight.Medium
            )
        }
    }


    data class PilotData(
        val name: String,
        val team: String,
        val color: Color,
        val icon: String,
        val totalPitStops: Int,
        val successfulPitStops: Int,
        val fastestTime: Double?
    )

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
                .width(140.dp)
                .height(120.dp),
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
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @Composable
    private fun PilotCard(
        pilotData: PilotData,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👤",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pilotData.name.split(" ")[0], // Solo el primer nombre
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = pilotData.team,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${pilotData.totalPitStops} paradas",
                    fontSize = 10.sp,
                    color = pilotData.color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    private fun getPilotData(pitStops: List<PitStop>): List<PilotData> {
        val pilots = listOf(
            "Lewis Hamilton" to Color(0xFF00D2BE),
            "Max Verstappen" to Color(0xFF0600EF),
            "Charles Leclerc" to Color(0xFFDC143C),
            "Lando Norris" to Color(0xFFFF8700),
            "Fernando Alonso" to Color(0xFF006F62)
        )

        return pilots.map { (name, color) ->
            val pilotPitStops = pitStops.filter { it.piloto.equals(name, ignoreCase = true) }
            val successfulPitStops = pilotPitStops.filter { it.estado == "OK" }
            val fastestTime = successfulPitStops.minByOrNull { it.tiempoSegundos }?.tiempoSegundos

            PilotData(
                name = name,
                team = when (name) {
                    "Lewis Hamilton" -> "Mercedes"
                    "Max Verstappen" -> "Red Bull"
                    "Charles Leclerc" -> "Ferrari"
                    "Lando Norris" -> "McLaren"
                    "Fernando Alonso" -> "Aston Martin"
                    else -> "Unknown"
                },
                color = color,
                icon = "👤",
                totalPitStops = pilotPitStops.size,
                successfulPitStops = successfulPitStops.size,
                fastestTime = fastestTime
            )
        }
    }
}


