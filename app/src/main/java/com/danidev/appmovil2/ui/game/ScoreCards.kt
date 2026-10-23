package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceUiState
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/** Tarjetas de puntos y de lanzamientos de la partida, con su barra de progreso. */
@Composable
fun ScoreCards(uiState: DiceUiState, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        ScoreCard(
            label = stringResource(R.string.stat_points),
            value = uiState.score,
            max = uiState.targetScore,
            color = if (tokens.isPlayful) tokens.highlight else tokens.surface,
            tilt = -1f,
            modifier = Modifier.weight(1f)
        )
        ScoreCard(
            label = stringResource(R.string.stat_launches),
            value = uiState.launches,
            max = uiState.maxLaunches,
            color = if (tokens.isPlayful) tokens.mint else tokens.surface,
            tilt = 1f,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreCard(
    label: String,
    value: Int,
    max: Int,
    color: Color,
    tilt: Float,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    // En el estilo juguetón las tarjetas son de color y el texto va en tinta oscura sobre ellas.
    val textColor = if (tokens.isPlayful) Color(0xFF2B1245) else tokens.ink
    val mutedColor = if (tokens.isPlayful) Color(0xFF2B1245) else tokens.muted
    StyledCard(color = color, tilt = tilt, modifier = modifier) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp)) {
            Text(
                text = if (tokens.isPlayful) label.uppercase() else label,
                fontSize = 13.sp,
                fontWeight = if (tokens.isPlayful) FontWeight.ExtraBold else FontWeight.Medium,
                color = mutedColor
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                Text(
                    text = value.toString(),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                Text(
                    text = " " + stringResource(R.string.stat_of, max),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = mutedColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            ProgressBar(fraction = value.toFloat() / max)
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float) {
    val tokens = LocalAppTokens.current
    val track = if (tokens.isPlayful) Color.White else tokens.soft
    val fill = if (tokens.isPlayful) tokens.accent else tokens.accent
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .height(if (tokens.isPlayful) 12.dp else 6.dp)
            .background(track, RoundedCornerShape(6.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .background(fill, RoundedCornerShape(6.dp))
        )
    }
}
