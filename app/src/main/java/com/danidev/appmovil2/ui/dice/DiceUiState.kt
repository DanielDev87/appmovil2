package com.danidev.appmovil2.ui.dice

/**
 * Estado inmutable de la pantalla de lanzamiento de dados.
 *
 * Es expuesto por [DiceViewModel] mediante un `StateFlow` y consumido por la UI
 * (`MoverDados`). Al ser una `data class` inmutable, cada cambio genera una copia
 * nueva, lo que permite a Compose detectar la actualización y recomponer.
 *
 * @property currentDiceValue Valor (1..6) del último lanzamiento completado.
 * @property history Historial de los últimos lanzamientos (HU-011), ordenado del
 * más reciente al más antiguo. Nunca supera [MAX_HISTORY_SIZE] elementos.
 */
data class DiceUiState(
    val currentDiceValue: Int = 1,
    val history: List<RollRecord> = emptyList()
) {
    companion object {
        /** Cantidad máxima de resultados que se conservan en [history] (HU-011). */
        const val MAX_HISTORY_SIZE = 10
    }
}
