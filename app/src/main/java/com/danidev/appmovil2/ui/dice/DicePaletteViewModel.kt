package com.danidev.appmovil2.ui.dice

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel de la paleta de color del dado (HU-04).
 *
 * Responsabilidades:
 * - Mantener la paleta elegida ([DicePalette]).
 * - Persistirla en [DicePaletteStore] cada vez que el usuario la cambia.
 *
 * Es independiente de [DiceViewModel], así que no interfiere con el historial
 * ni con el resto de la lógica del juego.
 *
 * @param store Almacén donde se guarda la paleta entre sesiones.
 */
class DicePaletteViewModel(private val store: DicePaletteStore) : ViewModel() {

    private val _palette = MutableStateFlow(store.load())

    /** Paleta actual, de solo lectura para la UI. */
    val palette: StateFlow<DicePalette> = _palette.asStateFlow()

    /** Fija la paleta elegida y la guarda. */
    fun selectPalette(palette: DicePalette) {
        _palette.value = palette
        store.save(palette)
    }
}

/**
 * Obtiene el [DicePaletteViewModel] de la pantalla actual, creado con el almacén
 * de `SharedPreferences` de la app.
 */
@Composable
fun rememberDicePaletteViewModel(): DicePaletteViewModel {
    val appContext = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer { DicePaletteViewModel(SharedPreferencesDicePaletteStore(appContext)) }
        }
    )
}
