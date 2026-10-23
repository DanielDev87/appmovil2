package com.danidev.appmovil2.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Ventana con los récords del jugador (partidas, victorias, rachas, mejor victoria y puntos) y la
 * lista de logros, con los que faltan en gris.
 */
@Composable
fun ProgressDialog(progress: PlayerProgress, onDismiss: () -> Unit) {
    val tokens = LocalAppTokens.current
    Dialog(onDismissRequest = onDismiss) {
        StyledCard(radius = 28.dp, modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.progress_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.ink
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Stat(stringResource(R.string.progress_games), progress.gamesPlayed.toString(), Modifier.weight(1f))
                    Stat(stringResource(R.string.progress_wins), progress.wins.toString(), Modifier.weight(1f))
                    Stat(stringResource(R.string.progress_criticals), progress.totalCriticals.toString(), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Stat("🔥 " + stringResource(R.string.progress_streak), progress.currentStreak.toString(), Modifier.weight(1f))
                    Stat(stringResource(R.string.progress_best_streak), progress.bestStreak.toString(), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Stat(
                        stringResource(R.string.progress_best_win),
                        if (progress.bestWinLaunches > 0) {
                            stringResource(R.string.progress_best_win_value, progress.bestWinLaunches)
                        } else {
                            stringResource(R.string.progress_best_win_none)
                        },
                        Modifier.weight(1f),
                        small = true
                    )
                    Stat(
                        stringResource(R.string.progress_best_score),
                        progress.bestScore.toString(),
                        Modifier.weight(1f),
                        small = true
                    )
                }

                Text(
                    text = stringResource(
                        R.string.progress_achievements,
                        progress.unlocked.size,
                        Achievement.entries.size
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.muted
                )
                Achievement.entries.forEach { achievement ->
                    AchievementRow(achievement, unlocked = achievement.id in progress.unlocked)
                }

                Text(
                    text = stringResource(R.string.progress_close),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.ink,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier, small: Boolean = false) {
    val tokens = LocalAppTokens.current
    StyledCard(color = tokens.soft, radius = 16.dp, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp)
        ) {
            Text(
                text = value,
                fontSize = if (small) 17.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tokens.ink
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.muted
            )
        }
    }
}

/** Fila de un logro: su ícono, el nombre y lo que hay que hacer; en gris si aún no se logra. */
@Composable
internal fun AchievementRow(achievement: Achievement, unlocked: Boolean, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (unlocked) 1f else 0.45f)
    ) {
        StyledCard(
            color = if (unlocked) tokens.accent else tokens.soft,
            radius = 14.dp,
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(46.dp)) {
                Text(text = if (unlocked) achievement.emoji else "🔒", fontSize = 22.sp)
            }
        }
        Column {
            Text(
                text = stringResource(achievement.titleRes),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tokens.ink
            )
            Text(
                text = stringResource(achievement.descriptionRes),
                fontSize = 13.sp,
                color = tokens.muted
            )
        }
    }
}
