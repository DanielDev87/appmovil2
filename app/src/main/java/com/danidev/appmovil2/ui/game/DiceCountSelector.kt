package com.danidev.appmovil2.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.StyledSegmented

/** HU-06: selección de modo de dados (1 dado / 2 dados). */
@Composable
fun DiceCountSelector(
    diceCount: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    StyledSegmented(
        options = listOf(stringResource(R.string.one_die), stringResource(R.string.two_dice)),
        selectedIndex = if (diceCount == 2) 1 else 0,
        onSelect = { index -> onSelect(index + 1) },
        enabled = enabled,
        modifier = modifier
    )
}
