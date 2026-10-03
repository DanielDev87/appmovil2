package com.danidev.appmovil2.ui.theme

import android.content.Context

/**
 * Almacén de la preferencia de tema (HU-03).
 *
 * Es una interfaz para que [ThemeViewModel] no dependa de Android y pueda
 * probarse con una implementación en memoria.
 */
interface ThemeStore {
    /** Devuelve la preferencia guardada, o [ThemeMode.SYSTEM] si no hay ninguna. */
    fun load(): ThemeMode

    /** Guarda la preferencia para que sobreviva al cierre de la app. */
    fun save(mode: ThemeMode)
}

/**
 * Implementación basada en `SharedPreferences`.
 *
 * Usa su propio archivo ("theme_prefs") para no mezclarse con otros datos
 * de la app, y no requiere dependencias adicionales.
 */
class SharedPreferencesThemeStore(context: Context) : ThemeStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): ThemeMode {
        val saved = preferences.getString(KEY_THEME_MODE, null)
        // Si el valor guardado no se reconoce, se vuelve a seguir al sistema.
        return ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.SYSTEM
    }

    override fun save(mode: ThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "theme_prefs"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
