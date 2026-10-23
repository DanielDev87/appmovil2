package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.isTablet

/**
 * Escenario donde se ven los dados: una tarjeta grande con la insignia de crítico o
 * pifia (HU-09) arriba y la suma de los dados no bloqueados (HU-06) abajo.
 *
 * @param indicator Resultado del último tiro de un solo dado.
 * @param sum Suma de los dados que no están bloqueados.
 * @param content Los dados.
 */
@Composable
fun DiceStage(
    indicator: RollIndicator,
    sum: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current
    val minHeight = if (isTablet()) 360.dp else 290.dp
    StyledCard(
        radius = 32.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .padding(top = 44.dp, bottom = 52.dp)
        ) {
            content()
        }

        if (indicator != RollIndicator.NONE) {
            val critical = indicator == RollIndicator.CRITICAL
            val label = stringResource(if (critical) R.string.roll_critical else R.string.roll_fumble)
            StyledCard(
                color = if (critical) tokens.accent else tokens.sky,
                radius = 999.dp,
                tilt = if (tokens.isPlayful) 4f else 0f,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp)
            ) {
                Text(
                    text = if (critical) "★ $label" else label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (critical) tokens.onAccent else if (tokens.isPlayful) tokens.onAccent else tokens.ink,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        StyledCard(
            color = if (tokens.isPlayful) tokens.sky else tokens.surface,
            radius = 999.dp,
            tilt = if (tokens.isPlayful) -2f else 0f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.sum_pill, sum),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (tokens.isPlayful) tokens.onAccent else tokens.ink,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )
        }
    }
}
