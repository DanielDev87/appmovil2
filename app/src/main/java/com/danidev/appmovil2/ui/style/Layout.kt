package com.danidev.appmovil2.ui.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Ancho mínimo (dp) de la parte corta de la pantalla desde el que se considera una tablet. */
private const val TABLET_MIN_SMALLEST_WIDTH_DP = 600

/** Cuánto se agrandan todos los textos y medidas en una tablet. */
private const val TABLET_UI_BOOST = 1.2f

/** `true` si el dispositivo es una tablet, en cualquier orientación. */
@Composable
fun isTablet(): Boolean =
    LocalConfiguration.current.smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP

/**
 * En una tablet agranda todo el contenido un [TABLET_UI_BOOST] (textos y medidas a la vez),
 * para que no se vea pequeño en una pantalla grande. En un teléfono no cambia nada.
 */
@Composable
fun TabletScaling(content: @Composable () -> Unit) {
    val base = LocalDensity.current
    if (isTablet()) {
        CompositionLocalProvider(
            LocalDensity provides Density(base.density * TABLET_UI_BOOST, base.fontScale),
            content = content
        )
    } else {
        content()
    }
}

/** Ancho máximo del contenido en una tablet horizontal; las pantallas de la partida lo comparten. */
private val WIDE_CONTENT_WIDTH = 1100.dp

/**
 * Ancho máximo del contenido de las pantallas de la partida (juego y estadísticas): amplio en una
 * tablet horizontal y más estrecho en vertical y en teléfonos. Así el ancho no cambia al pasar de
 * una pantalla a otra.
 */
@Composable
fun gameContentMaxWidth(): Dp {
    val configuration = LocalConfiguration.current
    val wideLandscape = isTablet() && configuration.screenWidthDp > configuration.screenHeightDp
    return if (wideLandscape) WIDE_CONTENT_WIDTH else MAX_CONTENT_WIDTH
}
