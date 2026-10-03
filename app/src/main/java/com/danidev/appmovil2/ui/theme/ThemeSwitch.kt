package com.danidev.appmovil2.ui.theme

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R

/*
 * HU-03: Selector de tema claro / oscuro manual.
 *
 * "Como usuario, quiero un interruptor (Switch) en la interfaz, para cambiar
 * manualmente entre el tema claro y oscuro de la aplicación."
 */

/**
 * Acceso al tema para cualquier pantalla.
 *
 * Se entrega mediante [LocalThemeController] (un `CompositionLocal`) para que
 * una pantalla pueda mostrar [ThemeSwitch] sin recibir parámetros extra: así no
 * cambia la firma de los composables compartidos y las pantallas que se agreguen
 * en el futuro pueden usar el switch sin tocar la navegación.
 *
 * @property isDark Tema que se está mostrando realmente (ya resuelto el modo "sistema").
 * @property onDarkChange Se invoca con el tema que el usuario eligió.
 */
class ThemeController(
    val isDark: Boolean,
    val onDarkChange: (Boolean) -> Unit
)

/** Controlador por defecto (previews y tests de UI): no hace nada al cambiar. */
val LocalThemeController = compositionLocalOf { ThemeController(isDark = false, onDarkChange = {}) }

/**
 * Interruptor para alternar entre tema claro y oscuro.
 *
 * Cambia el tema de inmediato porque modifica el estado que observa
 * `Appmovil2Theme`, que vuelve a componer toda la app.
 *
 * @param modifier Modificador opcional para el contenedor.
 */
@Composable
fun ThemeSwitch(modifier: Modifier = Modifier) {
    val controller = LocalThemeController.current
    val description = stringResource(R.string.theme_switch_description)

    Row(
        modifier = modifier.semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (controller.isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp)
        )
        Switch(
            checked = controller.isDark,
            onCheckedChange = controller.onDarkChange,
            // Con contenido que mida algo, Material 3 dibuja la bolita del mismo tamaño en
            // ambos estados; sin él, apagada mide 16 dp y encendida 24 dp.
            thumbContent = { Spacer(modifier = Modifier.size(1.dp)) },
            colors = SwitchDefaults.colors(
                // Activado = tema oscuro: pista rosa y círculo oscuro para que contraste con el fondo negro.
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onBackground,
                uncheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeSwitchPreview() {
    Appmovil2Theme {
        ThemeSwitch()
    }
}
