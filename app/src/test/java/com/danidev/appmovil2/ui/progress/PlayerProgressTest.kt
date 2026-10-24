package com.danidev.appmovil2.ui.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de los récords, la racha y los logros que se guardan al terminar cada partida. */
class PlayerProgressTest {

    private fun win(launches: Int = 6, score: Int = 30, criticals: Int = 0, sides: Int = 6) =
        GameResult(won = true, score = score, launches = launches, criticals = criticals, sides = sides)

    private fun loss(score: Int = 12, criticals: Int = 0) =
        GameResult(won = false, score = score, launches = 10, criticals = criticals, sides = 6)

    @Test
    fun `una victoria suma partida, victoria y racha`() {
        val record = PlayerProgress().recordGame(win())

        assertEquals(1, record.progress.gamesPlayed)
        assertEquals(1, record.progress.wins)
        assertEquals(1, record.progress.currentStreak)
        assertEquals(1, record.progress.bestStreak)
    }

    @Test
    fun `una derrota reinicia la racha pero conserva la mejor`() {
        var progress = PlayerProgress()
        repeat(3) { progress = progress.recordGame(win()).progress }

        progress = progress.recordGame(loss()).progress

        assertEquals(0, progress.currentStreak)
        assertEquals(3, progress.bestStreak)
        assertEquals(1, progress.losses)
    }

    @Test
    fun `la mejor victoria es la de menos lanzamientos`() {
        val first = PlayerProgress().recordGame(win(launches = 8))
        assertTrue(first.newBestWin)
        assertEquals(8, first.progress.bestWinLaunches)

        val better = first.progress.recordGame(win(launches = 5))
        assertTrue(better.newBestWin)
        assertEquals(5, better.progress.bestWinLaunches)

        val worse = better.progress.recordGame(win(launches = 9))
        assertFalse(worse.newBestWin)
        assertEquals(5, worse.progress.bestWinLaunches)
    }

    @Test
    fun `perder no cuenta como mejor victoria`() {
        val record = PlayerProgress().recordGame(loss())

        assertFalse(record.newBestWin)
        assertEquals(0, record.progress.bestWinLaunches)
    }

    @Test
    fun `se guarda la mayor cantidad de puntos y se avisa del nuevo record`() {
        val first = PlayerProgress().recordGame(loss(score = 20))
        assertTrue(first.newBestScore)

        val lower = first.progress.recordGame(loss(score = 10))
        assertFalse(lower.newBestScore)
        assertEquals(20, lower.progress.bestScore)
    }

    @Test
    fun `la primera victoria desbloquea su logro una sola vez`() {
        val first = PlayerProgress().recordGame(win())
        assertTrue(Achievement.FIRST_WIN in first.newAchievements)

        val second = first.progress.recordGame(win())
        assertFalse(Achievement.FIRST_WIN in second.newAchievements)
        assertTrue(Achievement.FIRST_WIN.id in second.progress.unlocked)
    }

    @Test
    fun `los criticos suman entre partidas y desbloquean sus logros`() {
        var record = PlayerProgress().recordGame(loss(criticals = 1))
        assertTrue(Achievement.FIRST_CRITICAL in record.newAchievements)

        record = record.progress.recordGame(loss(criticals = 9))
        assertEquals(10, record.progress.totalCriticals)
        assertTrue(Achievement.CRITICALS_10 in record.newAchievements)
    }

    @Test
    fun `tres victorias seguidas desbloquean la racha`() {
        var progress = PlayerProgress()
        var last: GameRecord? = null
        repeat(3) {
            last = progress.recordGame(win())
            progress = last!!.progress
        }

        assertTrue(Achievement.STREAK_3 in last!!.newAchievements)
    }

    @Test
    fun `una victoria rapida y una con D20 tienen su logro`() {
        val quick = PlayerProgress().recordGame(win(launches = 3))
        assertTrue(Achievement.QUICK_WIN in quick.newAchievements)

        val slow = PlayerProgress().recordGame(win(launches = 4))
        assertFalse(Achievement.QUICK_WIN in slow.newAchievements)

        val dragon = PlayerProgress().recordGame(win(sides = 20))
        assertTrue(Achievement.D20_WIN in dragon.newAchievements)
        assertFalse(Achievement.D20_WIN in PlayerProgress().recordGame(win(sides = 12)).newAchievements)
    }

    @Test
    fun `perder una partida con D20 no da el logro del D20`() {
        val record = PlayerProgress().recordGame(
            GameResult(won = false, score = 5, launches = 10, criticals = 0, sides = 20)
        )

        assertFalse(Achievement.D20_WIN in record.newAchievements)
    }

    @Test
    fun `jugar diez partidas desbloquea su logro`() {
        var progress = PlayerProgress()
        var last: GameRecord? = null
        repeat(10) {
            last = progress.recordGame(loss())
            progress = last!!.progress
        }

        assertTrue(Achievement.PLAYED_10 in last!!.newAchievements)
    }

    /** Almacén en memoria que reemplaza a `SharedPreferences` en las pruebas. */
    private class FakeProgressStore(var saved: PlayerProgress = PlayerProgress()) : ProgressStore {
        override fun load() = saved
        override fun save(progress: PlayerProgress) {
            saved = progress
        }
    }

    @Test
    fun `el ViewModel guarda el progreso y una nueva sesion lo recupera`() {
        val store = FakeProgressStore()
        val viewModel = ProgressViewModel(store)

        viewModel.recordGame(win())

        assertEquals(1, store.saved.wins)
        assertEquals(1, ProgressViewModel(store).progress.value.wins)
    }

    @Test
    fun `el ViewModel recuerda lo que cambio con la ultima partida y puede olvidarlo`() {
        val viewModel = ProgressViewModel(FakeProgressStore())
        assertNull(viewModel.lastRecord.value)

        viewModel.recordGame(win())
        assertTrue(Achievement.FIRST_WIN in viewModel.lastRecord.value!!.newAchievements)

        viewModel.clearLastRecord()
        assertNull(viewModel.lastRecord.value)
    }

    @Test
    fun `el avance hacia un logro con contador va de 0 a 1 y no pasa de 1`() {
        val none = PlayerProgress()
        assertEquals(0f, Achievement.WINS_5.fractionOf(none), 0.001f)

        val three = PlayerProgress(wins = 3)
        assertEquals(0.6f, Achievement.WINS_5.fractionOf(three), 0.001f)

        val many = PlayerProgress(wins = 40)
        assertEquals(1f, Achievement.WINS_5.fractionOf(many), 0.001f)
    }

    @Test
    fun `los logros sin contador no muestran avance`() {
        assertNull(Achievement.D20_WIN.goal)
        assertEquals(0f, Achievement.QUICK_WIN.fractionOf(PlayerProgress(wins = 9)), 0.001f)
    }

    @Test
    fun `cada logro con meta usa el dato que describe`() {
        val progress = PlayerProgress(gamesPlayed = 4, wins = 2, bestStreak = 2, totalCriticals = 7)

        assertEquals(2, Achievement.FIRST_WIN.goal!!.current(progress))
        assertEquals(2, Achievement.STREAK_3.goal!!.current(progress))
        assertEquals(7, Achievement.CRITICALS_10.goal!!.current(progress))
        assertEquals(4, Achievement.PLAYED_10.goal!!.current(progress))
    }
}
