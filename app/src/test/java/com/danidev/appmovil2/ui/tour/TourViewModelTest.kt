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
    fun `la primera vez el tour se abre solo`() {
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
    fun `se puede abrir otra vez a mano aunque ya se haya visto`() {
        val viewModel = TourViewModel(FakeTourStore(seen = true))

        viewModel.start()

        assertTrue(viewModel.visible.value)
        assertEquals(0, viewModel.stepIndex.value)
    }

    @Test
    fun `siguiente y atras recorren los pasos sin salirse del rango`() {
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
    fun `en el ultimo paso siguiente termina el tour y lo marca como visto`() {
        val store = FakeTourStore()
        val viewModel = TourViewModel(store)
        viewModel.start()

        repeat(viewModel.steps.size - 1) { viewModel.next() }
        assertTrue(viewModel.isLastStep)
        assertTrue(viewModel.visible.value)

        viewModel.next()

        assertFalse(viewModel.visible.value)
        assertTrue(store.seen)
    }

    @Test
    fun `omitir cierra el tour y tambien cuenta como visto`() {
        val store = FakeTourStore()
        val viewModel = TourViewModel(store)
        viewModel.start()
        viewModel.next()

        viewModel.finish()

        assertFalse(viewModel.visible.value)
        assertTrue(store.seen)
    }

    @Test
    fun `el tour no se reinicia si ya esta abierto al pedirlo la primera vez`() {
        val viewModel = TourViewModel(FakeTourStore())
        viewModel.start()
        viewModel.next()

        viewModel.showIfFirstTime()

        assertEquals(1, viewModel.stepIndex.value)
    }

    @Test
    fun `el tour tiene todos los pasos esperados en orden`() {
        val steps = TourViewModel(FakeTourStore()).steps

        assertEquals(TourStep.WELCOME, steps.first())
        assertEquals(TourStep.PROGRESS, steps.last())
        assertEquals(6, steps.size)
    }
}
