package com.danidev.appmovil2.ui.progress

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.danidev.appmovil2.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lo que el jugador ha logrado en todas sus partidas; se guarda entre sesiones.
 *
 * @property gamesPlayed Partidas terminadas, ganadas o perdidas.
 * @property wins Partidas ganadas.
 * @property currentStreak Victorias seguidas ahora; una derrota la devuelve a 0.
 * @property bestStreak La racha de victorias más larga.
 * @property bestWinLaunches Menos lanzamientos con los que ganó una partida; 0 si aún no ha ganado.
 * @property bestScore Más puntos sumados en una partida.
 * @property totalCriticals Tiros críticos en todas las partidas.
 * @property unlocked Identificadores de los logros ya desbloqueados.
 */
data class PlayerProgress(
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val bestWinLaunches: Int = 0,
    val bestScore: Int = 0,
    val totalCriticals: Int = 0,
    val unlocked: Set<String> = emptySet()
) {
    val losses: Int get() = gamesPlayed - wins
}

/**
 * Resultado de una partida terminada.
 *
 * @property sides Caras del dado con el que se jugó.
 */
data class GameResult(
    val won: Boolean,
    val score: Int,
    val launches: Int,
    val criticals: Int,
    val sides: Int
)

/**
 * Logros que se pueden desbloquear.
 *
 * @property goal Meta numérica del logro y cómo medir el avance, para mostrar "3 de 5"; `null` si el
 * logro no se mide con un contador (por ejemplo, ganar con un D20).
 */
enum class Achievement(
    val emoji: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val goal: AchievementGoal? = null,
    private val condition: (PlayerProgress, GameResult) -> Boolean
) {
    FIRST_WIN("🏆", R.string.ach_first_win, R.string.ach_first_win_desc, AchievementGoal(1) { it.wins }, { p, _ -> p.wins >= 1 }),
    WINS_5("🥇", R.string.ach_wins_5, R.string.ach_wins_5_desc, AchievementGoal(5) { it.wins }, { p, _ -> p.wins >= 5 }),
    WINS_10("👑", R.string.ach_wins_10, R.string.ach_wins_10_desc, AchievementGoal(10) { it.wins }, { p, _ -> p.wins >= 10 }),
    STREAK_3("🔥", R.string.ach_streak_3, R.string.ach_streak_3_desc, AchievementGoal(3) { it.bestStreak }, { p, _ -> p.bestStreak >= 3 }),
    FIRST_CRITICAL("🎯", R.string.ach_first_critical, R.string.ach_first_critical_desc, AchievementGoal(1) { it.totalCriticals }, { p, _ -> p.totalCriticals >= 1 }),
    CRITICALS_10("⭐", R.string.ach_criticals_10, R.string.ach_criticals_10_desc, AchievementGoal(10) { it.totalCriticals }, { p, _ -> p.totalCriticals >= 10 }),
    QUICK_WIN("⚡", R.string.ach_quick_win, R.string.ach_quick_win_desc, null, { _, r -> r.won && r.launches <= QUICK_WIN_LAUNCHES }),
    D20_WIN("🐉", R.string.ach_d20_win, R.string.ach_d20_win_desc, null, { _, r -> r.won && r.sides == 20 }),
    PLAYED_10("🎲", R.string.ach_played_10, R.string.ach_played_10_desc, AchievementGoal(10) { it.gamesPlayed }, { p, _ -> p.gamesPlayed >= 10 });

    /** Identificador estable con el que se guarda el logro. */
    val id: String get() = name

    internal fun isMet(progress: PlayerProgress, result: GameResult) = condition(progress, result)

    companion object {
        /** Lanzamientos o menos con los que una victoria cuenta como rápida. */
        const val QUICK_WIN_LAUNCHES = 3
    }
}

/**
 * Meta de un logro con contador.
 *
 * @property target Valor que hay que alcanzar.
 * @property current Cuánto lleva el jugador, a partir de su progreso.
 */
class AchievementGoal(val target: Int, val current: (PlayerProgress) -> Int)

/** Avance de [progress] hacia la meta de este logro, entre 0 y 1; 0 si el logro no tiene contador. */
fun Achievement.fractionOf(progress: PlayerProgress): Float {
    val g = goal ?: return 0f
    return (g.current(progress).toFloat() / g.target).coerceIn(0f, 1f)
}

/**
 * Lo que cambió al terminar una partida.
 *
 * @property progress Progreso ya actualizado.
 * @property newAchievements Logros desbloqueados justo con esta partida.
 * @property newBestWin `true` si se ganó con menos lanzamientos que nunca.
 * @property newBestScore `true` si se sumaron más puntos que nunca.
 */
data class GameRecord(
    val progress: PlayerProgress,
    val newAchievements: List<Achievement>,
    val newBestWin: Boolean,
    val newBestScore: Boolean
)

/** Aplica el [result] de una partida terminada al [progress] y calcula qué logros se ganaron. */
fun PlayerProgress.recordGame(result: GameResult): GameRecord {
    val streak = if (result.won) currentStreak + 1 else 0
    val newBestWin = result.won && (bestWinLaunches == 0 || result.launches < bestWinLaunches)
    val newBestScore = result.score > bestScore
    val updated = copy(
        gamesPlayed = gamesPlayed + 1,
        wins = wins + if (result.won) 1 else 0,
        currentStreak = streak,
        bestStreak = maxOf(bestStreak, streak),
        bestWinLaunches = if (newBestWin) result.launches else bestWinLaunches,
        bestScore = maxOf(bestScore, result.score),
        totalCriticals = totalCriticals + result.criticals
    )
    val earned = Achievement.entries.filter { it.id !in updated.unlocked && it.isMet(updated, result) }
    return GameRecord(
        progress = updated.copy(unlocked = updated.unlocked + earned.map { it.id }),
        newAchievements = earned,
        newBestWin = newBestWin,
        newBestScore = newBestScore && result.score > 0
    )
}

/**
 * Almacén del progreso; interfaz para poder probar el ViewModel sin Android.
 */
interface ProgressStore {
    fun load(): PlayerProgress
    fun save(progress: PlayerProgress)
}

/** Implementación de [ProgressStore] basada en `SharedPreferences`. */
class SharedPreferencesProgressStore(context: Context) : ProgressStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load() = PlayerProgress(
        gamesPlayed = preferences.getInt(KEY_GAMES, 0),
        wins = preferences.getInt(KEY_WINS, 0),
        currentStreak = preferences.getInt(KEY_STREAK, 0),
        bestStreak = preferences.getInt(KEY_BEST_STREAK, 0),
        bestWinLaunches = preferences.getInt(KEY_BEST_WIN, 0),
        bestScore = preferences.getInt(KEY_BEST_SCORE, 0),
        totalCriticals = preferences.getInt(KEY_CRITICALS, 0),
        unlocked = preferences.getStringSet(KEY_UNLOCKED, emptySet()).orEmpty().toSet()
    )

    override fun save(progress: PlayerProgress) {
        preferences.edit()
            .putInt(KEY_GAMES, progress.gamesPlayed)
            .putInt(KEY_WINS, progress.wins)
            .putInt(KEY_STREAK, progress.currentStreak)
            .putInt(KEY_BEST_STREAK, progress.bestStreak)
            .putInt(KEY_BEST_WIN, progress.bestWinLaunches)
            .putInt(KEY_BEST_SCORE, progress.bestScore)
            .putInt(KEY_CRITICALS, progress.totalCriticals)
            .putStringSet(KEY_UNLOCKED, progress.unlocked)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "progress_prefs"
        const val KEY_GAMES = "games"
        const val KEY_WINS = "wins"
        const val KEY_STREAK = "streak"
        const val KEY_BEST_STREAK = "best_streak"
        const val KEY_BEST_WIN = "best_win_launches"
        const val KEY_BEST_SCORE = "best_score"
        const val KEY_CRITICALS = "criticals"
        const val KEY_UNLOCKED = "unlocked"
    }
}

/**
 * ViewModel del progreso: guarda el resultado de cada partida y recuerda qué cambió con la
 * última, para mostrarlo en la pantalla de fin de partida.
 */
class ProgressViewModel(private val store: ProgressStore) : ViewModel() {

    private val _progress = MutableStateFlow(store.load())

    /** Progreso total del jugador. */
    val progress: StateFlow<PlayerProgress> = _progress.asStateFlow()

    private val _lastRecord = MutableStateFlow<GameRecord?>(null)

    /** Lo que cambió con la última partida terminada, o `null` si aún no hay. */
    val lastRecord: StateFlow<GameRecord?> = _lastRecord.asStateFlow()

    /** Registra una partida terminada. */
    fun recordGame(result: GameResult) {
        val record = _progress.value.recordGame(result)
        _progress.value = record.progress
        _lastRecord.value = record
        store.save(record.progress)
    }

    /** Olvida lo que cambió con la última partida (al empezar otra). */
    fun clearLastRecord() {
        _lastRecord.value = null
    }
}

/**
 * Obtiene el [ProgressViewModel] de la app. Se comparte entre pantallas para que la bienvenida
 * y la partida vean siempre el mismo progreso.
 */
@Composable
fun rememberProgressViewModel(): ProgressViewModel {
    val appContext = LocalContext.current.applicationContext
    val owner = generateSequence(LocalContext.current) { (it as? android.content.ContextWrapper)?.baseContext }
        .filterIsInstance<androidx.activity.ComponentActivity>()
        .first()
    return viewModel(
        viewModelStoreOwner = owner,
        factory = viewModelFactory {
            initializer { ProgressViewModel(SharedPreferencesProgressStore(appContext)) }
        }
    )
}
