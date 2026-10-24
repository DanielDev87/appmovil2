package com.danidev.appmovil2.ui.tour

import androidx.compose.ui.geometry.Rect

/** Posición de la esquina superior izquierda de la explicación del tour, en píxeles. */
data class TooltipPosition(val x: Float, val y: Float)

/**
 * Decide dónde poner la tarjeta de explicación del tour para que no tape lo que señala.
 *
 * - Sin elemento señalado ([target] es `null`), va centrada en la pantalla.
 * - Si cabe debajo del elemento, va debajo; si no, encima.
 * - Si el elemento es tan grande que no cabe ni arriba ni abajo, va pegada al borde donde haya más
 *   espacio, aunque tape una parte.
 * - Horizontalmente se centra en el elemento sin salirse de la pantalla.
 *
 * Todas las medidas están en píxeles.
 *
 * @param gap Espacio entre el elemento y la tarjeta.
 * @param margin Distancia mínima a los bordes de la pantalla.
 */
fun tooltipPosition(
    target: Rect?,
    screenWidth: Float,
    screenHeight: Float,
    cardWidth: Float,
    cardHeight: Float,
    gap: Float,
    margin: Float
): TooltipPosition {
    val minX = margin
    val maxX = (screenWidth - cardWidth - margin).coerceAtLeast(minX)
    val minY = margin
    val maxY = (screenHeight - cardHeight - margin).coerceAtLeast(minY)

    if (target == null) {
        return TooltipPosition(
            x = ((screenWidth - cardWidth) / 2f).coerceIn(minX, maxX),
            y = ((screenHeight - cardHeight) / 2f).coerceIn(minY, maxY)
        )
    }

    val x = (target.center.x - cardWidth / 2f).coerceIn(minX, maxX)
    val spaceBelow = screenHeight - target.bottom - margin
    val spaceAbove = target.top - margin
    val needed = cardHeight + gap
    val y = when {
        needed <= spaceBelow -> target.bottom + gap
        needed <= spaceAbove -> target.top - gap - cardHeight
        spaceBelow >= spaceAbove -> maxY
        else -> minY
    }
    return TooltipPosition(x = x, y = y.coerceIn(minY, maxY))
}
