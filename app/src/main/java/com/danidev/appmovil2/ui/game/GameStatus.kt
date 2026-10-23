package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.MAX_LAUNCHES
import com.danidev.appmovil2.R
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.TARGET_SCORE
import com.danidev.appmovil2.ui.dice.DiceUiState

/**
 * Marcadores de la partida: puntos, lanzamientos, tiros consecutivos (HU-08),
 * resultado final e indicador de crítico o pifia (HU-09).
 */
@Composable
fun GameStatus(uiState: DiceUiState, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(stringResource(R.string.score, uiState.score, TARGET_SCORE))
        Text(stringResource(R.string.launches, uiState.launches, MAX_LAUNCHES))
        Text(stringResource(R.string.roll_count, uiState.rollCount))
        when (uiState.outcome) {
            GameOutcome.WON -> Text(stringResource(R.string.game_won))
            GameOutcome.LAUNCH_LIMIT_REACHED -> Text(stringResource(R.string.launch_limit_reached))
            GameOutcome.IN_PROGRESS -> Unit
        }
        when (uiState.rollIndicator) {
            RollIndicator.CRITICAL -> Text(
                text = "🎯 ¡CRÍTICO!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            RollIndicator.FUMBLE -> Text(
                text = "💥 ¡PIFIA!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            RollIndicator.NONE -> Unit
        }
    }
}
