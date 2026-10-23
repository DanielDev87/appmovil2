package com.danidev.appmovil2.ui.player

import android.content.Context

/**
 * Almacén del nombre del jugador (HU-12).
 *
 * Es una interfaz para que [PlayerNameViewModel] no dependa de Android y pueda
 * probarse con una implementación en memoria.
 */
interface PlayerNameStore {
    /** Devuelve el nombre guardado, o una cadena vacía si todavía no hay ninguno. */
    fun load(): String

    /** Guarda el nombre para que sobreviva al cierre de la app. */
    fun save(name: String)
}

/**
 * Implementación basada en `SharedPreferences`, con su propio archivo
 * ("player_prefs") para no mezclarse con otros datos de la app.
 */
class SharedPreferencesPlayerNameStore(context: Context) : PlayerNameStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): String = preferences.getString(KEY_NAME, null).orEmpty()

    override fun save(name: String) {
        preferences.edit().putString(KEY_NAME, name).apply()
    }

    private companion object {
        const val PREFS_NAME = "player_prefs"
        const val KEY_NAME = "player_name"
    }
}
