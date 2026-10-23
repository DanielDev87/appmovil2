package com.danidev.appmovil2.ui.dice

import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.determineMultiplayerOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Pruebas de la partida con varios jugadores que se turnan en el mismo teléfono. */
class MultiplayerTest {

    private lateinit var viewModel: DiceViewModel

    @Before
    fun setUp() {
        viewModel = DiceViewModel()
    }

    private fun roll(value: Int) = viewModel.registerResult(listOf(value), listOf(value))

    private val state get() = viewModel.uiState.value

    @Test
    fun `por defecto juega una sola persona`() {
        assertFalse(state.isMultiplayer)
        assertEquals(1, state.players.size)
    }

    @Test
    fun `cambiar la cantidad de jugadores crea un marcador para cada uno`() {
        viewModel.setPlayerCount(3)

        assertTrue(state.isMultiplayer)
        assertEquals(3, state.players.size)
        assertEquals(0, state.currentPlayer)
    }

    @Test
    fun `la cantidad de jugadores se limita entre 1 y 4`() {
        viewModel.setPlayerCount(9)
        assertEquals(4, state.players.size)

        viewModel.setPlayerCount(0)
        assertEquals(1, state.players.size)
    }

    @Test
    fun `el turno pasa al siguiente jugador despues de cada tiro y vuelve al primero`() {
        viewModel.setPlayerCount(3)

        roll(2)
        assertEquals(1, state.currentPlayer)
        roll(3)
        assertEquals(2, state.currentPlayer)
        roll(4)
        assertEquals(0, state.currentPlayer)
    }

    @Test
    fun `cada jugador suma sus propios puntos y lanzamientos`() {
        viewModel.setPlayerCount(2)

        roll(5) // jugador 1
        roll(2) // jugador 2
        roll(4) // jugador 1

        assertEquals(listOf(9, 2), state.players.map { it.score })
        assertEquals(listOf(2, 1), state.players.map { it.launches })
    }

    @Test
    fun `los puntos que se muestran son los del jugador al que le toca`() {
        viewModel.setPlayerCount(2)
        roll(5)

        // Ahora le toca al jugador 2, que aún no ha tirado.
        assertEquals(0, state.score)
        roll(3)
        // Y otra vez al jugador 1, con sus 5 puntos.
        assertEquals(5, state.score)
    }

    @Test
    fun `los dados bloqueados se sueltan al cambiar de turno`() {
        viewModel.setPlayerCount(2)
        viewModel.setDiceCount(2)
        viewModel.toggleLock(0)

        viewModel.registerResult(listOf(3, 4), listOf(4))

        assertTrue(state.lockedIndices.isEmpty())
    }

    @Test
    fun `con un solo jugador el turno no cambia y los dados siguen bloqueados`() {
        viewModel.setDiceCount(2)
        viewModel.toggleLock(0)

        viewModel.registerResult(listOf(3, 4), listOf(4))

        assertEquals(0, state.currentPlayer)
        assertEquals(setOf(0), state.lockedIndices)
    }

    @Test
    fun `gana el primero que llega a la meta y el turno ya no cambia`() {
        viewModel.setPlayerCount(2)
        viewModel.setGoals(targetScore = 10, maxLaunches = 5)

        roll(6) // jugador 1: 6
        roll(2) // jugador 2: 2
        roll(5) // jugador 1: 11, llega a la meta

        assertEquals(GameOutcome.WON, state.outcome)
        assertEquals(listOf(0), state.winners)
        assertEquals(0, state.currentPlayer)
    }

    @Test
    fun `si nadie llega a la meta gana quien sumo mas al agotar los lanzamientos`() {
        viewModel.setPlayerCount(2)
        viewModel.setGoals(targetScore = 100, maxLaunches = 2)

        roll(1) // jugador 1
        roll(6) // jugador 2
        roll(2) // jugador 1: 3
        roll(3) // jugador 2: 9

        assertEquals(GameOutcome.LAUNCH_LIMIT_REACHED, state.outcome)
        assertEquals(listOf(1), state.winners)
    }

    @Test
    fun `si hay empate ganan todos los empatados`() {
        viewModel.setPlayerCount(3)
        viewModel.setGoals(targetScore = 100, maxLaunches = 1)

        roll(4)
        roll(4)
        roll(2)

        assertEquals(GameOutcome.LAUNCH_LIMIT_REACHED, state.outcome)
        assertEquals(listOf(0, 1), state.winners)
    }

    @Test
    fun `la partida sigue mientras a algun jugador le queden lanzamientos`() {
        viewModel.setPlayerCount(2)
        viewModel.setGoals(targetScore = 100, maxLaunches = 2)

        roll(3)
        roll(3)
        roll(3) // al jugador 2 aún le queda un lanzamiento

        assertEquals(GameOutcome.IN_PROGRESS, state.outcome)
        assertTrue(state.winners.isEmpty())
    }

    @Test
    fun `reiniciar conserva la cantidad de jugadores y devuelve el turno al primero`() {
        viewModel.setPlayerCount(3)
        roll(5)
        roll(5)

        viewModel.clearHistory()

        assertEquals(3, state.players.size)
        assertEquals(0, state.currentPlayer)
        assertTrue(state.players.all { it.score == 0 && it.launches == 0 })
    }

    @Test
    fun `fijar la misma cantidad de jugadores no borra la partida`() {
        viewModel.setPlayerCount(2)
        roll(5)

        viewModel.setPlayerCount(2)

        assertEquals(5, state.players[0].score)
    }

    @Test
    fun `cambiar la cantidad de jugadores empieza la partida de cero`() {
        viewModel.setPlayerCount(2)
        roll(5)

        viewModel.setPlayerCount(3)

        assertTrue(state.players.all { it.score == 0 })
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun `el resultado de varios jugadores se calcula con todos los marcadores`() {
        assertEquals(GameOutcome.WON, determineMultiplayerOutcome(listOf(10, 35), listOf(3, 3), 35, 8))
        assertEquals(GameOutcome.LAUNCH_LIMIT_REACHED, determineMultiplayerOutcome(listOf(10, 20), listOf(8, 8), 35, 8))
        assertEquals(GameOutcome.IN_PROGRESS, determineMultiplayerOutcome(listOf(10, 20), listOf(8, 7), 35, 8))
    }
}
