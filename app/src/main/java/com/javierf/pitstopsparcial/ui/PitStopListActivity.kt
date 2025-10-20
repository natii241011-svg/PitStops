package com.javierf.pitstopsparcial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javierf.pitstopsparcial.model.PitStop
import com.javierf.pitstopsparcial.theme.PitStopsParcialTheme
class PitStopListActivity : ComponentActivity() {
    private lateinit var pitStopsList: MutableList<PitStop>
    
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Actualizar la lista con los datos devueltos
            val updatedPitStops = result.data?.getParcelableArrayListExtra<PitStop>("updated_pitstops")
            if (updatedPitStops != null) {
                pitStopsList.clear()
                pitStopsList.addAll(updatedPitStops)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pitStopsList = intent.getParcelableArrayListExtra<PitStop>("pitstops")?.toMutableList() ?: mutableListOf()

        setContent {
            PitStopsParcialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PitStopListScreen(
                        pitStops = pitStopsList, 
                        onVolver = { 
                            val resultIntent = Intent()
                            resultIntent.putParcelableArrayListExtra("updated_pitstops", ArrayList(pitStopsList))
                            setResult(RESULT_OK, resultIntent)
                            finish() 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PitStopListScreen(
    pitStops: MutableList<PitStop>,
    onVolver: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var lista by remember { mutableStateOf(pitStops.toList()) }
    val context = LocalContext.current

    // Actualizar la lista local solo cuando se inicializa la pantalla
    LaunchedEffect(Unit) {
        lista = pitStops.toList()
    }

    // Función para eliminar un pit stop específico
    fun deletePitStop(pitStopToDelete: PitStop) {
        // Crear una copia de la lista original antes de modificar
        val originalSize = pitStops.size
        
        // Buscar el índice específico del pit stop a eliminar
        val indexToRemove = pitStops.indexOfFirst { it.id == pitStopToDelete.id }
        
        if (indexToRemove != -1) {
            // Eliminar solo el elemento en ese índice específico
            pitStops.removeAt(indexToRemove)
            
            // Actualizar la lista local con la lista modificada
            lista = pitStops.toList()
            
            // Mostrar mensaje de confirmación
            Toast.makeText(
                context,
                "✅ Pit stop de ${pitStopToDelete.piloto} eliminado (${originalSize} → ${pitStops.size})",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "❌ No se encontró el pit stop a eliminar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val filteredList = lista.filter {
        it.piloto.contains(searchQuery.text, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        // Header moderno
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏁",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = "Listado de Pit Stops",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        // Campo de búsqueda mejorado
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("🔍 Buscar por piloto") },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3498DB),
                    unfocusedBorderColor = Color(0xFFBDC3C7)
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Encabezado mejorado - ya no necesario con el nuevo diseño de tarjetas

        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(filteredList) { index, pitStop ->
                PitStopRow(index + 1, pitStop) {
                    deletePitStop(pitStop)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onVolver,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34495E)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "←",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    "Volver a Resumen",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PitStopRow(index: Int, pitStop: PitStop, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número de índice mejorado con gradiente
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFDC143C),
                                Color(0xFFB71C1C)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$index",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del piloto mejorada
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pitStop.piloto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pitStop.escuderia,
                    fontSize = 11.sp,
                    color = Color(0xFF7F8C8D),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tiempo con diseño mejorado
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pitStop.tiempoSegundos}s",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC143C)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Estado con diseño mejorado
            val colorEstado = if (pitStop.estado.equals("OK", true)) Color(0xFF27AE60) else Color(0xFFE74C3C)
            val iconEstado = if (pitStop.estado.equals("OK", true)) "✅" else "❌"

            Box(
                modifier = Modifier
                    .background(
                        color = if (pitStop.estado.equals("OK", true)) Color(0xFFE8F5E8) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = iconEstado,
                        fontSize = 12.sp
                    )
                    Text(
                        text = pitStop.estado,
                        fontSize = 10.sp,
                        color = colorEstado,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de eliminar con diseño moderno
            androidx.compose.material3.IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = Color(0xFFE74C3C),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Text("🗑️", fontSize = 14.sp)
            }
        }
    }
}
