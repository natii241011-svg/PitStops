package com.javierf.pitstopsparcial.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javierf.pitstopsparcial.model.PitStop
import com.javierf.pitstopsparcial.theme.PitStopsParcialTheme
class PitStopListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pitStops = intent.getParcelableArrayListExtra<PitStop>("pitstops") ?: arrayListOf()

        setContent {
            PitStopsParcialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PitStopListScreen(pitStops = pitStops, onVolver = { finish() })
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

    val filteredList = lista.filter {
        it.piloto.contains(searchQuery.text, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Listado Pit Stops",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("\uD83D\uDD0D Buscar") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F2F2))
                .padding(vertical = 8.dp)
        ) {
            Text(
                "#",
                modifier = Modifier.weight(0.1f).fillMaxWidth(),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                "Piloto",
                modifier = Modifier.weight(0.4f).fillMaxWidth(),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                "Sg",
                modifier = Modifier.weight(0.25f).fillMaxWidth(),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                "Estado",
                modifier = Modifier.weight(0.15f).fillMaxWidth(),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(filteredList) { index, pitStop ->
                PitStopRow(index + 1, pitStop) {
                    lista = lista.toMutableList().apply { remove(pitStop) }
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
fun PitStopRow(index: Int, pitStop: PitStop, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            index.toString(),
            modifier = Modifier.weight(0.1f).fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Text(
            pitStop.piloto,
            modifier = Modifier.weight(0.4f).fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Text(
            pitStop.tiempoSegundos.toString(),
            modifier = Modifier.weight(0.25f).fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        val colorEstado = if (pitStop.estado.equals("OK", true)) Color(0xFF4CAF50) else Color(0xFFF44336)
        val fontSizeEstado = if (pitStop.estado.equals("Fallido", true)) 12.sp else 14.sp
        Box(
            modifier = Modifier
                .weight(0.15f)
                .fillMaxWidth()
                .background(colorEstado, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                pitStop.estado,
                color = Color.White,
                fontSize = fontSizeEstado,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.size(32.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("🗑️", color = Color(0xFFF44336), fontSize = 12.sp)
        }
    }
}
