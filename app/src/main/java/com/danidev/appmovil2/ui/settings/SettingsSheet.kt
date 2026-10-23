package com.danidev.appmovil2.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.haptics.DiceHaptics
import com.danidev.appmovil2.ui.dice.DiceFace
import com.danidev.appmovil2.ui.dice.DicePalette
import com.danidev.appmovil2.ui.dice.DiceSize
import com.danidev.appmovil2.ui.player.StyledNameField
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.StyledSegmented
import com.danidev.appmovil2.ui.style.StyledSwitchRow
import com.danidev.appmovil2.ui.theme.LocalThemeController
import com.danidev.appmovil2.ui.theme.ThemeMode
import kotlin.math.roundToInt

/**
 * Hoja de ajustes de la partida: tema, color y tamaño del dado, nombre, sonido,
 * vibración y agitar para tirar.
 *
 * @param palette Color del dado en uso (HU-04).
 * @param sizeFactor Tamaño elegido para el dado (HU-05).
 * @param diceCount Cantidad de dados en juego; limita el tamaño máximo.
 * @param haptics Vibración (HU-16).
 * @param settings Sonido (HU-15) y agitar para tirar (HU-19).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    palette: DicePalette,
    onPaletteChange: (DicePalette) -> Unit,
    sizeFactor: Float,
    diceCount: Int,
    onSizeChange: (Float) -> Unit,
    haptics: DiceHaptics,
    settings: GameSettings,
    onSoundMutedChange: (Boolean) -> Unit,
    onShakeChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val theme = LocalThemeController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = tokens.surface,
        contentColor = tokens.ink
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tokens.ink
            )

            Label(stringResource(R.string.settings_theme))
            StyledSegmented(
                options = listOf(
                    stringResource(R.string.theme_light),
                    stringResource(R.string.theme_dark),
                    stringResource(R.string.theme_system)
                ),
                selectedIndex = when (theme.mode) {
                    ThemeMode.LIGHT -> 0
                    ThemeMode.DARK -> 1
                    ThemeMode.SYSTEM -> 2
                },
                onSelect = { index ->
                    theme.onModeChange(
                        when (index) {
                            0 -> ThemeMode.LIGHT
                            1 -> ThemeMode.DARK
                            else -> ThemeMode.SYSTEM
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Label(stringResource(R.string.settings_dice_color))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DicePalette.entries.forEach { option ->
                    PaletteOption(
                        option = option,
                        selected = option == palette,
                        onClick = { onPaletteChange(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Label(stringResource(R.string.settings_dice_size))
                val shown = DiceSize.effectiveFactor(sizeFactor, diceCount)
                Text(
                    text = stringResource(R.string.dice_size_percent, (shown * 100).roundToInt()),
                    fontWeight = FontWeight.Bold,
                    color = tokens.ink
                )
            }
            val sizeDescription = stringResource(R.string.dice_size_description)
            Slider(
                value = DiceSize.effectiveFactor(sizeFactor, diceCount),
                onValueChange = onSizeChange,
                valueRange = DiceSize.MIN_FACTOR..DiceSize.maxFactor(diceCount),
                colors = SliderDefaults.colors(
                    thumbColor = tokens.accent,
                    activeTrackColor = tokens.accent,
                    inactiveTrackColor = tokens.soft
                ),
                modifier = Modifier.semantics { contentDescription = sizeDescription }
            )

            StyledNameField()

            Column {
                StyledSwitchRow(
                    label = stringResource(R.string.settings_sound),
                    checked = !settings.soundMuted,
                    onCheckedChange = { onSoundMutedChange(!it) }
                )
                StyledSwitchRow(
                    label = stringResource(R.string.settings_vibration),
                    checked = haptics.isEnabled,
                    onCheckedChange = haptics::setVibrationEnabled
                )
                StyledSwitchRow(
                    label = stringResource(R.string.settings_shake),
                    checked = settings.shakeEnabled,
                    onCheckedChange = onShakeChange
                )
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppTokens.current.muted
    )
}

/** Opción de color del dado: un dado de muestra con el nombre de la paleta. */
@Composable
private fun PaletteOption(
    option: DicePalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    StyledCard(
        color = if (selected) tokens.soft else tokens.surface,
        radius = 18.dp,
        modifier = modifier.clickable(role = Role.RadioButton, onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            DiceFace(
                value = 6,
                sides = 6,
                palette = option,
                contentDescription = "",
                modifier = Modifier.size(52.dp)
            )
            Text(
                text = stringResource(option.labelRes),
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = tokens.ink
            )
        }
    }
}
