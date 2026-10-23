package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R

/** HU-06: cálculo sumatorio de los dados que no están bloqueados. */
@Composable
fun RollSum(
    unlockedValues: List<Int>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val currentSum = unlockedValues.sum()
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isDark) Color(0xFF381C48).copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer,
        border = if (isDark) BorderStroke(1.dp, Color(0xFFF6B5DA).copy(alpha = 0.25f)) else null,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = if (unlockedValues.size == 2) {
                stringResource(
                    R.string.dice_sum_multiple,
                    unlockedValues.getOrElse(0) { 1 },
                    unlockedValues.getOrElse(1) { 1 },
                    currentSum
                )
            } else {
                stringResource(R.string.dice_sum_single, currentSum)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
