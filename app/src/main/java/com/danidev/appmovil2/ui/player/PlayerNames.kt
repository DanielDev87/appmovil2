package com.danidev.appmovil2.ui.player

/**
 * Nombres con los que se muestra a cada jugador de la partida.
 *
 * El primero usa [firstName] (el que se escribe en la bienvenida) y los demás, los de [extras];
 * quien no tenga nombre aparece como lo que devuelva [fallback] para su número (1, 2, 3...).
 */
fun resolvePlayerNames(
    count: Int,
    firstName: String,
    extras: List<String>,
    fallback: (Int) -> String
): List<String> = List(count) { index ->
    val chosen = if (index == 0) firstName else extras.getOrElse(index - 1) { "" }
    chosen.trim().ifEmpty { fallback(index + 1) }
}
