package com.danidev.appmovil2.sensor

import android.hardware.SensorManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para la detección de sacudidas del acelerómetro (HU-19).
 */
class ShakeDetectorTest {

    @Test
    fun `fuerza G estatica en reposo es aproximadamente 1G`() {
        // En reposo sobre la mesa con Z apuntando hacia arriba:
        val gForce = ShakeDetector.calculateGForce(0f, 0f, SensorManager.GRAVITY_EARTH)
        assertTrue(gForce in 0.99f..1.01f)
    }

    @Test
    fun `fuerza G debajo del umbral no califica como sacudida`() {
        // Movimiento leve (1.5G):
        val gForce = ShakeDetector.calculateGForce(
            0.5f * SensorManager.GRAVITY_EARTH,
            0.5f * SensorManager.GRAVITY_EARTH,
            SensorManager.GRAVITY_EARTH
        )
        assertFalse(gForce > ShakeDetector.SHAKE_THRESHOLD)
    }

    @Test
    fun `fuerza G fuerte supera el umbral de sacudida`() {
        // Sacudida enérgica (2.5G):
        val gForce = ShakeDetector.calculateGForce(
            1.8f * SensorManager.GRAVITY_EARTH,
            1.5f * SensorManager.GRAVITY_EARTH,
            SensorManager.GRAVITY_EARTH
        )
        assertTrue(gForce > ShakeDetector.SHAKE_THRESHOLD)
    }
}
