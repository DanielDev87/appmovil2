package com.danidev.appmovil2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.haptics.rememberDiceHaptics
import com.danidev.appmovil2.ui.dice.DiceFace
import com.danidev.appmovil2.ui.dice.DicePalette
import com.danidev.appmovil2.ui.player.LocalPlayerName
import com.danidev.appmovil2.ui.player.StyledNameField
import com.danidev.appmovil2.ui.progress.ProgressDialog
import com.danidev.appmovil2.ui.progress.rememberProgressViewModel
import com.danidev.appmovil2.ui.settings.SettingsSheet
import com.danidev.appmovil2.ui.settings.rememberGameOptions
import com.danidev.appmovil2.ui.style.AppBackground
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.MAX_CONTENT_WIDTH
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.style.StyledIconButton
import com.danidev.appmovil2.ui.style.isTablet
import com.danidev.appmovil2.ui.theme.ThemeSwitch

/**
 * Pantalla de bienvenida: título, saludo con el nombre del jugador (HU-12) y el botón para empezar.
 * Jugar con amigos y el resto de opciones se eligen en Ajustes, que son los mismos que dentro de la
 * partida.
 *
 * El contenido se desplaza si no cabe y el botón de jugar queda fijo abajo.
 */
@Composable
fun WelcomeScreen(onStartClicked: () -> Unit, onNavigateToAbout: () -> Unit = {}) {
    val tokens = LocalAppTokens.current
    val isTablet = isTablet()
    var visible by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    val progress by rememberProgressViewModel().progress.collectAsState()
    val haptics = rememberDiceHaptics()

    LaunchedEffect(Unit) {
        visible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dicePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val heroDice = if (isTablet) 150.dp else 118.dp

    AppBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // HU-03: selector de tema claro / oscuro.
                ThemeSwitch()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyledIconButton(
                        icon = Icons.Filled.EmojiEvents,
                        contentDescription = stringResource(R.string.progress_open),
                        onClick = { showProgress = true }
                    )
                    StyledIconButton(
                        icon = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.welcome_settings),
                        onClick = { showSettings = true }
                    )
                    StyledIconButton(
                        icon = Icons.Filled.Info,
                        contentDescription = stringResource(R.string.about_open),
                        onClick = onNavigateToAbout
                    )
                }
            }

            // El contenido se reparte con espacios iguales y, si no cabe, se desplaza.
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxHeight)
                        .verticalScroll(rememberScrollState())
                ) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(800)) +
                            slideInVertically(initialOffsetY = { -50 })
                    ) {
                        Hero(heroDice = heroDice, pulse = pulse)
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) +
                            slideInVertically(initialOffsetY = { 50 })
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .widthIn(max = MAX_CONTENT_WIDTH)
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 8.dp)
                        ) {
                            // HU-12: nombre principal (jugador 1).
                            StyledNameField()
                        }
                    }
                }
            }

            // El botón de jugar queda fijo abajo: no se desplaza con el resto, siempre está a la vista.
            StyledButton(
                onClick = onStartClicked,
                modifier = Modifier
                    .widthIn(max = MAX_CONTENT_WIDTH)
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        if (tokens.isPlayful) R.string.welcome_play_playful else R.string.welcome_play
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.onAccent
                )
            }
        }
    }

    if (showProgress) {
        ProgressDialog(progress = progress, onDismiss = { showProgress = false })
    }

    if (showSettings) {
        SettingsSheet(
            options = rememberGameOptions(haptics = haptics),
            onDismiss = { showSettings = false }
        )
    }
}

/** Los dos dados, el título y el saludo con el nombre. */
@Composable
private fun Hero(heroDice: Dp, pulse: Float) {
    val tokens = LocalAppTokens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.scale(pulse)
        ) {
            DiceFace(
                value = 5,
                sides = 6,
                palette = DicePalette.ROSA,
                contentDescription = "",
                modifier = Modifier
                    .size(heroDice)
                    .rotate(-8f)
            )
            DiceFace(
                value = 2,
                sides = 6,
                palette = DicePalette.LILA,
                contentDescription = "",
                modifier = Modifier
                    .size(heroDice)
                    .rotate(10f)
            )
        }

        Spacer(Modifier.height(12.dp))

        val title = "Up Dice"
        Text(
            text = if (tokens.isPlayful) title.uppercase() else title,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = if (tokens.isPlayful) 2.sp else 0.sp,
            color = tokens.bgInk,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.welcome_tagline),
            fontSize = 16.sp,
            color = tokens.bgMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // HU-12: saludo con el nombre. Se reserva el espacio aunque no haya nombre para que la pantalla
        // no se mueva al empezar a escribir.
        val playerName = LocalPlayerName.current.displayName
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 12.dp)
                .height(32.dp)
        ) {
            if (playerName.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.player_greeting, playerName),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.bgInk,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
