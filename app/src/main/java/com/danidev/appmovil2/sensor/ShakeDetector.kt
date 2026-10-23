package com.danidev.appmovil2.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.sqrt

/**
 * Listener de acelerómetro para detectar sacudidas del dispositivo (HU-19).
 *
 * Mide la fuerza G combinada en los tres ejes (X, Y, Z). Cuando supera [SHAKE_THRESHOLD]
 * y ha pasado el tiempo de enfriamiento [SHAKE_COOLDOWN_MS], dispara [onShake].
 */
class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastShakeTimestamp: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gForce = calculateGForce(x, y, z)

        if (gForce > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp >= SHAKE_COOLDOWN_MS) {
                lastShakeTimestamp = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        // Umbral de fuerza G para considerar que el usuario sacudió el teléfono intencionalmente.
        const val SHAKE_THRESHOLD = 1.8f

        // Cooldown para evitar múltiples tiros accidentales con la misma sacudida.
        const val SHAKE_COOLDOWN_MS = 900L

        /**
         * Calcula la aceleración total (fuerza G normalizada).
         */
        fun calculateGForce(x: Float, y: Float, z: Float): Float {
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH
            return sqrt(gX * gX + gY * gY + gZ * gZ)
        }
    }
}

/**
 * Recuerda y gestiona la suscripción del acelerómetro vinculado al ciclo de vida (HU-19).
 *
 * Se activa en primer plano (ON_RESUME) y se desconecta automáticamente en segundo plano
 * (ON_PAUSE) o al salir del Composable para ahorrar 100% de batería.
 */
@Composable
fun rememberShakeDetector(
    enabled: Boolean = true,
    onShake: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(enabled, lifecycleOwner) {
        if (!enabled) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val detector = ShakeDetector { currentOnShake() }

        var isRegistered = false

        fun register() {
            if (!isRegistered && accelerometer != null) {
                sensorManager.registerListener(
                    detector,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
                )
                isRegistered = true
            }
        }

        fun unregister() {
            if (isRegistered) {
                sensorManager?.unregisterListener(detector)
                isRegistered = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> register()
                Lifecycle.Event.ON_PAUSE -> unregister()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        register()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            unregister()
        }
    }
}
