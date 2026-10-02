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
}
