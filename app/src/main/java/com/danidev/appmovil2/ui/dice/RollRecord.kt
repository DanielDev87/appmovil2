package com.danidev.appmovil2.ui.dice

/**
 * Representa un lanzamiento registrado en el historial (HU-011).
 *
 * Se necesita un [id] además del [value] porque los valores del dado se repiten
 * (p. ej. dos "4" seguidos). La lista `LazyRow` usa [id] como `key` para
 * identificar cada elemento de forma estable y animar correctamente la entrada
 * de los nuevos lanzamientos.
 *
 * @property id Identificador único y creciente del lanzamiento.
 * @property value Valor obtenido en el dado (1..6).
 */
data class RollRecord(
    val id: Long,
    val value: Int
)
