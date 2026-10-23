package com.danidev.appmovil2.ui.player

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas de los nombres con los que se muestra a cada jugador. */
class PlayerNamesTest {

    private val fallback = { number: Int -> "Jugador $number" }

    @Test
    fun `el primer jugador usa el nombre de la bienvenida y los demas los suyos`() {
        val names = resolvePlayerNames(3, "Mariana", listOf("Ana", "Luis", ""), fallback)

        assertEquals(listOf("Mariana", "Ana", "Luis"), names)
    }

    @Test
    fun `quien no tiene nombre aparece como Jugador y su numero`() {
        val names = resolvePlayerNames(3, "", listOf("", "Luis", ""), fallback)

        assertEquals(listOf("Jugador 1", "Jugador 2", "Luis"), names)
    }

    @Test
    fun `un nombre solo con espacios cuenta como vacio y se recortan los extremos`() {
        val names = resolvePlayerNames(2, "  Mariana  ", listOf("   "), fallback)

        assertEquals(listOf("Mariana", "Jugador 2"), names)
    }

    @Test
    fun `con un solo jugador solo hay un nombre`() {
        assertEquals(listOf("Jugador 1"), resolvePlayerNames(1, "", listOf("Ana", ""), fallback))
    }
}
