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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.topFaces
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Resumen de las tres caras que más han salido (HU-13). Aprovecha el espacio libre de una
 * tablet; el detalle completo está en la pantalla de estadísticas.
 */
@Composable
fun TopFaces(counts: Map<Int, Int>, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    val top = counts.topFaces(3)
    if (top.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        val title = stringResource(R.string.top_faces_title)
        Text(
            text = if (tokens.isPlayful) title.uppercase() else title,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tokens.bgMuted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            top.forEachIndexed { index, (face, count) ->
                StyledCard(
                    color = if (index == 0 && tokens.isPlayful) tokens.highlight else tokens.surface,
                    radius = 16.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.stats_face_label, face),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (index == 0 && tokens.isPlayful) tokens.onAccent else tokens.ink
                        )
                        Text(
                            text = pluralStringResource(R.plurals.stats_times, count, count),
                            fontSize = 13.sp,
                            color = if (index == 0 && tokens.isPlayful) tokens.onAccent else tokens.muted
                        )
                    }
                }
            }
        }
    }
}
