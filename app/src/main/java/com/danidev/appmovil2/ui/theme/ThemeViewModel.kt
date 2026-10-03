package com.danidev.appmovil2.ui.theme

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel del tema claro / oscuro (HU-03).
 *
 * Responsabilidades:
 * - Mantener la preferencia de tema actual ([ThemeMode]).
 * - Persistirla en [ThemeStore] cada vez que el usuario la cambia.
 *
 * Es independiente de [com.danidev.appmovil2.ui.dice.DiceViewModel] y de cualquier
 * pantalla: se crea una sola vez en `MainActivity` y su estado se aplica a
 * `Appmovil2Theme`, por lo que afecta a toda la jerarquía de Composables.
 *
 * @param store Almacén donde se guarda la preferencia entre sesiones.
 */
class ThemeViewModel(private val store: ThemeStore) : ViewModel() {

    private val _themeMode = MutableStateFlow(store.load())

    /** Preferencia actual, de solo lectura para la UI. */
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    /**
     * Fija el tema de forma manual y lo guarda.
     *
     * @param dark `true` para el tema oscuro, `false` para el claro.
     */
    fun setDarkTheme(dark: Boolean) {
        val mode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT
        _themeMode.value = mode
        store.save(mode)
    }
}
