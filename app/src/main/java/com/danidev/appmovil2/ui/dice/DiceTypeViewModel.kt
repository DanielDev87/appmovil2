package com.danidev.appmovil2.ui.dice

import android.content.Context
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
 * Almacén del tipo de dado elegido (HU-07).
 *
 * Es una interfaz para que [DiceTypeViewModel] no dependa de Android y pueda probarse
 * con una implementación en memoria.
 */
interface DiceTypeStore {
    /** Devuelve el tipo guardado, o [DiceType.DEFAULT] si no hay ninguno. */
    fun load(): DiceType

    /** Guarda el tipo para que sobreviva al cierre de la app. */
    fun save(type: DiceType)
}

/**
 * Implementación basada en `SharedPreferences`, con su propio archivo
 * ("dice_type_prefs") para no mezclarse con otros datos de la app.
 */
class SharedPreferencesDiceTypeStore(context: Context) : DiceTypeStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): DiceType {
        val saved = preferences.getString(KEY_TYPE, null)
        // Si el valor guardado no se reconoce, se vuelve al dado tradicional.
        return DiceType.entries.firstOrNull { it.name == saved } ?: DiceType.DEFAULT
    }

    override fun save(type: DiceType) {
        preferences.edit().putString(KEY_TYPE, type.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "dice_type_prefs"
        const val KEY_TYPE = "type"
    }
}

/**
 * ViewModel del tipo de dado (HU-07).
 *
 * Mantiene el número de caras elegido y lo guarda en [DiceTypeStore]. Es independiente
 * de [DiceViewModel], así que no interfiere con el historial ni con el resto del juego.
 *
 * @param store Almacén donde se guarda el tipo entre sesiones.
 */
class DiceTypeViewModel(private val store: DiceTypeStore) : ViewModel() {

    private val _type = MutableStateFlow(store.load())

    /** Tipo de dado actual, de solo lectura para la UI. */
    val type: StateFlow<DiceType> = _type.asStateFlow()

    /** Fija el tipo de dado y lo guarda. */
    fun select(type: DiceType) {
        _type.value = type
        store.save(type)
    }
}

/**
 * Obtiene el [DiceTypeViewModel] de la pantalla actual, creado con el almacén de
 * `SharedPreferences` de la app.
 */
@Composable
fun rememberDiceTypeViewModel(): DiceTypeViewModel {
    val appContext = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer { DiceTypeViewModel(SharedPreferencesDiceTypeStore(appContext)) }
        }
    )
}
