package com.danidev.appmovil2.haptics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias del feedback háptico (HU-16).
 *
 * Verifican que se vibra al detenerse el dado, que el tiro crítico vibra más tiempo,
 * que no se vibra con la vibración desactivada y que la elección se guarda.
 */
class DiceHapticsTest {

    /** Salida falsa que solo anota las vibraciones pedidas. */
    private class FakeOutput : HapticOutput {
        val pulses = mutableListOf<Long>()
        override fun pulse(durationMs: Long) {
            pulses += durationMs
        }
    }

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeStore(var enabled: Boolean = true) : HapticsStore {
        override fun loadEnabled() = enabled
        override fun saveEnabled(enabled: Boolean) {
            this.enabled = enabled
        }
    }

    private lateinit var output: FakeOutput
    private lateinit var store: FakeStore

    @Before
    fun setUp() {
        output = FakeOutput()
        store = FakeStore()
    }

    @Test
    fun `la vibracion empieza activada`() {
        assertTrue(DiceHaptics(output, store).isEnabled)
    }

    @Test
    fun `al detenerse el dado vibra una vez con la duracion normal`() {
        DiceHaptics(output, store).vibrateOnLand()

        assertEquals(listOf(DiceHaptics.LAND_MS), output.pulses)
    }

    @Test
    fun `un tiro critico o pifia vibra mas tiempo`() {
        DiceHaptics(output, store).vibrateOnLand(strong = true)

        assertEquals(listOf(DiceHaptics.STRONG_MS), output.pulses)
        assertTrue(DiceHaptics.STRONG_MS > DiceHaptics.LAND_MS)
    }

    @Test
    fun `con la vibracion desactivada no vibra`() {
        val haptics = DiceHaptics(output, store)

        haptics.setVibrationEnabled(false)
        haptics.vibrateOnLand()
        haptics.vibrateOnLand(strong = true)

        assertTrue(output.pulses.isEmpty())
    }

    @Test
    fun `al volver a activarla vuelve a vibrar`() {
        val haptics = DiceHaptics(output, store)
        haptics.setVibrationEnabled(false)

        haptics.setVibrationEnabled(true)
        haptics.vibrateOnLand()

        assertEquals(1, output.pulses.size)
    }

    @Test
    fun `la eleccion se guarda`() {
        DiceHaptics(output, store).setVibrationEnabled(false)

        assertFalse(store.enabled)
    }

    @Test
    fun `una nueva sesion recupera la vibracion desactivada`() {
        DiceHaptics(output, store).setVibrationEnabled(false)

        val nuevaSesion = DiceHaptics(output, store)

        assertFalse(nuevaSesion.isEnabled)
    }

    @Test
    fun `sin vibrador en el dispositivo no falla`() {
        // Un dispositivo sin motor de vibración entrega null; no debe lanzar errores.
        VibratorOutput(null).pulse(DiceHaptics.LAND_MS)
    }
}
