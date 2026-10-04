package com.danidev.appmovil2.ui.dice

/**
 * Estado inmutable de la pantalla de lanzamiento de dados.
 *
 * Es expuesto por [DiceViewModel] mediante un `StateFlow` y consumido por la UI
 * (`MoverDados`). Al ser una `data class` inmutable, cada cambio genera una copia
 * nueva, lo que permite a Compose detectar la actualización y recomponer.
 *
 * @property diceCount Cantidad de dados en juego (HU-06).
 * @property currentDiceValues Lista de valores (1..6) del último lanzamiento completado para cada dado.
 * @property history Historial de los últimos lanzamientos (HU-011), ordenado del
 * más reciente al más antiguo. Nunca supera [MAX_HISTORY_SIZE] elementos.
 */
data class DiceUiState(
    val diceCount: Int = 1,
    val currentDiceValues: List<Int> = listOf(1),
    val history: List<RollRecord> = emptyList()
) {
    /**
     * Suma acumulada de los valores actuales del último lanzamiento.
     */
    val currentDiceValue: Int
        get() = currentDiceValues.sum()

    companion object {
        /** Cantidad máxima de resultados que se conservan en [history] (HU-011). */
        const val MAX_HISTORY_SIZE = 10
    }
}
