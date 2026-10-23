package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.progress.PlayerProgress
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Tarjeta de la partida con lo que el jugador lleva y lo que debe superar: la racha de victorias
 * ahora, las victorias totales y el récord de puntos en una partida.
 */
@Composable
fun RecordsStrip(progress: PlayerProgress, modifier: Modifier = Modifier) {
    StyledCard(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            RecordCell(
                emoji = "🔥",
                value = progress.currentStreak.toString(),
                label = stringResource(R.string.records_streak)
            )
            RecordCell(
                emoji = "🏆",
                value = progress.wins.toString(),
                label = stringResource(R.string.progress_wins)
            )
            RecordCell(
                emoji = "🎯",
                value = if (progress.bestScore > 0) progress.bestScore.toString() else "—",
                label = stringResource(R.string.records_to_beat)
            )
        }
    }
}

@Composable
private fun RecordCell(emoji: String, value: String, label: String) {
    val tokens = LocalAppTokens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$emoji $value",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tokens.ink
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.muted
        )
    }
}
