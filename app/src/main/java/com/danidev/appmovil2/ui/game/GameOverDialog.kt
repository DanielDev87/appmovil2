package com.danidev.appmovil2.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.danidev.appmovil2.ui.dice.DiceUiState
import com.danidev.appmovil2.ui.progress.GameRecord
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.style.StyledCard
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Pausas (ms) entre los pasos de la entrada: cada elemento aparece uno tras otro. */
private const val STEP_TROPHY = 1
private const val STEP_TITLE = 2
private const val STEP_STATS = 3
private const val STEP_BUTTONS = 4
private val STEP_DELAYS_MS = longArrayOf(150, 300, 250, 500)

/**
 * Pantalla de fin de partida: victoria (se llegó a la meta de puntos) o fin por falta
 * de lanzamientos.
 *
 * No reemplaza la partida de golpe: oscurece la pantalla poco a poco y una tarjeta
 * aparece con una animación escalonada (trofeo, título, mensaje, números que suben
 * hasta su valor y, por último, los botones). Si se ganó, cae confeti.
 *
 * @param playerName Nombre del jugador (HU-12); vacío si no lo escribió.
 */
@Composable
fun GameOverDialog(
    uiState: DiceUiState,
    playerName: String,
    names: List<String>,
    record: GameRecord?,
    onPlayAgain: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val multi = uiState.isMultiplayer
    // Con varios jugadores siempre hay quien gane, así que se celebra siempre.
    val won = multi || uiState.outcome == GameOutcome.WON

    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        STEP_DELAYS_MS.forEach { pause ->
            kotlinx.coroutines.delay(pause)
            step++
        }
    }

    val scrim by animateFloatAsState(targetValue = 0.6f, animationSpec = tween(600), label = "scrim")
    val cardScale by animateFloatAsState(
        targetValue = if (step >= 1) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (step >= 1) 1f else 0f,
        animationSpec = tween(350),
        label = "cardAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrim))
        ) {
            StyledCard(
                radius = 32.dp,
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp)
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .scale(cardScale)
                    .alpha(cardAlpha)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Trophy(won = won, visible = step >= STEP_TROPHY)

                    AnimatedVisibility(
                        visible = step >= STEP_TITLE,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (multi) {
                                    multiTitle(uiState, names)
                                } else stringResource(
                                    when {
                                        won && tokens.isPlayful -> R.string.end_win_title_playful
                                        won -> R.string.end_win_title
                                        tokens.isPlayful -> R.string.end_lose_title_playful
                                        else -> R.string.end_lose_title
                                    }
                                ),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = tokens.ink,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (multi) multiMessage(uiState, names) else endMessage(won, playerName, uiState),
                                fontSize = 15.sp,
                                color = tokens.muted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = step >= STEP_STATS,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
                    ) {
                        if (multi) {
                            Scoreboard(uiState = uiState, names = names)
                        } else Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            EndStat(
                                value = uiState.score,
                                label = stringResource(R.string.end_points),
                                color = if (tokens.isPlayful) tokens.highlight else tokens.soft,
                                modifier = Modifier.weight(1f)
                            )
                            EndStat(
                                value = uiState.launches,
                                label = stringResource(R.string.end_launches),
                                color = if (tokens.isPlayful) tokens.mint else tokens.soft,
                                modifier = Modifier.weight(1f)
                            )
                            EndStat(
                                value = uiState.criticalCount,
                                label = stringResource(R.string.end_criticals),
                                color = if (tokens.isPlayful) tokens.sky else tokens.soft,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = step >= STEP_BUTTONS && record != null,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
                    ) {
                        if (record != null) Highlights(record)
                    }

                    AnimatedVisibility(
                        visible = step >= STEP_BUTTONS,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StyledButton(onClick = onPlayAgain, height = 56.dp) {
                                Text(
                                    text = stringResource(
                                        if (tokens.isPlayful) R.string.end_play_again_playful else R.string.end_play_again
                                    ),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tokens.onAccent
                                )
                            }
                            ShareResultButton(uiState = uiState, modifier = Modifier, names = names)
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                TextAction(stringResource(R.string.end_menu), onNavigateToMenu)
                                TextAction(stringResource(R.string.end_close), onDismiss)
                            }
                        }
                    }
                }
            }

            if (won) Confetti() else Rain()
        }
    }
}

/**
 * Novedades de la partida que acaba de terminar: la racha de victorias, los récords nuevos y los
 * logros desbloqueados.
 */
@Composable
private fun Highlights(record: GameRecord) {
    val tokens = LocalAppTokens.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (record.progress.currentStreak >= 2) {
            Highlight(stringResource(R.string.end_streak, record.progress.currentStreak))
        }
        if (record.newBestWin) Highlight("⚡ " + stringResource(R.string.end_new_best_win))
        if (record.newBestScore) Highlight("⭐ " + stringResource(R.string.end_new_best_score))
        record.newAchievements.forEach { achievement ->
            StyledCard(color = tokens.soft, radius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = stringResource(R.string.end_new_achievement).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.accent
                    )
                    Text(
                        text = achievement.emoji + " " + stringResource(achievement.titleRes),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.ink
                    )
                    Text(
                        text = stringResource(achievement.descriptionRes),
                        fontSize = 12.sp,
                        color = tokens.muted
                    )
                }
            }
        }
    }
}

@Composable
private fun Highlight(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LocalAppTokens.current.ink
    )
}

/**
 * Trofeo que entra rebotando y se balancea si se ganó; si se perdió, una cara triste que cae
 * desde arriba, rebota y queda ladeada.
 */
@Composable
private fun Trophy(won: Boolean, visible: Boolean) {
    val bounce by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow),
        label = "trophyBounce"
    )
    val fallOffset by animateFloatAsState(
        targetValue = if (visible) 0f else -140f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "dieFall"
    )
    val wobble = remember { Animatable(0f) }
    LaunchedEffect(visible, won) {
        if (!visible) return@LaunchedEffect
        if (won) {
            repeat(2) {
                wobble.animateTo(8f, tween(180))
                wobble.animateTo(-8f, tween(180))
            }
            wobble.animateTo(0f, tween(180))
        } else {
            wobble.animateTo(-18f, tween(400))
        }
    }
    Text(
        text = if (won) "🏆" else "😢",
        fontSize = 64.sp,
        modifier = if (won) {
            Modifier.scale(bounce).rotate(wobble.value)
        } else {
            Modifier.offset(y = fallOffset.dp).alpha(if (visible) 1f else 0f).rotate(wobble.value)
        }
    )
}

/** Cuadro con un número que sube desde 0 hasta su valor y una etiqueta debajo. */
@Composable
private fun EndStat(value: Int, label: String, color: Color, modifier: Modifier) {
    val tokens = LocalAppTokens.current
    val shown by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 900),
        label = "statCount"
    )
    val onColor = if (tokens.isPlayful) tokens.onAccent else tokens.ink
    StyledCard(color = color, radius = 18.dp, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = shown.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = onColor
            )
            Text(
                text = if (tokens.isPlayful) label.uppercase() else label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (tokens.isPlayful) tokens.onAccent else tokens.muted
            )
        }
    }
}

/** Una pieza de confeti: dónde empieza, cómo cae y cómo gira. */
private class ConfettiPiece(
    val x: Float,
    val speed: Float,
    val size: Float,
    val sway: Float,
    val phase: Float,
    val spin: Float,
    val color: Color
)

/**
 * Confeti que cae una sola vez por toda la pantalla, con los colores del estilo vigente.
 * Solo dibuja; no recibe toques.
 */
@Composable
private fun Confetti() {
    val tokens = LocalAppTokens.current
    val palette = listOf(tokens.accent, tokens.highlight, tokens.mint, tokens.sky, Color.White)
    val pieces = remember(palette) {
        val random = Random(7)
        List(70) {
            ConfettiPiece(
                x = random.nextFloat(),
                speed = 0.7f + random.nextFloat() * 0.6f,
                size = 7f + random.nextFloat() * 8f,
                sway = 0.02f + random.nextFloat() * 0.04f,
                phase = random.nextFloat() * 2f * PI.toFloat(),
                spin = 180f + random.nextFloat() * 540f,
                color = palette[random.nextInt(palette.size)]
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 3600, easing = LinearEasing))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        pieces.forEach { piece ->
            val y = (-0.08f + t * piece.speed * 1.25f) * size.height
            if (y < -piece.size || y > size.height + piece.size) return@forEach
            val x = (piece.x + piece.sway * sin(t * 9f + piece.phase)) * size.width
            val side = piece.size.dp.toPx()
            rotate(degrees = piece.spin * t, pivot = Offset(x, y)) {
                drawRect(
                    color = piece.color,
                    topLeft = Offset(x - side / 2, y - side / 4),
                    size = Size(side, side / 2)
                )
            }
        }
    }
}

/** Gota de la lluvia de la pantalla de derrota. */
private class RainDrop(val x: Float, val speed: Float, val length: Float, val phase: Float)

/**
 * Lluvia suave que cae una sola vez por la pantalla cuando se pierde: gotas finas y lentas, lo
 * contrario del confeti. Solo dibuja; no recibe toques.
 */
@Composable
private fun Rain() {
    val tokens = LocalAppTokens.current
    val color = tokens.sky.copy(alpha = 0.55f)
    val drops = remember {
        val random = Random(11)
        List(45) {
            RainDrop(
                x = random.nextFloat(),
                speed = 0.8f + random.nextFloat() * 0.5f,
                length = 14f + random.nextFloat() * 14f,
                phase = random.nextFloat()
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 3800, easing = LinearEasing))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        drops.forEach { drop ->
            val y = ((t * drop.speed * 1.4f + drop.phase * 0.3f) % 1.3f - 0.15f) * size.height
            val x = drop.x * size.width
            val len = drop.length.dp.toPx()
            drawLine(
                color = color,
                start = Offset(x, y),
                end = Offset(x - len * 0.15f, y + len),
                strokeWidth = 2.5.dp.toPx()
            )
        }
    }
}

/** Título de la pantalla de fin con varios jugadores: quién ganó, o empate. */
@Composable
private fun multiTitle(uiState: DiceUiState, names: List<String>): String {
    val winners = uiState.winners
    return if (winners.size == 1) {
        stringResource(R.string.end_multi_winner, names.getOrElse(winners.first()) { "" })
    } else {
        stringResource(R.string.end_multi_tie)
    }
}

/** Mensaje bajo el título con varios jugadores: cómo se ganó. */
@Composable
private fun multiMessage(uiState: DiceUiState, names: List<String>): String {
    val winners = uiState.winners
    val points = winners.firstOrNull()?.let { uiState.players[it].score } ?: 0
    val winnerNames = winners.joinToString(" y ") { names.getOrElse(it) { "" } }
    return when {
        winners.size > 1 -> stringResource(R.string.end_multi_tie_message, winnerNames, points)
        uiState.outcome == GameOutcome.WON ->
            stringResource(R.string.end_multi_reached, winnerNames, uiState.targetScore)
        else -> stringResource(R.string.end_multi_most, winnerNames, points)
    }
}

/**
 * Tabla de posiciones: los jugadores de más a menos puntos, con un trofeo para quien ganó. Cada
 * fila entra con un pequeño retraso, una tras otra.
 */
@Composable
private fun Scoreboard(uiState: DiceUiState, names: List<String>) {
    val tokens = LocalAppTokens.current
    val ranking = uiState.players.indices.sortedByDescending { uiState.players[it].score }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ranking.forEachIndexed { place, index ->
            val isWinner = index in uiState.winners
            StyledCard(
                color = if (isWinner) tokens.accent else tokens.soft,
                radius = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (isWinner) "🏆" else "${place + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isWinner) tokens.onAccent else tokens.muted,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = names.getOrElse(index) { "" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isWinner) tokens.onAccent else tokens.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.end_multi_points, uiState.players[index].score),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isWinner) tokens.onAccent else tokens.ink
                    )
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
        stringResource(R.string.end_lose_message_named, playerName, uiState.score, uiState.targetScore)
    else -> stringResource(R.string.end_lose_message, uiState.score, uiState.targetScore)
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LocalAppTokens.current.ink,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp)
    )
}
