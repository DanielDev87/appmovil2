package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceUiState
import com.danidev.appmovil2.ui.style.LocalAppTokens

/**
 * Línea de pistas bajo los dados: el contador de tiros consecutivos (HU-08), con su
 * botón para reiniciarlo, o el mensaje de fin de partida.
 */
@Composable
fun GameStatus(
    uiState: DiceUiState,
    shakeEnabled: Boolean,
    onResetRollCount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        modifier = modifier
    ) {
        val message = when (uiState.outcome) {
            GameOutcome.WON -> stringResource(R.string.game_won)
            GameOutcome.LAUNCH_LIMIT_REACHED -> stringResource(R.string.launch_limit_reached)
            GameOutcome.IN_PROGRESS -> stringResource(
                if (shakeEnabled) R.string.game_hint else R.string.game_hint_no_shake
            )
        }
        Text(
            text = message,
            fontSize = 13.sp,
            fontWeight = if (uiState.outcome == GameOutcome.IN_PROGRESS) FontWeight.Medium else FontWeight.Bold,
            color = tokens.bgMuted
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier.padding(top = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.roll_count, uiState.rollCount),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = tokens.bgInk
        )
        Text(
            text = stringResource(R.string.reset_counter),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tokens.accent,
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onResetRollCount)
                .padding(vertical = 4.dp)
        )
    }
}
