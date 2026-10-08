package com.danidev.appmovil2.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias del selector de tema de [ThemeViewModel] (HU-03).
 *
 * Verifican que el tema cambia al instante, que la elección se guarda y que
 * una nueva sesión recupera lo que el usuario eligió.
 */
class ThemeViewModelTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeThemeStore(var saved: ThemeMode = ThemeMode.SYSTEM) : ThemeStore {
        override fun load() = saved
        override fun save(mode: ThemeMode) {
            saved = mode
        }
    }

    @Test
    fun `sin eleccion previa el tema sigue al sistema`() {
        val viewModel = ThemeViewModel(FakeThemeStore())

        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun `activar el switch cambia a tema oscuro`() {
        val viewModel = ThemeViewModel(FakeThemeStore())

        viewModel.setDarkTheme(true)

        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)
    }

    @Test
    fun `desactivar el switch cambia a tema claro`() {
        val viewModel = ThemeViewModel(FakeThemeStore(saved = ThemeMode.DARK))

        viewModel.setDarkTheme(false)

        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.value)
    }

    @Test
    fun `la eleccion se guarda en el almacen`() {
        val store = FakeThemeStore()
        val viewModel = ThemeViewModel(store)

        viewModel.setDarkTheme(true)

        assertEquals(ThemeMode.DARK, store.saved)
    }

    @Test
    fun `una nueva sesion recupera el tema guardado`() {
        val store = FakeThemeStore()
        ThemeViewModel(store).setDarkTheme(true)

        val nuevaSesion = ThemeViewModel(store)

        assertEquals(ThemeMode.DARK, nuevaSesion.themeMode.value)
    }

    @Test
    fun `alternar varias veces deja el ultimo valor elegido`() {
        val store = FakeThemeStore()
        val viewModel = ThemeViewModel(store)

        viewModel.setDarkTheme(true)
        viewModel.setDarkTheme(false)
        viewModel.setDarkTheme(true)

        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)
        assertEquals(ThemeMode.DARK, store.saved)
    }
}
