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
