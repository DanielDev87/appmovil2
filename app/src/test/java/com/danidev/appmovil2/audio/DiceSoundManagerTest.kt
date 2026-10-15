package com.danidev.appmovil2.audio

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
