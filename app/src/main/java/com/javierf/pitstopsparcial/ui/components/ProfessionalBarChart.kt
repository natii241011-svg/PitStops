package com.javierf.pitstopsparcial.ui.components

import android.content.Context
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.RoundedCornerShape
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.javierf.pitstopsparcial.model.PitStop

@Composable
fun ProfessionalBarChart(
    pitStops: List<PitStop>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del gráfico
            Text(
                text = "📊 Gráfico de Pit Stops",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Etiqueta del eje Y
            Text(
                text = "Tiempos (s)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF34495E),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            if (pitStops.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "No hay datos para mostrar",
                            fontSize = 16.sp,
                            color = Color(0xFF7F8C8D),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Registra tu primer pit stop",
                            fontSize = 14.sp,
                            color = Color(0xFF95A5A6),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                // Gráfico profesional
                AndroidView(
                    factory = { context ->
                        createBarChart(context, pitStops)
                    },
                    update = { chart ->
                        updateChart(chart, pitStops)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
                
                // Etiqueta del eje X
                Text(
                    text = "Últimos pit stops",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF34495E),
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }
        }
    }
}

private fun createBarChart(context: Context, pitStops: List<PitStop>): BarChart {
    val chart = BarChart(context)
    
    // Configuración básica del gráfico
    chart.description.isEnabled = false
    chart.setDrawGridBackground(false)
    chart.setDrawBarShadow(false)
    chart.setDrawValueAboveBar(true)
    chart.setMaxVisibleValueCount(60)
    chart.setPinchZoom(false)
    chart.setDrawGridBackground(false)
    
    // Configurar eje X
    val xAxis = chart.xAxis
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(false)
    xAxis.setDrawAxisLine(true)
    xAxis.setDrawLabels(false) // No mostrar números en el eje X
    xAxis.granularity = 1f
    
    // Configurar eje Y izquierdo
    val leftAxis = chart.axisLeft
    leftAxis.setDrawGridLines(true)
    leftAxis.setDrawAxisLine(true)
    leftAxis.textSize = 12f
    leftAxis.textColor = android.graphics.Color.parseColor("#34495E")
    leftAxis.axisMinimum = 0f
    leftAxis.setLabelCount(6, true) // 6 etiquetas para mejor escala
    leftAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
            return "${value.toInt()}s"
        }
    }
    
    // Configurar eje Y derecho
    val rightAxis = chart.axisRight
    rightAxis.isEnabled = false
    
    // Configurar leyenda
    val legend = chart.legend
    legend.isEnabled = false
    
    // Configurar animación
    chart.animateY(1000)
    
    return chart
}

private fun updateChart(chart: BarChart, pitStops: List<PitStop>) {
    if (pitStops.isEmpty()) return
    
    // Tomar los últimos 10 pit stops
    val lastPitStops = pitStops.takeLast(10)
    
    // Crear entradas para el gráfico
    val entries = mutableListOf<BarEntry>()
    
    lastPitStops.forEachIndexed { index, pitStop ->
        entries.add(BarEntry(index.toFloat(), pitStop.tiempoSegundos.toFloat()))
    }
    
    // Crear dataset
    val dataSet = BarDataSet(entries, "Pit Stops")
    dataSet.color = android.graphics.Color.parseColor("#27AE60") // Verde
    dataSet.setDrawValues(false) // No mostrar valores encima de las barras
    
    // Configurar el dataset
    val dataSets = mutableListOf<IBarDataSet>()
    dataSets.add(dataSet)
    
    val barData = BarData(dataSets)
    barData.barWidth = 0.7f
    
    chart.data = barData
    chart.invalidate()
    
    // Configurar descripción del eje Y
    chart.setDescription(null) // Eliminar descripción por defecto
}
