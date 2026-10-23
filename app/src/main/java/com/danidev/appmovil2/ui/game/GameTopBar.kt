package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.StyledIconButton

/**
 * Barra superior de la partida: volver al menú, título con el turno del jugador (HU-12),
 * sonido (HU-15) y ajustes.
 */
@Composable
fun GameTopBar(
    isSoundMuted: Boolean,
    onToggleSound: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    onReset: () -> Unit,
    turnName: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val playerName = turnName
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyledIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back_to_menu),
            onClick = onNavigateToMenu
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val title = stringResource(R.string.game_title)
            if (tokens.isPlayful) {
                StyledCard(color = tokens.accent, radius = 999.dp, tilt = -2f) {
                    Text(
                        text = title.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = tokens.onAccent,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.bgInk
                )
            }
            if (playerName.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.player_turn, playerName),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.bgMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StyledIconButton(
                icon = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.reset_game_description),
                onClick = onReset
            )
            StyledIconButton(
                icon = if (isSoundMuted) {
                    Icons.AutoMirrored.Filled.VolumeOff
                } else {
                    Icons.AutoMirrored.Filled.VolumeUp
                },
                contentDescription = stringResource(
                    if (isSoundMuted) R.string.sound_disabled else R.string.sound_enabled
                ),
                onClick = onToggleSound
            )
            StyledIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings_open),
                onClick = onOpenSettings
            )
        }
    }
}
