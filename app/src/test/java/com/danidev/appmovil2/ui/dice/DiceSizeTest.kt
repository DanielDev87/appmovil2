package com.danidev.appmovil2.ui.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias del tamaño del dado (HU-05).
 *
 * Verifican que el factor del slider se traduce al tamaño real del dado, que con
 * dos dados nunca se pasa del ancho disponible y que el valor se guarda.
 */
class DiceSizeTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeDiceSizeStore(var saved: Float = 1f) : DiceSizeStore {
        override fun load() = saved
        override fun save(factor: Float) {
            saved = factor
        }
    }

    @Test
    fun `con factor 1 el dado conserva su tamano original`() {
        assertEquals(200f, DiceSize.sizeDp(diceCount = 1, factor = 1f), 0.001f)
        assertEquals(130f, DiceSize.sizeDp(diceCount = 2, factor = 1f), 0.001f)
    }

    @Test
    fun `el factor agranda y achica el dado`() {
        assertEquals(120f, DiceSize.sizeDp(diceCount = 1, factor = 0.6f), 0.001f)
        assertEquals(280f, DiceSize.sizeDp(diceCount = 1, factor = 1.4f), 0.001f)
    }

    @Test
    fun `con dos dados el tamano no supera el limite que cabe en pantalla`() {
        val maxDouble = DiceSize.sizeDp(diceCount = 2, factor = DiceSize.MAX_FACTOR_SINGLE)

        assertEquals(130f * DiceSize.MAX_FACTOR_DOUBLE, maxDouble, 0.001f)
        // Dos dados mas el espacio entre ellos deben caber en el ancho de un telefono.
        assertTrue(maxDouble * 2 + 16f + 32f <= 358f)
    }

    @Test
    fun `un factor fuera de rango se limita al minimo y al maximo`() {
        assertEquals(DiceSize.MIN_FACTOR, DiceSize.effectiveFactor(0.1f, diceCount = 1), 0.001f)
        assertEquals(DiceSize.MAX_FACTOR_SINGLE, DiceSize.effectiveFactor(5f, diceCount = 1), 0.001f)
    }

    @Test
    fun `el margen interior es proporcional al tamano`() {
        assertEquals(30f, DiceSize.paddingDp(200f), 0.001f)
        assertEquals(15f, DiceSize.paddingDp(100f), 0.001f)
    }

    @Test
    fun `sin eleccion previa el tamano es el original`() {
        val viewModel = DiceSizeViewModel(FakeDiceSizeStore())

        assertEquals(1f, viewModel.factor.value, 0.001f)
    }

    @Test
    fun `mover el slider cambia el factor y lo guarda`() {
        val store = FakeDiceSizeStore()
        val viewModel = DiceSizeViewModel(store)

        viewModel.setFactor(1.25f)

        assertEquals(1.25f, viewModel.factor.value, 0.001f)
        assertEquals(1.25f, store.saved, 0.001f)
    }

    @Test
    fun `el factor guardado nunca se sale del rango del slider`() {
        val store = FakeDiceSizeStore()
        val viewModel = DiceSizeViewModel(store)

        viewModel.setFactor(10f)

        assertEquals(DiceSize.MAX_FACTOR_SINGLE, viewModel.factor.value, 0.001f)
        assertEquals(DiceSize.MAX_FACTOR_SINGLE, store.saved, 0.001f)
    }

    @Test
    fun `una nueva sesion recupera el tamano guardado`() {
        val store = FakeDiceSizeStore()
        DiceSizeViewModel(store).setFactor(0.8f)

        val nuevaSesion = DiceSizeViewModel(store)

        assertEquals(0.8f, nuevaSesion.factor.value, 0.001f)
    }
}
