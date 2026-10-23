package com.danidev.appmovil2.ui.player

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel del nombre del jugador (HU-12).
 *
 * Responsabilidades:
 * - Mantener el nombre que escribe el usuario.
 * - Guardarlo en [PlayerNameStore] cada vez que cambia, para conservarlo al cerrar la app.
 *
 * Es independiente de [com.danidev.appmovil2.ui.dice.DiceViewModel] y de cualquier
 * pantalla: se crea una sola vez en `MainActivity` y se entrega a las pantallas
 * mediante [LocalPlayerName].
 *
 * @param store Almacén donde se guarda el nombre entre sesiones.
 */
class PlayerNameViewModel(private val store: PlayerNameStore) : ViewModel() {

    private val _name = MutableStateFlow(sanitize(store.load()))

    /** Nombre tal como lo escribió el usuario, de solo lectura para la UI. */
    val name: StateFlow<String> = _name.asStateFlow()

    /**
     * Actualiza el nombre y lo guarda.
     *
     * Se conserva tal cual lo escribe el usuario (con espacios intermedios) y
     * solo se limita su longitud; los espacios sobrantes se recortan al mostrarlo
     * (ver [PlayerNameController.displayName]).
     */
    fun setName(raw: String) {
        val clean = sanitize(raw)
        _name.value = clean
        store.save(clean)
    }

    companion object {
        /** Cantidad máxima de caracteres que se aceptan en el nombre. */
        const val MAX_LENGTH = 20

        /** Quita caracteres de control y recorta el nombre a [MAX_LENGTH]. */
        fun sanitize(raw: String): String = raw.filterNot { it.isISOControl() }.take(MAX_LENGTH)
    }
}
