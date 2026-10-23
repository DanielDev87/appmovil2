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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceFace
import com.danidev.appmovil2.ui.dice.DicePalette
import com.danidev.appmovil2.ui.dice.DiceSize
import com.danidev.appmovil2.ui.dice.DiceType
import com.danidev.appmovil2.ui.style.LocalAppTokens

/**
 * HU-06: dibuja los dados en juego. Tocar un dado lo bloquea o desbloquea.
 *
 * @param values Valor mostrado en cada dado (cambia durante la animación).
 * @param rotation Giro de la animación de lanzamiento, en grados.
 * @param scale Escala de la animación de lanzamiento.
 */
@Composable
fun DiceBoard(
    values: List<Int>,
    lockedIndices: Set<Int>,
    diceType: DiceType,
    palette: DicePalette,
    sizeFactor: Float,
    rotation: Float,
    scale: Float,
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
            val ringShape = RoundedCornerShape(24.dp)
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .clip(ringShape)
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
                    modifier = Modifier
                        .size(diceSize)
                        .padding(dicePadding)
                        .graphicsLayer {
                            val s = if (isLocked) 1f else scale
                            scaleX = s
                            scaleY = s
                            rotationZ = if (isLocked) 0f else if (index % 2 == 0) rotation else -rotation
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
