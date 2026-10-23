package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.haptics.DiceHaptics
import com.danidev.appmovil2.ui.theme.ThemeSwitch

/**
 * Barra superior de la partida: volver al menú, tema (HU-03), vibración (HU-16)
 * y sonido (HU-15).
 */
@Composable
fun GameTopBar(
    haptics: DiceHaptics,
    isSoundMuted: Boolean,
    onToggleSound: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onNavigateToMenu) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_to_menu),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            ThemeSwitch()
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { haptics.setVibrationEnabled(!haptics.isEnabled) }) {
                Icon(
                    imageVector = Icons.Filled.Vibration,
                    contentDescription = stringResource(
                        if (haptics.isEnabled) R.string.haptics_enabled else R.string.haptics_disabled
                    ),
                    tint = MaterialTheme.colorScheme.onBackground
                        .copy(alpha = if (haptics.isEnabled) 1f else 0.4f)
                )
            }
            IconButton(onClick = onToggleSound) {
                Icon(
                    imageVector = if (isSoundMuted) {
                        Icons.AutoMirrored.Filled.VolumeOff
                    } else {
                        Icons.AutoMirrored.Filled.VolumeUp
                    },
                    contentDescription = stringResource(
                        if (isSoundMuted) R.string.sound_disabled else R.string.sound_enabled
                    ),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings_open),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
