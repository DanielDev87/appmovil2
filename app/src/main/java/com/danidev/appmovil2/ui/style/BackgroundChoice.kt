package com.danidev.appmovil2.ui.style

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.danidev.appmovil2.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fondo que el usuario eligió para la app.
 *
 * @property colorArgb Color del fondo, o `null` para usar el del estilo.
 * @property gradient `true` para un degradado que aclara (o oscurece) el color hacia abajo.
 */
data class BackgroundChoice(
    val colorArgb: Int? = null,
    val gradient: Boolean = true
) {
    val isCustom: Boolean get() = colorArgb != null
}

/** Colores de fondo que se ofrecen listos para elegir. */
enum class BackgroundPreset(@StringRes val labelRes: Int, val color: Color) {
    SKY(R.string.bg_sky, Color(0xFFBFE3FF)),
    MINT(R.string.bg_mint, Color(0xFFC9F2E3)),
    PEACH(R.string.bg_peach, Color(0xFFFFD9C2)),
    LAVENDER(R.string.bg_lavender, Color(0xFFE2D4FF)),
    ROSE(R.string.bg_rose, Color(0xFFFFD3EC)),
    NIGHT(R.string.bg_night, Color(0xFF241B3D))
}

private val DarkInkOnLightBackground = Color(0xFF3E0F2C)
private val DarkMutedOnLightBackground = Color(0xFF6B3F5A)
private val LightInkOnDarkBackground = Color(0xFFF6E8F2)
private val LightMutedOnDarkBackground = Color(0xFFD9C3D3)

/** Cuánto se oscurece el color elegido cuando la app está en tema oscuro. */
private const val DARK_THEME_SHADE = 0.5f

/** Por debajo de esta luminancia el fondo se considera oscuro y el texto pasa a ser claro. */
private const val DARK_BACKGROUND_LUMINANCE = 0.4f

/**
 * Aplica el fondo elegido sobre los valores de diseño del estilo.
 *
 * Cambia el color (o degradado) del fondo y, según lo claro que sea, el color del texto
 * que va directamente sobre él. En tema oscuro el color se oscurece para que el cambio de
 * tema se note. Las tarjetas se tiñen con el color del fondo en lugar de
 * quedar blancas, para que no parezcan parches pegados, y su texto se ajusta a ese tinte.
 * Con `colorArgb == null` devuelve [tokens] sin cambios.
 */
fun applyBackground(tokens: AppTokens, choice: BackgroundChoice): AppTokens {
    val argb = choice.colorArgb ?: return tokens
    // En tema oscuro el color elegido se oscurece conservando su tono, para que el
    // interruptor de tema siga teniendo efecto con un fondo propio.
    val chosen = Color(argb)
    val base = if (tokens.isDark) lerp(chosen, Color.Black, DARK_THEME_SHADE) else chosen
    val isDarkBackground = base.luminance() < DARK_BACKGROUND_LUMINANCE
    val fadeTo = if (isDarkBackground) Color.Black else Color.White

    // Tarjetas: un tinte del fondo, más claro en fondos claros y apenas aclarado en oscuros.
    val surface = lerp(base, Color.White, if (isDarkBackground) 0.12f else 0.6f)
    val isDarkSurface = surface.luminance() < DARK_BACKGROUND_LUMINANCE
    val surfaceTone = if (isDarkSurface) Color.White else Color.Black

    val accent = accentFor(chosen, tokens)

    return tokens.copy(
        accent = accent,
        onAccent = if (accent.luminance() < ACCENT_DARK_LUMINANCE) Color.White else DarkInkOnLightBackground,
        bgTop = base,
        bgBottom = if (choice.gradient) lerp(base, fadeTo, 0.55f) else base,
        dots = tokens.dots?.let { lerp(base, if (isDarkBackground) Color.White else Color.Black, 0.1f) },
        bgInk = if (isDarkBackground) LightInkOnDarkBackground else DarkInkOnLightBackground,
        bgMuted = if (isDarkBackground) LightMutedOnDarkBackground else DarkMutedOnLightBackground,
        surface = surface,
        soft = lerp(surface, surfaceTone, 0.1f),
        ink = if (isDarkSurface) LightInkOnDarkBackground else DarkInkOnLightBackground,
        muted = if (isDarkSurface) LightMutedOnDarkBackground else DarkMutedOnLightBackground,
        // En el estilo equilibrado un borde fino separa la tarjeta del fondo; el juguetón ya tiene el suyo.
        outline = if (tokens.isPlayful) tokens.outline else lerp(surface, surfaceTone, 0.18f),
        borderWidth = if (tokens.isPlayful) tokens.borderWidth else 1.dp
    )
}

/** Por debajo de esta luminancia el acento es oscuro y su texto va en blanco. */
private const val ACCENT_DARK_LUMINANCE = 0.4f

/**
 * Color de acento (botón principal, progreso, interruptores) con el mismo tono que el fondo
 * elegido: más intenso y oscuro en tema claro, más suave y claro en tema oscuro.
 */
private fun accentFor(chosen: Color, tokens: AppTokens): Color {
    val hue = chosen.hue()
    return when {
        tokens.isPlayful -> Color.hsv(hue, 0.65f, 1f)
        tokens.isDark -> Color.hsv(hue, 0.45f, 1f)
        else -> Color.hsv(hue, 0.85f, 0.68f)
    }
}

/** Tono (0 a 360) del color; 0 para los grises, que no tienen tono. */
internal fun Color.hue(): Float {
    val max = maxOf(red, green, blue)
    val delta = max - minOf(red, green, blue)
    if (delta == 0f) return 0f
    val sector = when (max) {
        red -> ((green - blue) / delta) % 6f
        green -> (blue - red) / delta + 2f
        else -> (red - green) / delta + 4f
    }
    val degrees = sector * 60f
    return if (degrees < 0f) degrees + 360f else degrees
}

/** Almacén del fondo elegido; interfaz para poder probar el ViewModel sin Android. */
interface BackgroundStore {
    fun load(): BackgroundChoice
    fun save(choice: BackgroundChoice)
}

/** Implementación de [BackgroundStore] basada en `SharedPreferences`. */
class SharedPreferencesBackgroundStore(context: Context) : BackgroundStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load() = BackgroundChoice(
        colorArgb = if (preferences.contains(KEY_COLOR)) preferences.getInt(KEY_COLOR, 0) else null,
        gradient = preferences.getBoolean(KEY_GRADIENT, true)
    )

    override fun save(choice: BackgroundChoice) {
        preferences.edit().apply {
            if (choice.colorArgb == null) remove(KEY_COLOR) else putInt(KEY_COLOR, choice.colorArgb)
            putBoolean(KEY_GRADIENT, choice.gradient)
        }.apply()
    }

    private companion object {
        const val PREFS_NAME = "background_prefs"
        const val KEY_COLOR = "color"
        const val KEY_GRADIENT = "gradient"
    }
}

/** ViewModel del fondo: mantiene la elección y la guarda cada vez que cambia. */
class BackgroundViewModel(private val store: BackgroundStore) : ViewModel() {

    private val _choice = MutableStateFlow(store.load())

    /** Fondo elegido, de solo lectura para la UI. */
    val choice: StateFlow<BackgroundChoice> = _choice.asStateFlow()

    /** Elige un color de fondo; el degradado se conserva como estaba. */
    fun selectColor(color: Color) = change { it.copy(colorArgb = color.toArgbInt()) }

    /** Vuelve al fondo del estilo. */
    fun useStyleDefault() = change { it.copy(colorArgb = null) }

    fun setGradient(gradient: Boolean) = change { it.copy(gradient = gradient) }

    private fun change(transform: (BackgroundChoice) -> BackgroundChoice) {
        _choice.value = transform(_choice.value)
        store.save(_choice.value)
    }
}

/** Convierte un color de Compose a su entero ARGB, sin depender de clases de Android. */
internal fun Color.toArgbInt(): Int {
    fun channel(value: Float) = (value * 255f + 0.5f).toInt().coerceIn(0, 255)
    return (channel(alpha) shl 24) or (channel(red) shl 16) or (channel(green) shl 8) or channel(blue)
}

/** Acceso al fondo elegido y a su cambio para cualquier pantalla. */
class BackgroundController(
    val choice: BackgroundChoice,
    val onSelectColor: (Color) -> Unit,
    val onUseStyleDefault: () -> Unit,
    val onGradientChange: (Boolean) -> Unit
)

/** Controlador por defecto (previews): sin fondo propio y sin efecto al cambiar. */
val LocalBackgroundController =
    compositionLocalOf { BackgroundController(BackgroundChoice(), {}, {}, {}) }
