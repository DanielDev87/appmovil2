package com.danidev.appmovil2.ui.player

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.theme.Appmovil2Theme

/*
 * HU-12: Guardar el nombre del jugador.
 *
 * "Como usuario, quiero ingresar mi nombre en un campo de texto y que se conserve
 * al cerrar la app, para ver un saludo personalizado como 'Turno de: [Nombre]'."
 */

/**
 * Acceso al nombre del jugador para cualquier pantalla.
 *
 * Se entrega mediante [LocalPlayerName] (un `CompositionLocal`) para que una
 * pantalla pueda mostrar el nombre o el campo para editarlo sin recibir
 * parámetros extra, igual que ocurre con el tema (`LocalThemeController`).
 *
 * @property name Nombre tal como está escrito en el campo.
 * @property onNameChange Se invoca con el texto nuevo cuando el usuario escribe.
 */
class PlayerNameController(
    val name: String,
    val onNameChange: (String) -> Unit
) {
    /** Nombre listo para mostrar en un saludo: sin espacios al inicio ni al final. */
    val displayName: String get() = name.trim()
}

/** Controlador por defecto (previews y tests de UI): sin nombre y sin efecto al escribir. */
val LocalPlayerName = compositionLocalOf { PlayerNameController(name = "", onNameChange = {}) }

/**
 * Campo de texto para escribir el nombre del jugador.
 *
 * Cada letra se guarda al instante, así que no hace falta un botón de guardar.
 * Al pulsar "Listo" en el teclado se cierra el teclado.
 *
 * @param modifier Modificador opcional para el campo.
 */
@Composable
fun PlayerNameField(modifier: Modifier = Modifier) {
    val controller = LocalPlayerName.current
    val focusManager = LocalFocusManager.current
    val onBackground = MaterialTheme.colorScheme.onBackground

    OutlinedTextField(
        value = controller.name,
        onValueChange = controller.onNameChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(stringResource(R.string.player_name_label)) },
        supportingText = { Text(stringResource(R.string.player_name_hint)) },
        shape = MaterialTheme.shapes.large,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = onBackground,
            unfocusedTextColor = onBackground,
            focusedBorderColor = onBackground,
            unfocusedBorderColor = onBackground.copy(alpha = 0.5f),
            focusedLabelColor = onBackground,
            unfocusedLabelColor = onBackground.copy(alpha = 0.8f),
            focusedSupportingTextColor = onBackground.copy(alpha = 0.8f),
            unfocusedSupportingTextColor = onBackground.copy(alpha = 0.8f),
            cursorColor = onBackground
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PlayerNameFieldPreview() {
    Appmovil2Theme {
        PlayerNameField()
    }
}
