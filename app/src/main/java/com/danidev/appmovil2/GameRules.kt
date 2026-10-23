package com.danidev.appmovil2

const val TARGET_SCORE = 30
const val MAX_LAUNCHES = 10

enum class GameOutcome {
    IN_PROGRESS,
    WON,
    LAUNCH_LIMIT_REACHED
}

/** Límites que el jugador puede cambiar en Ajustes. */
const val MIN_TARGET_SCORE = 10
const val MAX_TARGET_SCORE = 200
const val MIN_MAX_LAUNCHES = 3
const val MAX_MAX_LAUNCHES = 50

/**
 * Resultado de la partida: gana quien llega a [targetScore] puntos; si antes se agotan los
 * [maxLaunches] lanzamientos, la partida termina sin victoria.
 */
fun determineGameOutcome(
    score: Int,
    launches: Int,
    targetScore: Int = TARGET_SCORE,
    maxLaunches: Int = MAX_LAUNCHES
): GameOutcome = when {
    score >= targetScore -> GameOutcome.WON
    launches >= maxLaunches -> GameOutcome.LAUNCH_LIMIT_REACHED
    else -> GameOutcome.IN_PROGRESS
}

/**
 * HU-09: Indicador de tiro afortunado.
 *
 * La cara más alta del dado (un 6 en el dado tradicional) se considera CRÍTICO.
 * Un 1 se considera PIFIA.
 * Cualquier otro resultado no muestra indicador.
 */
enum class RollIndicator {
    CRITICAL,
    FUMBLE,
    NONE
}

fun determineRollIndicator(result: Int, sides: Int = 6): RollIndicator = when (result) {
    sides -> RollIndicator.CRITICAL
    1 -> RollIndicator.FUMBLE
    else -> RollIndicator.NONE
}

/**
 * Indicador de un tiro con uno o varios dados: es crítico si algún dado sale en la cara más
 * alta y pifia si todos salen en 1; en cualquier otro caso no hay indicador.
 */
fun determineRollIndicator(results: List<Int>, sides: Int = 6): RollIndicator = when {
    results.isEmpty() -> RollIndicator.NONE
    results.any { it == sides } -> RollIndicator.CRITICAL
    results.all { it == 1 } -> RollIndicator.FUMBLE
    else -> RollIndicator.NONE
}


/**
 * Niveles de dificultad listos para elegir: cada uno fija los puntos para ganar y los
 * lanzamientos disponibles. Más puntos y menos lanzamientos es más difícil.
 */
enum class Difficulty(val targetScore: Int, val maxLaunches: Int) {
    EASY(targetScore = 20, maxLaunches = 12),
    NORMAL(targetScore = TARGET_SCORE, maxLaunches = MAX_LAUNCHES),
    HARD(targetScore = 40, maxLaunches = 8);

    companion object {
        /** La dificultad que coincide con esta meta, o `null` si la meta es personalizada. */
        fun matching(targetScore: Int, maxLaunches: Int): Difficulty? =
            entries.firstOrNull { it.targetScore == targetScore && it.maxLaunches == maxLaunches }
    }
}

/** Cantidad mínima y máxima de jugadores en una partida. */
const val MIN_PLAYERS = 1
const val MAX_PLAYERS = 3

/**
 * Resultado de una partida con varios jugadores que se turnan: gana quien llega primero a
 * [targetScore] puntos; si todos agotan sus [maxLaunches] lanzamientos antes, la partida termina
 * y gana quien sumó más (ver `DiceUiState.winners`).
 */
fun determineMultiplayerOutcome(
    scores: List<Int>,
    launches: List<Int>,
    targetScore: Int = TARGET_SCORE,
    maxLaunches: Int = MAX_LAUNCHES
): GameOutcome = when {
    scores.any { it >= targetScore } -> GameOutcome.WON
    launches.all { it >= maxLaunches } -> GameOutcome.LAUNCH_LIMIT_REACHED
    else -> GameOutcome.IN_PROGRESS
}
