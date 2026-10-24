package com.danidev.appmovil2.ui.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del fondo personalizable: aplicación sobre los valores de diseño y persistencia. */
class BackgroundChoiceTest {

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeBackgroundStore(var saved: BackgroundChoice = BackgroundChoice()) : BackgroundStore {
        override fun load() = saved
        override fun save(choice: BackgroundChoice) {
            saved = choice
        }
    }

    private val base = appTokens(AppStyle.BALANCED, isDark = false)

    @Test
    fun `sin color propio los valores de diseño no cambian`() {
        assertSame(base, applyBackground(base, BackgroundChoice(colorArgb = null)))
    }

    @Test
    fun `un fondo claro mantiene el texto oscuro`() {
        val result = applyBackground(base, BackgroundChoice(BackgroundPreset.SKY.color.toArgbInt()))

        assertEquals(BackgroundPreset.SKY.color, result.bgTop)
        assertTrue(result.bgInk.red < 0.5f)
    }

    @Test
    fun `un fondo oscuro pasa el texto a claro`() {
        val dark = applyBackground(
            appTokens(AppStyle.BALANCED, isDark = false),
            BackgroundChoice(BackgroundPreset.NIGHT.color.toArgbInt())
        )

        assertTrue(dark.bgInk.red > 0.8f)
        assertTrue(dark.bgMuted.red > 0.7f)
    }

    @Test
    fun `las tarjetas toman un tinte del fondo en lugar de quedar blancas`() {
        val result = applyBackground(base, BackgroundChoice(BackgroundPreset.SKY.color.toArgbInt()))

        assertNotEquals(Color.White, result.surface)
        assertTrue(result.surface.blue > result.surface.red)
    }

    @Test
    fun `el texto de las tarjetas se ajusta al tinte de la tarjeta`() {
        val onLight = applyBackground(base, BackgroundChoice(BackgroundPreset.SKY.color.toArgbInt()))
        val onNight = applyBackground(base, BackgroundChoice(BackgroundPreset.NIGHT.color.toArgbInt()))

        assertTrue(onLight.ink.red < 0.5f)
        assertTrue(onNight.ink.red > 0.8f)
    }

    @Test
    fun `en el estilo equilibrado la tarjeta lleva un borde fino y en el jugueton conserva el suyo`() {
        val argb = BackgroundPreset.MINT.color.toArgbInt()
        val playful = appTokens(AppStyle.PLAYFUL, isDark = false)

        assertTrue(applyBackground(base, BackgroundChoice(argb)).borderWidth.value > 0f)
        assertEquals(playful.outline, applyBackground(playful, BackgroundChoice(argb)).outline)
        assertEquals(playful.borderWidth, applyBackground(playful, BackgroundChoice(argb)).borderWidth)
    }

    @Test
    fun `con degradado la parte de abajo es distinta y sin degradado es igual`() {
        val argb = BackgroundPreset.MINT.color.toArgbInt()

        val gradient = applyBackground(base, BackgroundChoice(argb, gradient = true))
        val flat = applyBackground(base, BackgroundChoice(argb, gradient = false))

        assertNotEquals(gradient.bgTop, gradient.bgBottom)
        assertEquals(flat.bgTop, flat.bgBottom)
    }

    @Test
    fun `los puntos del estilo jugueton siguen al color del fondo`() {
        val playful = appTokens(AppStyle.PLAYFUL, isDark = false)

        val result = applyBackground(playful, BackgroundChoice(BackgroundPreset.PEACH.color.toArgbInt()))

        assertNotEquals(playful.dots, result.dots)
        assertNull(applyBackground(base, BackgroundChoice(Color.Red.toArgbInt())).dots)
    }

    @Test
    fun `elegir un color lo guarda y conserva el degradado`() {
        val store = FakeBackgroundStore(BackgroundChoice(gradient = false))
        val viewModel = BackgroundViewModel(store)

        viewModel.selectColor(BackgroundPreset.LAVENDER.color)

        assertEquals(BackgroundPreset.LAVENDER.color.toArgbInt(), viewModel.choice.value.colorArgb)
        assertFalse(store.saved.gradient)
        assertTrue(store.saved.isCustom)
    }

    @Test
    fun `volver al estilo quita el color propio`() {
        val store = FakeBackgroundStore()
        val viewModel = BackgroundViewModel(store)
        viewModel.selectColor(Color.Blue)

        viewModel.useStyleDefault()

        assertNull(viewModel.choice.value.colorArgb)
        assertNull(store.saved.colorArgb)
    }

    @Test
    fun `una nueva sesion recupera el fondo guardado`() {
        val store = FakeBackgroundStore()
        BackgroundViewModel(store).apply {
            selectColor(BackgroundPreset.ROSE.color)
            setGradient(false)
        }

        val restored = BackgroundViewModel(store).choice.value

        assertEquals(BackgroundPreset.ROSE.color.toArgbInt(), restored.colorArgb)
        assertFalse(restored.gradient)
    }

    @Test
    fun `toArgbInt convierte un color a su entero ARGB`() {
        assertEquals(0xFF112233.toInt(), Color(0xFF112233).toArgbInt())
    }

    @Test
    fun `el tema oscuro oscurece el fondo elegido y pone el texto claro`() {
        val argb = BackgroundPreset.SKY.color.toArgbInt()

        val light = applyBackground(appTokens(AppStyle.BALANCED, isDark = false), BackgroundChoice(argb))
        val dark = applyBackground(appTokens(AppStyle.BALANCED, isDark = true), BackgroundChoice(argb))

        assertTrue(dark.bgTop.luminance() < light.bgTop.luminance())
        assertTrue(dark.bgTop.luminance() < 0.25f)
        assertTrue(dark.bgTop.luminance() > 0.02f)
        assertTrue(dark.bgInk.red > 0.8f)
        assertTrue(dark.ink.red > 0.8f)
        assertTrue(light.ink.red < 0.5f)
    }

    @Test
    fun `el color del fondo conserva su tono en tema oscuro`() {
        val dark = applyBackground(
            appTokens(AppStyle.BALANCED, isDark = true),
            BackgroundChoice(BackgroundPreset.SKY.color.toArgbInt())
        )

        assertTrue(dark.bgTop.blue > dark.bgTop.red)
    }

    @Test
    fun `el acento toma el tono del fondo elegido`() {
        val sky = applyBackground(base, BackgroundChoice(BackgroundPreset.SKY.color.toArgbInt()))
        val peach = applyBackground(base, BackgroundChoice(BackgroundPreset.PEACH.color.toArgbInt()))

        assertTrue(sky.accent.blue > sky.accent.red)
        assertTrue(peach.accent.red > peach.accent.blue)
        assertNotEquals(base.accent, sky.accent)
    }

    @Test
    fun `el texto del boton principal se lee sobre el acento en cada estilo y tema`() {
        val argb = BackgroundPreset.MINT.color.toArgbInt()
        for (style in AppStyle.entries) {
            for (dark in listOf(false, true)) {
                val result = applyBackground(appTokens(style, dark), BackgroundChoice(argb))
                val contrast = kotlin.math.abs(result.accent.luminance() - result.onAccent.luminance())
                assertTrue("$style dark=$dark", contrast > 0.35f)
            }
        }
    }

    @Test
    fun `sin fondo propio el acento es el del estilo`() {
        assertEquals(base.accent, applyBackground(base, BackgroundChoice()).accent)
    }

    @Test
    fun `el tono de un color se calcula en grados`() {
        assertEquals(0f, Color.Red.hue(), 0.5f)
        assertEquals(120f, Color.Green.hue(), 0.5f)
        assertEquals(240f, Color.Blue.hue(), 0.5f)
        assertEquals(0f, Color.Gray.hue(), 0.5f)
    }
}
