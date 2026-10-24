package com.danidev.appmovil2.ui.dice

/**
 * Representa un lanzamiento registrado en el historial (HU-011 y HU-06).
 *
 * Se necesita un [id] además de los valores porque los resultados se repiten.
 * La lista `LazyRow` usa [id] como `key` para identificar cada elemento de forma estable
 * y animar correctamente la entrada de nuevos lanzamientos.
 *
 * @property id Identificador único y creciente del lanzamiento.
 * @property values Lista de valores obtenidos en los dados de este lanzamiento (HU-06).
 * @property value Suma total de los valores obtenidos en el lanzamiento.
 * @property sides Número de caras de los dados con los que se hizo el lanzamiento (HU-07);
 * el historial lo necesita para dibujar cada resultado según el tipo de dado.
 * @property diceSides Caras de cada dado de [values], en el mismo orden; permite dados distintos en un
 * mismo tiro. Si está vacía, todos tienen [sides].
 */
data class RollRecord(
    val id: Long,
    val values: List<Int>,
    val value: Int = values.sum(),
    val sides: Int = 6,
    val diceSides: List<Int> = emptyList()
) {
    /** Caras del dado en la posición [index] de [values]. */
    fun sidesAt(index: Int): Int = diceSides.getOrElse(index) { sides }

    /** Puntos que sumó el tiro: la suma de sus dados. */
    val points: Int get() = values.sum()


    constructor(id: Long, value: Int) : this(id = id, values = listOf(value), value = value)
}
