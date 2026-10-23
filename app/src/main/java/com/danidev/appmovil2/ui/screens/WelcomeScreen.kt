package com.danidev.appmovil2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.player.LocalPlayerName
import com.danidev.appmovil2.ui.player.PlayerNameField
import com.danidev.appmovil2.ui.style.LocalStyleController
import com.danidev.appmovil2.ui.style.PersonalizeSheet
import com.danidev.appmovil2.ui.theme.ThemeSwitch

@Composable
fun WelcomeScreen(onStartClicked: () -> Unit, onNavigateToAbout: () -> Unit = {}) {
    var visible by remember { mutableStateOf(false) }
    var showPersonalize by remember { mutableStateOf(false) }
    val styleController = LocalStyleController.current

    LaunchedEffect(Unit) {
        visible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dicePulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
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
            )
    ) {
        // HU-03: selector de tema claro / oscuro.
        ThemeSwitch(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
        )
        IconButton(
            onClick = { showPersonalize = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 56.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Palette,
                contentDescription = stringResource(R.string.personalize_open),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(
            onClick = onNavigateToAbout,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Acerca de",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { -50 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = "Up Dice \uD83C\uDFB2",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        lineHeight = 60.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Con cada lanzamiento, una nueva emoción",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // HU-12: saludo con el nombre del jugador. Se reserva el espacio aunque
                    // no haya nombre para que la pantalla no se mueva al empezar a escribir.
                    val playerName = LocalPlayerName.current.displayName
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .height(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playerName.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.player_greeting, playerName),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 500)) + slideInVertically(initialOffsetY = { 50 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // HU-12: campo para escribir el nombre del jugador.
                    PlayerNameField(modifier = Modifier.fillMaxWidth(0.8f))

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onStartClicked,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(
                            text = "Jugar ahora",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showPersonalize) {
        PersonalizeSheet(
            style = styleController.style,
            onStyleChange = styleController.onStyleChange,
            onDismiss = { showPersonalize = false }
        )
    }
}
