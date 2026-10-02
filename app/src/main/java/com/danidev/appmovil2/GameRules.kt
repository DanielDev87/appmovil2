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