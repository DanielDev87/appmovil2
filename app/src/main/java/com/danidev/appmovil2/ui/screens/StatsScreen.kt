package com.danidev.appmovil2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.dice.LocalDicePalette
import com.danidev.appmovil2.ui.dice.RollHistoryGrid
import com.danidev.appmovil2.ui.dice.RollStatistics
import com.danidev.appmovil2.ui.dice.rememberDicePaletteViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceTypeViewModel
import com.danidev.appmovil2.ui.style.AppBackground
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.MAX_CONTENT_WIDTH
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.StyledIconButton

/**
 * Pantalla de estadísticas de la partida (HU-13): lanzamientos, racha de tiros,
 * cuántas veces ha salido cada cara y los últimos lanzamientos.
 *
 * Comparte el [DiceViewModel] de la partida, así que muestra los datos al momento.
 */
@Composable
fun StatsScreen(diceViewModel: DiceViewModel = viewModel(), onNavigateBack: () -> Unit) {
    val tokens = LocalAppTokens.current
    val uiState by diceViewModel.uiState.collectAsState()
    val diceType by rememberDiceTypeViewModel().type.collectAsState()
    val palette by rememberDicePaletteViewModel().palette.collectAsState()

    AppBackground {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight()
                .widthIn(max = MAX_CONTENT_WIDTH)
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                StyledIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.stats_back),
                    onClick = onNavigateBack
                )
                val title = stringResource(R.string.stats_title)
                Text(
                    text = if (tokens.isPlayful) title.uppercase() else title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = if (tokens.isPlayful) 1.sp else 0.sp,
                    color = tokens.bgInk,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
                Spacer(Modifier.size(44.dp))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                // Resumen: cuántos tiros van, el promedio y el mejor tiro de la partida.
                val totalRolls = uiState.faceCounts.values.sum()
                val average = if (totalRolls == 0) 0.0 else
                    uiState.faceCounts.entries.sumOf { it.key * it.value }.toDouble() / totalRolls
                val best = uiState.faceCounts.filterValues { it > 0 }.keys.maxOrNull() ?: 0
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        label = stringResource(R.string.stat_launches),
                        value = uiState.launches.toString(),
                        color = if (tokens.isPlayful) tokens.highlight else tokens.surface,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = stringResource(R.string.stats_average),
                        value = String.format(java.util.Locale.getDefault(), "%.1f", average),
                        color = if (tokens.isPlayful) tokens.mint else tokens.surface,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = stringResource(R.string.stats_best),
                        value = best.toString(),
                        color = if (tokens.isPlayful) tokens.sky else tokens.surface,
                        modifier = Modifier.weight(1f)
                    )
                }

                RollStatistics(counts = uiState.faceCounts, faces = diceType.sides)

                CompositionLocalProvider(LocalDicePalette provides palette) {
                    RollHistoryGrid(history = uiState.history)
                }
            }

            StyledButton(
                onClick = onNavigateBack,
                height = 56.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.stats_keep_playing),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.onAccent
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    val tokens = LocalAppTokens.current
    val onColor = if (tokens.isPlayful) tokens.onAccent else tokens.ink
    StyledCard(color = color, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = if (tokens.isPlayful) label.uppercase() else label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (tokens.isPlayful) tokens.onAccent else tokens.muted
            )
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = onColor
            )
        }
    }
}
