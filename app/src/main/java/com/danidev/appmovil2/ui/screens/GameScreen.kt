package com.danidev.appmovil2.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.R
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.audio.rememberDiceSoundManager
import com.danidev.appmovil2.haptics.rememberDiceHaptics
import com.danidev.appmovil2.sensor.rememberShakeDetector
import com.danidev.appmovil2.ui.dice.DiceTypeSelector
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.dice.rememberDicePaletteViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceSizeViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceTypeViewModel
import com.danidev.appmovil2.ui.game.DiceBoard
import com.danidev.appmovil2.ui.game.DiceCountSelector
import com.danidev.appmovil2.ui.game.GameOverDialog
import com.danidev.appmovil2.ui.game.GameStatus
import com.danidev.appmovil2.ui.game.GameTopBar
import com.danidev.appmovil2.ui.game.DiceStage
import com.danidev.appmovil2.ui.game.RecentRolls
import com.danidev.appmovil2.ui.game.RollButton
import com.danidev.appmovil2.ui.game.ScoreCards
import com.danidev.appmovil2.ui.game.ShareResultButton
import com.danidev.appmovil2.ui.player.LocalPlayerName
import com.danidev.appmovil2.ui.settings.SettingsSheet
import com.danidev.appmovil2.ui.settings.rememberGameSettingsViewModel
import com.danidev.appmovil2.ui.style.AppBackground
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var rollAnimationKey by remember { mutableIntStateOf(0) }

    // HU-15: gestor de sonido vinculado al ciclo de vida.
    val soundManager = rememberDiceSoundManager()
    val settingsViewModel = rememberGameSettingsViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    // Pantalla de fin de partida: se puede cerrar para ver cómo quedó y vuelve a mostrarse
    // en la partida siguiente.
    var gameOverDismissed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(uiState.outcome) {
        if (uiState.outcome == GameOutcome.IN_PROGRESS) gameOverDismissed = false
    }
    LaunchedEffect(settings.soundMuted) {
        soundManager.setMuted(settings.soundMuted)
    }

    // HU-16: vibración al detenerse el dado; el usuario puede desactivarla.
    val haptics = rememberDiceHaptics()

    val coroutineScope = rememberCoroutineScope()

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

    val tokens = LocalAppTokens.current

    val hasUnlockedDice = uiState.lockedIndices.size < uiState.diceCount
    val canRoll = !isRolling && outcome == GameOutcome.IN_PROGRESS && hasUnlockedDice

    val triggerRoll: () -> Unit = {
        if (canRoll) {
            coroutineScope.launch {
                diceViewModel.startRoll()
                isRolling = true

                // HU-15: sonido de lanzamiento (inicio del tiro).
                soundManager.playRollSound()

                rollAnimationKey++

                val lockedIndices = uiState.lockedIndices
                val initialValues = currentDiceValues
                repeat(7) {
                    currentDiceValues = List(uiState.diceCount) { index ->
                        if (index in lockedIndices) {
                            initialValues.getOrElse(index) { 1 }
                        } else {
                            diceType.roll()
                        }
                    }
                    delay(75)
                }

                val finalValues = List(uiState.diceCount) { index ->
                    if (index in lockedIndices) {
                        initialValues.getOrElse(index) { 1 }
                    } else {
                        diceType.roll()
                    }
                }
                currentDiceValues = finalValues

                // HU-15: sonido de impacto (fin del tiro).
                soundManager.playLandSound()

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
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            GameTopBar(
                isSoundMuted = settings.soundMuted,
                onToggleSound = { settingsViewModel.setSoundMuted(!settings.soundMuted) },
                onNavigateToMenu = onNavigateToMenu,
                onOpenSettings = { showSettings = true }
            )

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

                // HU-06 y HU-07: modo de 1 o 2 dados y número de caras. Cambiar las caras
                // empieza una partida nueva.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiceCountSelector(
                        diceCount = uiState.diceCount,
                        enabled = !isRolling,
                        onSelect = diceViewModel::setDiceCount,
                        modifier = Modifier.width(200.dp)
                    )
                    DiceTypeSelector(
                        selected = diceType,
                        enabled = !isRolling,
                        onSelect = { type ->
                            if (type != diceType) {
                                typeViewModel.select(type)
                                resetGame()
                            }
                        }
                    )
                }

                DiceStage(
                    indicator = uiState.rollIndicator,
                    sum = currentDiceValues
                        .filterIndexed { index, _ -> index !in uiState.lockedIndices }
                        .sum()
                ) {
                    DiceBoard(
                        values = currentDiceValues,
                        lockedIndices = uiState.lockedIndices,
                        diceType = diceType,
                        palette = palette,
                        sizeFactor = sizeFactor,
                        rotation = rotation,
                        scale = scale,
                        canToggleLock = !isRolling && outcome == GameOutcome.IN_PROGRESS,
                        onToggleLock = diceViewModel::toggleLock
                    )
                }

                GameStatus(
                    uiState = uiState,
                    shakeEnabled = settings.shakeEnabled,
                    onResetRollCount = diceViewModel::resetRollCount
                )

                RollButton(enabled = canRoll, onClick = triggerRoll)

                if (outcome != GameOutcome.IN_PROGRESS) {
                    StyledButton(onClick = resetGame, height = 52.dp) {
                        Text(
                            text = stringResource(R.string.play_again),
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.onAccent
                        )
                    }
                }

                if (uiState.launches > 0) {
                    ShareResultButton(uiState = uiState)
                }

                RecentRolls(
                    history = uiState.history,
                    onOpenStats = onNavigateToStats
                )
            }
        }
    }

    if (outcome != GameOutcome.IN_PROGRESS && !isRolling && !gameOverDismissed) {
        GameOverDialog(
            uiState = uiState,
            playerName = LocalPlayerName.current.displayName,
            onPlayAgain = resetGame,
            onNavigateToMenu = onNavigateToMenu,
            onDismiss = { gameOverDismissed = true }
        )
    }

    if (showSettings) {
        SettingsSheet(
            palette = palette,
            onPaletteChange = paletteViewModel::selectPalette,
            sizeFactor = sizeFactor,
            diceCount = uiState.diceCount,
            onSizeChange = sizeViewModel::setFactor,
            haptics = haptics,
            settings = settings,
            onSoundMutedChange = settingsViewModel::setSoundMuted,
            onShakeChange = settingsViewModel::setShakeEnabled,
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
