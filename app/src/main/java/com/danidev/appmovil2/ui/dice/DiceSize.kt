package com.danidev.appmovil2.ui.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.theme.Appmovil2Theme
import kotlin.math.roundToInt

/*
 * HU-05: Modificador de tamaño del dado.
 *
 * "Como usuario, quiero ajustar el tamaño del dado en pantalla mediante un control
 * deslizante (Slider), para adaptarlo a mi preferencia visual."
 */

/**
 * Reglas del tamaño del dado.
 *
 * El usuario elige un factor (1.0 = tamaño original) y esta clase lo convierte en el
 * tamaño real en `dp` que recibe `Modifier.size()`. Vive aparte de la pantalla para
 * poder probarla sin Compose.
 */
object DiceSize {

    /** Factor más pequeño que permite el slider. */
    const val MIN_FACTOR = 0.6f

    /** Factor más grande con un solo dado. */
    const val MAX_FACTOR_SINGLE = 1.4f

    /**
     * Factor más grande con dos dados: más allá, los dos dados juntos ya no caben
     * en el ancho de un teléfono.
     */
    const val MAX_FACTOR_DOUBLE = 1.15f

    /** Tamaño original (factor 1.0) de cada dado cuando hay uno solo, en dp. */
    private const val BASE_SINGLE_DP = 200f

    /** Tamaño original (factor 1.0) de cada dado cuando hay dos, en dp. */
    private const val BASE_DOUBLE_DP = 130f

    /** Tamaño original (factor 1.0) de cada dado cuando hay tres, en dp. */
    private const val BASE_TRIPLE_DP = 92f

    /** Factor máximo con tres dados: más allá no caben juntos en el ancho de un teléfono. */
    const val MAX_FACTOR_TRIPLE = 1.0f

    /** Proporción del dado que se deja como margen interior. */
    private const val PADDING_RATIO = 0.15f

    /** Factor máximo permitido para la cantidad de dados indicada. */
    fun maxFactor(diceCount: Int): Float = when {
        diceCount >= 3 -> MAX_FACTOR_TRIPLE
        diceCount == 2 -> MAX_FACTOR_DOUBLE
        else -> MAX_FACTOR_SINGLE
    }

    /**
     * Factor realmente aplicado: el elegido por el usuario, limitado a lo que cabe
     * con la cantidad de dados actual. El valor guardado no se modifica, así que al
     * volver a un dado el slider recupera la elección original.
     */
    fun effectiveFactor(factor: Float, diceCount: Int): Float =
        factor.coerceIn(MIN_FACTOR, maxFactor(diceCount))

    /** Tamaño del dado (alto y ancho) en dp para el factor y la cantidad de dados dados. */
    fun sizeDp(diceCount: Int, factor: Float): Float {
        val base = when {
            diceCount >= 3 -> BASE_TRIPLE_DP
            diceCount == 2 -> BASE_DOUBLE_DP
            else -> BASE_SINGLE_DP
        }
        return base * effectiveFactor(factor, diceCount)
    }

    /** Margen interior del dado en dp, proporcional a su tamaño. */
    fun paddingDp(sizeDp: Float): Float = sizeDp * PADDING_RATIO

    /** Limita un factor al rango válido del slider. */
    fun coerce(factor: Float): Float = factor.coerceIn(MIN_FACTOR, MAX_FACTOR_SINGLE)
}

/**
 * Slider para cambiar el tamaño del dado (HU-05).
 *
 * Al moverlo cambia el `Modifier.size()` del dado al instante, porque la pantalla
 * recompone con el nuevo factor.
 *
 * @param factor Factor elegido por el usuario.
 * @param diceCount Cantidad de dados en juego; limita el máximo del slider.
 * @param onFactorChange Se invoca con el factor nuevo mientras el usuario desliza.
 * @param modifier Modificador opcional para el contenedor.
 */
@Composable
fun DiceSizeSlider(
    factor: Float,
    diceCount: Int,
    onFactorChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val shown = DiceSize.effectiveFactor(factor, diceCount)
    val description = stringResource(R.string.dice_size_description)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.dice_size_label),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = onBackground
        )
        Slider(
            value = shown,
            onValueChange = onFactorChange,
            valueRange = DiceSize.MIN_FACTOR..DiceSize.maxFactor(diceCount),
            colors = SliderDefaults.colors(
                thumbColor = onBackground,
                activeTrackColor = onBackground,
                inactiveTrackColor = onBackground.copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = description }
        )
        Text(
            text = stringResource(R.string.dice_size_percent, (shown * 100).roundToInt()),
            style = MaterialTheme.typography.bodyMedium,
            color = onBackground,
            modifier = Modifier.width(48.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiceSizeSliderPreview() {
    Appmovil2Theme {
        DiceSizeSlider(factor = 1f, diceCount = 1, onFactorChange = {})
    }
}
