package com.danidev.appmovil2.ui.dice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import com.danidev.appmovil2.ui.theme.LocalThemeController
import kotlin.math.min
import kotlin.math.sqrt

/*
 * Aspecto de los dados: estilo "obsidiana".
 *
 * Todos los dados (D4, D6, D8, D12 y D20) se dibujan como sólidos oscuros con aristas de
 * color brillante y un número —o puntos, en el cubo— en cada cara visible; el resultado
 * del tiro es el más grande. El diseño es el mismo en tema claro y oscuro: solo cambia el
 * color (la paleta elegida pone el acento, y en el tema oscuro las aristas brillan).
 */

/**
 * Cara de un dado, sea del tipo que sea.
 *
 * El tamaño y el giro los decide quien la use mediante [modifier], igual que con la imagen
 * original, así que la animación del lanzamiento funciona con todos los tipos.
 *
 * @param value Valor que muestra el dado (1..[sides]).
 * @param sides Número de caras del dado.
 * @param palette Paleta de color elegida (HU-04); da el color de acento.
 * @param contentDescription Texto para lectores de pantalla.
 * @param modifier Tamaño y transformaciones.
 * @param tumble Giro en el aire durante el lanzamiento; `null` cuando el dado está quieto.
 */
@Composable
fun DiceFace(
    value: Int,
    sides: Int,
    palette: DicePalette,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tumble: DieTumble? = null
) {
    ObsidianDie(
        value = value,
        sides = sides,
        palette = palette,
        isDark = LocalThemeController.current.isDark,
        contentDescription = contentDescription,
        modifier = modifier,
        tumble = tumble
    )
}

/**
 * Giro del dado en el aire durante un lanzamiento, en grados sobre cada eje. Con los tres en 0
 * el dado se ve en reposo.
 */
data class DieTumble(val yaw: Float, val pitch: Float, val roll: Float)

/**
 * Colores de un dado obsidiana.
 *
 * @property light Color de la cara más iluminada.
 * @property dark Color de la cara más oscura.
 * @property accent Color de las aristas y de los números.
 */
private class ObsidianTones(val light: Color, val dark: Color, val accent: Color)

private fun DicePalette.obsidian(): ObsidianTones = when (this) {
    DicePalette.LILA -> ObsidianTones(Color(0xFF3A3466), Color(0xFF0F0D24), Color(0xFFC9A8FF))
    DicePalette.ROSA -> ObsidianTones(Color(0xFF4D2547), Color(0xFF170A1B), Color(0xFFFF8FC8))
    DicePalette.MENTA -> ObsidianTones(Color(0xFF1F4848), Color(0xFF071A1B), Color(0xFF7FF0CF))
}

/** Posición (de 0 a 1 dentro de la cara) de los puntos de cada número del cubo. */
private val PIPS: Map<Int, List<Pair<Float, Float>>> = mapOf(
    1 to listOf(0.5f to 0.5f),
    2 to listOf(0.27f to 0.27f, 0.73f to 0.73f),
    3 to listOf(0.27f to 0.27f, 0.5f to 0.5f, 0.73f to 0.73f),
    4 to listOf(0.27f to 0.27f, 0.27f to 0.73f, 0.73f to 0.27f, 0.73f to 0.73f),
    5 to listOf(0.27f to 0.27f, 0.27f to 0.73f, 0.5f to 0.5f, 0.73f to 0.27f, 0.73f to 0.73f),
    6 to listOf(0.27f to 0.27f, 0.27f to 0.5f, 0.27f to 0.73f, 0.73f to 0.27f, 0.73f to 0.5f, 0.73f to 0.73f)
)

/** Tamaño (en píxeles) por debajo del cual solo se dibuja el número de la cara principal. */
private const val MIN_BOX_FOR_ALL_NUMBERS = 150f

/** Dado obsidiana: caras oscuras, aristas de color y un número en cada cara visible. */
@Composable
private fun ObsidianDie(
    value: Int,
    sides: Int,
    palette: DicePalette,
    isDark: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tumble: DieTumble? = null
) {
    val faces = if (tumble == null) {
        Polyhedra.project(sides)
    } else {
        Polyhedra.tumble(sides, tumble.yaw.toDouble(), tumble.pitch.toDouble(), tumble.roll.toDouble())
    }
    val numbers = Polyhedra.faceValues(sides, value, faces)
    val tones = palette.obsidian()
    val textMeasurer = rememberTextMeasurer()
    val isCube = sides == DiceType.D6.sides

    Canvas(modifier = modifier.semantics { this.contentDescription = contentDescription }) {
        val box = min(size.width, size.height)
        val origin = Offset((size.width - box) / 2f, (size.height - box) / 2f)

        fun at(p: Point2) = Offset(origin.x + p.x * box, origin.y + p.y * box)

        val paths = faces.map { face ->
            Path().apply {
                face.points.forEachIndexed { i, p ->
                    val o = at(p)
                    if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                }
                close()
            }
        }

        // 1. Cuerpo oscuro: cada cara más o menos clara según la luz que recibe.
        faces.forEachIndexed { i, face ->
            drawPath(paths[i], color = lerp(tones.dark, tones.light, 0.2f + 0.8f * face.brightness))
        }

        // 2. Aristas de color; en el tema oscuro brillan (dos trazos anchos y casi transparentes).
        val join = StrokeJoin.Round
        paths.forEach { path ->
            if (isDark) {
                drawPath(path, tones.accent.copy(alpha = 0.10f), style = Stroke(width = box * 0.05f, join = join))
                drawPath(path, tones.accent.copy(alpha = 0.22f), style = Stroke(width = box * 0.026f, join = join))
            }
            drawPath(path, tones.accent.copy(alpha = 0.9f), style = Stroke(width = box * 0.012f, join = join))
        }

        // 3. Números (o puntos en el cubo) en las caras visibles.
        val showAll = box >= MIN_BOX_FOR_ALL_NUMBERS
        faces.forEachIndexed { i, face ->
            if (!showAll && !face.isMain) return@forEachIndexed
            val color = if (face.isMain) tones.accent else tones.accent.copy(alpha = 0.78f)
            if (isCube) {
                drawPips(face, numbers[i], color, box, origin)
            } else if (face.isMain || face.inradius >= MIN_INRADIUS_FOR_NUMBER) {
                drawNumber(textMeasurer, numbers[i].toString(), face, color, box, origin)
            }
        }
    }
}

/** Radio mínimo (en caja de lado 1) para escribir un número en una cara que no es la principal. */
private const val MIN_INRADIUS_FOR_NUMBER = 0.06f

/** Escribe [text] centrado en [face]; el número del resultado es más grande que los demás. */
private fun DrawScope.drawNumber(
    textMeasurer: TextMeasurer,
    text: String,
    face: ProjectedFace,
    color: Color,
    box: Float,
    origin: Offset
) {
    val twoDigits = text.length > 1
    val fontPx = if (face.isMain) {
        min(face.inradius * (if (twoDigits) 1.15f else 1.45f), 0.3f)
    } else {
        min(face.inradius * (if (twoDigits) 0.95f else 1.2f), 0.2f)
    } * box
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(color = color, fontSize = fontPx.toSp(), fontWeight = FontWeight.ExtraBold)
    )
    val center = Offset(origin.x + face.center.x * box, origin.y + face.center.y * box)
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f)
    )
}

/** Dibuja los puntos del número [value] sobre una cara cuadrada (un paralelogramo en pantalla). */
private fun DrawScope.drawPips(face: ProjectedFace, value: Int, color: Color, box: Float, origin: Offset) {
    val p0 = face.points[0]
    val p1 = face.points[1]
    val p3 = face.points[3]
    val side = min(distance(p0, p1), distance(p0, p3))
    val radius = side * 0.085f * box
    PIPS[value].orEmpty().forEach { (u, v) ->
        val x = p0.x + u * (p1.x - p0.x) + v * (p3.x - p0.x)
        val y = p0.y + u * (p1.y - p0.y) + v * (p3.y - p0.y)
        drawCircle(color = color, radius = radius, center = Offset(origin.x + x * box, origin.y + y * box))
    }
}

private fun distance(a: Point2, b: Point2): Float =
    sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y))

@Preview(showBackground = true)
@Composable
private fun DiceFacePreview() {
    Appmovil2Theme {
        Row {
            DiceType.entries.forEachIndexed { index, type ->
                DiceFace(
                    value = (index + 3).coerceAtMost(type.sides),
                    sides = type.sides,
                    palette = DicePalette.ROSA,
                    contentDescription = type.label,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}
