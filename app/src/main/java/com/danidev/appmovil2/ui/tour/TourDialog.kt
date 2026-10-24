package com.danidev.appmovil2.ui.tour

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Tour guiado de la app: una ventana con un paso a la vez (ícono animado, título y explicación),
 * puntos que indican en cuál va y los botones Atrás, Siguiente y Omitir.
 *
 * Usa los colores y el estilo vigentes, así que cambia con el estilo, el tema y el fondo elegidos.
 *
 * @param stepIndex Posición del paso actual.
 * @param onNext Avanza; en el último paso termina el tour.
 * @param onBack Vuelve al paso anterior.
 * @param onSkip Cierra el tour sin terminarlo.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TourDialog(
    steps: List<TourStep>,
    stepIndex: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isLast = stepIndex == steps.lastIndex

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        ) {
            StyledCard(
                radius = 32.dp,
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp)
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    // Contenido del paso: cambia con un deslizamiento hacia el lado en que se avanza.
                    AnimatedContent(
                        targetState = stepIndex,
                        transitionSpec = {
                            val forward = targetState > initialState
                            (slideInHorizontally(tween(300)) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(300)))
                                .togetherWith(
                                    slideOutHorizontally(tween(300)) { if (forward) -it / 3 else it / 3 } + fadeOut(tween(200))
                                )
                                .using(SizeTransform(clip = false))
                        },
                        label = "tourStep"
                    ) { index ->
                        StepContent(steps[index])
                    }

                    Dots(count = steps.size, selected = stepIndex)

                    StyledButton(onClick = onNext, height = 54.dp) {
                        Text(
                            text = stringResource(if (isLast) R.string.tour_finish else R.string.tour_next),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.onAccent
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                        if (stepIndex > 0) {
                            TextAction(stringResource(R.string.tour_back), onBack)
                        }
                        if (!isLast) {
                            TextAction(stringResource(R.string.tour_skip), onSkip)
                        }
                    }
                }
            }
        }
    }
}

/** Ícono animado, título y explicación de un paso. */
@Composable
private fun StepContent(step: TourStep) {
    val tokens = LocalAppTokens.current
    val pulse by rememberInfiniteTransition(label = "tourIcon").animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "tourIconScale"
    )
    val lift by rememberInfiniteTransition(label = "tourLift").animateFloat(
        initialValue = 0f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "tourIconLift"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StyledCard(color = tokens.soft, radius = 40.dp, modifier = Modifier.size(104.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(104.dp)) {
                Text(
                    text = step.emoji,
                    fontSize = 52.sp,
                    modifier = Modifier
                        .scale(pulse)
                        // El paso de lanzar sube y baja, como el dado al tirarlo.
                        .graphicsLayer { translationY = if (step == TourStep.ROLL) lift else 0f }
                )
            }
        }
        Text(
            text = stringResource(step.titleRes),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tokens.ink,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(step.bodyRes),
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = tokens.muted,
            textAlign = TextAlign.Center
        )
    }
}

/** Puntos que indican en qué paso va el tour; el actual es más largo y de color de acento. */
@Composable
private fun Dots(count: Int, selected: Int) {
    val tokens = LocalAppTokens.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == selected) 26.dp else 9.dp, height = 9.dp)
                    .background(
                        if (index == selected) tokens.accent else tokens.muted.copy(alpha = 0.35f),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LocalAppTokens.current.ink,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp)
    )
}
