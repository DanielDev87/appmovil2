package com.danidev.appmovil2.ui.tour

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del tour guiado: cuándo se abre, cómo se recorre y que se recuerde que ya se vio. */
class TourViewModelTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeTourStore(var seen: Boolean = false) : TourStore {
        override fun loadSeen() = seen
        override fun saveSeen(seen: Boolean) {
            this.seen = seen
        }
    }

    @Test
    fun `la primera vez el tour se abre solo desde el principio`() {
        val viewModel = TourViewModel(FakeTourStore(seen = false))

        viewModel.showIfFirstTime()

        assertTrue(viewModel.visible.value)
        assertEquals(0, viewModel.stepIndex.value)
    }

    @Test
    fun `si ya se vio el tour no se vuelve a abrir solo`() {
        val viewModel = TourViewModel(FakeTourStore(seen = true))

        viewModel.showIfFirstTime()

        assertFalse(viewModel.visible.value)
    }

    @Test
    fun `se puede abrir a mano aunque ya se haya visto`() {
        val viewModel = TourViewModel(FakeTourStore(seen = true))

        viewModel.start()

        assertTrue(viewModel.visible.value)
        assertEquals(TourStep.INTRO, viewModel.currentStep)
    }

    @Test
    fun `desde el juego empieza en el primer paso del juego`() {
        val viewModel = TourViewModel(FakeTourStore())

        viewModel.start(TourScreen.GAME)

        assertEquals(TourStep.SCORES, viewModel.currentStep)
    }

    @Test
    fun `los pasos de bienvenida van antes que los del juego`() {
        val screens = TourStep.entries.map { it.screen }

        assertEquals(screens.sortedBy { it.ordinal }, screens)
        assertEquals(TourStep.PLAY, TourStep.entries.last { it.screen == TourScreen.WELCOME })
    }

    @Test
    fun `solo los pasos con elemento pueden ser interactivos`() {
        TourStep.entries.filter { it.interactive }.forEach { assertTrue(it.target != null) }
    }

    @Test
    fun `siguiente y atras recorren los pasos sin salirse`() {
        val viewModel = TourViewModel(FakeTourStore())
        viewModel.start()

        viewModel.back()
        assertEquals(0, viewModel.stepIndex.value)

        viewModel.next()
        viewModel.next()
        assertEquals(2, viewModel.stepIndex.value)

        viewModel.back()
        assertEquals(1, viewModel.stepIndex.value)
    }

    @Test
    fun `siguiente en el ultimo paso termina el tour y lo marca como visto`() {
        val store = FakeTourStore()
        val viewModel = TourViewModel(store)
        viewModel.start(TourScreen.GAME)

        repeat(viewModel.steps.size) { viewModel.next() }

        assertFalse(viewModel.visible.value)
        assertTrue(store.seen)
    }

    @Test
    fun `omitir cierra el tour y cuenta como visto`() {
        val store = FakeTourStore()
        val viewModel = TourViewModel(store)
        viewModel.start()

        viewModel.finish()

        assertFalse(viewModel.visible.value)
        assertTrue(store.seen)
    }

    @Test
    fun `tocar el elemento de un paso interactivo hace avanzar`() {
        val viewModel = TourViewModel(FakeTourStore())
        viewModel.start()
        viewModel.next() // THEME, interactivo

        assertTrue(viewModel.shouldAdvanceOnTouch(TourTargetId.THEME_SWITCH))
        assertFalse(viewModel.shouldAdvanceOnTouch(TourTargetId.PLAY_BUTTON))
    }

    @Test
    fun `tocar un elemento de un paso que solo señala no avanza`() {
        val viewModel = TourViewModel(FakeTourStore())
        viewModel.start()
        repeat(2) { viewModel.next() } // NAME, no interactivo

        assertFalse(viewModel.shouldAdvanceOnTouch(TourTargetId.NAME_FIELD))
    }

    @Test
    fun `con el tour cerrado tocar nada avanza`() {
        val viewModel = TourViewModel(FakeTourStore())

        assertFalse(viewModel.shouldAdvanceOnTouch(TourTargetId.THEME_SWITCH))
    }

    @Test
    fun `un toque tardio no se salta un paso que ya cambio`() {
        val viewModel = TourViewModel(FakeTourStore())
        viewModel.start()
        viewModel.next() // paso 1
        viewModel.next() // paso 2

        viewModel.advanceFrom(1)

        assertEquals(2, viewModel.stepIndex.value)
    }

    @Test
    fun `advanceFrom avanza si sigue en ese paso`() {
        val viewModel = TourViewModel(FakeTourStore())
        viewModel.start()

        viewModel.advanceFrom(0)

        assertEquals(1, viewModel.stepIndex.value)
    }
}
