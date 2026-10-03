package com.danidev.appmovil2.ui.dice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.ui.theme.Appmovil2Theme

/**
 * Selector de la paleta de color del dado (HU-04): un `FilterChip` por paleta,
 * con una muestra del color junto al nombre.
 *
 * @param selected Paleta que está en uso.
 * @param onSelect Se invoca con la paleta que el usuario toca.
 * @param modifier Modificador opcional para el contenedor.
 */
@Composable
fun DicePalettePicker(
    selected: DicePalette,
    onSelect: (DicePalette) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DicePalette.entries.forEach { palette ->
            FilterChip(
                selected = palette == selected,
                onClick = { onSelect(palette) },
                label = { Text(stringResource(palette.labelRes)) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(palette.swatch, CircleShape)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DicePalettePickerPreview() {
    Appmovil2Theme {
        DicePalettePicker(selected = DicePalette.ROSA, onSelect = {})
    }
}
