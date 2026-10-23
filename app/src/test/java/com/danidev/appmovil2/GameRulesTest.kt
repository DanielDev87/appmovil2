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
    fun difficultyMatchesItsGoalsAndIsNullForCustomOnes() {
        assertEquals(Difficulty.NORMAL, Difficulty.matching(TARGET_SCORE, MAX_LAUNCHES))
        assertEquals(Difficulty.HARD, Difficulty.matching(40, 8))
        assertEquals(null, Difficulty.matching(33, 7))
    }

    @Test
    fun harderDifficultyAsksMoreInFewerLaunches() {
        assert(Difficulty.HARD.targetScore > Difficulty.NORMAL.targetScore)
        assert(Difficulty.HARD.maxLaunches < Difficulty.NORMAL.maxLaunches)
        assert(Difficulty.EASY.targetScore < Difficulty.NORMAL.targetScore)
        assert(Difficulty.EASY.maxLaunches > Difficulty.NORMAL.maxLaunches)
    }

    @Test
    fun aMultiDiceRollIsCriticalIfAnyDieHitsMaxAndFumbleIfAllAreOnes() {
        assertEquals(RollIndicator.CRITICAL, determineRollIndicator(listOf(6, 2), 6))
        assertEquals(RollIndicator.FUMBLE, determineRollIndicator(listOf(1, 1), 6))
        assertEquals(RollIndicator.NONE, determineRollIndicator(listOf(1, 3), 6))
    }
}
