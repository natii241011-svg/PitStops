package com.javierf.pitstopsparcial

import com.javierf.pitstopsparcial.model.PitStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class PitStopsTest {

    /** Dataset pequeño para usar en los tests */
    private fun sampleList(): MutableList<PitStop> = mutableListOf(
        PitStop(
            id = 1L,
            piloto = "Lewis Hamilton",
            escuderia = "Mercedes",
            tiempoSegundos = 2.35,
            compuesto = "Soft",
            neumaticosCambiados = 4,
            estado = "OK",
            motivoFallo = null,
            mecanicoPrincipal = "Juan",
            fechaHora = LocalDateTime.now()
        ),
        PitStop(
            id = 2L,
            piloto = "Max Verstappen",
            escuderia = "Red Bull",
            tiempoSegundos = 2.10,
            compuesto = "Medium",
            neumaticosCambiados = 4,
            estado = "Fallido",
            motivoFallo = "Tuerca",
            mecanicoPrincipal = "Ana",
            fechaHora = LocalDateTime.now()
        ),
        PitStop(
            id = 3L,
            piloto = "Charles Leclerc",
            escuderia = "Ferrari",
            tiempoSegundos = 2.60,
            compuesto = "Hard",
            neumaticosCambiados = 4,
            estado = "OK",
            motivoFallo = null,
            mecanicoPrincipal = "Luis",
            fechaHora = LocalDateTime.now()
        )
    )

    /** 1) Encuentra el pit stop más rápido SOLO entre los que están OK */
    @Test
    fun fastestOk_withList_returnsFastestFromOkOnly() {
        val list = sampleList()

        // lógica in-test (sin util): filtrar OK y tomar el mínimo por tiempo
        val fastestOk = list.filter { it.estado.equals("OK", true) }
            .minByOrNull { it.tiempoSegundos }

        assertEquals(1L, fastestOk?.id)
        assertEquals(2.35, fastestOk!!.tiempoSegundos, 1e-6)
    }

    /** 2) Calcula el promedio SOLO con OK, y si no hay OK devuelve 0.0 */
    @Test
    fun averageOk_handlesOnlyOkAndEmptyAsZero() {
        val list = sampleList()

        val okTimes = list.filter { it.estado.equals("OK", true) }.map { it.tiempoSegundos }
        val avg = if (okTimes.isEmpty()) 0.0 else okTimes.average()

        // (2.35 + 2.60) / 2 = 2.475
        assertEquals(2.475, avg, 1e-6)

        // Caso sin OK
        val onlyFailed = list.filter { it.estado.equals("Fallido", true) }
        val okTimesEmpty = onlyFailed.filter { it.estado.equals("OK", true) }.map { it.tiempoSegundos }
        val avgEmpty = if (okTimesEmpty.isEmpty()) 0.0 else okTimesEmpty.average()

        assertEquals(0.0, avgEmpty, 0.0)
    }

    /** 3) Buscar por piloto (contains, ignoreCase) y eliminar por id */
    @Test
    fun filterByPilot_and_deleteById_works() {
        val list = sampleList()

        // Buscar: contiene "leW" -> debe devolver a "Lewis Hamilton"
        val filtered = list.filter { it.piloto.contains("leW", ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertEquals("Lewis Hamilton", filtered.first().piloto)

        // Eliminar por id (3L). Usamos mutableList para simular la eliminación real.
        val removed = list.removeIf { it.id == 3L }
        assertEquals(true, removed)
        assertEquals(2, list.size)
        val stillThere = list.firstOrNull { it.id == 3L }
        assertNull(stillThere)
    }
}

