package com.danidev.appmovil2.ui.style

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.launch

/**
 * Efecto al tocar un botón o una tarjeta: se encoge un poco mientras se mantiene presionado y
 * vuelve a su tamaño al soltar.
 *
 * Reemplaza al ondulado (ripple) de Material, que dibujaba un rectángulo de color por encima de
 * las esquinas redondeadas y de las sombras de los botones.
 */
object PressIndication : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        PressNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === PressIndication

    override fun hashCode(): Int = -1
}

/** Escala del contenido cuando está presionado. */
private const val PRESSED_SCALE = 0.96f

private class PressNode(private val interactionSource: InteractionSource) :
    Modifier.Node(), DrawModifierNode {

    private val scaleAnimation = Animatable(1f)

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press ->
                        launch { scaleAnimation.animateTo(PRESSED_SCALE, tween(durationMillis = 90)) }

                    is PressInteraction.Release, is PressInteraction.Cancel ->
                        launch { scaleAnimation.animateTo(1f, tween(durationMillis = 160)) }

                    else -> Unit
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val factor = scaleAnimation.value
        scale(factor, factor) {
            this@draw.drawContent()
        }
    }
}
