package com.danidev.appmovil2.ui.settings

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
import kotlinx.coroutines.flow.update

/**
 * Ajustes de la partida que se guardan entre sesiones.
 *
 * @property soundMuted `true` si el sonido de los dados está silenciado (HU-15).
 * @property shakeEnabled `true` si sacudir el teléfono lanza los dados (HU-19).
 */
data class GameSettings(
    val soundMuted: Boolean = false,
    val shakeEnabled: Boolean = true
)

/**
 * Almacén de [GameSettings].
 *
 * Es una interfaz para que [GameSettingsViewModel] no dependa de Android y pueda
 * probarse con una implementación en memoria.
 */
interface GameSettingsStore {
    fun load(): GameSettings
    fun save(settings: GameSettings)
}

/** Implementación de [GameSettingsStore] basada en `SharedPreferences`. */
class SharedPreferencesGameSettingsStore(context: Context) : GameSettingsStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load() = GameSettings(
        soundMuted = preferences.getBoolean(KEY_SOUND_MUTED, false),
        shakeEnabled = preferences.getBoolean(KEY_SHAKE_ENABLED, true)
    )

    override fun save(settings: GameSettings) {
        preferences.edit()
            .putBoolean(KEY_SOUND_MUTED, settings.soundMuted)
            .putBoolean(KEY_SHAKE_ENABLED, settings.shakeEnabled)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "game_settings_prefs"
        const val KEY_SOUND_MUTED = "sound_muted"
        const val KEY_SHAKE_ENABLED = "shake_enabled"
    }
}

/** ViewModel de los ajustes de la partida: los mantiene y los guarda al cambiar. */
class GameSettingsViewModel(private val store: GameSettingsStore) : ViewModel() {

    private val _settings = MutableStateFlow(store.load())

    /** Ajustes actuales, de solo lectura para la UI. */
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    fun setSoundMuted(muted: Boolean) = change { it.copy(soundMuted = muted) }

    fun setShakeEnabled(enabled: Boolean) = change { it.copy(shakeEnabled = enabled) }

    private fun change(transform: (GameSettings) -> GameSettings) {
        _settings.update(transform)
        store.save(_settings.value)
    }
}

/** Obtiene el [GameSettingsViewModel] de la pantalla actual, con el almacén de la app. */
@Composable
fun rememberGameSettingsViewModel(): GameSettingsViewModel {
    val appContext = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer { GameSettingsViewModel(SharedPreferencesGameSettingsStore(appContext)) }
        }
    )
}
