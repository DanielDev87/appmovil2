package com.danidev.appmovil2

import org.junit.Assert.assertEquals
import org.junit.Test

class GameRulesTest {
    @Test
    fun winsWhenTargetScoreIsReached() {
        assertEquals(GameOutcome.WON, determineGameOutcome(TARGET_SCORE, 4))
    }

    @Test
    fun reachingTargetOnLastLaunchWins() {
        assertEquals(GameOutcome.WON, determineGameOutcome(TARGET_SCORE, MAX_LAUNCHES))
    }

    @Test
    fun endsWhenLaunchLimitIsReachedWithoutTargetScore() {
        assertEquals(GameOutcome.LAUNCH_LIMIT_REACHED, determineGameOutcome(TARGET_SCORE - 1, MAX_LAUNCHES))
    }

    @Test
    fun normalDifficultyKeepsTheClassicGoalWithOneD6() {
        assertEquals(TARGET_SCORE, Difficulty.NORMAL.targetScore(sides = 6, diceCount = 1))
        assertEquals(MAX_LAUNCHES, Difficulty.NORMAL.maxLaunches)
    }

    @Test
    fun harderDifficultyAsksMoreInFewerLaunches() {
        val easy = Difficulty.EASY
        val normal = Difficulty.NORMAL
        val hard = Difficulty.HARD
        val expert = Difficulty.EXPERT
        assert(expert.goalFactor > hard.goalFactor && hard.goalFactor > normal.goalFactor)
        assert(normal.goalFactor > easy.goalFactor)
        assert(expert.maxLaunches < hard.maxLaunches && hard.maxLaunches < normal.maxLaunches)
        assert(normal.maxLaunches < easy.maxLaunches)
    }

    @Test
    fun theGoalScalesWithTheDiceSoEveryLevelIsEquallyHardOnAnyDie() {
        for (difficulty in Difficulty.entries) {
            // Pedir lo mismo respecto al promedio: el doble de caras pide aproximadamente el doble.
            val d6 = difficulty.targetScore(sides = 6, diceCount = 1)
            val d12 = difficulty.targetScore(sides = 12, diceCount = 1)
            assert(d12 > d6)
            // Dos dados suman más por tiro, así que la meta también sube.
            assert(difficulty.targetScore(6, 2) > d6)
        }
    }

    @Test
    fun hardDifficultyWithOneD6IsAroundThePointsExpectedForItsLaunches() {
        // 7 lanzamientos de un D6 esperan 24.5 puntos; Difícil pide un 25 % más y Experto, con solo 6
        // lanzamientos, un 40 % más de lo que se espera en ellos.
        assertEquals(31, Difficulty.HARD.targetScore(sides = 6, diceCount = 1))
        assertEquals(29, Difficulty.EXPERT.targetScore(sides = 6, diceCount = 1))
        assertEquals(32, Difficulty.EASY.targetScore(sides = 6, diceCount = 1))
    }

    @Test
    fun theGoalAlwaysStaysInsideTheAllowedRange() {
        for (difficulty in Difficulty.entries) for (sides in listOf(4, 6, 8, 12, 20)) for (dice in 1..2) {
            val target = difficulty.targetScore(sides, dice)
            assert(target in MIN_TARGET_SCORE..MAX_TARGET_SCORE) { "$difficulty D$sides x$dice = $target" }
        }
    }

    @Test
    fun aMultiDiceRollIsCriticalIfAnyDieHitsMaxAndFumbleIfAllAreOnes() {
        assertEquals(RollIndicator.CRITICAL, determineRollIndicator(listOf(6, 2), 6))
        assertEquals(RollIndicator.FUMBLE, determineRollIndicator(listOf(1, 1), 6))
        assertEquals(RollIndicator.NONE, determineRollIndicator(listOf(1, 3), 6))
    }
}
