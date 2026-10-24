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
 * ViewModel del tamaño del dado (HU-05).
 *
 * Responsabilidades:
 * - Mantener el factor de tamaño que elige el usuario con el slider.
 * - Guardarlo en [DiceSizeStore] para conservarlo al cerrar la app.
 *
 * Es independiente de [DiceViewModel], así que no interfiere con el historial ni con
 * el resto de la lógica del juego.
 *
 * @param store Almacén donde se guarda el factor entre sesiones.
 */
class DiceSizeViewModel(private val store: DiceSizeStore) : ViewModel() {

    private val _factor = MutableStateFlow(DiceSize.coerce(store.load()))

    /** Factor actual (1.0 = tamaño original), de solo lectura para la UI. */
    val factor: StateFlow<Float> = _factor.asStateFlow()

    /** Fija el factor, limitado al rango del slider, y lo guarda. */
    fun setFactor(value: Float) {
        val clean = DiceSize.coerce(value)
        _factor.value = clean
        store.save(clean)
    }
}

/**
 * Obtiene el [DiceSizeViewModel] de la pantalla actual, creado con el almacén de
 * `SharedPreferences` de la app.
 */
@Composable
fun rememberDiceSizeViewModel(): DiceSizeViewModel {
    val appContext = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer { DiceSizeViewModel(SharedPreferencesDiceSizeStore(appContext)) }
        }
    )
}
