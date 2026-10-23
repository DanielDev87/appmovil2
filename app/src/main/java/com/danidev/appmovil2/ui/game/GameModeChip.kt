package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceType
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Resumen de cómo se está jugando ("2 dados · D12"). Ocupa una sola línea y, al tocarlo, abre los
 * ajustes de la partida, donde se cambian los dados y sus caras.
 */
@Composable
fun GameModeChip(
    diceCount: Int,
    diceType: DiceType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val description = stringResource(R.string.mode_chip_description)
    StyledCard(
        color = if (tokens.isPlayful) tokens.highlight else tokens.surface,
        radius = 999.dp,
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val color = if (tokens.isPlayful) tokens.onAccent else tokens.ink
            Text(
                text = stringResource(if (diceCount == 2) R.string.mode_chip_dice_two else R.string.mode_chip_dice_one) +
                    " · " + diceType.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
