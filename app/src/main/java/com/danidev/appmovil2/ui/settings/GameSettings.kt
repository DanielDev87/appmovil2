package com.danidev.appmovil2.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import com.danidev.appmovil2.MAX_LAUNCHES
import com.danidev.appmovil2.MAX_MAX_LAUNCHES
import com.danidev.appmovil2.MAX_TARGET_SCORE
import com.danidev.appmovil2.MIN_MAX_LAUNCHES
import com.danidev.appmovil2.MIN_TARGET_SCORE
import com.danidev.appmovil2.TARGET_SCORE
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
 * @property targetScore Puntos que hay que alcanzar para ganar.
 * @property maxLaunches Lanzamientos disponibles en cada partida.
 */
data class GameSettings(
    val soundMuted: Boolean = false,
    val shakeEnabled: Boolean = true,
    val targetScore: Int = TARGET_SCORE,
    val maxLaunches: Int = MAX_LAUNCHES
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
        shakeEnabled = preferences.getBoolean(KEY_SHAKE_ENABLED, true),
        targetScore = preferences.getInt(KEY_TARGET_SCORE, TARGET_SCORE)
            .coerceIn(MIN_TARGET_SCORE, MAX_TARGET_SCORE),
        maxLaunches = preferences.getInt(KEY_MAX_LAUNCHES, MAX_LAUNCHES)
            .coerceIn(MIN_MAX_LAUNCHES, MAX_MAX_LAUNCHES)
    )

    override fun save(settings: GameSettings) {
        preferences.edit()
            .putBoolean(KEY_SOUND_MUTED, settings.soundMuted)
            .putBoolean(KEY_SHAKE_ENABLED, settings.shakeEnabled)
            .putInt(KEY_TARGET_SCORE, settings.targetScore)
            .putInt(KEY_MAX_LAUNCHES, settings.maxLaunches)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "game_settings_prefs"
        const val KEY_SOUND_MUTED = "sound_muted"
        const val KEY_SHAKE_ENABLED = "shake_enabled"
        const val KEY_TARGET_SCORE = "target_score"
        const val KEY_MAX_LAUNCHES = "max_launches"
    }
}

/** ViewModel de los ajustes de la partida: los mantiene y los guarda al cambiar. */
class GameSettingsViewModel(private val store: GameSettingsStore) : ViewModel() {

    private val _settings = MutableStateFlow(store.load())

    /** Ajustes actuales, de solo lectura para la UI. */
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    fun setSoundMuted(muted: Boolean) = change { it.copy(soundMuted = muted) }

    fun setShakeEnabled(enabled: Boolean) = change { it.copy(shakeEnabled = enabled) }

    /** Fija los puntos para ganar, dentro de los límites permitidos. */
    fun setTargetScore(value: Int) =
        change { it.copy(targetScore = value.coerceIn(MIN_TARGET_SCORE, MAX_TARGET_SCORE)) }

    /** Fija los lanzamientos disponibles, dentro de los límites permitidos. */
    fun setMaxLaunches(value: Int) =
        change { it.copy(maxLaunches = value.coerceIn(MIN_MAX_LAUNCHES, MAX_MAX_LAUNCHES)) }

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
