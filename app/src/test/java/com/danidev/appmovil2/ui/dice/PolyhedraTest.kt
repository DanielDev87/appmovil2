package com.danidev.appmovil2.ui.dice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias de la geometría de los dados D4, D8, D12 y D20 (HU-07).
 *
 * Verifican que cada dado es el sólido correcto, que lo que se ve tiene sentido
 * (caras del tamaño y forma esperados, dentro de la caja de dibujo) y que hay
 * una cara frontal clara donde escribir el número.
 */
class PolyhedraTest {

    @Test
    fun `cada dado es el solido con su numero de caras`() {
        assertEquals(4, Polyhedra.faceCount(4))
        assertEquals(8, Polyhedra.faceCount(8))
        assertEquals(12, Polyhedra.faceCount(12))
        assertEquals(20, Polyhedra.faceCount(20))
    }

    @Test
    fun `se ven varias caras pero nunca todas`() {
        Polyhedra.SUPPORTED_SIDES.forEach { sides ->
            val visibles = Polyhedra.project(sides).size
            assertTrue("D$sides: $visibles visibles", visibles in 2 until Polyhedra.faceCount(sides))
        }
    }

    @Test
    fun `el D4 se ve con tres caras y el D8 con cuatro`() {
        assertEquals(3, Polyhedra.project(4).size)
        assertEquals(4, Polyhedra.project(8).size)
    }

    @Test
    fun `el D12 tiene caras de cinco lados y los otros de tres`() {
        assertTrue(Polyhedra.project(12).all { it.points.size == 5 })
        listOf(4, 8, 20).forEach { sides ->
            assertTrue("D$sides", Polyhedra.project(sides).all { it.points.size == 3 })
        }
    }

    @Test
    fun `todos los puntos caben dentro de la caja de dibujo`() {
        Polyhedra.SUPPORTED_SIDES.forEach { sides ->
            Polyhedra.project(sides).forEach { face ->
                face.points.forEach { p ->
                    assertTrue("D$sides x=${p.x}", p.x in 0f..1f)
                    assertTrue("D$sides y=${p.y}", p.y in 0f..1f)
                }
            }
        }
    }

    @Test
    fun `el solido ocupa casi toda la caja`() {
        Polyhedra.SUPPORTED_SIDES.forEach { sides ->
            val points = Polyhedra.project(sides).flatMap { it.points }
            val ancho = points.maxOf { it.x } - points.minOf { it.x }
            val alto = points.maxOf { it.y } - points.minOf { it.y }
            assertTrue("D$sides ocupa ${maxOf(ancho, alto)}", maxOf(ancho, alto) > 0.85f)
        }
    }

    @Test
    fun `la luz de cada cara esta entre 0 y 1 y no todas son iguales`() {
        Polyhedra.SUPPORTED_SIDES.forEach { sides ->
            val brillos = Polyhedra.project(sides).map { it.brightness }
            assertTrue(brillos.all { it in 0f..1f })
            assertTrue("D$sides sin relieve", brillos.max() - brillos.min() > 0.2f)
        }
    }

    @Test
    fun `el resultado va en una sola cara con espacio para escribirlo`() {
        Polyhedra.SUPPORTED_SIDES.forEach { sides ->
            val faces = Polyhedra.project(sides)
            assertEquals("D$sides", 1, faces.count { it.isMain })
            val numerada = Polyhedra.numberedFace(faces)
            assertTrue("D$sides inradius=${numerada.inradius}", numerada.inradius > 0.05f)
        }
    }

    @Test
    fun `fuera del cubo el resultado va en la cara mas grande`() {
        listOf(4, 8, 12, 20).forEach { sides ->
            val faces = Polyhedra.project(sides)
            assertEquals("D$sides", faces.maxOf { it.area }, Polyhedra.numberedFace(faces).area, 0f)
        }
    }

    @Test
    fun `el cubo se ve con tres caras cuadradas y el resultado arriba`() {
        val faces = Polyhedra.project(6)

        assertEquals(6, Polyhedra.faceCount(6))
        assertEquals(3, faces.size)
        assertTrue(faces.all { it.points.size == 4 })
        // La cara de arriba (la principal) es la más alta de las tres.
        assertEquals(faces.minOf { it.center.y }, Polyhedra.numberedFace(faces).center.y, 0f)
    }

    @Test
    fun `las tres caras del cubo son iguales de grandes`() {
        val areas = Polyhedra.project(6).map { it.area }

        assertTrue(areas.max() - areas.min() < 0.01f)
    }

    @Test
    fun `cada cara visible lleva un numero y la principal lleva el resultado`() {
        Polyhedra.SUPPORTED_SIDES.forEach { sides ->
            val faces = Polyhedra.project(sides)
            val value = sides / 2
            val numeros = Polyhedra.faceValues(sides, value, faces)

            assertEquals(faces.size, numeros.size)
            assertEquals("D$sides", value, numeros[faces.indexOfFirst { it.isMain }])
            assertTrue("D$sides: $numeros", numeros.all { it in 1..sides })
            assertEquals("D$sides repetidos: $numeros", numeros.size, numeros.toSet().size)
        }
    }

    @Test
    fun `en el cubo las caras opuestas suman 7 para cualquier resultado`() {
        val faces = Polyhedra.project(6)
        for (value in 1..6) {
            val numeros = Polyhedra.faceValues(6, value, faces)

            // Ninguna de las tres caras visibles puede ser opuesta a otra: no suman 7.
            for (i in numeros.indices) for (j in i + 1 until numeros.size) {
                assertTrue("resultado $value: $numeros", numeros[i] + numeros[j] != 7)
            }
        }
    }

    @Test
    fun `los numeros de las caras no cambian el resultado ni la forma`() {
        val a = Polyhedra.faceValues(20, 1, Polyhedra.project(20))
        val b = Polyhedra.faceValues(20, 20, Polyhedra.project(20))

        assertEquals(1, a[0])
        assertEquals(20, b[0])
        assertEquals(Polyhedra.project(20).size, a.size)
    }

    @Test
    fun `un numero de caras sin solido propio usa el D20`() {
        assertEquals(Polyhedra.project(20), Polyhedra.project(7))
    }
}
