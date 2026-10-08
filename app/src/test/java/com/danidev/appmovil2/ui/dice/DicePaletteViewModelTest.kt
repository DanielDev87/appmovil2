package com.danidev.appmovil2.ui.dice

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias de la selección de paleta de [DicePaletteViewModel] (HU-04).
 *
 * Verifican que la paleta cambia al elegirla, que se guarda y que una nueva
 * sesión recupera la última elección.
 */
class DicePaletteViewModelTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeDicePaletteStore(var saved: DicePalette = DicePalette.LILA) : DicePaletteStore {
        override fun load() = saved
        override fun save(palette: DicePalette) {
            saved = palette
        }
    }

    @Test
    fun `sin eleccion previa el dado usa la paleta lila`() {
        val viewModel = DicePaletteViewModel(FakeDicePaletteStore())

        assertEquals(DicePalette.LILA, viewModel.palette.value)
    }

    @Test
    fun `elegir una paleta la deja como paleta actual`() {
        val viewModel = DicePaletteViewModel(FakeDicePaletteStore())

        viewModel.selectPalette(DicePalette.MENTA)

        assertEquals(DicePalette.MENTA, viewModel.palette.value)
    }

    @Test
    fun `la paleta elegida se guarda en el almacen`() {
        val store = FakeDicePaletteStore()
        val viewModel = DicePaletteViewModel(store)

        viewModel.selectPalette(DicePalette.ROSA)

        assertEquals(DicePalette.ROSA, store.saved)
    }

    @Test
    fun `una nueva sesion recupera la paleta guardada`() {
        val store = FakeDicePaletteStore()
        DicePaletteViewModel(store).selectPalette(DicePalette.MENTA)

        val nuevaSesion = DicePaletteViewModel(store)

        assertEquals(DicePalette.MENTA, nuevaSesion.palette.value)
    }

    @Test
    fun `elegir varias veces deja la ultima paleta`() {
        val store = FakeDicePaletteStore()
        val viewModel = DicePaletteViewModel(store)

        viewModel.selectPalette(DicePalette.ROSA)
        viewModel.selectPalette(DicePalette.MENTA)
        viewModel.selectPalette(DicePalette.ROSA)

        assertEquals(DicePalette.ROSA, viewModel.palette.value)
        assertEquals(DicePalette.ROSA, store.saved)
    }

    @Test
    fun `hay tres paletas y lila es la primera`() {
        assertEquals(3, DicePalette.entries.size)
        assertEquals(DicePalette.LILA, DicePalette.entries.first())
    }
}
