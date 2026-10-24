package com.danidev.appmovil2.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/*
 * HU-12: Guardar el nombre del jugador.
 *
 * "Como usuario, quiero ingresar mi nombre en un campo de texto y que se conserve
 * al cerrar la app, para ver un saludo personalizado como 'Turno de: [Nombre]'."
 */

/**
 * Acceso al nombre del jugador para cualquier pantalla.
 *
 * Se entrega mediante [LocalPlayerName] (un `CompositionLocal`) para que una
 * pantalla pueda mostrar el nombre o el campo para editarlo sin recibir
 * parámetros extra, igual que ocurre con el tema (`LocalThemeController`).
 *
 * @property name Nombre tal como está escrito en el campo.
 * @property onNameChange Se invoca con el texto nuevo cuando el usuario escribe.
 */
class PlayerNameController(
    val name: String,
    val onNameChange: (String) -> Unit
) {
    /** Nombre listo para mostrar en un saludo: sin espacios al inicio ni al final. */
    val displayName: String get() = name.trim()
}

/** Controlador por defecto (previews y tests de UI): sin nombre y sin efecto al escribir. */
val LocalPlayerName = compositionLocalOf { PlayerNameController(name = "", onNameChange = {}) }
