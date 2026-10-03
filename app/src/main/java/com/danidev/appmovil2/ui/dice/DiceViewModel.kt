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
 * - Mantener el valor actual del dado.
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
     * Genera un valor aleatorio entre 1 y 6 y lo registra como resultado final.
     */
    fun rollDice() {
        registerResult((1..6).random())
    }

    /**
     * Registra el resultado final de un lanzamiento.
     *
     * Actualiza el valor actual y lo inserta al inicio del historial, conservando
     * solo los [DiceUiState.MAX_HISTORY_SIZE] más recientes.
     *
     * Debe llamarse una única vez por lanzamiento, cuando termina la animación:
     * los valores intermedios que se muestran mientras el dado gira no son
     * resultados reales y no deben guardarse en el historial.
     *
     * @param value Valor obtenido en el dado (1..6).
     */
    fun registerResult(value: Int) {
        // El registro se crea fuera de `update` porque su lambda puede reintentarse.
        val newRecord = RollRecord(id = nextRollId++, value = value)
        _uiState.update { currentState ->
            currentState.copy(
                currentDiceValue = value,
                // El más reciente va primero; se descartan los que exceden el límite.
                history = (listOf(newRecord) + currentState.history)
                    .take(DiceUiState.MAX_HISTORY_SIZE)
            )
        }
    }
}
