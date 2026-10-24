package com.danidev.appmovil2.ui.settings

import com.danidev.appmovil2.Difficulty
import com.danidev.appmovil2.MAX_LAUNCHES
import com.danidev.appmovil2.MAX_TARGET_SCORE
import com.danidev.appmovil2.MIN_MAX_LAUNCHES
import com.danidev.appmovil2.TARGET_SCORE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de los ajustes de la partida: valores por defecto, cambio y persistencia. */
class GameSettingsViewModelTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeGameSettingsStore(var saved: GameSettings = GameSettings()) : GameSettingsStore {
        override fun load() = saved
        override fun save(settings: GameSettings) {
            saved = settings
        }
    }

    @Test
    fun `por defecto suena y agitar el telefono esta activado`() {
        val settings = GameSettingsViewModel(FakeGameSettingsStore()).settings.value

        assertFalse(settings.soundMuted)
        assertTrue(settings.shakeEnabled)
    }

    @Test
    fun `silenciar el sonido se aplica y se guarda`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)

        viewModel.setSoundMuted(true)

        assertTrue(viewModel.settings.value.soundMuted)
        assertTrue(store.saved.soundMuted)
    }

    @Test
    fun `cambiar un ajuste no modifica el otro`() {
        val viewModel = GameSettingsViewModel(FakeGameSettingsStore())

        viewModel.setShakeEnabled(false)

        assertFalse(viewModel.settings.value.shakeEnabled)
        assertFalse(viewModel.settings.value.soundMuted)
    }

    @Test
    fun `una nueva sesion recupera los ajustes guardados`() {
        val store = FakeGameSettingsStore()
        GameSettingsViewModel(store).apply {
            setSoundMuted(true)
            setShakeEnabled(false)
        }

        assertEquals(
            GameSettings(soundMuted = true, shakeEnabled = false),
            GameSettingsViewModel(store).settings.value
        )
    }

    @Test
    fun `la meta y los lanzamientos empiezan en los valores de siempre`() {
        val settings = GameSettingsViewModel(FakeGameSettingsStore()).settings.value

        assertEquals(TARGET_SCORE, settings.targetScore)
        assertEquals(MAX_LAUNCHES, settings.maxLaunches)
    }

    @Test
    fun `cambiar la meta se guarda y respeta los limites`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)

        viewModel.setTargetScore(50)
        viewModel.setMaxLaunches(1)

        assertEquals(50, store.saved.targetScore)
        assertEquals(MIN_MAX_LAUNCHES, viewModel.settings.value.maxLaunches)

        viewModel.setTargetScore(10_000)
        assertEquals(MAX_TARGET_SCORE, viewModel.settings.value.targetScore)
    }

    @Test
    fun `por defecto juega una persona y los nombres estan vacios`() {
        val settings = GameSettingsViewModel(FakeGameSettingsStore()).settings.value

        assertEquals(1, settings.playerCount)
        assertEquals(listOf("", ""), settings.playerNames)
    }

    @Test
    fun `la cantidad de jugadores se guarda y se limita entre 1 y 3`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)

        viewModel.setPlayerCount(3)
        assertEquals(3, store.saved.playerCount)

        viewModel.setPlayerCount(10)
        assertEquals(3, viewModel.settings.value.playerCount)
        viewModel.setPlayerCount(-2)
        assertEquals(1, viewModel.settings.value.playerCount)
    }

    @Test
    fun `se guarda el nombre de cada jugador adicional sin mezclarlos`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)

        viewModel.setPlayerName(1, "Ana")
        viewModel.setPlayerName(2, "Luis")

        assertEquals(listOf("Ana", "Luis"), store.saved.playerNames)
    }

    @Test
    fun `el nombre se recorta y no admite caracteres de control`() {
        val viewModel = GameSettingsViewModel(FakeGameSettingsStore())

        viewModel.setPlayerName(1, "a" + 10.toChar() + "b" + "x".repeat(40))

        val name = viewModel.settings.value.playerNames[0]
        assertEquals(MAX_PLAYER_NAME_LENGTH, name.length)
        assertFalse(name.any { it.isISOControl() })
    }

    @Test
    fun `una posicion de jugador fuera de rango se ignora`() {
        val viewModel = GameSettingsViewModel(FakeGameSettingsStore())

        viewModel.setPlayerName(0, "Yo")
        viewModel.setPlayerName(3, "Otro")

        assertEquals(listOf("", ""), viewModel.settings.value.playerNames)
    }

    @Test
    fun `por defecto se juega con un dado y la cantidad de dados se guarda y se limita`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)
        assertEquals(1, viewModel.settings.value.diceCount)

        viewModel.setDiceCount(2)
        assertEquals(2, store.saved.diceCount)

        viewModel.setDiceCount(7)
        assertEquals(3, viewModel.settings.value.diceCount)
        viewModel.setDiceCount(0)
        assertEquals(1, viewModel.settings.value.diceCount)
    }

    @Test
    fun `por defecto el nivel es Normal y cambiar la meta a mano lo quita`() {
        val viewModel = GameSettingsViewModel(FakeGameSettingsStore())
        assertEquals(Difficulty.NORMAL, viewModel.settings.value.difficulty)

        viewModel.setTargetScore(40)

        assertEquals(null, viewModel.settings.value.difficulty)
    }

    @Test
    fun `elegir un nivel fija la meta y los lanzamientos segun el dado y lo guarda`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)

        viewModel.setDifficulty(Difficulty.HARD, sides = 6, diceCount = 1)

        assertEquals(Difficulty.HARD, store.saved.difficulty)
        assertEquals(31, store.saved.targetScore)
        assertEquals(7, store.saved.maxLaunches)
    }

    @Test
    fun `al cambiar de dado el nivel elegido reajusta la meta`() {
        val viewModel = GameSettingsViewModel(FakeGameSettingsStore())
        viewModel.setDifficulty(Difficulty.HARD, sides = 6, diceCount = 1)
        val withD6 = viewModel.settings.value.targetScore

        viewModel.refreshDifficulty(sides = 20, diceCount = 1)

        assert(viewModel.settings.value.targetScore > withD6)
        assertEquals(Difficulty.HARD, viewModel.settings.value.difficulty)
    }

    @Test
    fun `con meta personalizada cambiar de dado no la toca`() {
        val viewModel = GameSettingsViewModel(FakeGameSettingsStore())
        viewModel.setTargetScore(50)

        viewModel.refreshDifficulty(sides = 20, diceCount = 2)

        assertEquals(50, viewModel.settings.value.targetScore)
    }

    @Test
    fun `la cantidad de dados llega a tres`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)

        viewModel.setDiceCount(3)
        assertEquals(3, store.saved.diceCount)
        viewModel.setDiceCount(9)
        assertEquals(3, viewModel.settings.value.diceCount)
    }

    @Test
    fun `se pueden elegir caras distintas para el segundo y el tercer dado`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)

        viewModel.setExtraDiceSides(1, 20)
        viewModel.setExtraDiceSides(2, 8)
        assertEquals(listOf(20, 8), store.saved.extraDiceSides)

        viewModel.setExtraDiceSides(1, 0)
        assertEquals(listOf(0, 8), viewModel.settings.value.extraDiceSides)
    }

    @Test
    fun `no se aceptan caras que no existen ni posiciones fuera de rango`() {
        val viewModel = GameSettingsViewModel(FakeGameSettingsStore())

        viewModel.setExtraDiceSides(1, 7)
        viewModel.setExtraDiceSides(0, 20)
        viewModel.setExtraDiceSides(3, 20)

        assertEquals(listOf(0, 0), viewModel.settings.value.extraDiceSides)
    }

    @Test
    fun `cambiar de nivel usa los dados distintos`() {
        val viewModel = GameSettingsViewModel(FakeGameSettingsStore())
        viewModel.setDifficulty(Difficulty.NORMAL, listOf(20, 6))

        assertEquals(Difficulty.NORMAL.targetScore(listOf(20, 6)), viewModel.settings.value.targetScore)
    }

    @Test
    fun `por defecto el texto es normal y el tamano se guarda`() {
        val store = FakeGameSettingsStore()
        val viewModel = GameSettingsViewModel(store)
        assertEquals(TextSize.NORMAL, viewModel.settings.value.textSize)

        viewModel.setTextSize(TextSize.EXTRA_LARGE)

        assertEquals(TextSize.EXTRA_LARGE, store.saved.textSize)
    }

    @Test
    fun `cada tamano de texto es mayor que el anterior y el normal no cambia nada`() {
        assertEquals(1.0f, TextSize.NORMAL.factor, 0.0001f)
        assertTrue(TextSize.LARGE.factor > TextSize.NORMAL.factor)
        assertTrue(TextSize.EXTRA_LARGE.factor > TextSize.LARGE.factor)
    }
}
