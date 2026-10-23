package com.danidev.appmovil2.ui.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias de las estadísticas de tiradas (HU-13).
 *
 * Verifican que cada cara cuenta cuántas veces salió, que los dados bloqueados no
 * suman, que el conteo no se recorta como el historial y que se reinicia con la partida.
 */
class RollStatisticsTest {

    private lateinit var viewModel: DiceViewModel

    @Before
    fun setUp() {
        viewModel = DiceViewModel()
    }

    private fun counts() = viewModel.uiState.value.faceCounts

    @Test
    fun `sin tiradas no hay conteos`() {
        assertTrue(counts().isEmpty())
    }

    @Test
    fun `cada tirada suma una salida a la cara obtenida`() {
        viewModel.registerResult(6)
        viewModel.registerResult(6)
        viewModel.registerResult(3)

        assertEquals(2, counts()[6])
        assertEquals(1, counts()[3])
        assertEquals(null, counts()[1])
    }

    @Test
    fun `con dos dados cada dado suma a su propia cara`() {
        viewModel.registerResult(listOf(4, 4))
        viewModel.registerResult(listOf(2, 5))

        assertEquals(2, counts()[4])
        assertEquals(1, counts()[2])
        assertEquals(1, counts()[5])
    }

    @Test
    fun `un dado bloqueado no vuelve a contar`() {
        // El 6 estaba bloqueado: solo cuenta el 3, que es el dado que se lanzó.
        viewModel.registerResult(values = listOf(6, 3), activeValues = listOf(3))

        assertEquals(1, counts()[3])
        assertEquals(null, counts()[6])
    }

    @Test
    fun `el conteo no se recorta como el historial`() {
        repeat(DiceUiState.MAX_HISTORY_SIZE + 5) { viewModel.registerResult(2) }

        assertEquals(DiceUiState.MAX_HISTORY_SIZE, viewModel.uiState.value.history.size)
        assertEquals(DiceUiState.MAX_HISTORY_SIZE + 5, counts()[2])
    }

    @Test
    fun `jugar de nuevo reinicia las estadisticas`() {
        viewModel.registerResult(5)

        viewModel.clearHistory()

        assertTrue(counts().isEmpty())
    }

    @Test
    fun `plusRolls no modifica el mapa original`() {
        val original = mapOf(1 to 1)

        val result = original.plusRolls(listOf(1, 2))

        assertEquals(mapOf(1 to 1), original)
        assertEquals(mapOf(1 to 2, 2 to 1), result)
    }
}
