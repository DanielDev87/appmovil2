package com.danidev.appmovil2.haptics

import com.danidev.appmovil2.RollIndicator
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/*
 * HU-16: Feedback háptico (vibración).
 *
 * "Como usuario, quiero sentir una pequeña vibración en el teléfono cuando el dado se
 * detenga, para tener una respuesta táctil del resultado."
 */

/**
 * Salida de vibración. Es una interfaz para que [DiceHaptics] pueda probarse sin un
 * teléfono: las pruebas usan una implementación que solo anota las llamadas.
 */
interface HapticOutput {
    /** Hace vibrar el dispositivo durante [durationMs] milisegundos. */
    fun pulse(durationMs: Long)

    /**
     * Hace vibrar siguiendo un patrón: [timingsMs] alterna esperas y vibraciones, empezando por una
     * espera (por ejemplo, `[0, 60, 70, 60]` son dos vibraciones cortas). Si la salida no sabe hacer
     * patrones, vibra una sola vez durante el total de las vibraciones.
     */
    fun pattern(timingsMs: LongArray) {
        pulse(timingsMs.filterIndexed { index, _ -> index % 2 == 1 }.sum())
    }
}

/**
 * Implementación que usa el motor de vibración real del dispositivo.
 *
 * No hace nada si el teléfono no tiene vibrador o si el sistema rechaza la orden, para
 * que la app nunca se caiga por esto.
 */
class VibratorOutput(private val vibrator: Vibrator?) : HapticOutput {

    override fun pulse(durationMs: Long) {
        val motor = vibrator ?: return
        try {
            if (!motor.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                motor.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                motor.vibrate(durationMs)
            }
        } catch (_: Throwable) {
            // Sin permiso o sin servicio de vibración: se ignora.
        }
    }

    override fun pattern(timingsMs: LongArray) {
        val motor = vibrator ?: return
        try {
            if (!motor.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                motor.vibrate(VibrationEffect.createWaveform(timingsMs, -1))
            } else {
                @Suppress("DEPRECATION")
                motor.vibrate(timingsMs, -1)
            }
        } catch (_: Throwable) {
            // Sin permiso o sin servicio de vibración: se ignora.
        }
    }
}

/**
 * Almacén de la preferencia "vibración activada".
 *
 * Es una interfaz para poder probar [DiceHaptics] sin `SharedPreferences`.
 */
interface HapticsStore {
    /** Devuelve si la vibración está activada; por defecto, sí. */
    fun loadEnabled(): Boolean

    /** Guarda la preferencia para conservarla al cerrar la app. */
    fun saveEnabled(enabled: Boolean)
}

/** Implementación de [HapticsStore] con `SharedPreferences` ("haptics_prefs"). */
class SharedPreferencesHapticsStore(context: Context) : HapticsStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun loadEnabled(): Boolean = preferences.getBoolean(KEY_ENABLED, true)

    override fun saveEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "haptics_prefs"
        const val KEY_ENABLED = "enabled"
    }
}

/**
 * Feedback háptico del dado (HU-16).
 *
 * Da una vibración corta cuando el dado se detiene, y una un poco más larga si el
 * tiro fue crítico o pifia. El usuario puede desactivarla y la elección se guarda.
 *
 * @param output Dónde se produce la vibración.
 * @param store Dónde se guarda si está activada.
 */
class DiceHaptics(
    private val output: HapticOutput,
    private val store: HapticsStore
) {
    /** `true` si la vibración está activada; es observable por Compose. */
    var isEnabled: Boolean by mutableStateOf(store.loadEnabled())
        private set

    /** Activa o desactiva la vibración y guarda la elección. */
    fun setVibrationEnabled(enabled: Boolean) {
        isEnabled = enabled
        store.saveEnabled(enabled)
    }

    /**
     * Vibra al terminar un lanzamiento.
     *
     * @param strong `true` para un tiro crítico o pifia, que vibra más tiempo.
     */
    fun vibrateOnLand(strong: Boolean = false) {
        if (!isEnabled) return
        output.pulse(if (strong) STRONG_MS else LAND_MS)
    }

    /**
     * Vibra al terminar un tiro según su resultado: una vibración corta si fue normal, dos rápidas
     * si fue un crítico y una larga y grave si fue una pifia.
     */
    fun vibrateOnResult(indicator: RollIndicator) {
        if (!isEnabled) return
        when (indicator) {
            RollIndicator.CRITICAL -> output.pattern(CRITICAL_PATTERN)
            RollIndicator.FUMBLE -> output.pulse(FUMBLE_MS)
            RollIndicator.NONE -> output.pulse(LAND_MS)
        }
    }

    /**
     * Vibra al terminar la partida: un redoble de tres vibraciones si se ganó y una sola larga si se
     * perdió.
     */
    fun vibrateOnGameOver(won: Boolean) {
        if (!isEnabled) return
        if (won) output.pattern(WIN_PATTERN) else output.pulse(LOSE_MS)
    }

    companion object {
        /** Dos vibraciones rápidas para un tiro crítico. */
        val CRITICAL_PATTERN = longArrayOf(0, 60, 70, 60)

        /** Duración de la vibración larga de una pifia. */
        const val FUMBLE_MS = 180L

        /** Tres vibraciones que van creciendo, para una victoria. */
        val WIN_PATTERN = longArrayOf(0, 60, 60, 90, 60, 150)

        /** Duración de la vibración larga de una derrota. */
        const val LOSE_MS = 320L

        /** Duración de la vibración de un lanzamiento normal. */
        const val LAND_MS = 40L

        /** Duración de la vibración de un tiro crítico o pifia. */
        const val STRONG_MS = 90L
    }
}

/**
 * Crea y recuerda un [DiceHaptics] conectado al vibrador del teléfono y a
 * `SharedPreferences` (HU-16).
 */
@Composable
fun rememberDiceHaptics(): DiceHaptics {
    val context = LocalContext.current.applicationContext
    return remember {
        DiceHaptics(
            output = VibratorOutput(systemVibrator(context)),
            store = SharedPreferencesHapticsStore(context)
        )
    }
}

/** Obtiene el vibrador del sistema, o `null` si el dispositivo no lo ofrece. */
private fun systemVibrator(context: Context): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
