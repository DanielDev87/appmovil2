package com.danidev.appmovil2.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import com.danidev.appmovil2.ui.dice.DiceUiState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
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
import com.danidev.appmovil2.MAX_PLAYERS
import com.danidev.appmovil2.R
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.audio.rememberDiceSoundManager
import com.danidev.appmovil2.haptics.rememberDiceHaptics
import com.danidev.appmovil2.sensor.rememberShakeDetector
import com.danidev.appmovil2.ui.dice.DiceType
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.dice.effectiveDiceSides
import com.danidev.appmovil2.ui.dice.rememberDicePaletteViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceSizeViewModel
import com.danidev.appmovil2.ui.dice.rememberDiceTypeViewModel
import com.danidev.appmovil2.ui.game.DiceBoard
import com.danidev.appmovil2.ui.game.GameInfoPanel
import com.danidev.appmovil2.ui.game.GameOverDialog
import com.danidev.appmovil2.ui.game.GameStatus
import com.danidev.appmovil2.ui.game.GameTopBar
import com.danidev.appmovil2.ui.game.DiceStage
import com.danidev.appmovil2.ui.game.RecentRolls
import com.danidev.appmovil2.ui.game.PlayAgainButton
import com.danidev.appmovil2.ui.game.ResetGameDialog
import com.danidev.appmovil2.ui.game.RecordsStrip
import com.danidev.appmovil2.ui.progress.PlayerProgress
import com.danidev.appmovil2.ui.player.resolvePlayerNames
import com.danidev.appmovil2.ui.game.PlayersBar
import com.danidev.appmovil2.ui.game.RollButton
import com.danidev.appmovil2.ui.game.ScoreCards
import com.danidev.appmovil2.ui.game.ShareResultButton
import com.danidev.appmovil2.ui.player.LocalPlayerName
import com.danidev.appmovil2.ui.progress.GameResult
import com.danidev.appmovil2.ui.progress.rememberProgressViewModel
import com.danidev.appmovil2.ui.settings.SettingsSheet
import com.danidev.appmovil2.ui.settings.rememberGameOptions
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

/** Distancia mínima (en píxeles) que hay que deslizar el dado hacia arriba para lanzarlo. */
private const val SWIPE_THROW_DISTANCE_PX = 120f

/** Punto del lanzamiento (de 0 a 1) en el que el dado toca la mesa por primera vez. */
private const val THROW_LANDING = 0.7f

/** Pausa (ms) entre el último tiro y la pantalla de fin de partida. */
private const val GAME_OVER_DELAY_MS = 450L

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
    // Caras de cada dado en juego: el primero usa el tipo elegido y los demás pueden ser distintos.
    val diceSides = effectiveDiceSides(settings.diceCount, diceType.sides, settings.extraDiceSides)
    var showSettings by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Pantalla de fin de partida: se puede cerrar para ver cómo quedó y vuelve a mostrarse
    // en la partida siguiente.
    var gameOverDismissed by rememberSaveable { mutableStateOf(false) }
    // Se espera un momento tras el último tiro para que se vea el dado antes de la pantalla final.
    var showGameOver by remember { mutableStateOf(false) }
    // HU-16: vibración al detenerse el dado; el usuario puede desactivarla.
    val haptics = rememberDiceHaptics()

    // Récords, racha y logros: se guarda cada partida terminada una sola vez.
    val progressViewModel = rememberProgressViewModel()
    val lastRecord by progressViewModel.lastRecord.collectAsState()
    val progress by progressViewModel.progress.collectAsState()
    var recorded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(uiState.outcome, isRolling) {
        if (uiState.outcome != GameOutcome.IN_PROGRESS && !isRolling) {
            if (!recorded) {
                recorded = true
                // Los récords y logros son de una sola persona; con varios jugadores no cuentan.
                if (!uiState.isMultiplayer) progressViewModel.recordGame(
                    GameResult(
                        won = uiState.outcome == GameOutcome.WON,
                        score = uiState.score,
                        launches = uiState.launches,
                        criticals = uiState.criticalCount,
                        sides = diceSides.first()
                    )
                )
            }
            delay(GAME_OVER_DELAY_MS)
            showGameOver = true
            // Con varios jugadores siempre hay quien gane, así que se celebra siempre.
            haptics.vibrateOnGameOver(uiState.isMultiplayer || uiState.outcome == GameOutcome.WON)
        } else {
            showGameOver = false
        }
    }
    LaunchedEffect(uiState.outcome) {
        if (uiState.outcome == GameOutcome.IN_PROGRESS) {
            gameOverDismissed = false
            recorded = false
            progressViewModel.clearLastRecord()
        }
    }
    // La meta (puntos y lanzamientos) se puede cambiar en Ajustes sin perder la partida en curso.
    // Cantidad de dados: se elige en Ajustes (también desde la bienvenida) y la partida la aplica.
    LaunchedEffect(settings.diceCount) {
        diceViewModel.setDiceCount(settings.diceCount)
    }
    // Cantidad de jugadores: al cambiarla, la partida empieza de cero con los marcadores nuevos.
    LaunchedEffect(settings.playerCount) {
        diceViewModel.setPlayerCount(settings.playerCount)
    }
    val firstPlayerName = LocalPlayerName.current.displayName
    val fallbackNames = (1..MAX_PLAYERS).map { stringResource(R.string.player_default_name, it) }
    val playerNames = resolvePlayerNames(
        count = uiState.players.size,
        firstName = firstPlayerName,
        extras = settings.playerNames,
        fallback = { number -> fallbackNames[number - 1] }
    )
    LaunchedEffect(settings.targetScore, settings.maxLaunches) {
        diceViewModel.setGoals(settings.targetScore, settings.maxLaunches)
    }
    LaunchedEffect(settings.soundMuted) {
        soundManager.setMuted(settings.soundMuted)
    }


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
    // "Jugar de nuevo" reemplaza a "Tirar" solo después de que el jugador vio la ventana de fin de
    // partida; así, si tira rápido, no reinicia por accidente antes de enterarse de cómo terminó.
    val showPlayAgain = outcome != GameOutcome.IN_PROGRESS && gameOverDismissed
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
                        DiceType.fromSides(diceSides.getOrElse(index) { diceType.sides }).roll()
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
                val unlockedSides = diceSides.filterIndexed { index, _ -> index !in lockedIndices }
                diceViewModel.registerResult(
                    values = finalValues,
                    activeValues = unlockedValues,
                    diceSides = unlockedSides,
                    modifier = settings.rollModifier
                )

                // HU-16: vibración al detenerse el dado; distinta en crítico, pifia o un tiro normal.
                haptics.vibrateOnResult(diceViewModel.uiState.value.rollIndicator)

                isRolling = false
            }
        }
    }

    // Deslizar el dado hacia arriba también lo lanza, como si se tirara con la mano.
    val currentTrigger by rememberUpdatedState(triggerRoll)
    val swipeToThrow = Modifier.pointerInput(Unit) {
        var dragged = 0f
        detectVerticalDragGestures(
            onDragStart = { dragged = 0f },
            onVerticalDrag = { _, dy -> dragged += dy },
            onDragEnd = { if (dragged < -SWIPE_THROW_DISTANCE_PX) currentTrigger() }
        )
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
                onReset = { if (!isRolling) showResetConfirm = true },
                turnName = if (uiState.isMultiplayer) {
                    playerNames.getOrElse(uiState.currentPlayer) { "" }
                } else {
                    firstPlayerName
                }
            )

            // Piezas de la partida; se acomodan distinto en vertical y en una tablet horizontal.
            val stage: @Composable (Modifier, Dp) -> Unit = { stageModifier, stageMinHeight ->
                DiceStage(
                    indicator = uiState.rollIndicator,
                    sum = currentDiceValues
                        .filterIndexed { index, _ -> index !in uiState.lockedIndices }
                        .sum(),
                    rollModifier = settings.rollModifier,
                    modifier = stageModifier,
                    minHeight = stageMinHeight
                ) {
                    DiceBoard(
                        values = currentDiceValues,
                        lockedIndices = uiState.lockedIndices,
                        diceSides = diceSides,
                        palette = palette,
                        sizeFactor = sizeFactor,
                        throwProgress = throwAnimation.value,
                        canToggleLock = !isRolling && outcome == GameOutcome.IN_PROGRESS,
                        onToggleLock = diceViewModel::toggleLock
                    )
                }
            }
            // Botón principal: tirar, o jugar de nuevo cuando la partida terminó y ya se vio el aviso.
            val mainButton: @Composable () -> Unit = {
                if (showPlayAgain) {
                    PlayAgainButton(onClick = resetGame)
                } else {
                    RollButton(enabled = canRoll, onClick = triggerRoll)
                }
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
                        stage(swipeToThrow.weight(1f), 180.dp)
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
                                diceSides = diceSides,
                                rollCount = uiState.rollCount,
                                onClick = { showSettings = true }
                            )
                            RecordsOrPlayers(uiState, progress, playerNames)
                            RecentRolls(
                                history = uiState.history,
                                onOpenStats = onNavigateToStats,
                                palette = palette,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        if (outcome != GameOutcome.IN_PROGRESS) {
                            GameStatus(uiState = uiState)
                        }
                        ShareSlot(uiState = uiState, names = playerNames)
                        mainButton()
                    }
                }
            } else {
                // Vertical: arriba los marcadores y los datos; en el centro el dado con los últimos
                // tiros pegados debajo; el botón principal queda fijo abajo, al alcance del pulgar.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    ScoreCards(uiState = uiState)
                    GameInfoPanel(
                        diceCount = uiState.diceCount,
                        diceSides = diceSides,
                        rollCount = uiState.rollCount,
                        onClick = { showSettings = true }
                    )
                    RecordsOrPlayers(uiState, progress, playerNames)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // El escenario se ajusta al tamaño del dado; el bloque queda cerca de los
                    // marcadores: sobra mucho menos espacio arriba que abajo.
                    Spacer(modifier = Modifier.weight(0.3f))
                    stage(swipeToThrow, 0.dp)
                    if (outcome != GameOutcome.IN_PROGRESS) {
                        GameStatus(uiState = uiState)
                    }
                    // Los últimos tiros van sin tarjeta, directamente sobre el fondo.
                    RecentRolls(
                        history = uiState.history,
                        onOpenStats = onNavigateToStats,
                        palette = palette,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.weight(1.7f))
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp)
                ) {
                    // Compartir aparece desde el primer tiro, justo encima del botón principal. Su
                    // lugar se reserva desde el inicio para que nada cambie de tamaño al tirar.
                    ShareSlot(uiState = uiState, names = playerNames)
                    mainButton()
                }
            }
        }
    }

    if (showGameOver && !gameOverDismissed) {
        GameOverDialog(
            uiState = uiState,
            playerName = firstPlayerName,
            names = playerNames,
            record = lastRecord,
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
            options = rememberGameOptions(
                haptics = haptics,
                isRolling = isRolling,
                onDiceTypeChanged = {
                    // Los puntos y lanzamientos siguen; solo se limpian los dados en pantalla.
                    diceViewModel.onDiceTypeChanged()
                    currentDiceValues = List(uiState.diceCount) { 1 }
                }
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

/** Alto del botón de compartir; el espacio se reserva aunque todavía no se muestre. */
private val SHARE_SLOT_HEIGHT = 44.dp

/** Botón de compartir desde el primer tiro; antes, un espacio vacío del mismo alto. */
@Composable
private fun ShareSlot(uiState: DiceUiState, names: List<String>) {
    if (uiState.launches > 0) {
        ShareResultButton(uiState = uiState, names = names)
    } else {
        Spacer(modifier = Modifier.height(SHARE_SLOT_HEIGHT))
    }
}

/**
 * Con una persona, sus récords (racha, victorias y a superar); con varias, el marcador de todos los
 * jugadores con el del turno resaltado.
 */
@Composable
private fun RecordsOrPlayers(uiState: DiceUiState, progress: PlayerProgress, names: List<String>) {
    if (uiState.isMultiplayer) {
        PlayersBar(players = uiState.players, names = names, currentPlayer = uiState.currentPlayer)
    } else {
        RecordsStrip(progress = progress)
    }
}
