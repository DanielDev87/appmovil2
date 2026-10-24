package com.danidev.appmovil2.ui.style

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estilo visual de la app. Se puede cambiar en cualquier momento desde "Personalizar".
 *
 * - [BALANCED]: tarjetas blancas con sombra suave y acento rosa (el diseño por defecto).
 * - [PLAYFUL]: bordes gruesos, sombras duras y colores vivos.
 */
enum class AppStyle {
    BALANCED,
    PLAYFUL
}

/**
 * Almacén de la preferencia de estilo.
 *
 * Es una interfaz para que [StyleViewModel] no dependa de Android y pueda
 * probarse con una implementación en memoria.
 */
interface StyleStore {
    /** Devuelve el estilo guardado, o [AppStyle.BALANCED] si no hay ninguno. */
    fun load(): AppStyle

    /** Guarda el estilo para que sobreviva al cierre de la app. */
    fun save(style: AppStyle)
}

/** Implementación de [StyleStore] basada en `SharedPreferences`. */
class SharedPreferencesStyleStore(context: Context) : StyleStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): AppStyle {
        val saved = preferences.getString(KEY_STYLE, null)
        // Un valor desconocido vuelve al estilo por defecto.
        return AppStyle.entries.firstOrNull { it.name == saved } ?: AppStyle.BALANCED
    }

    override fun save(style: AppStyle) {
        preferences.edit().putString(KEY_STYLE, style.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "style_prefs"
        const val KEY_STYLE = "app_style"
    }
}

/**
 * ViewModel del estilo visual: mantiene el estilo elegido y lo guarda cada vez
 * que cambia. Se crea una sola vez en `MainActivity`.
 */
class StyleViewModel(private val store: StyleStore) : ViewModel() {

    private val _style = MutableStateFlow(store.load())

    /** Estilo actual, de solo lectura para la UI. */
    val style: StateFlow<AppStyle> = _style.asStateFlow()

    fun select(style: AppStyle) {
        _style.value = style
        store.save(style)
    }
}

/**
 * Acceso al estilo vigente y a su cambio para cualquier pantalla, igual que
 * `LocalThemeController` con el tema.
 */
class StyleController(val style: AppStyle, val onStyleChange: (AppStyle) -> Unit)

/** Controlador por defecto (previews): estilo equilibrado y sin efecto al cambiar. */
val LocalStyleController = compositionLocalOf { StyleController(AppStyle.BALANCED) {} }
