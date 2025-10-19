package com.javierf.pitstopsparcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.javierf.pitstopsparcial.model.PitStop
import com.javierf.pitstopsparcial.ui.PitStopFormActivity
import com.javierf.pitstopsparcial.ui.theme.PitStopsParcialTheme   // ✅ usa tu tema Compose

class MainActivity : ComponentActivity() {

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
                        }
                    )
                }
            }
        }
    }

    // launcher para recibir el resultado del formulario
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val pit =
                result.data?.getParcelableExtra<PitStop>(PitStopFormActivity.EXTRA_PITSTOP)
            if (pit != null) {
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
    onRegistrarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onRegistrarClick) {
            Text("Registrar Pit Stop")
        }
    }
}
