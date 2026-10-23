package com.danidev.appmovil2.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.isTablet

/**
 * Escenario donde se ven los dados, sin fondo propio: con el aviso de crítico o pifia (HU-09) arriba
 * y la suma de los dados no bloqueados (HU-06) abajo.
 *
 * El aviso entra con un rebote y late mientras dura, para que se note sin tapar los dados.
 *
 * @param indicator Resultado del último tiro.
 * @param sum Suma de los dados que no están bloqueados.
 * @param content Los dados.
 */
@Composable
fun DiceStage(
    indicator: RollIndicator,
    sum: Int,
    modifier: Modifier = Modifier,
    minHeight: Dp = if (isTablet()) 300.dp else 230.dp,
    content: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current
    // Sin tarjeta de fondo: los dados se apoyan directamente sobre la pantalla y proyectan su
    // propia sombra, así se ve más natural cuando salen despedidos al lanzar.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        // El dado y la suma forman un solo grupo centrado: la suma queda justo debajo de la
        // sombra, con un espacio entre ambas.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .padding(top = 44.dp)
        ) {
            content()
            Spacer(modifier = Modifier.height(18.dp))
            StyledCard(
                color = if (tokens.isPlayful) tokens.sky else tokens.surface,
                radius = 999.dp,
                tilt = if (tokens.isPlayful) -2f else 0f
            ) {
                Text(
                    text = stringResource(R.string.sum_pill, sum),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (tokens.isPlayful) tokens.onAccent else tokens.ink,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = indicator != RollIndicator.NONE,
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) + fadeIn(tween(200)),
            exit = scaleOut(tween(200)) + fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            val pulse by rememberInfiniteTransition(label = "badgePulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                label = "badgePulseScale"
            )
            val critical = indicator == RollIndicator.CRITICAL
            val label = stringResource(if (critical) R.string.roll_critical else R.string.roll_fumble)
            StyledCard(
                color = if (critical) tokens.accent else tokens.sky,
                radius = 999.dp,
                tilt = if (tokens.isPlayful) 4f else 0f,
                modifier = Modifier.scale(pulse)
            ) {
                Text(
                    text = if (critical) "★ $label ★" else "💥 $label",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (critical || tokens.isPlayful) tokens.onAccent else tokens.ink,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                )
            }
        }
    }
}
