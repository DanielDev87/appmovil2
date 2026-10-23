package com.danidev.appmovil2.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.MAX_PLAYERS
import com.danidev.appmovil2.MIN_PLAYERS
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Una tarjeta por cada cantidad de jugadores (de [MIN_PLAYERS] a [MAX_PLAYERS]), con un dibujo de
 * personas, el número y la palabra "jugador(es)". La elegida se resalta con el color de acento.
 *
 * @param selected Cantidad de jugadores elegida.
 * @param onSelect Se invoca con la cantidad cuya tarjeta se toca.
 * @param enabled `false` para bloquearlas, por ejemplo mientras los dados giran.
 * @param from Primera cantidad que se ofrece; con 2 se omite "1 jugador" (modo con amigos).
 */
@Composable
fun PlayerCountCards(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    from: Int = MIN_PLAYERS
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier.fillMaxWidth()) {
        for (count in from..MAX_PLAYERS) {
            PlayerCountCard(
                count = count,
                selected = count == selected,
                enabled = enabled,
                onClick = { onSelect(count) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlayerCountCard(
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val tokens = LocalAppTokens.current
    val background by animateColorAsState(
        targetValue = if (selected) tokens.accent else tokens.surface,
        animationSpec = tween(200),
        label = "playerCardColor"
    )
    val onBackground = if (selected) tokens.onAccent else tokens.ink
    val description = stringResource(R.string.players_card_description, count)
    StyledCard(
        color = background,
        radius = 18.dp,
        modifier = modifier
            .semantics {
                contentDescription = description
                this.selected = selected
            }
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            // Un ícono por jugador, del color del texto para que se lea también sobre la tarjeta elegida.
            Row {
                repeat(count) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = count.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = onBackground
            )
            Text(
                text = pluralStringResource(R.plurals.players_card_label, count),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = onBackground
            )
        }
    }
}
