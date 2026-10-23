package com.danidev.appmovil2.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Campo para escribir el nombre del jugador (HU-12) con el aspecto del estilo vigente.
 *
 * Igual que [PlayerNameField], cada letra se guarda al instante y "Listo" cierra el teclado.
 */
@Composable
fun StyledNameField(modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    val controller = LocalPlayerName.current
    val focusManager = LocalFocusManager.current

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(R.string.player_name_label),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.muted
            )
            BasicTextField(
                value = controller.name,
                onValueChange = controller.onNameChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = tokens.ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(tokens.accent),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                decorationBox = { inner ->
                    Box {
                        if (controller.name.isEmpty()) {
                            Text(
                                text = stringResource(R.string.player_name_hint),
                                fontSize = 16.sp,
                                color = tokens.muted.copy(alpha = 0.7f)
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            )
        }
    }
}
