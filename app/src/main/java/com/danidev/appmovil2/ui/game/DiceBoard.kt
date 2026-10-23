package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceFace
import com.danidev.appmovil2.ui.dice.DicePalette
import com.danidev.appmovil2.ui.dice.DiceSize
import com.danidev.appmovil2.ui.dice.DiceType
import com.danidev.appmovil2.ui.dice.DieTumble
import com.danidev.appmovil2.ui.style.LocalAppTokens
import kotlin.math.PI
import kotlin.math.sin

/** Parte del lanzamiento (de 0 a 1) en la que el dado sube y baja por primera vez. */
private const val FLIGHT_END = 0.7f

/** Parte del lanzamiento en la que el dado deja de girar; después solo termina de rebotar. */
private const val SPIN_END = 0.85f

/** Altura del dado en el aire, de 0 (en la mesa) a 1 (lo más alto): sube, cae y rebota una vez. */
internal fun throwHeight(progress: Float): Float = when {
    progress < FLIGHT_END -> sin(PI.toFloat() * progress / FLIGHT_END)
    progress < 0.9f -> 0.18f * sin(PI.toFloat() * (progress - FLIGHT_END) / 0.2f)
    else -> 0f
}

/**
 * Giro que le falta al dado para quedar en reposo, de 1 (recién lanzado) a 0: empieza girando
 * rápido y se va frenando hasta pararse.
 */
internal fun spinLeft(progress: Float): Float {
    val left = 1f - (progress / SPIN_END).coerceIn(0f, 1f)
    return left * left
}

/**
 * HU-06: dibuja los dados en juego. Tocar un dado lo bloquea o desbloquea.
 *
 * Al lanzar, cada dado sale hacia arriba girando sobre los tres ejes, cae con una sombra que
 * crece en el suelo, rebota una vez y se detiene mostrando su resultado.
 *
 * @param values Valor de cada dado (ya es el resultado final mientras gira).
 * @param throwProgress Avance del lanzamiento: 0 al lanzar y 1 con el dado quieto.
 */
@Composable
fun DiceBoard(
    values: List<Int>,
    lockedIndices: Set<Int>,
    diceType: DiceType,
    palette: DicePalette,
    sizeFactor: Float,
    throwProgress: Float,
    canToggleLock: Boolean,
    onToggleLock: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // HU-05: el factor del tamaño cambia el Modifier.size() del dado; con 2 dados se limita
        // para que sigan cabiendo en el ancho de la pantalla.
        val diceSizeDp = DiceSize.sizeDp(values.size, sizeFactor)
        val diceSize = diceSizeDp.dp
        val dicePadding = DiceSize.paddingDp(diceSizeDp).dp

        values.forEachIndexed { index, dieVal ->
            val isLocked = index in lockedIndices
            // Un dado bloqueado no se lanza: se queda quieto mientras los demás giran.
            val progress = if (isLocked) 1f else throwProgress
            val height = throwHeight(progress)
            val spin = spinLeft(progress)
            // El segundo dado gira hacia el otro lado para que no se muevan idénticos.
            val direction = if (index % 2 == 0) 1f else -1f
            val tumble = if (spin > 0f && !isLocked) {
                DieTumble(
                    yaw = direction * 1080f * spin,
                    pitch = direction * 720f * spin,
                    roll = -direction * 360f * spin
                )
            } else {
                null
            }

            val ringShape = RoundedCornerShape(24.dp)
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .drawBehind {
                        // Sombra en el suelo: se achica y se aclara cuando el dado está en el aire.
                        val shadowWidth = size.width * (0.62f - 0.26f * height)
                        val shadowHeight = size.height * (0.10f - 0.04f * height)
                        drawOval(
                            color = Color.Black.copy(alpha = 0.30f - 0.18f * height),
                            topLeft = Offset((size.width - shadowWidth) / 2f, size.height * 0.92f),
                            size = Size(shadowWidth, shadowHeight)
                        )
                    }
                    .then(
                        if (isLocked) Modifier.border(3.dp, tokens.accent, ringShape) else Modifier
                    )
                    .clickable(enabled = canToggleLock, role = Role.Button) { onToggleLock(index) }
            ) {
                // HU-07: con D6 se dibuja con puntos; con otros dados, en número estilizado.
                DiceFace(
                    value = dieVal,
                    sides = diceType.sides,
                    palette = palette,
                    contentDescription = if (isLocked) {
                        stringResource(R.string.dice_locked_description, dieVal)
                    } else {
                        dieVal.toString()
                    },
                    tumble = tumble,
                    modifier = Modifier
                        .size(diceSize)
                        .padding(dicePadding)
                        .graphicsLayer {
                            // Sube hasta casi un dado de altura y se acerca un poco a la cámara.
                            translationY = -height * size.height * 0.9f
                            val zoom = 1f + 0.12f * height
                            scaleX = zoom
                            scaleY = zoom
                        }
                )
                if (isLocked) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(tokens.accent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.dice_locked_label),
                            tint = tokens.onAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
