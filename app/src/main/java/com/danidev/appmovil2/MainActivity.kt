package com.danidev.appmovil2

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.danidev.appmovil2.ui.style.BackgroundController
import com.danidev.appmovil2.ui.style.BackgroundViewModel
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.LocalBackgroundController
import com.danidev.appmovil2.ui.style.SharedPreferencesBackgroundStore
import com.danidev.appmovil2.ui.style.applyBackground
import com.danidev.appmovil2.ui.style.LocalStyleController
import com.danidev.appmovil2.ui.style.SharedPreferencesStyleStore
import com.danidev.appmovil2.ui.style.StyleController
import com.danidev.appmovil2.ui.style.PressIndication
import com.danidev.appmovil2.ui.style.StyleViewModel
import com.danidev.appmovil2.ui.settings.rememberGameSettingsViewModel
import com.danidev.appmovil2.ui.style.TabletScaling
import com.danidev.appmovil2.ui.style.TextScaling
import com.danidev.appmovil2.ui.style.appTokens
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import com.danidev.appmovil2.ui.theme.LocalThemeController
import com.danidev.appmovil2.ui.theme.SharedPreferencesThemeStore
import com.danidev.appmovil2.ui.theme.ThemeController
import com.danidev.appmovil2.ui.theme.ThemeMode
import com.danidev.appmovil2.ui.theme.ThemeViewModel
import com.danidev.appmovil2.ui.tour.LocalTourController
import com.danidev.appmovil2.ui.tour.SharedPreferencesTourStore
import com.danidev.appmovil2.ui.tour.TourController
import com.danidev.appmovil2.ui.tour.TourDialog
import com.danidev.appmovil2.ui.tour.TourViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    // Fondo elegido por el usuario, guardado entre sesiones.
    private val backgroundViewModel: BackgroundViewModel by viewModels {
        viewModelFactory {
            initializer { BackgroundViewModel(SharedPreferencesBackgroundStore(applicationContext)) }
        }
    }

    // Tour guiado: se muestra la primera vez y se puede repetir desde Ajustes.
    private val tourViewModel: TourViewModel by viewModels {
        viewModelFactory {
            initializer { TourViewModel(SharedPreferencesTourStore(applicationContext)) }
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
            val background by backgroundViewModel.choice.collectAsState()
            // Tamaño del texto de toda la app (accesibilidad), elegido en Ajustes.
            val gameSettings by rememberGameSettingsViewModel().settings.collectAsState()
            val textSize = gameSettings.textSize
            val tourVisible by tourViewModel.visible.collectAsState()
            val tourStep by tourViewModel.stepIndex.collectAsState()
            LaunchedEffect(Unit) { tourViewModel.showIfFirstTime() }

            CompositionLocalProvider(
                LocalThemeController provides ThemeController(
                    isDark = darkTheme,
                    onDarkChange = themeViewModel::setDarkTheme,
                    mode = themeMode,
                    onModeChange = themeViewModel::setThemeMode
                ),
                LocalPlayerName provides PlayerNameController(
                    name = playerName,
                    onNameChange = playerNameViewModel::setName
                ),
                LocalStyleController provides StyleController(
                    style = appStyle,
                    onStyleChange = styleViewModel::select
                ),
                LocalBackgroundController provides BackgroundController(
                    choice = background,
                    onSelectColor = backgroundViewModel::selectColor,
                    onUseStyleDefault = backgroundViewModel::useStyleDefault,
                    onGradientChange = backgroundViewModel::setGradient
                ),
                LocalTourController provides TourController(onShowTour = tourViewModel::start),
                LocalAppTokens provides applyBackground(appTokens(appStyle, darkTheme), background)
            ) {
                Appmovil2Theme(darkTheme = darkTheme) {
                    TabletScaling {
                        TextScaling(factor = textSize.factor) {
                            // Sin ondulado de Material: se usa el efecto de presionar propio de la app.
                            CompositionLocalProvider(
                                LocalIndication provides PressIndication,
                                LocalRippleConfiguration provides null
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    val navController = rememberNavController()
                                    AppNavGraph(navController = navController)
                                }

                                if (tourVisible) {
                                    TourDialog(
                                        steps = tourViewModel.steps,
                                        stepIndex = tourStep,
                                        onNext = tourViewModel::next,
                                        onBack = tourViewModel::back,
                                        onSkip = tourViewModel::finish
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
