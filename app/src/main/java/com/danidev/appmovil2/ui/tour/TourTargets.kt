package com.danidev.appmovil2.ui.tour

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/** Elementos de la app que el tour puede señalar. */
enum class TourTargetId {
    THEME_SWITCH,
    NAME_FIELD,
    WELCOME_PROGRESS,
    WELCOME_SETTINGS,
    PLAY_BUTTON,
    SCORE_CARDS,
    INFO_PANEL,
    DICE_STAGE,
    ROLL_BUTTON,
    RECENT_ROLLS,
    TOP_SOUND,
    TOP_RESET,
    TOP_SETTINGS
}

/**
 * Dónde está en pantalla cada elemento que el tour puede señalar.
 *
 * Cada pantalla marca sus botones con [tourTarget] y aquí queda su posición, siempre actualizada; si
 * una pantalla no está en pantalla, sus elementos no aparecen. El tour usa estas posiciones para
 * dibujar el resplandor.
 */
class TourTargets {
    /** Posición de cada elemento marcado, en el sistema de coordenadas de la raíz de la app. */
    val bounds = mutableStateMapOf<TourTargetId, Rect>()

    /** Se invoca cuando el usuario toca un elemento marcado (sin impedir que el toque funcione). */
    var onTouched: (TourTargetId) -> Unit = {}
}

/** Posiciones de los elementos del tour para cualquier pantalla; por defecto, un registro vacío. */
val LocalTourTargets = staticCompositionLocalOf { TourTargets() }

/**
 * Marca este elemento como algo que el tour puede señalar: guarda su posición y avisa cuando se toca.
 *
 * El aviso del toque se hace sin consumirlo, así el botón sigue funcionando como siempre.
 */
@Composable
fun Modifier.tourTarget(id: TourTargetId): Modifier {
    val targets = LocalTourTargets.current
    DisposableEffect(id, targets) {
        onDispose { targets.bounds.remove(id) }
    }
    return this
        .onGloballyPositioned { coordinates -> targets.bounds[id] = coordinates.boundsInRoot() }
        .pointerInput(id, targets) {
            awaitEachGesture {
                // Se mira el toque antes que nadie (pasada inicial) y no se consume.
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                if (waitForUpOrCancellation(pass = PointerEventPass.Initial) != null) {
                    targets.onTouched(id)
                }
            }
        }
}
