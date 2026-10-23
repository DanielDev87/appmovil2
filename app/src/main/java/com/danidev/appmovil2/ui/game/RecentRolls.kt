package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.RollRecord
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Tira corta con los últimos tiros (HU-011). El más reciente va primero y resaltado.
 * El enlace "Estadísticas" abre la pantalla de estadísticas (HU-13).
 */
@Composable
fun RecentRolls(
    history: List<RollRecord>,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            val title = stringResource(R.string.recent_rolls)
            Text(
                text = if (tokens.isPlayful) title.uppercase() else title,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tokens.bgMuted
            )
            val link = stringResource(R.string.stats_link)
            if (tokens.isPlayful) {
                StyledCard(
                    color = tokens.highlight,
                    radius = 999.dp,
                    modifier = Modifier.clickable(role = Role.Button, onClick = onOpenStats)
                ) {
                    Text(
                        text = link.uppercase() + " ›",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.onAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "$link ›",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.accent,
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onOpenStats)
                        .padding(vertical = 4.dp)
                )
            }
        }

        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.roll_history_empty),
                fontSize = 14.sp,
                color = tokens.bgMuted
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history, key = { it.id }) { record ->
                    val latest = record.id == history.first().id
                    StyledCard(
                        color = if (latest) tokens.accent else tokens.surface,
                        radius = if (tokens.isPlayful) 22.dp else 14.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                            Text(
                                text = record.value.toString(),
                                fontSize = if (record.value > 99) 14.sp else 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (latest) tokens.onAccent else tokens.ink
                            )
                        }
                    }
                }
            }
        }
    }
}
