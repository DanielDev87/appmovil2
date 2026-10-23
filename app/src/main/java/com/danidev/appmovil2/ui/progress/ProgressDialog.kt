package com.danidev.appmovil2.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.DialogProperties
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/** Ancho a partir del cual los logros se muestran en dos columnas. */
private val TWO_COLUMNS_MIN_WIDTH = 560.dp

/**
 * Ventana "Mis logros", ordenada en tres bloques:
 * 1. Un resumen con cuántos logros lleva y una barra de avance.
 * 2. Los números del jugador (partidas, victorias, rachas, críticos, victoria más rápida y récord).
 * 3. La lista de logros, en una o dos columnas; los que faltan van en gris, con candado y, si se
 *    miden con un contador, con su avance ("3 de 5").
 */
@Composable
fun ProgressDialog(progress: PlayerProgress, onDismiss: () -> Unit) {
    val tokens = LocalAppTokens.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        StyledCard(
            radius = 28.dp,
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 760.dp)
                .fillMaxWidth()
        ) {
            BoxWithConstraints {
                val twoColumns = maxWidth >= TWO_COLUMNS_MIN_WIDTH
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 680.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Header(progress)
                    StatsPanel(progress, wide = twoColumns)

                    Text(
                        text = stringResource(R.string.progress_list_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.muted
                    )
                    val columns = if (twoColumns) 2 else 1
                    Achievement.entries.chunked(columns).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            row.forEach { achievement ->
                                AchievementTile(
                                    achievement = achievement,
                                    progress = progress,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                            // Completa la fila para que las tarjetas mantengan el mismo ancho.
                            repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }

                    Text(
                        text = stringResource(R.string.progress_close),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.ink,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable(role = Role.Button, onClick = onDismiss)
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/** Título con el trofeo y la barra de cuántos logros se han desbloqueado. */
@Composable
private fun Header(progress: PlayerProgress) {
    val tokens = LocalAppTokens.current
    val unlocked = progress.unlocked.size
    val total = Achievement.entries.size
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StyledCard(color = tokens.accent, radius = 16.dp, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                    Text(text = "🏆", fontSize = 26.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.progress_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.ink
                )
                Text(
                    text = stringResource(R.string.progress_achievements, unlocked, total),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.muted
                )
            }
        }
        ProgressBar(fraction = unlocked.toFloat() / total, height = 10)
    }
}

/** Los números del jugador en una sola tarjeta, en tres filas separadas por una línea fina. */
@Composable
private fun StatsPanel(progress: PlayerProgress, wide: Boolean) {
    val tokens = LocalAppTokens.current
    val dash = stringResource(R.string.progress_best_win_none)
    val games = Stat("🎲", progress.gamesPlayed.toString(), stringResource(R.string.progress_games))
    val wins = Stat("🏆", progress.wins.toString(), stringResource(R.string.progress_wins))
    val criticals = Stat("🎯", progress.totalCriticals.toString(), stringResource(R.string.progress_criticals))
    val streak = Stat("🔥", progress.currentStreak.toString(), stringResource(R.string.progress_streak))
    val bestStreak = Stat("⭐", progress.bestStreak.toString(), stringResource(R.string.progress_best_streak))
    val fastest = Stat(
        "⚡",
        if (progress.bestWinLaunches > 0) {
            stringResource(R.string.progress_best_win_value, progress.bestWinLaunches)
        } else {
            dash
        },
        stringResource(R.string.progress_best_win),
        compact = true
    )
    val bestScore = Stat(
        "📈",
        if (progress.bestScore > 0) progress.bestScore.toString() else dash,
        stringResource(R.string.progress_best_score),
        compact = true
    )
    StyledCard(color = tokens.soft, radius = 20.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (wide) {
                // Pantalla ancha: cuatro números arriba y tres abajo, para que el bloque sea bajo.
                StatsRow(games, wins, criticals, streak)
                Divider()
                StatsRow(bestStreak, fastest, bestScore)
            } else {
                StatsRow(games, wins, criticals)
                Divider()
                StatsRow(streak, bestStreak)
                Divider()
                StatsRow(fastest, bestScore)
            }
        }
    }
}

private class Stat(val emoji: String, val value: String, val label: String, val compact: Boolean = false)

@Composable
private fun StatsRow(vararg stats: Stat) {
    val tokens = LocalAppTokens.current
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        stats.forEach { stat ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(text = stat.emoji, fontSize = 16.sp)
                Text(
                    text = stat.value,
                    fontSize = if (stat.compact) 18.sp else 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.ink
                )
                Text(
                    text = stat.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.muted
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalAppTokens.current.muted.copy(alpha = 0.18f))
    )
}

/** Barra de avance con el color de acento sobre un fondo suave. */
@Composable
private fun ProgressBar(fraction: Float, height: Int) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(tokens.soft, RoundedCornerShape(height.dp))
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .background(tokens.accent, RoundedCornerShape(height.dp))
            )
        }
    }
}

/**
 * Tarjeta de un logro: ícono, nombre y qué hay que hacer. Desbloqueado va a todo color; si falta,
 * va atenuado con un candado y, si tiene contador, con su avance.
 */
@Composable
private fun AchievementTile(achievement: Achievement, progress: PlayerProgress, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    val unlocked = achievement.id in progress.unlocked
    StyledCard(
        color = if (unlocked) tokens.soft else tokens.surface,
        radius = 18.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (unlocked) 1f else 0.6f)
                .padding(12.dp)
        ) {
            StyledCard(
                color = if (unlocked) tokens.accent else tokens.soft,
                radius = 14.dp,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                    Text(text = if (unlocked) achievement.emoji else "🔒", fontSize = 22.sp)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(achievement.titleRes),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.ink
                )
                Text(
                    text = stringResource(achievement.descriptionRes),
                    fontSize = 12.sp,
                    color = tokens.muted
                )
                val goal = achievement.goal
                if (goal != null && !unlocked) {
                    Spacer(Modifier.height(2.dp))
                    ProgressBar(fraction = achievement.fractionOf(progress), height = 6)
                    Text(
                        text = stringResource(
                            R.string.progress_of,
                            goal.current(progress).coerceAtMost(goal.target),
                            goal.target
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.muted
                    )
                }
            }
        }
    }
}
