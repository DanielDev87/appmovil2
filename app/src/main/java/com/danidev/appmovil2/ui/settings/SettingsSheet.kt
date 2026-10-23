package com.danidev.appmovil2.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danidev.appmovil2.MAX_MAX_LAUNCHES
import com.danidev.appmovil2.MAX_TARGET_SCORE
import com.danidev.appmovil2.MIN_MAX_LAUNCHES
import com.danidev.appmovil2.MIN_TARGET_SCORE
import com.danidev.appmovil2.R
import com.danidev.appmovil2.haptics.DiceHaptics
import com.danidev.appmovil2.ui.dice.DiceFace
import com.danidev.appmovil2.ui.dice.DicePalette
import com.danidev.appmovil2.ui.dice.DiceSize
import com.danidev.appmovil2.ui.dice.DiceType
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.LocalStyleController
import com.danidev.appmovil2.ui.style.PersonalizeContent
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.StyledSegmented
import com.danidev.appmovil2.ui.style.StyledStepper
import com.danidev.appmovil2.ui.style.StyledSwitchRow
import com.danidev.appmovil2.ui.style.isTablet
import com.danidev.appmovil2.ui.theme.LocalThemeController
import com.danidev.appmovil2.ui.theme.ThemeMode
import kotlin.math.roundToInt

/** Qué ajustes de la partida se pueden cambiar desde [SettingsSheet]. */
class GameOptions(
    val palette: DicePalette,
    val onPaletteChange: (DicePalette) -> Unit,
    val sizeFactor: Float,
    val onSizeChange: (Float) -> Unit,
    val diceCount: Int,
    val onDiceCountChange: (Int) -> Unit,
    val diceType: DiceType,
    val onDiceTypeChange: (DiceType) -> Unit,
    val haptics: DiceHaptics,
    val settings: GameSettings,
    val onSoundMutedChange: (Boolean) -> Unit,
    val onShakeChange: (Boolean) -> Unit,
    val onTargetScoreChange: (Int) -> Unit,
    val onMaxLaunchesChange: (Int) -> Unit,
    /** `true` mientras los dados giran: no se pueden cambiar los dados en ese momento. */
    val isRolling: Boolean
)

/**
 * Ajustes dentro de la partida, en dos pantallas: "Partida" (dados, meta, sonido, vibración y
 * agitar) y "Personalizar" (tema, nombre, estilo y fondo, igual que en la bienvenida).
 *
 * En una tablet sale como panel lateral; en un teléfono, como una hoja desde abajo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(options: GameOptions, onDismiss: () -> Unit) {
    val tokens = LocalAppTokens.current

    val body: @Composable () -> Unit = {
        var tab by rememberSaveable { mutableIntStateOf(0) }
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
            StyledSegmented(
                options = listOf(
                    stringResource(R.string.settings_tab_game),
                    stringResource(R.string.settings_tab_personalize)
                ),
                selectedIndex = tab,
                onSelect = { tab = it },
                modifier = Modifier.fillMaxWidth()
            )
            if (tab == 0) GameTab(options) else PersonalizeTab()
        }
    }

    // En una tablet los ajustes salen como un panel lateral; en un teléfono, como una hoja
    // que sube desde abajo.
    if (isTablet()) {
        SidePanel(onDismiss = onDismiss, content = body)
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = tokens.surface,
            contentColor = tokens.ink
        ) {
            body()
        }
    }
}

/** Primera pantalla: lo que cambia la partida. */
@Composable
private fun GameTab(options: GameOptions) {
    val tokens = LocalAppTokens.current

    Label(stringResource(R.string.settings_dice))
    StyledSegmented(
        options = listOf(stringResource(R.string.one_die), stringResource(R.string.two_dice)),
        selectedIndex = if (options.diceCount == 2) 1 else 0,
        onSelect = { options.onDiceCountChange(it + 1) },
        enabled = !options.isRolling,
        modifier = Modifier.fillMaxWidth()
    )

    Label(stringResource(R.string.settings_faces))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        DiceType.entries.forEach { type ->
            val selected = type == options.diceType
            StyledCard(
                color = if (selected) tokens.accent else tokens.surface,
                radius = 14.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !options.isRolling, role = Role.RadioButton) {
                        options.onDiceTypeChange(type)
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = type.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selected) tokens.onAccent else tokens.ink
                    )
                }
            }
        }
    }

    Label(stringResource(R.string.settings_dice_color))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DicePalette.entries.forEach { option ->
            PaletteOption(
                option = option,
                selected = option == options.palette,
                onClick = { options.onPaletteChange(option) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    Label(stringResource(R.string.settings_dice_size))
    DiceSizePicker(
        factor = options.sizeFactor,
        diceCount = options.diceCount,
        palette = options.palette,
        diceType = options.diceType,
        onFactorChange = options.onSizeChange
    )

    Label(stringResource(R.string.settings_goals))
    StyledStepper(
        label = stringResource(R.string.settings_target_score),
        value = options.settings.targetScore,
        onValueChange = options.onTargetScoreChange,
        min = MIN_TARGET_SCORE,
        max = MAX_TARGET_SCORE,
        step = 5
    )
    StyledStepper(
        label = stringResource(R.string.settings_max_launches),
        value = options.settings.maxLaunches,
        onValueChange = options.onMaxLaunchesChange,
        min = MIN_MAX_LAUNCHES,
        max = MAX_MAX_LAUNCHES,
        step = 1
    )

    Column {
        StyledSwitchRow(
            label = stringResource(R.string.settings_sound),
            checked = !options.settings.soundMuted,
            onCheckedChange = { options.onSoundMutedChange(!it) }
        )
        StyledSwitchRow(
            label = stringResource(R.string.settings_vibration),
            checked = options.haptics.isEnabled,
            onCheckedChange = options.haptics::setVibrationEnabled
        )
        StyledSwitchRow(
            label = stringResource(R.string.settings_shake),
            checked = options.settings.shakeEnabled,
            onCheckedChange = options.onShakeChange
        )
    }
}

/** Segunda pantalla: lo mismo que "Personalizar" en la bienvenida, más el tema. */
@Composable
private fun PersonalizeTab() {
    val theme = LocalThemeController.current
    val styleController = LocalStyleController.current

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

    PersonalizeContent(style = styleController.style, onStyleChange = styleController.onStyleChange)
}

/** Panel pegado al borde derecho, con el resto de la pantalla atenuado; tocarlo fuera lo cierra. */
@Composable
private fun SidePanel(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val tokens = LocalAppTokens.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(0.85f)
                    .background(tokens.surface, RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                content()
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


/** Tamaños que se pueden elegir y el factor que usa cada uno (1.0 es el tamaño original). */
private enum class SizeChoice(val labelRes: Int, val factor: Float) {
    SMALL(R.string.size_small, 0.7f),
    NORMAL(R.string.size_normal, 1.0f),
    LARGE(R.string.size_large, 1.3f)
}

/**
 * Elige el tamaño del dado entre tres opciones y muestra una muestra del dado con ese tamaño, para
 * ver el resultado sin salir de los ajustes. Con dos dados el tamaño grande se limita a lo que cabe
 * en pantalla (ver [DiceSize.effectiveFactor]).
 */
@Composable
private fun DiceSizePicker(
    factor: Float,
    diceCount: Int,
    palette: DicePalette,
    diceType: DiceType,
    onFactorChange: (Float) -> Unit
) {
    val shown = DiceSize.effectiveFactor(factor, diceCount)
    // La opción marcada es la más cercana al tamaño que realmente se está usando.
    val selected = SizeChoice.entries.minBy { kotlin.math.abs(DiceSize.effectiveFactor(it.factor, diceCount) - shown) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StyledSegmented(
            options = SizeChoice.entries.map { stringResource(it.labelRes) },
            selectedIndex = selected.ordinal,
            onSelect = { onFactorChange(SizeChoice.entries[it].factor) },
            modifier = Modifier.fillMaxWidth()
        )
        // Muestra: el dado con su tamaño elegido, a escala, para ver el resultado sin salir.
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(104.dp)) {
            DiceFace(
                value = diceType.sides.coerceAtMost(6),
                sides = diceType.sides,
                palette = palette,
                contentDescription = stringResource(R.string.dice_size_description),
                modifier = Modifier.size((72f * shown).dp)
            )
        }
    }
}
