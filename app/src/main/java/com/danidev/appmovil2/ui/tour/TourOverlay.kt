package com.danidev.appmovil2.ui.tour

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.style.StyledCard
import kotlin.math.roundToInt

/** Cuánto se agranda el recuadro resaltado respecto al elemento, para que no lo corte. */
private val SPOTLIGHT_PADDING = 8.dp

/** Altura que se supone a la explicación hasta que se mide de verdad. */
private val CARD_HEIGHT_GUESS = 230.dp

/**
 * Tour dinámico sobre la pantalla real: oscurece todo menos el elemento que explica, lo rodea con un
 * resplandor que late y pone al lado una tarjeta con su explicación.
 *
 * Si el paso es [TourStep.interactive], el elemento resaltado queda libre para tocarlo de verdad; lo que
 * pasa con el resto de la pantalla (oscurecida) es nada, porque el toque se bloquea. Sin elemento
 * señalado, la tarjeta va al centro.
 *
 * @param index Posición del paso actual, desde 0.
 * @param total Cantidad de pasos del tour.
 */
@Composable
fun TourOverlay(
    step: TourStep,
    index: Int,
    total: Int,
    isLast: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val density = LocalDensity.current
    val target = step.target?.let { LocalTourTargets.current.bounds[it] }
    val padding = with(density) { SPOTLIGHT_PADDING.toPx() }
    val spotlight = target?.let {
        Rect(it.left - padding, it.top - padding, it.right + padding, it.bottom + padding)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // El recuadro se desliza de un elemento al siguiente.
        val shown = spotlight ?: Rect(Offset(screenWidth / 2f, screenHeight / 2f), 0f)
        val animated by animateRectAsState(targetValue = shown, animationSpec = tween(350), label = "spotlight")

        val pulse by rememberInfiniteTransition(label = "tourPulse").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "tourPulseValue"
        )

        // 1. Fondo oscuro con un hueco redondeado y un anillo que late alrededor.
        val accent = tokens.accent
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scrim = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, Size(size.width, size.height)))
                if (spotlight != null) {
                    addRoundRect(RoundRect(animated, CornerRadius(cornerFor(animated))))
                }
            }
            drawPath(scrim, Color.Black.copy(alpha = 0.72f))
            if (spotlight != null) {
                val grow = 3.dp.toPx() * pulse
                val ring = Rect(animated.left - grow, animated.top - grow, animated.right + grow, animated.bottom + grow)
                drawRoundRect(
                    color = accent.copy(alpha = 0.95f - 0.45f * pulse),
                    topLeft = ring.topLeft,
                    size = ring.size,
                    cornerRadius = CornerRadius(cornerFor(ring)),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // 2. Bloqueo de toques: todo, o todo menos el elemento si el paso es interactivo.
        if (spotlight == null || !step.interactive) {
            Blocker(Modifier.fillMaxSize())
        } else {
            val hole = animated
            fun dp(px: Float) = with(density) { px.coerceAtLeast(0f).toDp() }
            // Arriba, abajo, izquierda y derecha del hueco.
            Blocker(Modifier.offset { IntOffset(0, 0) }.size(dp(screenWidth), dp(hole.top)))
            Blocker(
                Modifier
                    .offset { IntOffset(0, hole.bottom.roundToInt()) }
                    .size(dp(screenWidth), dp(screenHeight - hole.bottom))
            )
            Blocker(
                Modifier
                    .offset { IntOffset(0, hole.top.roundToInt()) }
                    .size(dp(hole.left), dp(hole.height))
            )
            Blocker(
                Modifier
                    .offset { IntOffset(hole.right.roundToInt(), hole.top.roundToInt()) }
                    .size(dp(screenWidth - hole.right), dp(hole.height))
            )
        }

        // 3. Explicación, colocada junto al elemento sin taparlo.
        val cardWidth = with(density) { minOf(maxWidth - 32.dp, 420.dp).toPx() }
        var cardSize by remember { mutableStateOf(IntSize.Zero) }
        val cardHeight = if (cardSize.height > 0) cardSize.height.toFloat() else with(density) { CARD_HEIGHT_GUESS.toPx() }
        val position = tooltipPosition(
            target = spotlight,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            gap = with(density) { 16.dp.toPx() },
            margin = with(density) { 16.dp.toPx() }
        )
        val offset by animateIntOffsetAsState(
            targetValue = IntOffset(position.x.roundToInt(), position.y.roundToInt()),
            animationSpec = tween(350),
            label = "tourCardOffset"
        )
        Box(
            modifier = Modifier
                .offset { offset }
                .width(with(density) { cardWidth.toDp() })
                .onSizeChanged { cardSize = it }
        ) {
            StepCard(
                step = step,
                index = index,
                total = total,
                isLast = isLast,
                onNext = onNext,
                onBack = onBack,
                onSkip = onSkip
            )
        }
    }
}

/** Radio de las esquinas del recuadro: redondo en un botón, suave en una tarjeta grande. */
private fun cornerFor(rect: Rect): Float = minOf(minOf(rect.width, rect.height) / 2f, 56f)

/** Zona transparente que se come los toques para que no lleguen a lo que hay debajo. */
@Composable
private fun Blocker(modifier: Modifier) {
    Box(modifier = modifier.pointerInput(Unit) { detectTapGestures { } })
}

/** Tarjeta con el paso, su explicación, la pista de qué tocar y los botones. */
@Composable
private fun StepCard(
    step: TourStep,
    index: Int,
    total: Int,
    isLast: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val tokens = LocalAppTokens.current
    StyledCard(radius = 26.dp, modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.tour_step_counter, index + 1, total),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.accent
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Progreso: una barrita por cada paso, hasta el actual rellenas.
                    val dots = 10
                    val filled = ((index + 1) * dots / total.toFloat()).roundToInt().coerceIn(1, dots)
                    repeat(dots) { dot ->
                        Box(
                            modifier = Modifier
                                .size(width = 10.dp, height = 5.dp)
                                .background(
                                    if (dot < filled) tokens.accent else tokens.muted.copy(alpha = 0.3f),
                                    CircleShape
                                )
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "tourText"
            ) { shownStep ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(shownStep.titleRes),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.ink
                    )
                    Text(
                        text = stringResource(shownStep.bodyRes),
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = tokens.muted
                    )
                    if (shownStep.interactive) {
                        Text(
                            text = "👆 " + stringResource(R.string.tour_try_it),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.accent
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isLast) TextAction(stringResource(R.string.tour_skip), onSkip)
                if (index > 0) TextAction(stringResource(R.string.tour_back), onBack)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    StyledButton(onClick = onNext, height = 46.dp, modifier = Modifier.widthIn(max = 150.dp)) {
                        Text(
                            text = stringResource(if (isLast) R.string.tour_finish else R.string.tour_next),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.onAccent,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
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
