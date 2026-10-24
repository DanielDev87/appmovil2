package com.danidev.appmovil2.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.danidev.appmovil2.R
import com.danidev.appmovil2.RollIndicator

/**
 * Interfaz para el reproductor de efectos de sonido del dado (HU-15).
 */
interface DiceAudioPlayer {
    val isMuted: Boolean
    fun setMuted(muted: Boolean)
    fun playRollSound()
    fun playLandSound()

    /** Suena un efecto propio para un crítico o una pifia; un tiro normal no añade nada. */
    fun playResultSound(indicator: RollIndicator)

    /** Suena la melodía de victoria o de derrota al terminar la partida. */
    fun playGameOverSound(won: Boolean)

    fun stopRollSound()
    fun pauseAll()
    fun resumeAll()
    fun release()
}

/**
 * Gestor de efectos de sonido utilizando [SoundPool] vinculado al ciclo de vida
 * del lanzamiento y al ciclo de vida de la aplicación Android (HU-15).
 *
 * Características:
 * - Emplea [SoundPool] para baja latencia en efectos de audio breves (rodar y asentamiento de dados).
 * - Vinculado al ciclo de vida del lanzamiento: reproduce el rodado al iniciar el tiro
 *   y el impacto/asentamiento cuando los dados se detienen.
 * - Vinculado al ciclo de vida de Android: pausa automáticamente las pistas en segundo plano (ON_PAUSE),
 *   las reanuda en primer plano (ON_RESUME) y libera los recursos en destrucción (ON_DESTROY / onDispose).
 */
class DiceSoundManager(
    private val context: Context,
    private val soundPoolFactory: () -> SoundPool = {
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }
) : DiceAudioPlayer {

    private var soundPool: SoundPool? = null
    private var rollSoundId: Int = 0
    private var landSoundId: Int = 0
    private var criticalSoundId: Int = 0
    private var fumbleSoundId: Int = 0
    private var winSoundId: Int = 0
    private var loseSoundId: Int = 0
    private val loadedSounds = mutableSetOf<Int>()

    var isRollLoaded: Boolean = false
        private set

    var isLandLoaded: Boolean = false
        private set

    private var activeRollStreamId: Int = 0

    override var isMuted: Boolean = false
        private set

    override fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            stopRollSound()
        }
    }

    init {
        try {
            val pool = soundPoolFactory()
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSounds += sampleId
                    if (sampleId == rollSoundId) isRollLoaded = true
                    if (sampleId == landSoundId) isLandLoaded = true
                }
            }
            soundPool = pool
            loadSounds()
        } catch (_: Throwable) {
            // Manejo seguro en entornos donde SoundPool no está inicializado
        }
    }

    private fun loadSounds() {
        soundPool?.let { pool ->
            try {
                rollSoundId = pool.load(context, R.raw.dice_roll, 1)
                landSoundId = pool.load(context, R.raw.dice_land, 1)
                criticalSoundId = pool.load(context, R.raw.dice_critical, 1)
                fumbleSoundId = pool.load(context, R.raw.dice_fumble, 1)
                winSoundId = pool.load(context, R.raw.game_win, 1)
                loseSoundId = pool.load(context, R.raw.game_lose, 1)
            } catch (_: Throwable) {
                // Previene caídas si los recursos no pueden cargarse
            }
        }
    }

    /**
     * Vinculado al inicio del lanzamiento: reproduce el efecto de rodado.
     */
    override fun playRollSound() {
        if (isMuted) return
        stopRollSound()
        soundPool?.let { pool ->
            if (isRollLoaded && rollSoundId != 0) {
                activeRollStreamId = pool.play(rollSoundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    /**
     * Detiene el efecto de rodado si sigue activo.
     */
    override fun stopRollSound() {
        if (activeRollStreamId != 0) {
            soundPool?.stop(activeRollStreamId)
            activeRollStreamId = 0
        }
    }

    /**
     * Vinculado al final del lanzamiento: reproduce el impacto de los dados al detenerse.
     */
    override fun playLandSound() {
        if (isMuted) return
        stopRollSound()
        soundPool?.let { pool ->
            if (isLandLoaded && landSoundId != 0) {
                pool.play(landSoundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    override fun playResultSound(indicator: RollIndicator) {
        when (indicator) {
            RollIndicator.CRITICAL -> playLoaded(criticalSoundId)
            RollIndicator.FUMBLE -> playLoaded(fumbleSoundId)
            else -> Unit
        }
    }

    override fun playGameOverSound(won: Boolean) {
        playLoaded(if (won) winSoundId else loseSoundId)
    }

    private fun playLoaded(soundId: Int) {
        if (isMuted || soundId == 0 || soundId !in loadedSounds) return
        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    /**
     * Pausa todas las reproducciones de audio de SoundPool (e.g. al pasar a ON_PAUSE).
     */
    override fun pauseAll() {
        soundPool?.autoPause()
    }

    /**
     * Reanuda los sonidos pausados si el audio no está silenciado (e.g. al pasar a ON_RESUME).
     */
    override fun resumeAll() {
        if (!isMuted) {
            soundPool?.autoResume()
        }
    }

    /**
     * Libera completamente las instancias nativas de SoundPool (e.g. al salir de la pantalla / ON_DESTROY).
     */
    override fun release() {
        stopRollSound()
        soundPool?.release()
        soundPool = null
        isRollLoaded = false
        isLandLoaded = false
        loadedSounds.clear()
    }
}

/**
 * Crea y recuerda una instancia de [DiceSoundManager] vinculada al ciclo de vida
 * de Jetpack Compose y al ciclo de vida de la Activity (HU-15).
 */
@Composable
fun rememberDiceSoundManager(): DiceSoundManager {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    val soundManager = remember { DiceSoundManager(context) }

    DisposableEffect(lifecycleOwner, soundManager) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> soundManager.pauseAll()
                Lifecycle.Event.ON_RESUME -> soundManager.resumeAll()
                Lifecycle.Event.ON_DESTROY -> soundManager.release()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            soundManager.release()
        }
    }

    return soundManager
}
