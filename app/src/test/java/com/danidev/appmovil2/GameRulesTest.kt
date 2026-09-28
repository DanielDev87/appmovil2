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
}