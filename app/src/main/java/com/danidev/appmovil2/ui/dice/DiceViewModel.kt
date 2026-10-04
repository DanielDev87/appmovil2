package com.danidev.appmovil2.ui.dice

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel de la pantalla de dados.
 *
 * Responsabilidades:
 * - Mantener la cantidad de dados activa y sus valores actuales (HU-06).
 * - Mantener el historial de los últimos [DiceUiState.MAX_HISTORY_SIZE] resultados (HU-011).
 *
 * Al vivir en un ViewModel, el estado sobrevive a cambios de configuración
 * (p. ej. rotar la pantalla), a diferencia de un `remember` dentro del Composable.
 */
class DiceViewModel : ViewModel() {

    // Estado mutable privado: solo el ViewModel puede modificarlo.
    private val _uiState = MutableStateFlow(DiceUiState())

    /** Estado público de solo lectura que observa la UI. */
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    // Contador para asignar un id único a cada lanzamiento (ver [RollRecord]).
    private var nextRollId = 0L

    /**
     * Cambia la cantidad de dados en juego (HU-06).
     *
     * @param count Número de dados a usar (mínimo 1).
     */
    fun setDiceCount(count: Int) {
        val validCount = count.coerceAtLeast(1)
        _uiState.update { currentState ->
            val newValues = List(validCount) { index ->
                currentState.currentDiceValues.getOrElse(index) { 1 }
            }
            currentState.copy(
                diceCount = validCount,
                currentDiceValues = newValues
            )
        }
    }

    /**
     * Genera valores aleatorios entre 1 y 6 para la cantidad de dados activa
     * y los registra como resultado final.
     */
    fun rollDice() {
        val count = _uiState.value.diceCount
        val values = List(count) { (1..6).random() }
        registerResult(values)
    }

    /**
     * Registra el resultado de un solo dado (para compatibilidad).
     *
     * @param value Valor obtenido en el dado (1..6).
     */
    fun registerResult(value: Int) {
        registerResult(listOf(value))
    }

    /**
     * Registra el resultado final de un lanzamiento de múltiples dados (HU-06).
     *
     * Actualiza los valores actuales y los inserta al inicio del historial, conservando
     * solo los [DiceUiState.MAX_HISTORY_SIZE] más recientes.
     *
     * Debe llamarse una única vez por lanzamiento, cuando termina la animación:
     * los valores intermedios que se muestran mientras los dados giran no son
     * resultados reales y no deben guardarse en el historial.
     *
     * @param values Lista de valores obtenidos en los dados (1..6 cada uno).
     */
    fun registerResult(values: List<Int>) {
        val newRecord = RollRecord(id = nextRollId++, values = values)
        _uiState.update { currentState ->
            currentState.copy(
                currentDiceValues = values,
                history = (listOf(newRecord) + currentState.history)
                    .take(DiceUiState.MAX_HISTORY_SIZE)
            )
        }
    }
}
