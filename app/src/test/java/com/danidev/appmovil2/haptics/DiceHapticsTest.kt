package com.danidev.appmovil2.haptics

import com.danidev.appmovil2.RollIndicator
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
        val patterns = mutableListOf<List<Long>>()
        override fun pulse(durationMs: Long) {
            pulses += durationMs
        }

        override fun pattern(timingsMs: LongArray) {
            patterns += timingsMs.toList()
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

    @Test
    fun `un tiro normal vibra una vez corto`() {
        DiceHaptics(output, store).vibrateOnResult(RollIndicator.NONE)

        assertEquals(listOf(DiceHaptics.LAND_MS), output.pulses)
    }

    @Test
    fun `un critico vibra con dos pulsos rapidos y una pifia con uno largo`() {
        val haptics = DiceHaptics(output, store)

        haptics.vibrateOnResult(RollIndicator.CRITICAL)
        assertEquals(listOf(DiceHaptics.CRITICAL_PATTERN.toList()), output.patterns)

        haptics.vibrateOnResult(RollIndicator.FUMBLE)
        assertEquals(listOf(DiceHaptics.FUMBLE_MS), output.pulses)
        assertTrue(DiceHaptics.FUMBLE_MS > DiceHaptics.LAND_MS)
    }

    @Test
    fun `ganar vibra con un redoble y perder con una vibracion larga`() {
        val haptics = DiceHaptics(output, store)

        haptics.vibrateOnGameOver(won = true)
        assertEquals(listOf(DiceHaptics.WIN_PATTERN.toList()), output.patterns)

        haptics.vibrateOnGameOver(won = false)
        assertEquals(listOf(DiceHaptics.LOSE_MS), output.pulses)
    }

    @Test
    fun `con la vibracion apagada ningun resultado vibra`() {
        store.enabled = false
        val haptics = DiceHaptics(output, store)

        RollIndicator.entries.forEach { haptics.vibrateOnResult(it) }
        haptics.vibrateOnGameOver(won = true)
        haptics.vibrateOnGameOver(won = false)

        assertTrue(output.pulses.isEmpty())
        assertTrue(output.patterns.isEmpty())
    }

    @Test
    fun `una salida sin patrones vibra una vez durante el total de las vibraciones`() {
        val simple = object : HapticOutput {
            val pulses = mutableListOf<Long>()
            override fun pulse(durationMs: Long) {
                pulses += durationMs
            }
        }

        simple.pattern(longArrayOf(0, 60, 70, 60))

        assertEquals(listOf(120L), simple.pulses)
    }
}
