package com.danidev.appmovil2.ui.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val onBackground = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.dice_type_label),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = onBackground,
            modifier = Modifier.padding(end = 8.dp)
        )
        Box {
            OutlinedButton(onClick = { expanded = true }, enabled = enabled) {
                Text(text = selected.label, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.dice_type_description)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DiceType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.dice_type_option, type.label, type.sides),
                                fontWeight = if (type == selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingIcon = {
                            if (type == selected) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
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
}

@Preview(showBackground = true)
@Composable
private fun DiceTypeSelectorPreview() {
    Appmovil2Theme {
        DiceTypeSelector(selected = DiceType.D20, onSelect = {})
    }
}
