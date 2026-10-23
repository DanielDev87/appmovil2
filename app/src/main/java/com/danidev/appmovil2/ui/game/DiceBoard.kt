package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceFace
import com.danidev.appmovil2.ui.dice.DicePalette
import com.danidev.appmovil2.ui.dice.DiceSize
import com.danidev.appmovil2.ui.dice.DiceType

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
    isDark: Boolean,
    canToggleLock: Boolean,
    onToggleLock: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(16.dp)
    ) {
        // HU-05: el slider cambia el Modifier.size() del dado; con 2 dados se limita
        // para que sigan cabiendo en el ancho de la pantalla.
        val diceSizeDp = DiceSize.sizeDp(values.size, sizeFactor)
        val diceSize = diceSizeDp.dp
        val dicePadding = DiceSize.paddingDp(diceSizeDp).dp

        values.forEachIndexed { index, dieVal ->
            val isLocked = index in lockedIndices
            Card(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isLocked) 4.dp else if (isDark) 6.dp else 12.dp
                ),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = if (isLocked) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else if (isDark) {
                        Color(0xFF2C183B).copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (isLocked) {
                    BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                } else if (isDark) {
                    BorderStroke(1.dp, Color(0xFFF6B5DA).copy(alpha = 0.25f))
                } else null,
                modifier = Modifier.clickable(enabled = canToggleLock) { onToggleLock(index) }
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
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
                        LockedBadge()
                    }
                }
            }
        }
    }
}

@Composable
private fun LockedBadge() {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.padding(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = stringResource(R.string.dice_locked_label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
