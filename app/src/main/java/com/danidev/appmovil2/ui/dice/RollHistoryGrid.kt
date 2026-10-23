package com.danidev.appmovil2.ui.dice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard
import kotlinx.coroutines.delay

/**
 * Los últimos tiros (HU-011) como una cuadrícula de tarjetas, del más reciente al más antiguo.
 * Cada tarjeta muestra los dados que salieron y el número del tiro; entran una tras otra.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RollHistoryGrid(history: List<RollRecord>, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    val palette = LocalDicePalette.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier.fillMaxWidth()) {
        val title = stringResource(R.string.stats_history_title)
        Text(
            text = if (tokens.isPlayful) title.uppercase() else title,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tokens.bgMuted
        )
        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.roll_history_empty),
                fontSize = 14.sp,
                color = tokens.bgMuted
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                history.forEachIndexed { index, record ->
                    val appear = remember(record.id) { Animatable(0f) }
                    LaunchedEffect(record.id) {
                        delay(index * 60L)
                        appear.animateTo(
                            1f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                        )
                    }
                    val latest = index == 0
                    StyledCard(
                        color = if (latest) tokens.soft else tokens.surface,
                        radius = 18.dp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = appear.value
                            scaleY = appear.value
                            alpha = appear.value.coerceIn(0f, 1f)
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                record.values.forEach { value ->
                                    DiceFace(
                                        value = value,
                                        sides = record.sides,
                                        palette = palette,
                                        contentDescription = value.toString(),
                                        modifier = Modifier.size(if (record.values.size > 1) 44.dp else 60.dp)
                                    )
                                }
                            }
                            Text(
                                text = "#${history.size - index}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (latest) tokens.accent else tokens.muted
                            )
                        }
                    }
                }
            }
        }
    }
}
