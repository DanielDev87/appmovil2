package com.danidev.appmovil2.ui.dice

import android.content.Context

/**
 * Almacén de la paleta elegida para el dado (HU-04).
 *
 * Es una interfaz para que [DicePaletteViewModel] no dependa de Android y pueda
 * probarse con una implementación en memoria.
 */
interface DicePaletteStore {
    /** Devuelve la paleta guardada, o [DicePalette.LILA] si no hay ninguna. */
    fun load(): DicePalette

    /** Guarda la paleta para que sobreviva al cierre de la app. */
    fun save(palette: DicePalette)
}

/**
 * Implementación basada en `SharedPreferences`, con su propio archivo
 * ("dice_palette_prefs") para no mezclarse con otros datos de la app.
 */
class SharedPreferencesDicePaletteStore(context: Context) : DicePaletteStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): DicePalette {
        val saved = preferences.getString(KEY_PALETTE, null)
        // Si el valor guardado no se reconoce, se vuelve al lila original.
        return DicePalette.entries.firstOrNull { it.name == saved } ?: DicePalette.LILA
    }

    override fun save(palette: DicePalette) {
        preferences.edit().putString(KEY_PALETTE, palette.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "dice_palette_prefs"
        const val KEY_PALETTE = "palette"
    }
}
