package com.danidev.appmovil2.ui.dice

import android.content.Context

/**
 * Almacén del tamaño elegido para el dado (HU-05).
 *
 * Es una interfaz para que [DiceSizeViewModel] no dependa de Android y pueda
 * probarse con una implementación en memoria.
 */
interface DiceSizeStore {
    /** Devuelve el factor guardado, o 1.0 (tamaño original) si no hay ninguno. */
    fun load(): Float

    /** Guarda el factor para que sobreviva al cierre de la app. */
    fun save(factor: Float)
}

/**
 * Implementación basada en `SharedPreferences`, con su propio archivo
 * ("dice_size_prefs") para no mezclarse con otros datos de la app.
 */
class SharedPreferencesDiceSizeStore(context: Context) : DiceSizeStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): Float = DiceSize.coerce(preferences.getFloat(KEY_FACTOR, 1f))

    override fun save(factor: Float) {
        preferences.edit().putFloat(KEY_FACTOR, factor).apply()
    }

    private companion object {
        const val PREFS_NAME = "dice_size_prefs"
        const val KEY_FACTOR = "factor"
    }
}
