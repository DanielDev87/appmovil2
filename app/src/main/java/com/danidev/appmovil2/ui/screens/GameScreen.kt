package com.danidev.appmovil2.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.R
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.audio.rememberDiceSoundManager
import com.danidev.appmovil2.haptics.rememberDiceHaptics
import com.danidev.appmovil2.sensor.rememberShakeDetector
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.dice.rememberDicePaletteViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceSizeViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceTypeViewModel
import com.danidev.appmovil2.ui.game.DiceBoard
import com.danidev.appmovil2.ui.game.GameInfoPanel
import com.danidev.appmovil2.ui.game.GameModeChip
import com.danidev.appmovil2.ui.game.GameOverDialog
import com.danidev.appmovil2.ui.game.GameStatus
import com.danidev.appmovil2.ui.game.GameTopBar
import com.danidev.appmovil2.ui.game.DiceStage
import com.danidev.appmovil2.ui.game.RecentRolls
import com.danidev.appmovil2.ui.game.PlayAgainButton
import com.danidev.appmovil2.ui.game.ResetGameDialog
import com.danidev.appmovil2.ui.game.RollButton
import com.danidev.appmovil2.ui.game.ScoreCards
import com.danidev.appmovil2.ui.game.ShareResultButton
import com.danidev.appmovil2.ui.player.LocalPlayerName
import com.danidev.appmovil2.ui.settings.GameOptions
import com.danidev.appmovil2.ui.settings.SettingsSheet
import com.danidev.appmovil2.ui.settings.rememberGameSettingsViewModel
import com.danidev.appmovil2.ui.style.AppBackground
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.gameContentMaxWidth
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.isTablet
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Punto del lanzamiento (de 0 a 1) en el que el dado toca la mesa por primera vez. */
private const val THROW_LANDING = 0.7f

/** Pausa (ms) entre el último tiro y la pantalla de fin de partida. */
private const val GAME_OVER_DELAY_MS = 800L

/**
 * Pantalla de juego: muestra uno o dos dados (HU-06), el botón para lanzarlos,
 * el cálculo sumatorio y el historial de los últimos lanzamientos (HU-011).
 *
 * La animación del lanzamiento usa estado local (`currentDiceValues`, `isRolling`) porque
 * es puramente visual. El resultado final se registra en [DiceViewModel], que
 * conserva el historial, los puntos y los contadores de la partida.
 *
 * @param diceViewModel ViewModel con el historial y el estado de la partida.
 */
@Composable
fun GameScreen(
    diceViewModel: DiceViewModel = viewModel(),
    onNavigateToMenu: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
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
    val outcome = uiState.outcome
    var isRolling by remember { mutableStateOf(false) }
    // Avance del lanzamiento: 0 al lanzar y 1 con los dados quietos.
    val throwAnimation = remember { Animatable(1f) }

    // HU-15: gestor de sonido vinculado al ciclo de vida.
    val soundManager = rememberDiceSoundManager()
    val settingsViewModel = rememberGameSettingsViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Pantalla de fin de partida: se puede cerrar para ver cómo quedó y vuelve a mostrarse
    // en la partida siguiente.
    var gameOverDismissed by rememberSaveable { mutableStateOf(false) }
    // Se espera un momento tras el último tiro para que se vea el dado antes de la pantalla final.
    var showGameOver by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.outcome, isRolling) {
        if (uiState.outcome != GameOutcome.IN_PROGRESS && !isRolling) {
            delay(GAME_OVER_DELAY_MS)
            showGameOver = true
        } else {
            showGameOver = false
        }
    }
    LaunchedEffect(uiState.outcome) {
        if (uiState.outcome == GameOutcome.IN_PROGRESS) gameOverDismissed = false
    }
    // La meta (puntos y lanzamientos) se puede cambiar en Ajustes sin perder la partida en curso.
    LaunchedEffect(settings.targetScore, settings.maxLaunches) {
        diceViewModel.setGoals(settings.targetScore, settings.maxLaunches)
    }
    LaunchedEffect(settings.soundMuted) {
        soundManager.setMuted(settings.soundMuted)
    }

    // HU-16: vibración al detenerse el dado; el usuario puede desactivarla.
    val haptics = rememberDiceHaptics()

    val coroutineScope = rememberCoroutineScope()

    // Reinicia la partida al pulsar "Jugar de nuevo".
    val resetGame: () -> Unit = {
        soundManager.stopRollSound()
        diceViewModel.clearHistory()
        diceViewModel.clearLocks()
        diceViewModel.resetRollCount()
        currentDiceValues = List(uiState.diceCount) { 1 }
    }

    LaunchedEffect(uiState.diceCount, uiState.currentDiceValues) {
        if (!isRolling) {
            currentDiceValues = uiState.currentDiceValues
        }
    }

    val tokens = LocalAppTokens.current
    val configuration = LocalConfiguration.current
    val isWideLandscape = isTablet() && configuration.screenWidthDp > configuration.screenHeightDp

    val hasUnlockedDice = uiState.lockedIndices.size < uiState.diceCount
    val canRoll = !isRolling && outcome == GameOutcome.IN_PROGRESS && hasUnlockedDice

    val triggerRoll: () -> Unit = {
        if (canRoll) {
            coroutineScope.launch {
                diceViewModel.startRoll()
                isRolling = true

                // HU-15: sonido de lanzamiento (inicio del tiro).
                soundManager.playRollSound()

                // El resultado se decide al lanzar, así los números de las caras no cambian
                // mientras el dado gira y se detiene mostrando exactamente ese valor.
                val lockedIndices = uiState.lockedIndices
                val finalValues = List(uiState.diceCount) { index ->
                    if (index in lockedIndices) {
                        currentDiceValues.getOrElse(index) { 1 }
                    } else {
                        diceType.roll()
                    }
                }
                currentDiceValues = finalValues

                // El dado sube girando, cae (suena el impacto) y rebota hasta quedarse quieto.
                throwAnimation.snapTo(0f)
                throwAnimation.animateTo(THROW_LANDING, tween(durationMillis = 750, easing = LinearEasing))
                // HU-15: sonido de impacto de los dados.
                soundManager.playLandSound()
                throwAnimation.animateTo(1f, tween(durationMillis = 350, easing = LinearEasing))

                val unlockedValues = finalValues.filterIndexed { index, _ -> index !in lockedIndices }
                diceViewModel.registerResult(finalValues, unlockedValues, diceType.sides)

                // HU-16: vibración al detenerse el dado; más larga en crítico o pifia.
                haptics.vibrateOnLand(
                    strong = diceViewModel.uiState.value.rollIndicator != RollIndicator.NONE
                )

                isRolling = false
            }
        }
    }

    // HU-19: detección de sacudida por acelerómetro para lanzar los dados.
    rememberShakeDetector(enabled = canRoll && settings.shakeEnabled, onShake = triggerRoll)

    AppBackground {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight()
                .widthIn(max = gameContentMaxWidth())
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            GameTopBar(
                isSoundMuted = settings.soundMuted,
                onToggleSound = { settingsViewModel.setSoundMuted(!settings.soundMuted) },
                onNavigateToMenu = onNavigateToMenu,
                onOpenSettings = { showSettings = true },
                onReset = { if (!isRolling) showResetConfirm = true }
            )

            // Piezas de la partida; se acomodan distinto en vertical y en una tablet horizontal.
            val modeChip: @Composable () -> Unit = {
                // HU-06 y HU-07: los dados y sus caras se cambian desde los ajustes; aquí solo
                // se muestra un resumen.
                GameModeChip(
                    diceCount = uiState.diceCount,
                    diceType = diceType,
                    onClick = { showSettings = true }
                )
            }
            val stage: @Composable (Modifier, Dp) -> Unit = { stageModifier, stageMinHeight ->
                DiceStage(
                    indicator = uiState.rollIndicator,
                    sum = currentDiceValues
                        .filterIndexed { index, _ -> index !in uiState.lockedIndices }
                        .sum(),
                    modifier = stageModifier,
                    minHeight = stageMinHeight
                ) {
                    DiceBoard(
                        values = currentDiceValues,
                        lockedIndices = uiState.lockedIndices,
                        diceType = diceType,
                        palette = palette,
                        sizeFactor = sizeFactor,
                        throwProgress = throwAnimation.value,
                        canToggleLock = !isRolling && outcome == GameOutcome.IN_PROGRESS,
                        onToggleLock = diceViewModel::toggleLock
                    )
                }
            }
            val actions: @Composable () -> Unit = {
                GameStatus(uiState = uiState)

                // Al terminar la partida, "Jugar de nuevo" ocupa el lugar del botón de tirar.
                if (outcome == GameOutcome.IN_PROGRESS) {
                    RollButton(enabled = canRoll, onClick = triggerRoll)
                } else {
                    PlayAgainButton(onClick = resetGame)
                }

                if (uiState.launches > 0) {
                    ShareResultButton(uiState = uiState)
                }

                RecentRolls(
                    history = uiState.history,
                    onOpenStats = onNavigateToStats
                )
            }

            if (isWideLandscape) {
                // Tablet horizontal: a la izquierda los marcadores y el dado; a la derecha los datos
                // de la partida y los últimos tiros, con el botón de tirar fijo abajo.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        ScoreCards(uiState = uiState)
                        stage(Modifier.weight(1f), 180.dp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            GameInfoPanel(
                                diceCount = uiState.diceCount,
                                diceType = diceType,
                                rollCount = uiState.rollCount,
                                onClick = { showSettings = true }
                            )
                            StyledCard(modifier = Modifier.fillMaxWidth()) {
                                RecentRolls(
                                    history = uiState.history,
                                    onOpenStats = onNavigateToStats,
                                    onCard = true,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        if (outcome != GameOutcome.IN_PROGRESS) {
                            GameStatus(uiState = uiState)
                        }
                        if (uiState.launches > 0) {
                            ShareResultButton(uiState = uiState)
                        }
                        // Al terminar la partida, "Jugar de nuevo" ocupa el lugar del botón de tirar.
                        if (outcome == GameOutcome.IN_PROGRESS) {
                            RollButton(enabled = canRoll, onClick = triggerRoll)
                        } else {
                            PlayAgainButton(onClick = resetGame)
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    ScoreCards(uiState = uiState)
                    modeChip()
                    stage(Modifier, if (isTablet()) 300.dp else 230.dp)
                    actions()
                }
            }
        }
    }

    if (showGameOver && !gameOverDismissed) {
        GameOverDialog(
            uiState = uiState,
            playerName = LocalPlayerName.current.displayName,
            onPlayAgain = resetGame,
            onNavigateToMenu = onNavigateToMenu,
            onDismiss = { gameOverDismissed = true }
        )
    }

    if (showResetConfirm) {
        ResetGameDialog(
            onConfirm = {
                resetGame()
                showResetConfirm = false
            },
            onDismiss = { showResetConfirm = false }
        )
    }

    if (showSettings) {
        SettingsSheet(
            options = GameOptions(
                palette = palette,
                onPaletteChange = paletteViewModel::selectPalette,
                sizeFactor = sizeFactor,
                onSizeChange = sizeViewModel::setFactor,
                diceCount = uiState.diceCount,
                onDiceCountChange = diceViewModel::setDiceCount,
                diceType = diceType,
                onDiceTypeChange = { type ->
                    if (type != diceType) {
                        typeViewModel.select(type)
                        // Los puntos y lanzamientos siguen; solo se limpian los dados en pantalla.
                        diceViewModel.onDiceTypeChanged()
                        currentDiceValues = List(uiState.diceCount) { 1 }
                    }
                },
                haptics = haptics,
                settings = settings,
                onSoundMutedChange = settingsViewModel::setSoundMuted,
                onShakeChange = settingsViewModel::setShakeEnabled,
                onTargetScoreChange = settingsViewModel::setTargetScore,
                onMaxLaunchesChange = settingsViewModel::setMaxLaunches,
                isRolling = isRolling
            ),
            onDismiss = { showSettings = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    Appmovil2Theme {
        GameScreen()
    }
}
