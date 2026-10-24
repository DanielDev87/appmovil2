package com.danidev.appmovil2.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import com.danidev.appmovil2.MAX_LAUNCHES
import com.danidev.appmovil2.Difficulty
import com.danidev.appmovil2.MAX_DICE
import com.danidev.appmovil2.MAX_MAX_LAUNCHES
import com.danidev.appmovil2.MAX_ROLL_MODIFIER
import com.danidev.appmovil2.MIN_ROLL_MODIFIER
import com.danidev.appmovil2.MAX_PLAYERS
import com.danidev.appmovil2.MIN_PLAYERS
import com.danidev.appmovil2.MAX_TARGET_SCORE
import com.danidev.appmovil2.MIN_MAX_LAUNCHES
import com.danidev.appmovil2.MIN_TARGET_SCORE
import com.danidev.appmovil2.TARGET_SCORE
import com.danidev.appmovil2.ui.dice.DiceType
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
 * @property difficulty Nivel elegido, que reajusta la meta si cambia el dado; `null` cuando la meta se
 * puso a mano.
 * @property playerCount Personas que juegan, turnándose en el mismo teléfono.
 * @property diceCount Dados en juego, de 1 a [MAX_DICE] (HU-06); se guarda aquí para poder elegirlo
 * también desde la bienvenida.
 * @property rollModifier Número que se suma a cada tiro, de [MIN_ROLL_MODIFIER] a [MAX_ROLL_MODIFIER].
 * @property extraDiceSides Caras del dado 2 y del dado 3 cuando son distintos del primero; 0 significa
 * "igual que el primero".
 * @property playerNames Nombres de los jugadores 2 a 4; vacío usa "Jugador N". El del jugador 1 es
 * el nombre que se escribe en la bienvenida.
 */
data class GameSettings(
    val soundMuted: Boolean = false,
    val shakeEnabled: Boolean = true,
    val targetScore: Int = TARGET_SCORE,
    val maxLaunches: Int = MAX_LAUNCHES,
    val difficulty: Difficulty? = Difficulty.NORMAL,
    val playerCount: Int = MIN_PLAYERS,
    val diceCount: Int = 1,
    val rollModifier: Int = 0,
    val extraDiceSides: List<Int> = List(MAX_DICE - 1) { 0 },
    val playerNames: List<String> = List(MAX_PLAYERS - 1) { "" }
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
            .coerceIn(MIN_MAX_LAUNCHES, MAX_MAX_LAUNCHES),
        playerCount = preferences.getInt(KEY_PLAYER_COUNT, MIN_PLAYERS).coerceIn(MIN_PLAYERS, MAX_PLAYERS),
        diceCount = preferences.getInt(KEY_DICE_COUNT, 1).coerceIn(1, MAX_DICE),
        rollModifier = preferences.getInt(KEY_ROLL_MODIFIER, 0).coerceIn(MIN_ROLL_MODIFIER, MAX_ROLL_MODIFIER),
        extraDiceSides = List(MAX_DICE - 1) { preferences.getInt(KEY_EXTRA_SIDES + (it + 2), 0) },
        difficulty = loadDifficulty(),
        playerNames = List(MAX_PLAYERS - 1) { preferences.getString(KEY_PLAYER_NAME + (it + 2), "").orEmpty() }
    )

    /** El nivel guardado; sin dato, es Normal si la meta guardada es la de siempre y personalizada si no. */
    private fun loadDifficulty(): Difficulty? {
        if (!preferences.contains(KEY_DIFFICULTY)) {
            val target = preferences.getInt(KEY_TARGET_SCORE, TARGET_SCORE)
            val launches = preferences.getInt(KEY_MAX_LAUNCHES, MAX_LAUNCHES)
            return if (target == TARGET_SCORE && launches == MAX_LAUNCHES) Difficulty.NORMAL else null
        }
        val saved = preferences.getString(KEY_DIFFICULTY, null)
        return Difficulty.entries.firstOrNull { it.name == saved }
    }

    override fun save(settings: GameSettings) {
        preferences.edit()
            .putBoolean(KEY_SOUND_MUTED, settings.soundMuted)
            .putBoolean(KEY_SHAKE_ENABLED, settings.shakeEnabled)
            .putInt(KEY_TARGET_SCORE, settings.targetScore)
            .putInt(KEY_MAX_LAUNCHES, settings.maxLaunches)
            .putInt(KEY_PLAYER_COUNT, settings.playerCount)
            .putInt(KEY_DICE_COUNT, settings.diceCount)
            .putInt(KEY_ROLL_MODIFIER, settings.rollModifier)
            .putString(KEY_DIFFICULTY, settings.difficulty?.name ?: DIFFICULTY_CUSTOM)
            .apply {
                settings.extraDiceSides.forEachIndexed { i, sides -> putInt(KEY_EXTRA_SIDES + (i + 2), sides) }
                settings.playerNames.forEachIndexed { i, name -> putString(KEY_PLAYER_NAME + (i + 2), name) }
            }
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "game_settings_prefs"
        const val KEY_SOUND_MUTED = "sound_muted"
        const val KEY_SHAKE_ENABLED = "shake_enabled"
        const val KEY_TARGET_SCORE = "target_score"
        const val KEY_MAX_LAUNCHES = "max_launches"
        const val KEY_PLAYER_COUNT = "player_count"
        const val KEY_DICE_COUNT = "dice_count"
        const val KEY_ROLL_MODIFIER = "roll_modifier"
        const val KEY_EXTRA_SIDES = "extra_sides_"
        const val KEY_DIFFICULTY = "difficulty"
        const val DIFFICULTY_CUSTOM = "custom"
        const val KEY_PLAYER_NAME = "player_name_"
    }
}

/** Largo máximo del nombre de un jugador adicional. */
const val MAX_PLAYER_NAME_LENGTH = 14

/** ViewModel de los ajustes de la partida: los mantiene y los guarda al cambiar. */
class GameSettingsViewModel(private val store: GameSettingsStore) : ViewModel() {

    private val _settings = MutableStateFlow(store.load())

    /** Ajustes actuales, de solo lectura para la UI. */
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    fun setSoundMuted(muted: Boolean) = change { it.copy(soundMuted = muted) }

    fun setShakeEnabled(enabled: Boolean) = change { it.copy(shakeEnabled = enabled) }

    /** Fija los puntos para ganar, dentro de los límites permitidos. */
    fun setTargetScore(value: Int) = change {
        it.copy(targetScore = value.coerceIn(MIN_TARGET_SCORE, MAX_TARGET_SCORE), difficulty = null)
    }

    /** Fija los lanzamientos disponibles, dentro de los límites permitidos. */
    fun setMaxLaunches(value: Int) = change {
        it.copy(maxLaunches = value.coerceIn(MIN_MAX_LAUNCHES, MAX_MAX_LAUNCHES), difficulty = null)
    }

    /**
     * Elige un nivel de dificultad: fija los lanzamientos y la meta que le tocan al dado de [sides] caras
     * con [diceCount] dados.
     */
    fun setDifficulty(difficulty: Difficulty, sides: Int, diceCount: Int) =
        setDifficulty(difficulty, List(diceCount) { sides })

    /** Igual que la anterior, con las caras de cada dado y el modificador de tiro actual. */
    fun setDifficulty(difficulty: Difficulty, diceSides: List<Int>) = change {
        it.copy(
            difficulty = difficulty,
            targetScore = difficulty.targetScore(diceSides, it.rollModifier),
            maxLaunches = difficulty.maxLaunches
        )
    }

    /**
     * Si hay un nivel elegido, reajusta la meta al nuevo dado o cantidad de dados; con meta personalizada
     * no toca nada.
     */
    fun refreshDifficulty(sides: Int, diceCount: Int) =
        refreshDifficulty(List(diceCount) { sides })

    /** Igual que la anterior, con las caras de cada dado (pueden ser distintas). */
    fun refreshDifficulty(diceSides: List<Int>) {
        val chosen = _settings.value.difficulty ?: return
        setDifficulty(chosen, diceSides)
    }

    /** Fija cuántos dados se usan: 1 o 2. */
    fun setDiceCount(value: Int) = change { it.copy(diceCount = value.coerceIn(1, MAX_DICE)) }

    /** Fija el número que se suma a cada tiro, entre [MIN_ROLL_MODIFIER] y [MAX_ROLL_MODIFIER]. */
    fun setRollModifier(value: Int) =
        change { it.copy(rollModifier = value.coerceIn(MIN_ROLL_MODIFIER, MAX_ROLL_MODIFIER)) }

    /**
     * Fija las caras del dado en la posición [index] (de 1 a [MAX_DICE] - 1, es decir, el dado 2 o el 3);
     * 0 vuelve a "igual que el primero". Solo se aceptan caras de dados que existen.
     */
    fun setExtraDiceSides(index: Int, sides: Int) {
        if (index !in 1 until MAX_DICE) return
        val valid = if (sides == 0 || sides in DiceType.entries.map { it.sides }) sides else return
        change { settings ->
            settings.copy(extraDiceSides = settings.extraDiceSides.mapIndexed { i, old -> if (i == index - 1) valid else old })
        }
    }

    /** Fija cuántas personas juegan, dentro de los límites permitidos. */
    fun setPlayerCount(value: Int) =
        change { it.copy(playerCount = value.coerceIn(MIN_PLAYERS, MAX_PLAYERS)) }

    /**
     * Fija el nombre del jugador en la posición [index] (de 1 a 3, es decir, los jugadores 2 a 4).
     * Se limita a [MAX_PLAYER_NAME_LENGTH] caracteres y se quitan los de control.
     */
    fun setPlayerName(index: Int, name: String) {
        if (index !in 1 until MAX_PLAYERS) return
        val clean = name.filterNot { it.isISOControl() }.take(MAX_PLAYER_NAME_LENGTH)
        change { settings ->
            settings.copy(playerNames = settings.playerNames.mapIndexed { i, old -> if (i == index - 1) clean else old })
        }
    }

    private fun change(transform: (GameSettings) -> GameSettings) {
        _settings.update(transform)
        store.save(_settings.value)
    }
}

/**
 * Obtiene el [GameSettingsViewModel] de la app. Se comparte entre la bienvenida y la partida, para
 * que lo que se elige en una (jugadores, nombres, meta) lo vea la otra al instante.
 */
@Composable
fun rememberGameSettingsViewModel(): GameSettingsViewModel {
    val appContext = LocalContext.current.applicationContext
    val owner = generateSequence(LocalContext.current) { (it as? android.content.ContextWrapper)?.baseContext }
        .filterIsInstance<androidx.activity.ComponentActivity>()
        .first()
    return viewModel(
        viewModelStoreOwner = owner,
        factory = viewModelFactory {
            initializer { GameSettingsViewModel(SharedPreferencesGameSettingsStore(appContext)) }
        }
    )
}
