package com.danidev.appmovil2

const val TARGET_SCORE = 30
const val MAX_LAUNCHES = 10

enum class GameOutcome {
    IN_PROGRESS,
    WON,
    LAUNCH_LIMIT_REACHED
}

fun determineGameOutcome(score: Int, launches: Int): GameOutcome = when {
    score >= TARGET_SCORE -> GameOutcome.WON
    launches >= MAX_LAUNCHES -> GameOutcome.LAUNCH_LIMIT_REACHED
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
