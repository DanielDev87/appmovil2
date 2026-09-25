package com.danidev.appmovil2.ui.dice

import androidx.annotation.DrawableRes
import com.danidev.appmovil2.R

/**
 * Devuelve el recurso drawable que corresponde a la cara del dado indicada.
 *
 * Se centraliza aquí para que la pantalla principal (`MoverDados`) y el
 * historial de lanzamientos (`RollHistory`) usen exactamente la misma imagen
 * para cada valor.
 *
 * @param value Valor del dado (1..6). Cualquier otro valor muestra la cara 6.
 * @return Id del recurso drawable de la cara del dado.
 */
@DrawableRes
fun diceImageRes(value: Int): Int = when (value) {
    1 -> R.drawable.dice_1
    2 -> R.drawable.dice_2
    3 -> R.drawable.dice_3
    4 -> R.drawable.dice_4
    5 -> R.drawable.dice_5
    else -> R.drawable.dice_6
}
