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
 */
data class RollRecord(
    val id: Long,
    val values: List<Int>,
    val value: Int = values.sum()
) {
    constructor(id: Long, value: Int) : this(id = id, values = listOf(value), value = value)
}
