package com.danidev.appmovil2.ui.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias del historial de lanzamientos de [DiceViewModel] (HU-011).
 *
 * Verifican los criterios de aceptación a nivel de lógica: el historial se
 * actualiza con cada lanzamiento, el más reciente va primero y nunca se
 * guardan más de [DiceUiState.MAX_HISTORY_SIZE] resultados.
 */
class DiceViewModelTest {

    private lateinit var viewModel: DiceViewModel

    @Before
    fun setUp() {
        viewModel = DiceViewModel()
    }

    @Test
    fun `el historial empieza vacio`() {
        assertTrue(viewModel.uiState.value.history.isEmpty())
    }

    @Test
    fun `registrar un resultado actualiza el valor actual y el historial`() {
        viewModel.registerResult(4)

        val state = viewModel.uiState.value
        assertEquals(4, state.currentDiceValue)
        assertEquals(listOf(4), state.history.map { it.value })
    }

    @Test
    fun `el resultado mas reciente queda primero`() {
        listOf(1, 2, 3).forEach(viewModel::registerResult)

        assertEquals(listOf(3, 2, 1), viewModel.uiState.value.history.map { it.value })
    }

    @Test
    fun `el historial conserva solo los ultimos 10 resultados`() {
        // 12 lanzamientos: 1..6 y luego 1..6 otra vez.
        val rolls = (1..6) + (1..6)
        rolls.forEach(viewModel::registerResult)

        val history = viewModel.uiState.value.history
        assertEquals(DiceUiState.MAX_HISTORY_SIZE, history.size)
        // Se descartan los 2 más antiguos (1 y 2 del primer ciclo).
        assertEquals(rolls.reversed().take(10), history.map { it.value })
    }

    @Test
    fun `cada lanzamiento tiene un id unico aunque el valor se repita`() {
        repeat(3) { viewModel.registerResult(5) }

        val ids = viewModel.uiState.value.history.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `rollDice registra un valor entre 1 y 6`() {
        viewModel.rollDice()

        val value = viewModel.uiState.value.history.single().value
        assertTrue(value in 1..6)
    }

    @Test
    fun `setDiceCount actualiza la cantidad de dados y sus valores`() {
        viewModel.setDiceCount(2)

        val state = viewModel.uiState.value
        assertEquals(2, state.diceCount)
        assertEquals(2, state.currentDiceValues.size)
    }

    @Test
    fun `registerResult con lista de dados registra los valores y calcula la suma`() {
        viewModel.registerResult(listOf(3, 5))

        val state = viewModel.uiState.value
        assertEquals(listOf(3, 5), state.currentDiceValues)
        assertEquals(8, state.currentDiceValue)
        assertEquals(listOf(3, 5), state.history.first().values)
        assertEquals(8, state.history.first().value)
    }

    @Test
    fun `rollDice con dos dados genera dos valores entre 1 y 6`() {
        viewModel.setDiceCount(2)
        viewModel.rollDice()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentDiceValues.size)
        assertTrue(state.currentDiceValues.all { it in 1..6 })
        assertEquals(state.currentDiceValues.sum(), state.currentDiceValue)
    }

    @Test
    fun `clearHistory reinicia el historial y los valores de los dados`() {
        viewModel.setDiceCount(2)
        viewModel.registerResult(listOf(3, 5))
        assertEquals(1, viewModel.uiState.value.history.size)

        viewModel.clearHistory()

        val state = viewModel.uiState.value
        assertTrue(state.history.isEmpty())
        assertEquals(listOf(1, 1), state.currentDiceValues)
        assertEquals(2, state.diceCount)
    }
}
