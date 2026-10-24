package com.danidev.appmovil2.ui.dice

import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.determineRollIndicator
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias del tipo de dado (HU-07).
 *
 * Verifican los rangos de cada dado, que se guarda la elección, que el historial
 * recuerda con qué dado se hizo cada tiro y que crítico y pifia se adaptan al dado.
 */
class DiceTypeTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeDiceTypeStore(var saved: DiceType = DiceType.DEFAULT) : DiceTypeStore {
        override fun load() = saved
        override fun save(type: DiceType) {
            saved = type
        }
    }

    @Test
    fun `los tipos disponibles son D4 D6 D8 D12 y D20`() {
        assertEquals(listOf(4, 6, 8, 12, 20), DiceType.entries.map { it.sides })
        assertEquals(listOf("D4", "D6", "D8", "D12", "D20"), DiceType.entries.map { it.label })
    }

    @Test
    fun `el dado por defecto es el tradicional de 6 caras`() {
        assertEquals(DiceType.D6, DiceType.DEFAULT)
        assertTrue(DiceType.D6.isClassic)
        assertFalse(DiceType.D20.isClassic)
    }

    @Test
    fun `cada dado da siempre un valor entre 1 y su numero de caras`() {
        val random = Random(42)
        DiceType.entries.forEach { type ->
            repeat(500) {
                val value = type.roll(random)
                assertTrue("$type dio $value", value in 1..type.sides)
            }
        }
    }

    @Test
    fun `con muchas tiradas un dado alcanza todas sus caras`() {
        val random = Random(7)
        DiceType.entries.forEach { type ->
            val seen = (1..2000).map { type.roll(random) }.toSet()
            assertEquals("$type", (1..type.sides).toSet(), seen)
        }
    }

    @Test
    fun `fromSides encuentra el tipo o vuelve al tradicional`() {
        assertEquals(DiceType.D12, DiceType.fromSides(12))
        assertEquals(DiceType.D6, DiceType.fromSides(7))
    }

    @Test
    fun `sin eleccion previa el tipo es D6`() {
        assertEquals(DiceType.D6, DiceTypeViewModel(FakeDiceTypeStore()).type.value)
    }

    @Test
    fun `elegir un tipo lo deja como actual y lo guarda`() {
        val store = FakeDiceTypeStore()
        val viewModel = DiceTypeViewModel(store)

        viewModel.select(DiceType.D20)

        assertEquals(DiceType.D20, viewModel.type.value)
        assertEquals(DiceType.D20, store.saved)
    }

    @Test
    fun `una nueva sesion recupera el tipo guardado`() {
        val store = FakeDiceTypeStore()
        DiceTypeViewModel(store).select(DiceType.D8)

        assertEquals(DiceType.D8, DiceTypeViewModel(store).type.value)
    }

    @Test
    fun `el historial recuerda con que dado se hizo cada tiro`() {
        val viewModel = DiceViewModel()

        viewModel.registerResult(values = listOf(17), activeValues = listOf(17), sides = 20)

        assertEquals(20, viewModel.uiState.value.history.single().sides)
        assertEquals(17, viewModel.uiState.value.history.single().value)
    }

    @Test
    fun `sin indicar las caras el historial usa el dado de 6`() {
        val viewModel = DiceViewModel()

        viewModel.registerResult(4)

        assertEquals(6, viewModel.uiState.value.history.single().sides)
    }

    @Test
    fun `rollDice respeta el numero de caras`() {
        val viewModel = DiceViewModel()

        repeat(100) { viewModel.rollDice(sides = 20) }

        assertTrue(viewModel.uiState.value.history.all { it.value in 1..20 })
        assertTrue(viewModel.uiState.value.history.all { it.sides == 20 })
    }

    @Test
    fun `la cara mas alta de cada dado es critico y el 1 es pifia`() {
        DiceType.entries.forEach { type ->
            assertEquals(RollIndicator.CRITICAL, determineRollIndicator(type.sides, type.sides))
            assertEquals(RollIndicator.FUMBLE, determineRollIndicator(1, type.sides))
        }
    }

    @Test
    fun `un resultado intermedio no muestra indicador`() {
        assertEquals(RollIndicator.NONE, determineRollIndicator(6, 20))
        assertEquals(RollIndicator.NONE, determineRollIndicator(10, 12))
    }

    @Test
    fun `con el dado de 6 el indicador sigue funcionando como antes`() {
        assertEquals(RollIndicator.CRITICAL, determineRollIndicator(6))
        assertEquals(RollIndicator.FUMBLE, determineRollIndicator(1))
        assertEquals(RollIndicator.NONE, determineRollIndicator(3))
    }
}
