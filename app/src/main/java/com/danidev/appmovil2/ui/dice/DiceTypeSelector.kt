package com.danidev.appmovil2.ui.dice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.theme.Appmovil2Theme

/**
 * Selector desplegable (`DropdownMenu`) del número de caras del dado (HU-07).
 *
 * Muestra el tipo actual en un botón; al pulsarlo se abre la lista con D4, D6, D8, D12
 * y D20, y el tipo elegido aparece marcado.
 *
 * @param selected Tipo de dado en uso.
 * @param onSelect Se invoca con el tipo que el usuario elige.
 * @param enabled `false` para bloquearlo, por ejemplo mientras el dado está girando.
 * @param modifier Modificador opcional para el contenedor.
 */
@Composable
fun DiceTypeSelector(
    selected: DiceType,
    onSelect: (DiceType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val tokens = LocalAppTokens.current

    Box(modifier = modifier) {
        StyledCard(
            color = if (tokens.isPlayful) tokens.highlight else tokens.surface,
            radius = 16.dp,
            modifier = Modifier
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(enabled = enabled, role = Role.Button) { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(48.dp)
                    .padding(start = 18.dp, end = 10.dp)
            ) {
                Text(
                    text = selected.label,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (tokens.isPlayful) tokens.onAccent else tokens.ink
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.dice_type_description),
                    tint = if (tokens.isPlayful) tokens.onAccent else tokens.ink
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = tokens.surface
        ) {
            DiceType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.dice_type_option, type.label, type.sides),
                            fontWeight = if (type == selected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = tokens.ink
                        )
                    },
                    trailingIcon = {
                        if (type == selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = tokens.accent
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(type)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiceTypeSelectorPreview() {
    Appmovil2Theme {
        DiceTypeSelector(selected = DiceType.D20, onSelect = {})
    }
}
