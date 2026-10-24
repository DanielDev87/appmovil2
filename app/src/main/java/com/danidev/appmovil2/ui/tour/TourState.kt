package com.danidev.appmovil2.ui.tour

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import com.danidev.appmovil2.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pasos del tour guiado, en el orden en que se muestran.
 *
 * @property emoji Ícono grande del paso.
 */
enum class TourStep(
    val emoji: String,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
) {
    WELCOME("🎲", R.string.tour_welcome_title, R.string.tour_welcome_body),
    ROLL("👆", R.string.tour_roll_title, R.string.tour_roll_body),
    LOCK("🔒", R.string.tour_lock_title, R.string.tour_lock_body),
    SETTINGS("⚙️", R.string.tour_settings_title, R.string.tour_settings_body),
    FRIENDS("👥", R.string.tour_friends_title, R.string.tour_friends_body),
    PROGRESS("🏆", R.string.tour_progress_title, R.string.tour_progress_body)
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
 * desde Ajustes ([start]). Terminarlo u omitirlo cuenta como visto.
 */
class TourViewModel(private val store: TourStore) : ViewModel() {

    private val _visible = MutableStateFlow(false)

    /** `true` mientras el tour está en pantalla. */
    val visible: StateFlow<Boolean> = _visible.asStateFlow()

    private val _stepIndex = MutableStateFlow(0)

    /** Posición del paso actual, desde 0. */
    val stepIndex: StateFlow<Int> = _stepIndex.asStateFlow()

    val steps: List<TourStep> = TourStep.entries

    /** `true` en el último paso, donde "Siguiente" pasa a ser "¡Empezar!". */
    val isLastStep: Boolean get() = _stepIndex.value == steps.lastIndex

    /** Abre el tour solo si nunca se había mostrado. */
    fun showIfFirstTime() {
        if (!store.loadSeen() && !_visible.value) start()
    }

    /** Abre el tour desde el primer paso. */
    fun start() {
        _stepIndex.value = 0
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
