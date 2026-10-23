package com.danidev.appmovil2.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.ui.dice.PlayerStats
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Marcador de la partida con varios jugadores: una casilla por jugador con su nombre y sus
 * puntos. La del jugador al que le toca tirar se resalta con el color de acento.
 *
 * @param names Nombre de cada jugador, en el mismo orden que [players].
 */
@Composable
fun PlayersBar(
    players: List<PlayerStats>,
    names: List<String>,
    currentPlayer: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        players.forEachIndexed { index, player ->
            PlayerChip(
                name = names.getOrElse(index) { "" },
                score = player.score,
                active = index == currentPlayer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlayerChip(name: String, score: Int, active: Boolean, modifier: Modifier) {
    val tokens = LocalAppTokens.current
    val background by animateColorAsState(
        targetValue = if (active) tokens.accent else tokens.surface,
        animationSpec = tween(250),
        label = "playerChipColor"
    )
    val onBackground = if (active) tokens.onAccent else tokens.ink
    val onBackgroundMuted = if (active) tokens.onAccent else tokens.muted
    StyledCard(
        color = background,
        radius = 16.dp,
        modifier = modifier.semantics { contentDescription = "$name: $score" }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = onBackgroundMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = score.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = onBackground
            )
        }
    }
}
