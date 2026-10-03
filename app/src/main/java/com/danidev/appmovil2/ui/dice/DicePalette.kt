package com.danidev.appmovil2.ui.dice

import androidx.annotation.StringRes
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.danidev.appmovil2.R
import kotlin.math.cos
import kotlin.math.sin

/*
 * HU-04: Selección de color de dados.
 *
 * "Como usuario, quiero elegir entre 3 paletas de colores para el dado, para
 * personalizar la apariencia visual."
 */

/**
 * Paletas de color disponibles para el dado (HU-04).
 *
 * Los dibujos del dado (`dice_1` a `dice_6`) están pintados en lila. En vez de
 * duplicarlos por cada paleta, cada una desplaza el matiz de ese lila con un
 * [ColorFilter]: conserva el sombreado y los puntos negros, y cualquier dado
 * nuevo que se agregue cambia de color sin crear imágenes adicionales.
 *
 * @property labelRes Nombre que se muestra en el selector.
 * @property swatch Color de muestra que se dibuja junto al nombre.
 * @property colorFilter Filtro que se aplica al dado; `null` para [LILA], que
 * es el color original de las imágenes.
 */
enum class DicePalette(
    @StringRes val labelRes: Int,
    val swatch: Color,
    hueDegrees: Float,
    saturation: Float
) {
    LILA(R.string.dice_palette_lila, Color(0xFFE1BEE7), hueDegrees = 0f, saturation = 1f),
    ROSA(R.string.dice_palette_rosa, Color(0xFFF48FB1), hueDegrees = 45f, saturation = 1.5f),
    MENTA(R.string.dice_palette_menta, Color(0xFF80CBC4), hueDegrees = -130f, saturation = 1.5f);

    // `lazy`: crear un ColorFilter requiere clases de Android, así que solo se hace al usarlo.
    val colorFilter: ColorFilter? by lazy {
        if (hueDegrees == 0f && saturation == 1f) {
            null
        } else {
            ColorFilter.colorMatrix(hueSaturationMatrix(hueDegrees, saturation))
        }
    }
}

/**
 * Paleta que usan los dados del árbol de Composables actual.
 *
 * Se entrega mediante un `CompositionLocal` para que el dado de la pantalla
 * principal y el historial (`RollHistory`) usen la misma paleta sin cambiar la
 * firma de ninguno de los dos.
 */
val LocalDicePalette = compositionLocalOf { DicePalette.LILA }

/**
 * Matriz de color que rota el matiz y ajusta la saturación.
 *
 * La rotación conserva la luminosidad (el negro y el gris no cambian) y usa los
 * coeficientes estándar de `feColorMatrix hueRotate`.
 */
private fun hueSaturationMatrix(hueDegrees: Float, saturation: Float): ColorMatrix {
    val radians = Math.toRadians(hueDegrees.toDouble())
    val c = cos(radians).toFloat()
    val s = sin(radians).toFloat()
    val hue = ColorMatrix(
        floatArrayOf(
            0.213f + c * 0.787f - s * 0.213f, 0.715f - c * 0.715f - s * 0.715f, 0.072f - c * 0.072f + s * 0.928f, 0f, 0f,
            0.213f - c * 0.213f + s * 0.143f, 0.715f + c * 0.285f + s * 0.140f, 0.072f - c * 0.072f - s * 0.283f, 0f, 0f,
            0.213f - c * 0.213f - s * 0.787f, 0.715f - c * 0.715f + s * 0.715f, 0.072f + c * 0.928f + s * 0.072f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
    val saturate = ColorMatrix().apply { setToSaturation(saturation) }
    hue *= saturate
    return hue
}
