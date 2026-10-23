package com.danidev.appmovil2.ui.style

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Valores de diseño de un estilo en un tema (claro u oscuro).
 *
 * Las pantallas no escriben colores ni radios a mano: los leen de [LocalAppTokens],
 * así el mismo código se ve Equilibrado o Juguetón según lo que elija el usuario.
 *
 * @property ink Color del texto principal.
 * @property muted Color del texto secundario.
 * @property outline Color del borde grueso (solo se dibuja si [borderWidth] > 0).
 * @property shadow Color de la sombra dura del estilo juguetón.
 * @property surface Fondo de las tarjetas.
 * @property accent Color principal: botón de tirar, barras de progreso, selección.
 * @property onAccent Texto sobre [accent].
 * @property soft Versión suave del acento: pistas de progreso y fondos de selección.
 * @property highlight Color de apoyo amarillo (en el estilo equilibrado repite [soft]).
 * @property mint Color de apoyo verde.
 * @property sky Color de apoyo azul.
 * @property bgTop Color de fondo (arriba, si es degradado).
 * @property bgBottom Color de fondo (abajo, si es degradado).
 * @property dots Color de los puntos decorativos del fondo; nulo si no hay.
 * @property cardRadius Radio de las tarjetas.
 * @property buttonRadius Radio de los botones grandes.
 * @property borderWidth Grosor del borde de las tarjetas.
 * @property shadowOffset Desplazamiento de la sombra dura; 0 en el estilo equilibrado.
 * @property bgInk Color del texto que va directamente sobre el fondo (no dentro de una
 * tarjeta); cambia solo cuando el usuario elige un fondo propio, para que siempre se lea.
 * @property bgMuted Versión secundaria de [bgInk].
 */
@Immutable
data class AppTokens(
    val style: AppStyle,
    val isDark: Boolean,
    val ink: Color,
    val muted: Color,
    val outline: Color,
    val shadow: Color,
    val surface: Color,
    val accent: Color,
    val onAccent: Color,
    val soft: Color,
    val highlight: Color,
    val mint: Color,
    val sky: Color,
    val bgTop: Color,
    val bgBottom: Color,
    val dots: Color?,
    val cardRadius: Dp,
    val buttonRadius: Dp,
    val borderWidth: Dp,
    val shadowOffset: Dp,
    val bgInk: Color = ink,
    val bgMuted: Color = muted
) {
    val isPlayful: Boolean get() = style == AppStyle.PLAYFUL
}

/** Construye los valores de diseño del [style] en tema claro u oscuro. */
fun appTokens(style: AppStyle, isDark: Boolean): AppTokens = when (style) {
    AppStyle.BALANCED -> if (isDark) balancedDark else balancedLight
    AppStyle.PLAYFUL -> if (isDark) playfulDark else playfulLight
}

private val balancedLight = AppTokens(
    style = AppStyle.BALANCED,
    isDark = false,
    ink = Color(0xFF3E0F2C),
    muted = Color(0xFF7A4A66),
    outline = Color(0x26D81B6A),
    shadow = Color(0xFF781E50),
    surface = Color(0xFFFFF1F8),
    accent = Color(0xFFD81B6A),
    onAccent = Color(0xFFFFFFFF),
    soft = Color(0xFFF6DCEB),
    highlight = Color(0xFFF6DCEB),
    mint = Color(0xFFF6DCEB),
    sky = Color(0xFFF6DCEB),
    bgTop = Color(0xFFFFD3EC),
    bgBottom = Color(0xFFFFFFFF),
    dots = null,
    cardRadius = 20.dp,
    buttonRadius = 22.dp,
    borderWidth = 1.dp,
    shadowOffset = 0.dp
)

private val balancedDark = AppTokens(
    style = AppStyle.BALANCED,
    isDark = true,
    ink = Color(0xFFF6E8F2),
    muted = Color(0xFFC9A6BE),
    outline = Color(0x40F6B5DA),
    shadow = Color(0xFF000000),
    surface = Color(0xFF2C183B),
    accent = Color(0xFFFF8CC8),
    onAccent = Color(0xFF2A0C30),
    soft = Color(0xFF452250),
    highlight = Color(0xFF452250),
    mint = Color(0xFF452250),
    sky = Color(0xFF452250),
    bgTop = Color(0xFF381B46),
    bgBottom = Color(0xFF13091B),
    dots = null,
    cardRadius = 20.dp,
    buttonRadius = 22.dp,
    borderWidth = 1.dp,
    shadowOffset = 0.dp
)

private val playfulLight = AppTokens(
    style = AppStyle.PLAYFUL,
    isDark = false,
    ink = Color(0xFF2B1245),
    muted = Color(0xFF5B3F7A),
    outline = Color(0xFF2B1245),
    shadow = Color(0xFF2B1245),
    surface = Color(0xFFFFFFFF),
    accent = Color(0xFFFF4FA3),
    onAccent = Color(0xFF2B1245),
    soft = Color(0xFFFFE6F2),
    highlight = Color(0xFFFFD23F),
    mint = Color(0xFF3FE0B5),
    sky = Color(0xFF5AA9FF),
    bgTop = Color(0xFFE9DBFF),
    bgBottom = Color(0xFFE9DBFF),
    dots = Color(0xFFD2B8FA),
    cardRadius = 22.dp,
    buttonRadius = 26.dp,
    borderWidth = 3.dp,
    shadowOffset = 4.dp
)

private val playfulDark = AppTokens(
    style = AppStyle.PLAYFUL,
    isDark = true,
    ink = Color(0xFFF3E8FF),
    muted = Color(0xFFBBA4DA),
    outline = Color(0xFFF3E8FF),
    shadow = Color(0xFF000000),
    surface = Color(0xFF2E1850),
    accent = Color(0xFFFF4FA3),
    onAccent = Color(0xFF2B1245),
    soft = Color(0xFF4A2A78),
    highlight = Color(0xFFFFD23F),
    mint = Color(0xFF3FE0B5),
    sky = Color(0xFF5AA9FF),
    bgTop = Color(0xFF1B0B2E),
    bgBottom = Color(0xFF1B0B2E),
    dots = Color(0xFF2A1646),
    cardRadius = 22.dp,
    buttonRadius = 26.dp,
    borderWidth = 3.dp,
    shadowOffset = 4.dp
)

/** Valores de diseño vigentes; los entrega `MainActivity` según el estilo y el tema. */
val LocalAppTokens = staticCompositionLocalOf { appTokens(AppStyle.BALANCED, isDark = false) }
