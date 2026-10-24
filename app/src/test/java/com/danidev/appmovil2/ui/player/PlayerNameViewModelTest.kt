package com.danidev.appmovil2.ui.player

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias del nombre del jugador (HU-12).
 *
 * Verifican que el nombre se guarda al escribirlo, que una nueva sesión lo
 * recupera y que se limita su longitud sin perder los espacios intermedios.
 */
class PlayerNameViewModelTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakePlayerNameStore(var saved: String = "") : PlayerNameStore {
        override fun load() = saved
        override fun save(name: String) {
            saved = name
        }
    }

    @Test
    fun `sin nombre guardado el campo empieza vacio`() {
        val viewModel = PlayerNameViewModel(FakePlayerNameStore())

        assertEquals("", viewModel.name.value)
    }

    @Test
    fun `escribir un nombre lo deja como nombre actual`() {
        val viewModel = PlayerNameViewModel(FakePlayerNameStore())

        viewModel.setName("Mariana")

        assertEquals("Mariana", viewModel.name.value)
    }

    @Test
    fun `el nombre se guarda en el almacen`() {
        val store = FakePlayerNameStore()
        val viewModel = PlayerNameViewModel(store)

        viewModel.setName("Mariana")

        assertEquals("Mariana", store.saved)
    }

    @Test
    fun `una nueva sesion recupera el nombre guardado`() {
        val store = FakePlayerNameStore()
        PlayerNameViewModel(store).setName("Mariana")

        val nuevaSesion = PlayerNameViewModel(store)

        assertEquals("Mariana", nuevaSesion.name.value)
    }

    @Test
    fun `se conservan los espacios intermedios mientras se escribe`() {
        val viewModel = PlayerNameViewModel(FakePlayerNameStore())

        viewModel.setName("Ana ")
        assertEquals("Ana ", viewModel.name.value)

        viewModel.setName("Ana Maria")
        assertEquals("Ana Maria", viewModel.name.value)
    }

    @Test
    fun `el nombre no supera la longitud maxima`() {
        val viewModel = PlayerNameViewModel(FakePlayerNameStore())

        viewModel.setName("a".repeat(PlayerNameViewModel.MAX_LENGTH + 10))

        assertEquals(PlayerNameViewModel.MAX_LENGTH, viewModel.name.value.length)
    }

    @Test
    fun `se descartan los caracteres de control`() {
        val viewModel = PlayerNameViewModel(FakePlayerNameStore())

        viewModel.setName("Ma\nria\t")

        assertEquals("Maria", viewModel.name.value)
    }

    @Test
    fun `borrar el nombre deja el campo vacio y lo guarda vacio`() {
        val store = FakePlayerNameStore(saved = "Mariana")
        val viewModel = PlayerNameViewModel(store)

        viewModel.setName("")

        assertEquals("", viewModel.name.value)
        assertEquals("", store.saved)
    }

    @Test
    fun `el nombre para mostrar recorta los espacios de los extremos`() {
        val controller = PlayerNameController(name = "  Mariana  ", onNameChange = {})

        assertEquals("Mariana", controller.displayName)
    }

    @Test
    fun `un nombre solo con espacios no se muestra`() {
        val controller = PlayerNameController(name = "   ", onNameChange = {})

        assertEquals("", controller.displayName)
    }
}
