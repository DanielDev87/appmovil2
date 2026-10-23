package com.danidev.appmovil2.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.danidev.appmovil2.ui.dice.DicePalettePicker
import com.danidev.appmovil2.ui.dice.DiceSizeSlider
import com.danidev.appmovil2.ui.dice.DiceTypeSelector
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.dice.LocalDicePalette
import com.danidev.appmovil2.ui.dice.RollHistory
import com.danidev.appmovil2.ui.dice.RollStatistics
import com.danidev.appmovil2.ui.dice.rememberDicePaletteViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceSizeViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceTypeViewModel
import com.danidev.appmovil2.ui.game.DiceBoard
import com.danidev.appmovil2.ui.game.DiceCountSelector
import com.danidev.appmovil2.ui.game.GameStatus
import com.danidev.appmovil2.ui.game.GameTopBar
import com.danidev.appmovil2.ui.game.RollButton
import com.danidev.appmovil2.ui.game.RollSum
import com.danidev.appmovil2.ui.game.ShareResultButton
import com.danidev.appmovil2.ui.player.LocalPlayerName
import com.danidev.appmovil2.ui.settings.SettingsSheet
import com.danidev.appmovil2.ui.settings.rememberGameSettingsViewModel
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import com.danidev.appmovil2.ui.theme.LocalThemeController
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
fun GameScreen(diceViewModel: DiceViewModel = viewModel(), onNavigateToMenu: () -> Unit = {}) {
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

    val isDark = LocalThemeController.current.isDark
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF381B46), Color(0xFF13091B)))
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.background
            )
        )
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        GameTopBar(
            haptics = haptics,
            isSoundMuted = settings.soundMuted,
            onToggleSound = { settingsViewModel.setSoundMuted(!settings.soundMuted) },
            onNavigateToMenu = onNavigateToMenu,
            onOpenSettings = { showSettings = true }
        )

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

            DiceCountSelector(
                diceCount = uiState.diceCount,
                enabled = !isRolling,
                onSelect = diceViewModel::setDiceCount
            )

            // HU-07: selector del número de caras. Al cambiarlo empieza una partida nueva.
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

            DiceBoard(
                values = currentDiceValues,
                lockedIndices = uiState.lockedIndices,
                diceType = diceType,
                palette = palette,
                sizeFactor = sizeFactor,
                rotation = rotation,
                scale = scale,
                isDark = isDark,
                canToggleLock = !isRolling && outcome == GameOutcome.IN_PROGRESS,
                onToggleLock = diceViewModel::toggleLock
            )

            RollSum(
                unlockedValues = currentDiceValues.filterIndexed { index, _ ->
                    index !in uiState.lockedIndices
                },
                isDark = isDark
            )

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

            GameStatus(uiState = uiState)

            Spacer(modifier = Modifier.height(8.dp))

            RollButton(enabled = canRoll, onClick = triggerRoll)

            if (outcome != GameOutcome.IN_PROGRESS) {
                OutlinedButton(
                    onClick = resetGame,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.play_again))
                }
            }

            if (uiState.launches > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                ShareResultButton(uiState = uiState)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HU-08: reinicia el contador de tiros consecutivos.
            IconButton(onClick = diceViewModel::resetRollCount) {
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
