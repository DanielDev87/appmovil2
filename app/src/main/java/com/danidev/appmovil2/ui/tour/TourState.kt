package com.danidev.appmovil2.ui.tour

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import com.danidev.appmovil2.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Pantalla en la que ocurre un paso del tour. */
enum class TourScreen { WELCOME, GAME }

/**
 * Pasos del tour, en el orden en que se muestran.
 *
 * @property screen Pantalla donde está lo que se señala; el tour cambia de pantalla solo.
 * @property target Elemento que se resalta; `null` para un paso centrado, sin señalar nada.
 * @property interactive `true` si el usuario puede tocar de verdad lo que se resalta: lo que toque
 * funciona y el tour pasa al siguiente paso. En los demás pasos el elemento solo se señala y se avanza
 * con "Siguiente".
 */
enum class TourStep(
    val screen: TourScreen,
    val target: TourTargetId?,
    val interactive: Boolean,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
) {
    INTRO(TourScreen.WELCOME, null, false, R.string.tour_intro_title, R.string.tour_intro_body),
    THEME(TourScreen.WELCOME, TourTargetId.THEME_SWITCH, true, R.string.tour_theme_title, R.string.tour_theme_body),
    NAME(TourScreen.WELCOME, TourTargetId.NAME_FIELD, false, R.string.tour_name_title, R.string.tour_name_body),
    PROGRESS(TourScreen.WELCOME, TourTargetId.WELCOME_PROGRESS, false, R.string.tour_progress_title, R.string.tour_progress_body),
    SETTINGS(TourScreen.WELCOME, TourTargetId.WELCOME_SETTINGS, false, R.string.tour_settings_title, R.string.tour_settings_body),
    PLAY(TourScreen.WELCOME, TourTargetId.PLAY_BUTTON, true, R.string.tour_play_title, R.string.tour_play_body),
    SCORES(TourScreen.GAME, TourTargetId.SCORE_CARDS, false, R.string.tour_scores_title, R.string.tour_scores_body),
    INFO(TourScreen.GAME, TourTargetId.INFO_PANEL, false, R.string.tour_info_title, R.string.tour_info_body),
    DICE(TourScreen.GAME, TourTargetId.DICE_STAGE, true, R.string.tour_dice_title, R.string.tour_dice_body),
    ROLL(TourScreen.GAME, TourTargetId.ROLL_BUTTON, true, R.string.tour_roll_title, R.string.tour_roll_body),
    RECENT(TourScreen.GAME, TourTargetId.RECENT_ROLLS, false, R.string.tour_recent_title, R.string.tour_recent_body),
    SOUND(TourScreen.GAME, TourTargetId.TOP_SOUND, true, R.string.tour_sound_title, R.string.tour_sound_body),
    RESET(TourScreen.GAME, TourTargetId.TOP_RESET, false, R.string.tour_reset_title, R.string.tour_reset_body),
    GAME_SETTINGS(TourScreen.GAME, TourTargetId.TOP_SETTINGS, false, R.string.tour_game_settings_title, R.string.tour_game_settings_body),
    DONE(TourScreen.GAME, null, false, R.string.tour_done_title, R.string.tour_done_body)
}

/**
 * Almacén de si el jugador ya vio el tour; interfaz para poder probar el ViewModel sin Android.
 */
interface TourStore {
    /** `true` si el tour ya se mostró alguna vez. */
    fun loadSeen(): Boolean

    fun saveSeen(seen: Boolean)
}

/** Implementación de [TourStore] basada en `SharedPreferences`. */
class SharedPreferencesTourStore(context: Context) : TourStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun loadSeen() = preferences.getBoolean(KEY_SEEN, false)

    override fun saveSeen(seen: Boolean) {
        preferences.edit().putBoolean(KEY_SEEN, seen).apply()
    }

    private companion object {
        const val PREFS_NAME = "tour_prefs"
        const val KEY_SEEN = "seen"
    }
}

/**
 * Estado del tour: si está abierto, en qué paso va y si ya se vio alguna vez.
 *
 * Se abre solo la primera vez que se inicia la app ([showIfFirstTime]) y, cuando el jugador quiere,
 * desde Ajustes ([start]), empezando por la pantalla en la que está. Terminarlo u omitirlo cuenta como
 * visto.
 */
class TourViewModel(private val store: TourStore) : ViewModel() {

    private val _visible = MutableStateFlow(false)

    /** `true` mientras el tour está en pantalla. */
    val visible: StateFlow<Boolean> = _visible.asStateFlow()

    private val _stepIndex = MutableStateFlow(0)

    /** Posición del paso actual dentro de [steps], desde 0. */
    val stepIndex: StateFlow<Int> = _stepIndex.asStateFlow()

    val steps: List<TourStep> = TourStep.entries

    /** Paso en el que va el tour. */
    val currentStep: TourStep get() = steps[_stepIndex.value]

    /** `true` en el último paso, donde "Siguiente" pasa a ser "¡Listo!". */
    val isLastStep: Boolean get() = _stepIndex.value == steps.lastIndex

    /** Abre el tour desde el principio, solo si nunca se había mostrado. */
    fun showIfFirstTime() {
        if (!store.loadSeen() && !_visible.value) start(TourScreen.WELCOME)
    }

    /**
     * Abre el tour. Empieza en el primer paso de la pantalla en la que está el usuario, para no
     * sacarlo de una partida en curso.
     */
    fun start(from: TourScreen = TourScreen.WELCOME) {
        _stepIndex.value = steps.indexOfFirst { it.screen == from }.coerceAtLeast(0)
        _visible.value = true
    }

    /** Avanza un paso; en el último, termina el tour. */
    fun next() {
        if (isLastStep) finish() else _stepIndex.value += 1
    }

    /** Vuelve al paso anterior; en el primero no hace nada. */
    fun back() {
        if (_stepIndex.value > 0) _stepIndex.value -= 1
    }

    /**
     * Avanza solo si el tour sigue en el paso [index]. Se usa cuando el usuario toca el elemento
     * resaltado: así un toque tardío no se salta un paso que ya cambió.
     */
    fun advanceFrom(index: Int) {
        if (_visible.value && _stepIndex.value == index) next()
    }

    /** Dice si tocar [target] debe hacer avanzar el tour: solo en un paso interactivo que lo señala. */
    fun shouldAdvanceOnTouch(target: TourTargetId): Boolean =
        _visible.value && currentStep.interactive && currentStep.target == target

    /** Cierra el tour y lo marca como visto. */
    fun finish() {
        _visible.value = false
        store.saveSeen(true)
    }
}

/**
 * Acceso al tour para cualquier pantalla: abrirlo desde Ajustes sin pasar el ViewModel por la
 * navegación.
 */
class TourController(val onShowTour: () -> Unit)

/** Controlador por defecto (previews): no hace nada. */
val LocalTourController = compositionLocalOf { TourController {} }
