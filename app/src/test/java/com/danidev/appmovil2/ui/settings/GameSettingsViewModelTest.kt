package com.danidev.appmovil2.ui.settings

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
}
