package com.danidev.appmovil2.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Ancho máximo del contenido en pantallas grandes, para que no se estire en tablets. */
val MAX_CONTENT_WIDTH = 600.dp

/**
 * Dibuja el fondo de una tarjeta según el estilo vigente.
 *
 * - Equilibrado: sombra suave, y en tema oscuro un borde fino para marcar la tarjeta.
 * - Juguetón: borde grueso y sombra dura desplazada; [tilt] la inclina unos grados.
 */
@Composable
fun Modifier.styledSurface(
    color: Color = LocalAppTokens.current.surface,
    radius: Dp = LocalAppTokens.current.cardRadius,
    tilt: Float = 0f
): Modifier {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(radius)
    return if (tokens.isPlayful) {
        val offsetPx = tokens.shadowOffset
        this
            .graphicsLayer { rotationZ = tilt }
            .drawBehind {
                val o = offsetPx.toPx()
                drawRoundRect(
                    color = tokens.shadow,
                    topLeft = Offset(o, o),
                    size = size,
                    cornerRadius = CornerRadius(radius.toPx())
                )
            }
            .clip(shape)
            .background(color)
            .border(tokens.borderWidth, tokens.outline, shape)
    } else {
        this
            .shadow(
                elevation = if (tokens.isDark) 0.dp else 8.dp,
                shape = shape,
                ambientColor = tokens.shadow.copy(alpha = 0.25f),
                spotColor = tokens.shadow.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(color)
            .then(
                if (tokens.borderWidth > 0.dp) {
                    Modifier.border(tokens.borderWidth, tokens.outline, shape)
                } else {
                    Modifier
                }
            )
    }
}

/** Tarjeta que se adapta al estilo vigente. */
@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    color: Color = LocalAppTokens.current.surface,
    radius: Dp = LocalAppTokens.current.cardRadius,
    tilt: Float = 0f,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.styledSurface(color = color, radius = radius, tilt = tilt),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Botón grande del estilo vigente. El contenido se centra y debe usar
 * `LocalAppTokens.current.onAccent` (o el color que corresponda a [color]) para su texto.
 */
@Composable
fun StyledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = LocalAppTokens.current.accent,
    height: Dp = 64.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .alpha(if (enabled) 1f else 0.5f)
            .styledSurface(color = color, radius = tokens.buttonRadius)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        content = content
    )
}

/** Fondo de pantalla del estilo vigente: degradado, o puntos decorativos en el juguetón. */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(tokens.bgTop, tokens.bgBottom)))
            .drawBehind {
                val dots = tokens.dots ?: return@drawBehind
                val step = 24.dp.toPx()
                val radius = 2.5.dp.toPx()
                var y = step / 2
                while (y < size.height) {
                    var x = step / 2
                    while (x < size.width) {
                        drawCircle(dots, radius, Offset(x, y))
                        x += step
                    }
                    y += step
                }
            },
        content = content
    )
}

/** Botón redondo con un ícono, para las barras superiores. */
@Composable
fun StyledIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalAppTokens.current.ink
) {
    val tokens = LocalAppTokens.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .styledSurface(radius = 22.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
