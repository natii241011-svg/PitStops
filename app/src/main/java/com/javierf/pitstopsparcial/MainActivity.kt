package com.javierf.pitstopsparcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javierf.pitstopsparcial.model.PitStop
import com.javierf.pitstopsparcial.ui.PitStopFormComposeActivity
import com.javierf.pitstopsparcial.ui.PitStopListActivity
import com.javierf.pitstopsparcial.ui.components.ProfessionalBarChart
import com.javierf.pitstopsparcial.theme.PitStopsParcialTheme

// ✅ Clave local para el intent de resultado
object Extras {
    const val PITSTOP = "extra_pitstop"
}

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
                            intent.putParcelableArrayListExtra("pitstops", ArrayList(pitStops))
                            listLauncher.launch(intent)
                        },
                        pitStops = pitStops,
                        statisticsUpdateTrigger = statisticsUpdateTrigger
                    )
                }
            }
        }
    }

    // ⬇️ Recibe el nuevo PitStop desde el formulario Compose
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val pit: PitStop? = if (android.os.Build.VERSION.SDK_INT >= 33) {
                result.data?.getParcelableExtra(Extras.PITSTOP, PitStop::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<PitStop>(Extras.PITSTOP)
            }

            if (pit != null) {
                pitStops.add(pit)
                Toast.makeText(
                    this,
                    "✅ Nuevo pit stop: ${pit.piloto} • ${pit.tiempoSegundos}s • ${pit.estado}",
                    Toast.LENGTH_LONG
                ).show()
                updateStatistics()
            }
        }
    }

    // ⬇️ Recibe la lista actualizada desde la pantalla de listado (cuando se eliminan)
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
                    "🔄 Lista actualizada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateStatistics() {
        statisticsUpdateTrigger++
    }

    @Composable
    private fun HomeScreen(
        onRegistrarClick: () -> Unit,
        onVerListadoClick: () -> Unit,
        pitStops: List<PitStop>,
        statisticsUpdateTrigger: Int
    ) {
        var lastUpdateTime by remember { mutableStateOf("") }
        var showUpdateMessage by remember { mutableStateOf(false) }

        LaunchedEffect(statisticsUpdateTrigger) {
            if (statisticsUpdateTrigger > 0) {
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
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ======= Estadísticas =======
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val fastestPitStop =
                            pitStops.filter { it.estado == "OK" }.minByOrNull { it.tiempoSegundos }
                        val averageTime = pitStops.filter { it.estado == "OK" }
                            .map { it.tiempoSegundos }
                            .average()
                            .takeIf { !it.isNaN() } ?: 0.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (lastUpdateTime.isNotEmpty()) {
                                Text(
                                    text = "📊 Actualizado: $lastUpdateTime",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7F8C8D)
                                )
                            }
                            Text(
                                text = "🔄 ${pitStops.size} registros",
                                fontSize = 12.sp,
                                color = Color(0xFF3498DB)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        StatisticRow("🏎️", "Pit stop más rápido: ${fastestPitStop?.tiempoSegundos ?: "N/A"} s")
                        StatisticRow(
                            "⏱️",
                            "Promedio de tiempos: ${if (averageTime > 0) String.format("%.2f", averageTime) else "N/A"} s"
                        )
                        StatisticRow("🔢", "Total de paradas: ${pitStops.size}")
                    }
                }

                // ======= Gráfico + últimos registros =======
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Últimos pit stops",
                                fontSize = 18.sp,
                                color = Color(0xFF2C3E50)
                            )
                            Button(
                                onClick = {
                                    lastUpdateTime =
                                        java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                            .format(java.util.Date())
                                    showUpdateMessage = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB)),
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "🔄 Actualizar", fontSize = 10.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ProfessionalBarChart(pitStops = pitStops.takeLast(5))

                        if (pitStops.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "📋 Detalles de los últimos ${minOf(5, pitStops.size)}",
                                fontSize = 14.sp,
                                color = Color(0xFF2C3E50)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            pitStops.takeLast(5).forEachIndexed { index, pit ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("#${index + 1}  ${pit.piloto}", fontSize = 12.sp)
                                    Text("${pit.tiempoSegundos}s", fontSize = 12.sp, color = Color(0xFFDC143C))
                                    val estadoIcon = if (pit.estado == "OK") "✅" else "❌"
                                    val estadoColor = if (pit.estado == "OK") Color(0xFF27AE60) else Color(0xFFE74C3C)
                                    Text("$estadoIcon ${pit.estado}", fontSize = 12.sp, color = estadoColor)
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
                                    Text("✅", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                                    Text(
                                        text = "Estadísticas y gráfico actualizados",
                                        fontSize = 12.sp,
                                        color = Color(0xFF27AE60)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ======= Botones inferiores =======
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
                    Text("Registrar Pit Stop", color = Color.White, fontSize = 16.sp)
                }

                Button(
                    onClick = onVerListadoClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34495E)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text("Ver Listado", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }

    @Composable
    private fun StatisticRow(icon: String, text: String) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            Text(text = icon, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
            Text(text = text, fontSize = 18.sp, color = Color(0xFF2C3E50))
        }
    }
}
