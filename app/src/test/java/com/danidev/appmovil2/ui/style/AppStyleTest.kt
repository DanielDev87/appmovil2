package com.danidev.appmovil2.ui.style

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del estilo visual: que se guarde, que una nueva sesión lo recupere y que
 * cada estilo tenga valores de diseño propios en tema claro y oscuro.
 */
class AppStyleTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeStyleStore(var saved: AppStyle = AppStyle.BALANCED) : StyleStore {
        override fun load() = saved
        override fun save(style: AppStyle) {
            saved = style
        }
    }

    @Test
    fun `sin preferencia guardada el estilo es equilibrado`() {
        assertEquals(AppStyle.BALANCED, StyleViewModel(FakeStyleStore()).style.value)
    }

    @Test
    fun `elegir un estilo lo aplica y lo guarda`() {
        val store = FakeStyleStore()
        val viewModel = StyleViewModel(store)

        viewModel.select(AppStyle.PLAYFUL)

        assertEquals(AppStyle.PLAYFUL, viewModel.style.value)
        assertEquals(AppStyle.PLAYFUL, store.saved)
    }

    @Test
    fun `una nueva sesion recupera el estilo guardado`() {
        val store = FakeStyleStore()
        StyleViewModel(store).select(AppStyle.PLAYFUL)

        assertEquals(AppStyle.PLAYFUL, StyleViewModel(store).style.value)
    }

    @Test
    fun `el estilo jugueton tiene borde grueso y sombra dura`() {
        val playful = appTokens(AppStyle.PLAYFUL, isDark = false)

        assertTrue(playful.isPlayful)
        assertTrue(playful.borderWidth.value > 0f)
        assertTrue(playful.shadowOffset.value > 0f)
        assertEquals(playful.bgTop, playful.bgBottom)
        assertTrue(playful.dots != null)
    }

    @Test
    fun `el estilo equilibrado no usa sombra dura ni puntos`() {
        val balanced = appTokens(AppStyle.BALANCED, isDark = false)

        assertFalse(balanced.isPlayful)
        assertEquals(0f, balanced.borderWidth.value)
        assertEquals(0f, balanced.shadowOffset.value)
        assertNull(balanced.dots)
    }

    @Test
    fun `el tema oscuro del estilo equilibrado marca las tarjetas con un borde fino`() {
        val dark = appTokens(AppStyle.BALANCED, isDark = true)

        assertTrue(dark.borderWidth.value > 0f)
    }

    @Test
    fun `cada combinacion de estilo y tema tiene su propio texto y fondo`() {
        val all = AppStyle.entries.flatMap { style ->
            listOf(false, true).map { dark -> appTokens(style, dark) }
        }

        assertEquals(4, all.map { it.bgTop to it.ink }.toSet().size)
        all.forEach { assertNotEquals(it.ink, it.bgTop) }
    }
}
