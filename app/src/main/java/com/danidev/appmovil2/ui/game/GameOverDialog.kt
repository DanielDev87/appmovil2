package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.R
import com.danidev.appmovil2.TARGET_SCORE
import com.danidev.appmovil2.ui.dice.DiceUiState
import com.danidev.appmovil2.ui.style.AppBackground
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Pantalla de fin de partida: victoria (se llegó a la meta de puntos) o fin por falta
 * de lanzamientos. Muestra los números de la partida y permite jugar de nuevo, compartir
 * el resultado, volver al menú o cerrarla para ver cómo quedó la partida.
 *
 * @param playerName Nombre del jugador (HU-12); vacío si no lo escribió.
 */
@Composable
fun GameOverDialog(
    uiState: DiceUiState,
    playerName: String,
    onPlayAgain: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val won = uiState.outcome == GameOutcome.WON

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        AppBackground {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(text = if (won) "🏆" else "🎲", fontSize = 72.sp)

                Text(
                    text = stringResource(
                        when {
                            won && tokens.isPlayful -> R.string.end_win_title_playful
                            won -> R.string.end_win_title
                            else -> R.string.end_lose_title
                        }
                    ),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.bgInk,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = endMessage(won, playerName, uiState),
                    fontSize = 16.sp,
                    color = tokens.bgMuted,
                    textAlign = TextAlign.Center
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    EndStat(
                        value = uiState.score,
                        label = stringResource(R.string.end_points),
                        color = if (tokens.isPlayful) tokens.highlight else tokens.surface,
                        modifier = Modifier.weight(1f)
                    )
                    EndStat(
                        value = uiState.launches,
                        label = stringResource(R.string.end_launches),
                        color = if (tokens.isPlayful) tokens.mint else tokens.surface,
                        modifier = Modifier.weight(1f)
                    )
                    EndStat(
                        value = uiState.criticalCount,
                        label = stringResource(R.string.end_criticals),
                        color = if (tokens.isPlayful) tokens.sky else tokens.surface,
                        modifier = Modifier.weight(1f)
                    )
                }

                StyledButton(onClick = onPlayAgain, height = 60.dp, modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = stringResource(
                            if (tokens.isPlayful) R.string.end_play_again_playful else R.string.end_play_again
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.onAccent
                    )
                }

                ShareResultButton(uiState = uiState)

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    TextAction(stringResource(R.string.end_menu), onNavigateToMenu)
                    TextAction(stringResource(R.string.end_close), onDismiss)
                }
            }
        }
    }
}

@Composable
private fun endMessage(won: Boolean, playerName: String, uiState: DiceUiState): String = when {
    won && playerName.isNotEmpty() ->
        stringResource(R.string.end_win_message_named, playerName, uiState.score, uiState.launches)
    won -> stringResource(R.string.end_win_message, uiState.score, uiState.launches)
    playerName.isNotEmpty() ->
        stringResource(R.string.end_lose_message_named, playerName, uiState.score, TARGET_SCORE)
    else -> stringResource(R.string.end_lose_message, uiState.score, TARGET_SCORE)
}

@Composable
private fun EndStat(value: Int, label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    val tokens = LocalAppTokens.current
    StyledCard(color = color, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        ) {
            Text(
                text = value.toString(),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (tokens.isPlayful) tokens.onAccent else tokens.ink
            )
            Text(
                text = if (tokens.isPlayful) label.uppercase() else label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (tokens.isPlayful) tokens.onAccent else tokens.muted
            )
        }
    }
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LocalAppTokens.current.bgInk,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp)
    )
}
