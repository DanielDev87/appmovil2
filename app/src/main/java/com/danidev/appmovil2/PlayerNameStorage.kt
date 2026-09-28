package com.danidev.appmovil2

import android.content.Context

interface PlayerNameStorage {
    fun saveName(name: String)
    fun getName(): String
}

class SharedPreferencesPlayerNameStorage(context: Context) : PlayerNameStorage {
    private val sharedPreferences = context.getSharedPreferences("player_preferences", Context.MODE_PRIVATE)

    override fun saveName(name: String) {
        sharedPreferences.edit().putString(KEY_PLAYER_NAME, name.trim()).apply()
    }

    override fun getName(): String {
        return sharedPreferences.getString(KEY_PLAYER_NAME, "") ?: ""
    }

    companion object {
        private const val KEY_PLAYER_NAME = "player_name"
    }
}

class InMemoryPlayerNameStorage : PlayerNameStorage {
    private var playerName: String = ""

    override fun saveName(name: String) {
        playerName = name.trim()
    }

    override fun getName(): String = playerName
}
