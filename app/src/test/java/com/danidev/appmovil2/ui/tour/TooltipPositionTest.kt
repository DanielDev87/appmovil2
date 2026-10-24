package com.danidev.appmovil2.ui.tour

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de dónde se coloca la explicación del tour respecto a lo que señala. */
class TooltipPositionTest {

    private val width = 1000f
    private val height = 2000f
    private val card = 600f to 300f

    private fun position(target: Rect?) = tooltipPosition(
        target = target,
        screenWidth = width,
        screenHeight = height,
        cardWidth = card.first,
        cardHeight = card.second,
        gap = 20f,
        margin = 16f
    )

    @Test
    fun `sin elemento va centrada`() {
        val p = position(null)

        assertEquals(200f, p.x, 0.01f)
        assertEquals(850f, p.y, 0.01f)
    }

    @Test
    fun `si cabe va debajo del elemento`() {
        val p = position(Rect(400f, 100f, 600f, 200f))

        assertEquals(220f, p.y, 0.01f)
    }

    @Test
    fun `si no cabe debajo va encima`() {
        val p = position(Rect(400f, 1700f, 600f, 1800f))

        assertEquals(1700f - 20f - 300f, p.y, 0.01f)
    }

    @Test
    fun `no se sale por los lados`() {
        val left = position(Rect(0f, 100f, 50f, 200f))
        val right = position(Rect(950f, 100f, 1000f, 200f))

        assertEquals(16f, left.x, 0.01f)
        assertEquals(1000f - 600f - 16f, right.x, 0.01f)
    }

    @Test
    fun `si el elemento es enorme queda dentro de la pantalla`() {
        val p = position(Rect(0f, 100f, 1000f, 1950f))

        assertTrue(p.y >= 16f)
        assertTrue(p.y + card.second <= height - 16f)
    }
}
