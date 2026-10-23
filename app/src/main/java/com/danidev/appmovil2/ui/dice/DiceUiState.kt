package com.danidev.appmovil2.ui.dice

import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.MAX_LAUNCHES
import com.danidev.appmovil2.TARGET_SCORE
import com.danidev.appmovil2.determineGameOutcome
import com.danidev.appmovil2.determineMultiplayerOutcome

/**
 * Lo que lleva un jugador en la partida en curso.
 *
 * @property score Puntos acumulados.
 * @property launches Lanzamientos hechos.
 * @property criticals Tiros críticos.
 */
data class PlayerStats(
    val score: Int = 0,
    val launches: Int = 0,
    val criticals: Int = 0
)

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
 * @property players Lo que lleva cada jugador; con un solo jugador la lista tiene un elemento.
 * @property currentPlayer Posición del jugador al que le toca tirar.
 * @property rollCount Tiros consecutivos del contador (HU-08); se reinicia a mano.
 * @property rollIndicator Crítico o pifia del último tiro de un solo dado (HU-09).
 * @property targetScore Puntos que hay que alcanzar para ganar.
 * @property maxLaunches Lanzamientos disponibles en la partida.
 */
data class DiceUiState(
    val diceCount: Int = 1,
    val currentDiceValues: List<Int> = listOf(1),
    val lockedIndices: Set<Int> = emptySet(),
    val history: List<RollRecord> = emptyList(),
    val faceCounts: Map<Int, Int> = emptyMap(),
    val players: List<PlayerStats> = listOf(PlayerStats()),
    val currentPlayer: Int = 0,
    val rollCount: Int = 0,
    val rollIndicator: RollIndicator = RollIndicator.NONE,
    val targetScore: Int = TARGET_SCORE,
    val maxLaunches: Int = MAX_LAUNCHES
) {
    /** `true` si juegan varias personas, turnándose en el mismo teléfono. */
    val isMultiplayer: Boolean get() = players.size > 1

    /** Lo que lleva el jugador al que le toca tirar. */
    private val current: PlayerStats get() = players[currentPlayer.coerceIn(players.indices)]

    /** Puntos del jugador al que le toca tirar. */
    val score: Int get() = current.score

    /** Lanzamientos del jugador al que le toca tirar. */
    val launches: Int get() = current.launches

    /** Críticos del jugador al que le toca tirar. */
    val criticalCount: Int get() = current.criticals

    /** Resultado de la partida según los puntos y lanzamientos acumulados. */
    val outcome: GameOutcome
        get() = if (isMultiplayer) {
            determineMultiplayerOutcome(
                scores = players.map { it.score },
                launches = players.map { it.launches },
                targetScore = targetScore,
                maxLaunches = maxLaunches
            )
        } else {
            determineGameOutcome(score, launches, targetScore, maxLaunches)
        }

    /**
     * Posiciones de quienes ganaron la partida; vacía mientras sigue en juego.
     *
     * Con un jugador, gana si llegó a la meta. Con varios, gana quien llegó a la meta y, si
     * nadie llegó, quien sumó más; si hay empate, ganan todos los empatados.
     */
    val winners: List<Int>
        get() = when {
            outcome == GameOutcome.IN_PROGRESS -> emptyList()
            !isMultiplayer -> if (outcome == GameOutcome.WON) listOf(0) else emptyList()
            else -> {
                val reached = players.indices.filter { players[it].score >= targetScore }
                val pool = reached.ifEmpty { players.indices.toList() }
                val top = pool.maxOf { players[it].score }
                pool.filter { players[it].score == top }
            }
        }

    //Suma acumulada de los valores de los dados no bloqueados.
    val currentDiceValue: Int
        get() = currentDiceValues.filterIndexed { index, _ -> index !in lockedIndices }.sum()

    companion object {
        /** Cantidad máxima de resultados que se conservan en [history] (HU-011). */
        const val MAX_HISTORY_SIZE = 10
    }
}
