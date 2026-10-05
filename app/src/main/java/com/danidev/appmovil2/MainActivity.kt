package com.danidev.appmovil2

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import com.danidev.appmovil2.ui.dice.DicePalettePicker
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.dice.LocalDicePalette
import com.danidev.appmovil2.ui.dice.RollHistory
import com.danidev.appmovil2.ui.dice.diceImageRes
import com.danidev.appmovil2.ui.dice.rememberDicePaletteViewModel
import com.danidev.appmovil2.ui.navigation.AppNavGraph
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import com.danidev.appmovil2.ui.theme.LocalThemeController
import com.danidev.appmovil2.ui.theme.SharedPreferencesThemeStore
import com.danidev.appmovil2.ui.theme.ThemeController
import com.danidev.appmovil2.ui.theme.ThemeMode
import com.danidev.appmovil2.ui.theme.ThemeSwitch
import com.danidev.appmovil2.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {

    // HU-03: preferencia de tema (claro / oscuro), guardada entre sesiones.
    private val themeViewModel: ThemeViewModel by viewModels {
        viewModelFactory {
            initializer { ThemeViewModel(SharedPreferencesThemeStore(applicationContext)) }
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
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { darkTheme }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { darkTheme }
                    )
                )
                onDispose {}
            }

            CompositionLocalProvider(
                LocalThemeController provides ThemeController(
                    isDark = darkTheme,
                    onDarkChange = themeViewModel::setDarkTheme
                )
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

/**
 * Pantalla principal: muestra uno o dos dados (HU-06), el botón para lanzarlos,
 * el cálculo sumatorio y el historial de los últimos lanzamientos (HU-011).
 *
 * La animación del lanzamiento usa estado local (`currentDiceValues`, `isRolling`) porque
 * es puramente visual. El resultado final se registra en [DiceViewModel], que
 * es quien conserva el historial.
 *
 * @param diceViewModel ViewModel con el historial y modo de dados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoverDados(diceViewModel: DiceViewModel = viewModel()) {
    // Historial y estado del dado (HU-06 / HU-011); se recompone con cada nuevo registro.
    val uiState by diceViewModel.uiState.collectAsState()

    // Paleta de color del dado (HU-04), guardada entre sesiones.
    val paletteViewModel = rememberDicePaletteViewModel()
    val palette by paletteViewModel.palette.collectAsState()

    var currentDiceValues by remember { mutableStateOf(uiState.currentDiceValues) }
    var score by remember { mutableIntStateOf(0) }
    var launches by remember { mutableIntStateOf(0) }
    val outcome = determineGameOutcome(score, launches)
    var isRolling by remember { mutableStateOf(false) }
    var rollAnimationKey by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.diceCount, uiState.currentDiceValues) {
        if (!isRolling) {
            currentDiceValues = uiState.currentDiceValues
        }
    }

    val rotation by animateFloatAsState(
        targetValue = rollAnimationKey * 720f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "diceRotation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isRolling) 0.8f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "diceScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // HU-03: selector de tema claro / oscuro.
        ThemeSwitch(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 8.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp, horizontal = 16.dp)
        ) {
            // HU-06: Selección de modo de dados (1 Dado / 2 Dados)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.dice_mode_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 8.dp)
                )
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = uiState.diceCount == 1,
                        onClick = {
                            if (!isRolling) {
                                diceViewModel.setDiceCount(1)
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(stringResource(R.string.one_die))
                    }
                    SegmentedButton(
                        selected = uiState.diceCount == 2,
                        onClick = {
                            if (!isRolling) {
                                diceViewModel.setDiceCount(2)
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(stringResource(R.string.two_dice))
                    }
                }
            }

            // HU-06: Renderizado de múltiples dados simultáneos
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                val diceSize = if (currentDiceValues.size > 1) 130.dp else 200.dp
                val dicePadding = if (currentDiceValues.size > 1) 16.dp else 32.dp

                currentDiceValues.forEachIndexed { index, dieVal ->
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Image(
                            painter = painterResource(diceImageRes(dieVal)),
                            contentDescription = dieVal.toString(),
                            colorFilter = palette.colorFilter,
                            modifier = Modifier
                                .size(diceSize)
                                .padding(dicePadding)
                                .scale(scale)
                                .rotate(if (index % 2 == 0) rotation else -rotation)
                        )
                    }
                }
            }

            // HU-06: Cálculo sumatorio de los dados
            val currentSum = currentDiceValues.sum()
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (currentDiceValues.size == 2) {
                        stringResource(
                            R.string.dice_sum_multiple,
                            currentDiceValues.getOrElse(0) { 1 },
                            currentDiceValues.getOrElse(1) { 1 },
                            currentSum
                        )
                    } else {
                        stringResource(R.string.dice_sum_single, currentSum)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // HU-04: selector de paleta de color del dado.
            DicePalettePicker(
                selected = palette,
                onSelect = paletteViewModel::selectPalette
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.score, score, TARGET_SCORE))
            Text(stringResource(R.string.launches, launches, MAX_LAUNCHES))
            when (outcome) {
                GameOutcome.WON -> Text(stringResource(R.string.game_won))
                GameOutcome.LAUNCH_LIMIT_REACHED -> Text(stringResource(R.string.launch_limit_reached))
                GameOutcome.IN_PROGRESS -> Unit
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!isRolling && outcome == GameOutcome.IN_PROGRESS) {
                        coroutineScope.launch {
                            isRolling = true
                            rollAnimationKey++

                            repeat(9) {
                                currentDiceValues = List(uiState.diceCount) { (1..6).random() }
                                delay(100)
                            }

                            val finalValues = List(uiState.diceCount) { (1..6).random() }
                            currentDiceValues = finalValues
                            diceViewModel.registerResult(finalValues)
                            score += finalValues.sum()
                            launches++
                            isRolling = false
                        }
                    }
                },
                enabled = !isRolling && outcome == GameOutcome.IN_PROGRESS,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(
                    text = stringResource(R.string.roll),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (outcome != GameOutcome.IN_PROGRESS) {
                OutlinedButton(
                    onClick = {
                        diceViewModel.clearHistory()
                        currentDiceValues = List(uiState.diceCount) { 1 }
                        score = 0
                        launches = 0
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.play_again))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HU-011: lista desplazable con los últimos 10 lanzamientos.
            CompositionLocalProvider(LocalDicePalette provides palette) {
                RollHistory(history = uiState.history)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoverDadosPreview() {
    Appmovil2Theme {
        MoverDados()
    }
}

