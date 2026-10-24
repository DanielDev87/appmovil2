package com.danidev.appmovil2.ui.dice

/**
 * Caras de cada dado en juego: el primero usa [firstSides] y los demás, las que se hayan elegido en
 * [extraSides] (la posición 0 es el dado 2); un 0 significa "igual que el primero".
 */
fun effectiveDiceSides(count: Int, firstSides: Int, extraSides: List<Int>): List<Int> =
    List(count) { index ->
        if (index == 0) firstSides else extraSides.getOrElse(index - 1) { 0 }.takeIf { it > 0 } ?: firstSides
    }

/**
 * Nombre corto de los dados en juego: "D6" si todos son iguales, o los distintos juntos, por ejemplo
 * "D20 + D6".
 */
fun diceLabel(sides: List<Int>): String =
    if (sides.distinct().size <= 1) "D${sides.firstOrNull() ?: 6}" else sides.joinToString(" + ") { "D$it" }

/** Cara más alta entre los dados en juego; las estadísticas muestran las caras hasta ese número. */
fun highestFace(sides: List<Int>): Int = sides.maxOrNull() ?: 6
