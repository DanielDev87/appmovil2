package com.danidev.appmovil2.ui.theme

/**
 * Preferencia de tema elegida por el usuario (HU-03).
 *
 * - [SYSTEM]: aún no ha elegido; la app sigue el tema del dispositivo.
 * - [LIGHT] / [DARK]: elección manual con el switch; tiene prioridad sobre el sistema.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
