package com.danidev.appmovil2

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Vibration
import com.danidev.appmovil2.haptics.rememberDiceHaptics
import com.danidev.appmovil2.audio.rememberDiceSoundManager
import com.danidev.appmovil2.sensor.rememberShakeDetector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
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
import com.danidev.appmovil2.ui.dice.DiceFace
import com.danidev.appmovil2.ui.dice.DicePalettePicker
import com.danidev.appmovil2.ui.dice.DiceTypeSelector
import com.danidev.appmovil2.ui.dice.rememberDiceTypeViewModel
import com.danidev.appmovil2.ui.dice.DiceSize
import com.danidev.appmovil2.ui.dice.DiceSizeSlider
import com.danidev.appmovil2.ui.dice.rememberDiceSizeViewModel
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.dice.LocalDicePalette
import com.danidev.appmovil2.ui.dice.RollHistory
import com.danidev.appmovil2.ui.dice.RollStatistics
import com.danidev.appmovil2.ui.dice.diceImageRes
import com.danidev.appmovil2.ui.dice.rememberDicePaletteViewModel
import com.danidev.appmovil2.ui.navigation.AppNavGraph
import com.danidev.appmovil2.ui.player.LocalPlayerName
import com.danidev.appmovil2.ui.player.PlayerNameController
import com.danidev.appmovil2.ui.player.PlayerNameViewModel
import com.danidev.appmovil2.ui.player.SharedPreferencesPlayerNameStore
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import com.danidev.appmovil2.ui.theme.LocalThemeController
import com.danidev.appmovil2.ui.theme.SharedPreferencesThemeStore
import com.danidev.appmovil2.ui.theme.ThemeController
import com.danidev.appmovil2.ui.theme.ThemeMode
import com.danidev.appmovil2.ui.theme.ThemeSwitch
import com.danidev.appmovil2.ui.theme.ThemeViewModel
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext

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

            CompositionLocalProvider(
                LocalThemeController provides ThemeController(
                    isDark = darkTheme,
                    onDarkChange = themeViewModel::setDarkTheme
                ),
                LocalPlayerName provides PlayerNameController(
                    name = playerName,
                    onNameChange = playerNameViewModel::setName
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
fun MoverDados(diceViewModel: DiceViewModel = viewModel(), onNavigateToMenu: () -> Unit = {}) {
    // Historial y estado del dado (HU-06 / HU-011); se recompone con cada nuevo registro.
    val uiState by diceViewModel.uiState.collectAsState()

    // Paleta de color del dado (HU-04), guardada entre sesiones.
    val paletteViewModel = rememberDicePaletteViewModel()
    val palette by paletteViewModel.palette.collectAsState()

    // Tipo de dado (HU-07: D4, D6, D8, D12, D20), guardado entre sesiones.
    val typeViewModel = rememberDiceTypeViewModel()
    val diceType by typeViewModel.type.collectAsState()
    // Tamaño del dado (HU-05), elegido con un slider y guardado entre sesiones.
    val sizeViewModel = rememberDiceSizeViewModel()
    val sizeFactor by sizeViewModel.factor.collectAsState()

    var currentDiceValues by remember { mutableStateOf(uiState.currentDiceValues) }
    val score = uiState.score
    val launches = uiState.launches
    val outcome = uiState.outcome
    var isRolling by remember { mutableStateOf(false) }
    var rollAnimationKey by remember { mutableIntStateOf(0) }
    // HU-08 y HU-09: contador de tiros e indicador de crítico/pifia viven en el ViewModel.
    val rollCount = uiState.rollCount
    val rollIndicator = uiState.rollIndicator

    // HU-15: Gestor de sonido SoundPool vinculado al ciclo de vida
    val soundManager = rememberDiceSoundManager()
    var isSoundMuted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isSoundMuted) {
        soundManager.setMuted(isSoundMuted)
    }

    // HU-16: vibración al detenerse el dado; el usuario puede desactivarla.
    val haptics = rememberDiceHaptics()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Reinicia la partida; se usa al pulsar "Jugar de nuevo" y al cambiar el tipo de dado
    // (HU-07), porque los puntos de un dado no son comparables con los de otro.
    val resetGame: () -> Unit = {
        soundManager.stopRollSound()
        diceViewModel.clearHistory()
        diceViewModel.clearLocks()
        currentDiceValues = List(uiState.diceCount) { 1 }
    }

    LaunchedEffect(uiState.diceCount, uiState.currentDiceValues) {
        if (!isRolling) {
            currentDiceValues = uiState.currentDiceValues
        }
    }

    val rotation by animateFloatAsState(
        targetValue = rollAnimationKey * 720f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "diceRotation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isRolling) 0.85f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "diceScale"
    )

    val isDark = LocalThemeController.current.isDark
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF381B46),
                Color(0xFF13091B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.background
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Barra superior con botones funcionales sin superposicion ni bloqueo de toques
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Botón para regresar al menú principal
                IconButton(onClick = onNavigateToMenu) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_to_menu),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // HU-03: selector de tema claro / oscuro.
                ThemeSwitch()
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // HU-16: Botón para activar o desactivar la vibración.
                IconButton(onClick = { haptics.setVibrationEnabled(!haptics.isEnabled) }) {
                    Icon(
                        imageVector = Icons.Filled.Vibration,
                        contentDescription = stringResource(
                            if (haptics.isEnabled) R.string.haptics_enabled else R.string.haptics_disabled
                        ),
                        tint = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = if (haptics.isEnabled) 1f else 0.4f)
                    )
                }

                // HU-15: Botón para alternar sonido (silenciar / activar)
                IconButton(
                    onClick = {
                        isSoundMuted = !isSoundMuted
                    }
                ) {
                    Icon(
                        imageVector = if (isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = stringResource(
                            if (isSoundMuted) R.string.sound_disabled else R.string.sound_enabled
                        ),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // HU-12: saludo personalizado con el nombre del jugador.
            val playerName = LocalPlayerName.current.displayName
            if (playerName.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.player_turn, playerName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

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

            // HU-07: selector desplegable del número de caras. Al cambiarlo empieza una partida nueva.
            DiceTypeSelector(
                selected = diceType,
                enabled = !isRolling,
                onSelect = { type ->
                    if (type != diceType) {
                        typeViewModel.select(type)
                        resetGame()
                    }
                },
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // HU-06: Renderizado de múltiples dados simultáneos
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                // HU-05: el slider cambia el Modifier.size() del dado; con 2 dados se limita
                // para que sigan cabiendo en el ancho de la pantalla.
                val diceSizeDp = DiceSize.sizeDp(currentDiceValues.size, sizeFactor)
                val diceSize = diceSizeDp.dp
                val dicePadding = DiceSize.paddingDp(diceSizeDp).dp

                currentDiceValues.forEachIndexed { index, dieVal ->
                    val isLocked = index in uiState.lockedIndices
                    Card(
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isLocked) 4.dp else if (isDark) 6.dp else 12.dp
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLocked) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else if (isDark) {
                                Color(0xFF2C183B).copy(alpha = 0.65f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isLocked) {
                            BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                        } else if (isDark) {
                            BorderStroke(1.dp, Color(0xFFF6B5DA).copy(alpha = 0.25f))
                        } else null,
                        modifier = Modifier.clickable(
                            enabled = !isRolling && outcome == GameOutcome.IN_PROGRESS
                        ) {
                            diceViewModel.toggleLock(index)
                        }
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            // HU-07: con D6 se dibuja con puntos; con otros dados, en número estilizado.
                            DiceFace(
                                value = dieVal,
                                sides = diceType.sides,
                                palette = palette,
                                contentDescription = if (isLocked) {
                                    stringResource(R.string.dice_locked_description, dieVal)
                                } else {
                                    dieVal.toString()
                                },
                                modifier = Modifier
                                    .size(diceSize)
                                    .padding(dicePadding)
                                    .graphicsLayer {
                                        val s = if (isLocked) 1f else scale
                                        scaleX = s
                                        scaleY = s
                                        rotationZ = if (isLocked) 0f else if (index % 2 == 0) rotation else -rotation
                                    }
                            )
                            if (isLocked) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = stringResource(R.string.dice_locked_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // HU-06: Cálculo sumatorio de los dados no bloqueados
            val unlockedDiceValues = currentDiceValues.filterIndexed { index, _ -> index !in uiState.lockedIndices }
            val currentSum = unlockedDiceValues.sum()
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isDark) Color(0xFF381C48).copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer,
                border = if (isDark) BorderStroke(1.dp, Color(0xFFF6B5DA).copy(alpha = 0.25f)) else null,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (unlockedDiceValues.size == 2) {
                        stringResource(
                            R.string.dice_sum_multiple,
                            unlockedDiceValues.getOrElse(0) { 1 },
                            unlockedDiceValues.getOrElse(1) { 1 },
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

            // HU-05: slider para cambiar el tamaño del dado.
            DiceSizeSlider(
                factor = sizeFactor,
                diceCount = uiState.diceCount,
                onFactorChange = sizeViewModel::setFactor,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))


            Text(stringResource(R.string.score, score, TARGET_SCORE))
            Text(stringResource(R.string.launches, launches, MAX_LAUNCHES))
            Text(stringResource(R.string.roll_count,rollCount ))
            when (outcome) {
                GameOutcome.WON -> Text(stringResource(R.string.game_won))
                GameOutcome.LAUNCH_LIMIT_REACHED -> Text(stringResource(R.string.launch_limit_reached))
                GameOutcome.IN_PROGRESS -> Unit
            }

            // HU-09: indicador visual de crítico o pifia.
            when (rollIndicator) {
                RollIndicator.CRITICAL -> {
                    Text(
                        text = "🎯 ¡CRÍTICO!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                RollIndicator.FUMBLE -> {
                    Text(
                        text = "💥 ¡PIFIA!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                RollIndicator.NONE -> Unit
            }

            Spacer(modifier = Modifier.height(8.dp))

            val hasUnlockedDice = uiState.lockedIndices.size < uiState.diceCount

            val triggerRoll: () -> Unit = {
                if (!isRolling && outcome == GameOutcome.IN_PROGRESS && hasUnlockedDice) {
                    coroutineScope.launch {
                        diceViewModel.startRoll()
                        isRolling = true

                        // HU-15: Reproducir sonido de lanzamiento (ciclo de vida - inicio del tiro)
                        soundManager.playRollSound()

                        rollAnimationKey++

                        val initialValues = currentDiceValues
                        repeat(7) {
                            currentDiceValues = List(uiState.diceCount) { index ->
                                if (index in uiState.lockedIndices) {
                                    initialValues.getOrElse(index) { 1 }
                                } else {
                                    diceType.roll()
                                }
                            }
                            delay(75)
                        }

                        val finalValues = List(uiState.diceCount) { index ->
                            if (index in uiState.lockedIndices) {
                                initialValues.getOrElse(index) { 1 }
                            } else {
                                diceType.roll()
                            }
                        }
                        currentDiceValues = finalValues

                        // HU-15: Reproducir sonido de impacto de dados (ciclo de vida - fin del tiro)
                        soundManager.playLandSound()

                        val unlockedValues = finalValues.filterIndexed { index, _ -> index !in uiState.lockedIndices }
                        diceViewModel.registerResult(finalValues, unlockedValues, diceType.sides)

                        // HU-16: vibración al detenerse el dado; más larga en crítico o pifia.
                        haptics.vibrateOnLand(
                            strong = diceViewModel.uiState.value.rollIndicator != RollIndicator.NONE
                        )

                        isRolling = false
                    }
                }
            }

            // HU-19: Detección de sacudida por acelerómetro para lanzar dados
            rememberShakeDetector(
                enabled = !isRolling && outcome == GameOutcome.IN_PROGRESS && hasUnlockedDice,
                onShake = triggerRoll
            )

            Button(
                onClick = triggerRoll,
                enabled = !isRolling && outcome == GameOutcome.IN_PROGRESS && hasUnlockedDice,
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
                    onClick = resetGame,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.play_again))
                }
            }

            // HU 18: Compartir resultado.
            if (launches > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                val shareMessage = if (outcome == GameOutcome.WON) {
                    stringResource(R.string.share_message_won, score, launches)
                } else {
                    stringResource(R.string.share_message, score, TARGET_SCORE, launches)
                }
                val chooserTitle = stringResource(R.string.share_chooser_title)
                val noApp = stringResource(R.string.share_no_app)

                OutlinedButton(
                    onClick = { shareResult(context, shareMessage, chooserTitle, noApp) },
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth(0.55f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.share_result),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            IconButton (
                onClick = diceViewModel::resetRollCount
            ) {
                Image(
                    painter = painterResource(R.drawable.reiniciar),
                    contentDescription = "Reiniciar contador",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // HU-011: lista desplazable con los últimos 10 lanzamientos.
            CompositionLocalProvider(LocalDicePalette provides palette) {
                RollHistory(history = uiState.history)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HU-13: cuántas veces ha salido cada cara en la partida.
            RollStatistics(counts = uiState.faceCounts, faces = diceType.sides)

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * HU 18: Compartir resultado.
 * Lanza un Intent ACTION_SEND para enviar el texto a una app externa
 * (WhatsApp, Gmail, Mensajes, etc.).
 */
private fun shareResult(
    context: android.content.Context,
    message: String,
    chooserTitle: String,
    noAppMessage: String
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    val chooser = Intent.createChooser(sendIntent, chooserTitle)
    if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, noAppMessage, Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun MoverDadosPreview() {
    Appmovil2Theme {
        MoverDados()
    }
}
