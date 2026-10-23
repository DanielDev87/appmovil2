package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceUiState
import com.danidev.appmovil2.ui.style.LocalAppTokens

/**
 * Línea bajo los dados: el contador de tiros consecutivos (HU-08) o, al terminar la partida,
 * el mensaje de fin. El contador se reinicia al empezar otra partida.
 */
@Composable
fun GameStatus(uiState: DiceUiState, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    val text = when (uiState.outcome) {
        GameOutcome.WON -> stringResource(R.string.game_won)
        GameOutcome.LAUNCH_LIMIT_REACHED -> stringResource(R.string.launch_limit_reached)
        GameOutcome.IN_PROGRESS -> stringResource(R.string.roll_count, uiState.rollCount)
    }
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = tokens.bgMuted,
        modifier = modifier
    )
}
