package com.danidev.appmovil2.ui.dice

import kotlin.random.Random

/*
 * HU-07: Configuración del tipo de dado (D4, D6, D8, D12, D20).
 *
 * "Como usuario, quiero seleccionar el número de caras del dado, para poder usar la
 * app en juegos de rol."
 */

/**
 * Tipos de dado disponibles, de los juegos de rol.
 *
 * @property sides Número de caras; el dado da un valor entre 1 y [sides].
 */
enum class DiceType(val sides: Int) {
    D4(4),
    D6(6),
    D8(8),
    D12(12),
    D20(20);

    /** Nombre corto del dado, por ejemplo "D20". */
    val label: String get() = name

    /** `true` para el dado tradicional de 6 caras, que se dibuja con sus puntos. */
    val isClassic: Boolean get() = this == D6

    /** Lanza el dado: devuelve un valor entre 1 y [sides], ambos incluidos. */
    fun roll(random: Random = Random.Default): Int = random.nextInt(1, sides + 1)

    companion object {
        /** Tipo por defecto: el dado tradicional. */
        val DEFAULT = D6

        /** Devuelve el tipo con ese número de caras, o [DEFAULT] si no existe. */
        fun fromSides(sides: Int): DiceType = entries.firstOrNull { it.sides == sides } ?: DEFAULT
    }
}
