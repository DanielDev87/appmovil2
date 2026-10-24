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

    @Test
    fun `topFaces devuelve las mas frecuentes de mayor a menor`() {
        val counts = mapOf(1 to 2, 2 to 5, 3 to 0, 4 to 3, 5 to 1)

        assertEquals(listOf(2 to 5, 4 to 3, 1 to 2), counts.topFaces(3))
    }

    @Test
    fun `topFaces desempata por la cara mas baja y omite las que no salieron`() {
        val counts = mapOf(6 to 2, 3 to 2, 4 to 0)

        assertEquals(listOf(3 to 2, 6 to 2), counts.topFaces(3))
        assertTrue(emptyMap<Int, Int>().topFaces().isEmpty())
    }

    @Test
    fun `en una tablet caben mas tarjetas por fila segun el dado`() {
        assertEquals(3, columnsFor(6, wide = false))
        assertEquals(4, columnsFor(12, wide = false))
        assertEquals(3, columnsFor(6, wide = true))
        assertEquals(4, columnsFor(8, wide = true))
        assertEquals(4, columnsFor(12, wide = true))
        assertEquals(5, columnsFor(20, wide = true))
    }
}
