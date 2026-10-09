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


    fun toggleLock(index: Int) {
        _uiState.update { currentState ->
            if (index < 0 || index >= currentState.diceCount) return@update currentState
            val newLocked = if (index in currentState.lockedIndices) {
                currentState.lockedIndices - index
            } else {
                currentState.lockedIndices + index
            }
            currentState.copy(lockedIndices = newLocked)
        }
    }

    //Desbloquea los dados.
    fun clearLocks() {
        _uiState.update { currentState ->
            currentState.copy(lockedIndices = emptySet())
        }
    }

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
            val newLocked = currentState.lockedIndices.filter { it < validCount }.toSet()
            currentState.copy(
                diceCount = validCount,
                currentDiceValues = newValues,
                lockedIndices = newLocked
            )
        }
    }

    /**
     * Genera valores aleatorios entre 1 y 6 para los dados no bloqueados
     * y registra el resultado final.
     */
    fun rollDice() {
        val currentState = _uiState.value
        val values = List(currentState.diceCount) { index ->
            if (index in currentState.lockedIndices) {
                currentState.currentDiceValues.getOrElse(index) { 1 }
            } else {
                (1..6).random()
            }
        }
        val activeValues = values.filterIndexed { index, _ -> index !in currentState.lockedIndices }
        registerResult(values, activeValues)
    }

    /**
     * Registra el resultado de un solo dado (para compatibilidad).
     *
     * @param value Valor obtenido en el dado (1..6).
     */
    fun registerResult(value: Int) {
        registerResult(listOf(value), listOf(value))
    }

    /**
     * Registra el resultado final de un lanzamiento de múltiples dados (HU-06).
     *
     * Actualiza los valores actuales y los inserta al inicio del historial, conservando
     * solo los [DiceUiState.MAX_HISTORY_SIZE] más recientes.
     *
     * @param values Lista completa de valores obtenidos en los dados.
     * @param activeValues Lista de valores de los dados no bloqueados que cuentan en este tiro.
     */
    fun registerResult(values: List<Int>, activeValues: List<Int> = values) {
        val recordValues = activeValues.ifEmpty { values }
        val newRecord = RollRecord(id = nextRollId++, values = recordValues)
        _uiState.update { currentState ->
            currentState.copy(
                currentDiceValues = values,
                history = (listOf(newRecord) + currentState.history)
                    .take(DiceUiState.MAX_HISTORY_SIZE)
            )
        }
    }

    /**
     * Limpia el historial de lanzamientos (HU-011) y reinicia los valores de los dados.
     *
     * Se invoca al reiniciar la partida ("Jugar de nuevo") para que la nueva partida
     * comience sin los lanzamientos anteriores.
     */
    fun clearHistory() {
        _uiState.update { currentState ->
            currentState.copy(
                currentDiceValues = List(currentState.diceCount) { 1 },
                history = emptyList()
            )
        }
    }
}
