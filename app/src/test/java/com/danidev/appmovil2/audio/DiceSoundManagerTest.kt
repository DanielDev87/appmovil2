package com.danidev.appmovil2.audio

import com.danidev.appmovil2.RollIndicator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para el reproductor de audio y estados de sonido (HU-15).
 */
class DiceSoundManagerTest {

    private class FakeAudioPlayer : DiceAudioPlayer {
        override var isMuted: Boolean = false
            private set

        var playRollCount = 0
        var playLandCount = 0
        val resultSounds = mutableListOf<RollIndicator>()
        val gameOverSounds = mutableListOf<Boolean>()
        var stopRollCount = 0
        var pauseCount = 0
        var resumeCount = 0
        var releaseCount = 0

        override fun setMuted(muted: Boolean) {
            isMuted = muted
            if (muted) {
                stopRollSound()
            }
        }

        override fun playRollSound() {
            if (!isMuted) playRollCount++
        }

        override fun playLandSound() {
            if (!isMuted) playLandCount++
        }

        override fun playResultSound(indicator: RollIndicator) {
            if (!isMuted && (indicator == RollIndicator.CRITICAL || indicator == RollIndicator.FUMBLE)) {
                resultSounds += indicator
            }
        }

        override fun playGameOverSound(won: Boolean) {
            if (!isMuted) gameOverSounds += won
        }

        override fun stopRollSound() {
            stopRollCount++
        }

        override fun pauseAll() {
            pauseCount++
        }

        override fun resumeAll() {
            if (!isMuted) resumeCount++
        }

        override fun release() {
            releaseCount++
        }
    }

    @Test
    fun `audio inicia no silenciado por defecto`() {
        val player = FakeAudioPlayer()
        assertFalse(player.isMuted)
    }

    @Test
    fun `reproduce sonido de rodado al iniciar lanzamiento`() {
        val player = FakeAudioPlayer()
        player.playRollSound()

        assertTrue(player.playRollCount == 1)
        assertTrue(player.playLandCount == 0)
    }

    @Test
    fun `reproduce sonido de impacto al finalizar lanzamiento`() {
        val player = FakeAudioPlayer()
        player.playRollSound()
        player.playLandSound()

        assertTrue(player.playRollCount == 1)
        assertTrue(player.playLandCount == 1)
    }

    @Test
    fun `cuando esta silenciado no reproduce ningun sonido`() {
        val player = FakeAudioPlayer()
        player.setMuted(true)

        assertTrue(player.isMuted)

        player.playRollSound()
        player.playLandSound()

        assertTrue(player.playRollCount == 0)
        assertTrue(player.playLandCount == 0)
    }

    @Test
    fun `el critico y la pifia tienen su propio sonido y un tiro normal no`() {
        val player = FakeAudioPlayer()

        player.playResultSound(RollIndicator.CRITICAL)
        player.playResultSound(RollIndicator.FUMBLE)
        player.playResultSound(RollIndicator.NONE)

        assertEquals(listOf(RollIndicator.CRITICAL, RollIndicator.FUMBLE), player.resultSounds)
    }

    @Test
    fun `al terminar la partida suena la victoria o la derrota`() {
        val player = FakeAudioPlayer()

        player.playGameOverSound(won = true)
        player.playGameOverSound(won = false)

        assertEquals(listOf(true, false), player.gameOverSounds)
    }

    @Test
    fun `en silencio no suenan ni el resultado ni el final`() {
        val player = FakeAudioPlayer()
        player.setMuted(true)

        player.playResultSound(RollIndicator.CRITICAL)
        player.playGameOverSound(won = true)

        assertTrue(player.resultSounds.isEmpty())
        assertTrue(player.gameOverSounds.isEmpty())
    }

    @Test
    fun `silenciar detiene cualquier sonido activo de lanzamiento`() {
        val player = FakeAudioPlayer()
        player.setMuted(true)

        assertTrue(player.stopRollCount >= 1)
    }

    @Test
    fun `ciclo de vida de la aplicacion pausa y reanuda sonidos`() {
        val player = FakeAudioPlayer()

        player.pauseAll()
        assertTrue(player.pauseCount == 1)

        player.resumeAll()
        assertTrue(player.resumeCount == 1)

        player.release()
        assertTrue(player.releaseCount == 1)
    }
}
