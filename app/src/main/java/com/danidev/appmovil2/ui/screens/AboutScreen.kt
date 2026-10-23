package com.danidev.appmovil2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceFace
import com.danidev.appmovil2.ui.dice.DicePalette
import com.danidev.appmovil2.ui.style.AppBackground
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard
import com.danidev.appmovil2.ui.style.StyledIconButton
import com.danidev.appmovil2.ui.theme.ThemeSwitch

/** Pantalla "Acerca de": qué es la app, su versión y el equipo, con el estilo elegido. */
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val tokens = LocalAppTokens.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                StyledIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.about_back),
                    onClick = onNavigateBack
                )
                val title = stringResource(R.string.about_open)
                Text(
                    text = if (tokens.isPlayful) title.uppercase() else title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = if (tokens.isPlayful) 1.sp else 0.sp,
                    color = tokens.bgInk,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
                // HU-03: selector de tema claro / oscuro.
                ThemeSwitch()
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp)
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { -40 })
                ) {
                    StyledCard(radius = 32.dp, tilt = if (tokens.isPlayful) -4f else 0f) {
                        DiceFace(
                            value = 6,
                            sides = 6,
                            palette = DicePalette.LILA,
                            contentDescription = stringResource(R.string.about_logo),
                            modifier = Modifier
                                .padding(12.dp)
                                .size(110.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700, delayMillis = 100))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.about_app_name),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.bgInk,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StyledCard(
                            color = if (tokens.isPlayful) tokens.highlight else tokens.soft,
                            radius = 999.dp
                        ) {
                            Text(
                                text = stringResource(R.string.about_version),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tokens.isPlayful) tokens.onAccent else tokens.ink,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    AboutCard(
                        title = stringResource(R.string.about_what_title),
                        body = stringResource(R.string.about_what_body),
                        tilt = if (tokens.isPlayful) -1f else 0f
                    )
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700, delayMillis = 350)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    AboutCard(
                        title = stringResource(R.string.about_team_title),
                        body = stringResource(R.string.about_team_body),
                        tilt = if (tokens.isPlayful) 1f else 0f
                    )
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700, delayMillis = 600))
                ) {
                    Text(
                        text = stringResource(R.string.about_footer),
                        fontSize = 12.sp,
                        color = tokens.bgMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutCard(title: String, body: String, tilt: Float) {
    val tokens = LocalAppTokens.current
    StyledCard(tilt = tilt, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = tokens.ink
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                fontSize = 14.sp,
                color = tokens.muted,
                lineHeight = 22.sp
            )
        }
    }
}
