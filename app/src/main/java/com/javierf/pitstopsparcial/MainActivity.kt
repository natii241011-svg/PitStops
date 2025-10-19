package com.javierf.pitstopsparcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.javierf.pitstopsparcial.model.PitStop
import com.javierf.pitstopsparcial.ui.PitStopFormActivity
import com.javierf.pitstopsparcial.ui.PitStopListActivity
import com.javierf.pitstopsparcial.theme.PitStopsParcialTheme
class MainActivity : ComponentActivity() {
    private val pitStops = mutableListOf<PitStop>()

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
                            val intent = Intent(this, PitStopFormActivity::class.java)
                            launcher.launch(intent)
                        },
                        onVerListadoClick = {
                            val intent = Intent(this, PitStopListActivity::class.java)
                            intent.putParcelableArrayListExtra(
                                "pitstops",
                                ArrayList(pitStops)
                            )
                            startActivity(intent)
                        }
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
                    "Guardado: ${pit.piloto} • ${pit.tiempoSegundos}s • ${pit.estado}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onRegistrarClick: () -> Unit,
    onVerListadoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onRegistrarClick,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Registrar Pit Stop")
        }

        Button(
            onClick = onVerListadoClick,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Listado Pit Stops")
        }
    }
}

