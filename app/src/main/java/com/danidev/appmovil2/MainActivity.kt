package com.danidev.appmovil2

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.rememberNavController
import com.danidev.appmovil2.ui.navigation.AppNavGraph
import com.danidev.appmovil2.ui.player.LocalPlayerName
import com.danidev.appmovil2.ui.player.PlayerNameController
import com.danidev.appmovil2.ui.player.PlayerNameViewModel
import com.danidev.appmovil2.ui.player.SharedPreferencesPlayerNameStore
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.LocalStyleController
import com.danidev.appmovil2.ui.style.SharedPreferencesStyleStore
import com.danidev.appmovil2.ui.style.StyleController
import com.danidev.appmovil2.ui.style.StyleViewModel
import com.danidev.appmovil2.ui.style.appTokens
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import com.danidev.appmovil2.ui.theme.LocalThemeController
import com.danidev.appmovil2.ui.theme.SharedPreferencesThemeStore
import com.danidev.appmovil2.ui.theme.ThemeController
import com.danidev.appmovil2.ui.theme.ThemeMode
import com.danidev.appmovil2.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {

    // HU-03: preferencia de tema (claro / oscuro), guardada entre sesiones.
    private val themeViewModel: ThemeViewModel by viewModels {
        viewModelFactory {
            initializer { ThemeViewModel(SharedPreferencesThemeStore(applicationContext)) }
        }
    }

    // HU-12: nombre del jugador, guardado entre sesiones.
    private val playerNameViewModel: PlayerNameViewModel by viewModels {
        viewModelFactory {
            initializer { PlayerNameViewModel(SharedPreferencesPlayerNameStore(applicationContext)) }
        }
    }

    // Estilo visual (Equilibrado / Juguetón), guardado entre sesiones.
    private val styleViewModel: StyleViewModel by viewModels {
        viewModelFactory {
            initializer { StyleViewModel(SharedPreferencesStyleStore(applicationContext)) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            // HU-03: "sistema" solo aplica hasta que el usuario elige con el switch.
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Las barras del sistema deben seguir el tema de la app, no el del dispositivo.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = AndroidColor.TRANSPARENT,
                        darkScrim = AndroidColor.TRANSPARENT,
                        detectDarkMode = { darkTheme }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = AndroidColor.TRANSPARENT,
                        darkScrim = AndroidColor.TRANSPARENT,
                        detectDarkMode = { darkTheme }
                    )
                )
                onDispose {}
            }

            val playerName by playerNameViewModel.name.collectAsState()
            val appStyle by styleViewModel.style.collectAsState()

            CompositionLocalProvider(
                LocalThemeController provides ThemeController(
                    isDark = darkTheme,
                    onDarkChange = themeViewModel::setDarkTheme
                ),
                LocalPlayerName provides PlayerNameController(
                    name = playerName,
                    onNameChange = playerNameViewModel::setName
                ),
                LocalStyleController provides StyleController(
                    style = appStyle,
                    onStyleChange = styleViewModel::select
                ),
                LocalAppTokens provides appTokens(appStyle, darkTheme)
            ) {
                Appmovil2Theme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        AppNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
