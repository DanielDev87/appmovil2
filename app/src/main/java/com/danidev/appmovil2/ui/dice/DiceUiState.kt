package com.danidev.appmovil2.ui.dice

import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.MAX_LAUNCHES
import com.danidev.appmovil2.TARGET_SCORE
import com.danidev.appmovil2.determineGameOutcome

/**
 * Estado inmutable de la pantalla de lanzamiento de dados.
 *
 * Es expuesto por [DiceViewModel] mediante un `StateFlow` y consumido por la UI
 * (`MoverDados`). Al ser una `data class` inmutable, cada cambio genera una copia
 * nueva, lo que permite a Compose detectar la actualización y recomponer.
 *
 * @property diceCount Cantidad de dados en juego (HU-06).
 * @property currentDiceValues Lista de valores (1..6) del último lanzamiento completado para cada dado.
 * @property lockedIndices Conjunto de índices de dados bloqueados para no ser regenerados en la siguiente tirada.
 * @property history Historial de los últimos lanzamientos (HU-011), ordenado del
 * más reciente al más antiguo. Nunca supera [MAX_HISTORY_SIZE] elementos.
 * @property faceCounts Cuántas veces ha salido cada cara en la partida (HU-13); la
 * clave es el valor de la cara. A diferencia de [history], no se recorta a 10.
 * @property score Puntos acumulados en la partida en curso.
 * @property launches Lanzamientos hechos en la partida en curso.
 * @property rollCount Tiros consecutivos del contador (HU-08); se reinicia a mano.
 * @property rollIndicator Crítico o pifia del último tiro de un solo dado (HU-09).
 * @property criticalCount Cuántos tiros críticos van en la partida en curso.
 * @property targetScore Puntos que hay que alcanzar para ganar.
 * @property maxLaunches Lanzamientos disponibles en la partida.
 */
data class DiceUiState(
    val diceCount: Int = 1,
    val currentDiceValues: List<Int> = listOf(1),
    val lockedIndices: Set<Int> = emptySet(),
    val history: List<RollRecord> = emptyList(),
    val faceCounts: Map<Int, Int> = emptyMap(),
    val score: Int = 0,
    val launches: Int = 0,
    val rollCount: Int = 0,
    val rollIndicator: RollIndicator = RollIndicator.NONE,
    val criticalCount: Int = 0,
    val targetScore: Int = TARGET_SCORE,
    val maxLaunches: Int = MAX_LAUNCHES
) {
    /** Resultado de la partida según los puntos y lanzamientos acumulados. */
    val outcome: GameOutcome
        get() = determineGameOutcome(score, launches, targetScore, maxLaunches)

    //Suma acumulada de los valores de los dados no bloqueados.
    val currentDiceValue: Int
        get() = currentDiceValues.filterIndexed { index, _ -> index !in lockedIndices }.sum()

    companion object {
        /** Cantidad máxima de resultados que se conservan en [history] (HU-011). */
        const val MAX_HISTORY_SIZE = 10
    }
}
