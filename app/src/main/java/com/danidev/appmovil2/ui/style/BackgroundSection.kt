package com.danidev.appmovil2.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R

/** Saturación fija de los colores que se crean con los controles de "Otro color". */
private const val CUSTOM_SATURATION = 0.35f

/**
 * Sección "Fondo" de Personalizar: colores listos para elegir, un color propio con dos
 * controles (tono y claridad) y el interruptor del degradado.
 */
@Composable
fun BackgroundSection(modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    val controller = LocalBackgroundController.current
    val choice = controller.choice

    var hue by rememberSaveable { mutableFloatStateOf(210f) }
    var brightness by rememberSaveable { mutableFloatStateOf(0.95f) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SectionLabel(stringResource(R.string.personalize_background))
            val selectedPreset = BackgroundPreset.entries
                .firstOrNull { it.color.toArgbInt() == choice.colorArgb }
            Text(
                text = when {
                    !choice.isCustom -> stringResource(R.string.bg_style_default)
                    selectedPreset != null -> stringResource(selectedPreset.labelRes)
                    else -> stringResource(R.string.bg_custom)
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.accent
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Swatch(
                color = tokens.bgTop.takeIf { !choice.isCustom } ?: Color.Transparent,
                label = stringResource(R.string.bg_style_default),
                selected = !choice.isCustom,
                onClick = controller.onUseStyleDefault
            )
            BackgroundPreset.entries.forEach { preset ->
                Swatch(
                    color = preset.color,
                    label = stringResource(preset.labelRes),
                    selected = choice.colorArgb == preset.color.toArgbInt(),
                    onClick = { controller.onSelectColor(preset.color) }
                )
            }
        }

        Text(
            text = stringResource(R.string.bg_other_color),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = tokens.ink
        )
        val hueDescription = stringResource(R.string.bg_hue)
        Slider(
            value = hue,
            onValueChange = {
                hue = it
                controller.onSelectColor(Color.hsv(hue, CUSTOM_SATURATION, brightness))
            },
            valueRange = 0f..360f,
            colors = sliderColors(),
            modifier = Modifier.semantics { contentDescription = hueDescription }
        )
        val brightnessDescription = stringResource(R.string.bg_brightness)
        Slider(
            value = brightness,
            onValueChange = {
                brightness = it
                controller.onSelectColor(Color.hsv(hue, CUSTOM_SATURATION, brightness))
            },
            valueRange = 0.15f..1f,
            colors = sliderColors(),
            modifier = Modifier.semantics { contentDescription = brightnessDescription }
        )

        StyledSwitchRow(
            label = stringResource(R.string.bg_gradient),
            checked = choice.gradient,
            onCheckedChange = controller.onGradientChange
        )

        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = tokens.muted,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(16.dp)
            )
            Text(
                text = stringResource(R.string.bg_note),
                fontSize = 13.sp,
                color = tokens.muted
            )
        }
    }
}

@Composable
private fun sliderColors() = LocalAppTokens.current.let { tokens ->
    SliderDefaults.colors(
        thumbColor = tokens.accent,
        activeTrackColor = tokens.accent,
        inactiveTrackColor = tokens.soft
    )
}

/** Círculo de color seleccionable; el elegido lleva un anillo y una marca. */
@Composable
private fun Swatch(color: Color, label: String, selected: Boolean, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) tokens.accent else tokens.muted.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = label }
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = tokens.accent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
