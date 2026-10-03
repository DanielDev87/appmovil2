package com.danidev.appmovil2.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DiceTopFace,
    secondary = DiceSideFace,
    tertiary = DiceShadow,
    background = DiceBlack,
    surface = DiceBlack,
    onPrimary = DiceBlack,
    onSecondary = DiceBlack,
    onTertiary = DiceBlack,
    onBackground = DiceWhite,
    onSurface = DiceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = DiceTopFace,
    secondary = DiceSideFace,
    tertiary = DiceBackground,
    background = DiceWhite,
    surface = DiceWhite,
    onPrimary = DiceTextPink,
    onSecondary = DiceTextPink,
    onTertiary = DiceTextPink,
    onBackground = DiceTextPink,
    onSurface = DiceTextPink,
    onSurfaceVariant = DiceTextPink.copy(alpha = 0.8f)
)

@Composable
fun Appmovil2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Desactivado para forzar los colores del dado
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
