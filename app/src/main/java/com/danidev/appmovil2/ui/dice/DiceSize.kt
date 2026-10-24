package com.danidev.appmovil2.ui.dice


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
