package com.danidev.appmovil2.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Selector de una opción entre varias, con el aspecto del estilo vigente.
 *
 * @param options Texto de cada opción.
 * @param selectedIndex Posición de la opción elegida.
 * @param onSelect Se invoca con la posición que el usuario toca.
 */
@Composable
fun StyledSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val tokens = LocalAppTokens.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .styledSurface(radius = 16.dp)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        if (selected) tokens.accent else androidx.compose.ui.graphics.Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = enabled, role = Role.RadioButton) { onSelect(index) }
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (selected) tokens.onAccent else tokens.muted
                )
            }
        }
    }
}

/** Fila con un texto a la izquierda y un interruptor a la derecha. */
@Composable
fun StyledSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = tokens.ink
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = { Box(Modifier.size(1.dp)) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = tokens.onAccent,
                checkedTrackColor = tokens.accent,
                checkedBorderColor = tokens.accent,
                uncheckedThumbColor = tokens.muted,
                uncheckedTrackColor = tokens.soft,
                uncheckedBorderColor = tokens.muted
            )
        )
    }
}

/**
 * Fila con una etiqueta y, a la derecha, un valor que se cambia con los botones − y +.
 *
 * @param step Cuánto cambia el valor con cada toque.
 * @param format Cómo se escribe el valor (por ejemplo, con signo).
 */
@Composable
fun StyledStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    step: Int,
    modifier: Modifier = Modifier,
    format: (Int) -> String = { it.toString() }
) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = tokens.ink)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StepperButton("−", enabled = value > min) { onValueChange((value - step).coerceAtLeast(min)) }
            Text(
                text = format(value),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tokens.ink,
                modifier = Modifier.width(52.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            StepperButton("+", enabled = value < max) { onValueChange((value + step).coerceAtMost(max)) }
        }
    }
}

@Composable
private fun StepperButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    StyledCard(
        color = if (enabled) tokens.accent else tokens.soft,
        radius = 14.dp,
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
            Text(
                text = symbol,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (enabled) tokens.onAccent else tokens.muted
            )
        }
    }
}
