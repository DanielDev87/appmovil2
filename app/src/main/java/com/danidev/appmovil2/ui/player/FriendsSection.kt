package com.danidev.appmovil2.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.settings.GameSettings
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.StyledSwitchRow

/**
 * "Jugar con amigos": opcional. Apagado, juega una sola persona. Al encenderlo se elige si juegan 2 o 3
 * y aparece un campo de nombre, a todo el ancho, por cada amigo. El nombre del jugador 1 es el principal
 * ("Tu nombre").
 *
 * @param settings Ajustes actuales (cantidad de jugadores y nombres).
 * @param onPlayerCountChange Se invoca con la cantidad de jugadores elegida (1 al apagarlo).
 * @param onPlayerNameChange Se invoca con la posición del amigo (1 o 2, es decir, jugadores 2 y 3) y su nombre.
 * @param enabled `false` para bloquearlo, por ejemplo mientras los dados giran.
 */
@Composable
fun FriendsSection(
    settings: GameSettings,
    onPlayerCountChange: (Int) -> Unit,
    onPlayerNameChange: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val tokens = LocalAppTokens.current
    val withFriends = settings.playerCount > 1

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier.fillMaxWidth()) {
        StyledCard(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                StyledSwitchRow(
                    label = stringResource(R.string.friends_title),
                    checked = withFriends,
                    onCheckedChange = { on ->
                        if (enabled) onPlayerCountChange(if (on) 2 else 1)
                    }
                )
                Text(
                    text = stringResource(R.string.friends_subtitle),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.muted,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = withFriends,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerCountCards(
                    selected = settings.playerCount,
                    onSelect = onPlayerCountChange,
                    enabled = enabled,
                    from = 2
                )
                // Un campo a todo el ancho por cada amigo (jugadores 2 y 3).
                for (index in 1 until settings.playerCount) {
                    StyledTextField(
                        label = stringResource(R.string.settings_player_name, index + 1),
                        value = settings.playerNames[index - 1],
                        onValueChange = { onPlayerNameChange(index, it) },
                        hint = stringResource(R.string.settings_player_name_hint, index + 1),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
