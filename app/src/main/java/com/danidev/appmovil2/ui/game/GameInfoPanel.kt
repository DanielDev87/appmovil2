package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import com.danidev.appmovil2.ui.dice.DiceType
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Tarjeta con los datos de la partida en tres casillas: cuántos dados, cuántas caras y los tiros
 * consecutivos (HU-08). Tocarla abre los ajustes, donde se cambian los dados y sus caras.
 */
@Composable
fun GameInfoPanel(
    diceCount: Int,
    diceType: DiceType,
    rollCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.mode_chip_description)
    StyledCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        ) {
            InfoCell(label = stringResource(R.string.info_dice), value = diceCount.toString())
            InfoCell(label = stringResource(R.string.info_faces), value = diceType.label)
            InfoCell(label = stringResource(R.string.info_streak), value = rollCount.toString())
        }
    }
}

@Composable
private fun InfoCell(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tokens.ink
        )
        Text(
            text = if (tokens.isPlayful) label.uppercase() else label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.muted
        )
    }
}
