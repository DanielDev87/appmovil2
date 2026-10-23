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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Campo de texto de una sola línea con el aspecto del estilo vigente: una tarjeta con la etiqueta
 * arriba y lo escrito debajo. Se usa para los nombres de los jugadores adicionales.
 *
 * @param hint Texto que se ve mientras el campo está vacío.
 */
@Composable
fun StyledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    val focusManager = LocalFocusManager.current

    StyledCard(modifier = modifier.fillMaxWidth(), radius = 16.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.muted)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = tokens.ink, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                cursorBrush = SolidColor(tokens.accent),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(text = hint, fontSize = 15.sp, color = tokens.muted.copy(alpha = 0.7f))
                        }
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
