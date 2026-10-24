package com.danidev.appmovil2.ui.dice

import com.danidev.appmovil2.Difficulty
import com.danidev.appmovil2.MAX_DICE
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.determineRollIndicator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Pruebas del control sobre los dados: hasta tres, con modificador y con caras distintas. */
class DiceControlTest {

    private lateinit var viewModel: DiceViewModel

    @Before
    fun setUp() {
        viewModel = DiceViewModel()
    }

    private val state get() = viewModel.uiState.value

    @Test
    fun `se pueden usar hasta tres dados`() {
        viewModel.setDiceCount(3)

        assertEquals(3, state.diceCount)
        assertEquals(3, state.currentDiceValues.size)
    }

    @Test
    fun `la cantidad de dados se limita entre uno y el maximo`() {
        viewModel.setDiceCount(9)
        assertEquals(MAX_DICE, state.diceCount)

        viewModel.setDiceCount(0)
        assertEquals(1, state.diceCount)
    }

    @Test
    fun `un modificador positivo suma puntos al tiro`() {
        viewModel.registerResult(values = listOf(4), activeValues = listOf(4), diceSides = listOf(6), modifier = 2)

        assertEquals(6, state.score)
        assertEquals(2, state.history.first().modifier)
    }

    @Test
    fun `un modificador negativo resta pero el tiro no baja de cero puntos`() {
        viewModel.registerResult(listOf(5), listOf(5), diceSides = listOf(6), modifier = -2)
        assertEquals(3, state.score)

        viewModel.registerResult(listOf(1), listOf(1), diceSides = listOf(6), modifier = -5)
        assertEquals(3, state.score)
    }

    @Test
    fun `los puntos de un tiro en el historial incluyen el modificador`() {
        viewModel.registerResult(listOf(3, 4), listOf(3, 4), diceSides = listOf(6, 6), modifier = 1)

        assertEquals(8, state.history.first().points)
        assertEquals(7, state.history.first().value)
    }

    @Test
    fun `cada dado del tiro recuerda sus propias caras`() {
        viewModel.registerResult(listOf(17, 4), listOf(17, 4), diceSides = listOf(20, 6))

        val record = state.history.first()
        assertEquals(20, record.sidesAt(0))
        assertEquals(6, record.sidesAt(1))
    }

    @Test
    fun `con dados distintos el critico es del dado que saca su cara mas alta`() {
        // El 6 es el maximo de un D6 pero un 6 en un D20 no lo es.
        assertEquals(RollIndicator.CRITICAL, determineRollIndicator(listOf(20, 3), listOf(20, 6)))
        assertEquals(RollIndicator.NONE, determineRollIndicator(listOf(6, 3), listOf(20, 6)))
        assertEquals(RollIndicator.CRITICAL, determineRollIndicator(listOf(6, 3), listOf(6, 20)))
    }

    @Test
    fun `la pifia exige que todos los dados salgan en uno`() {
        assertEquals(RollIndicator.FUMBLE, determineRollIndicator(listOf(1, 1, 1), listOf(20, 6, 12)))
        assertEquals(RollIndicator.NONE, determineRollIndicator(listOf(1, 2, 1), listOf(20, 6, 12)))
    }

    @Test
    fun `el critico se registra con las caras de cada dado`() {
        viewModel.registerResult(listOf(20, 2), listOf(20, 2), diceSides = listOf(20, 6))

        assertEquals(RollIndicator.CRITICAL, state.rollIndicator)
        assertEquals(1, state.players.first().criticals)
    }

    @Test
    fun `la meta de la dificultad cuenta cada dado con sus caras y el modificador`() {
        // D20 + D6: promedios 10.5 + 3.5 = 14 por tiro; 10 lanzamientos y factor de Normal.
        val mixed = Difficulty.NORMAL.targetScore(listOf(20, 6), modifier = 0)
        assertEquals(Math.round(14.0 * 10 * Difficulty.NORMAL.goalFactor).toInt(), mixed)

        val withBonus = Difficulty.NORMAL.targetScore(listOf(20, 6), modifier = 3)
        assertTrue(withBonus > mixed)

        val withPenalty = Difficulty.NORMAL.targetScore(listOf(20, 6), modifier = -3)
        assertTrue(withPenalty < mixed)
    }

    @Test
    fun `la meta con la lista de caras coincide con la de siempre para un solo tipo de dado`() {
        for (difficulty in Difficulty.entries) {
            assertEquals(
                difficulty.targetScore(sides = 6, diceCount = 2),
                difficulty.targetScore(listOf(6, 6), modifier = 0)
            )
        }
    }
}
