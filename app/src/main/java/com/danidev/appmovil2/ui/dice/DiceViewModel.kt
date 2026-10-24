package com.danidev.appmovil2.ui.dice

import androidx.lifecycle.ViewModel
import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.MAX_DICE
import com.danidev.appmovil2.MAX_PLAYERS
import com.danidev.appmovil2.MIN_PLAYERS
import com.danidev.appmovil2.RollIndicator
import com.danidev.appmovil2.determineRollIndicator
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
 * - Llevar los puntos, los lanzamientos, el contador de tiros (HU-08) y el indicador
 *   de crítico o pifia (HU-09), para que no se pierdan al rotar la pantalla.
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
     * Marca el inicio de un tiro: suma al contador de tiros consecutivos (HU-08)
     * y limpia el indicador de crítico o pifia (HU-09).
     */
    fun startRoll() {
        _uiState.update { it.copy(rollCount = it.rollCount + 1, rollIndicator = RollIndicator.NONE) }
    }

    /**
     * Cambia la meta de la partida: puntos para ganar y lanzamientos disponibles. La partida
     * en curso se conserva; solo cambia cuándo termina.
     */
    fun setGoals(targetScore: Int, maxLaunches: Int) {
        _uiState.update { it.copy(targetScore = targetScore, maxLaunches = maxLaunches) }
    }

    /**
     * Avisa que el jugador cambió el tipo de dado en plena partida. Los puntos, los lanzamientos
     * y el historial se conservan; se limpian los dados en pantalla, los bloqueos y el conteo
     * por cara, porque ese conteo solo tiene sentido para un mismo tipo de dado.
     */
    fun onDiceTypeChanged() {
        _uiState.update {
            it.copy(
                currentDiceValues = List(it.diceCount) { 1 },
                lockedIndices = emptySet(),
                faceCounts = emptyMap(),
                rollIndicator = RollIndicator.NONE
            )
        }
    }

    /** Reinicia el contador de tiros consecutivos (HU-08). */
    fun resetRollCount() {
        _uiState.update { it.copy(rollCount = 0) }
    }

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
     * @param count Número de dados a usar, de 1 a [MAX_DICE].
     */
    fun setDiceCount(count: Int) {
        val validCount = count.coerceIn(1, MAX_DICE)
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
     * Genera valores aleatorios entre 1 y [sides] para los dados no bloqueados
     * y registra el resultado final.
     *
     * @param sides Número de caras del dado (HU-07); por defecto, 6.
     */
    fun rollDice(sides: Int = 6) {
        val currentState = _uiState.value
        val values = List(currentState.diceCount) { index ->
            if (index in currentState.lockedIndices) {
                currentState.currentDiceValues.getOrElse(index) { 1 }
            } else {
                (1..sides).random()
            }
        }
        val activeValues = values.filterIndexed { index, _ -> index !in currentState.lockedIndices }
        registerResult(values, activeValues, sides)
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
     * @param sides Número de caras de los dados del lanzamiento (HU-07); por defecto, 6.
     */
    fun registerResult(values: List<Int>, activeValues: List<Int> = values, sides: Int = 6) {
        registerResult(
            values = values,
            activeValues = activeValues,
            diceSides = List(activeValues.ifEmpty { values }.size) { sides }
        )
    }

    /**
     * Registra un lanzamiento con dados que pueden tener distinto número de caras.
     *
     * @param diceSides Caras de cada dado de [activeValues] (o de [values] si no hay activos), en el
     * mismo orden; el crítico y la pifia se evalúan dado por dado con sus propias caras.
     */
    fun registerResult(
        values: List<Int>,
        activeValues: List<Int> = values,
        diceSides: List<Int>
    ) {
        val recordValues = activeValues.ifEmpty { values }
        val sides = diceSides.firstOrNull() ?: 6
        val newRecord = RollRecord(
            id = nextRollId++,
            values = recordValues,
            sides = sides,
            diceSides = diceSides
        )
        _uiState.update { currentState ->
            // HU-09: crítico si algún dado que cuenta sale en su cara más alta; pifia si todos en 1.
            val indicator = determineRollIndicator(recordValues, diceSides)
            val index = currentState.currentPlayer.coerceIn(currentState.players.indices)
            val rollPoints = activeValues.sum()
            val updatedPlayers = currentState.players.mapIndexed { i, player ->
                if (i != index) {
                    player
                } else {
                    PlayerStats(
                        score = player.score + rollPoints,
                        launches = player.launches + 1,
                        criticals = player.criticals + if (indicator == RollIndicator.CRITICAL) 1 else 0
                    )
                }
            }
            val updated = currentState.copy(
                currentDiceValues = values,
                history = (listOf(newRecord) + currentState.history)
                    .take(DiceUiState.MAX_HISTORY_SIZE),
                // HU-13: cada dado que cuenta en este tiro suma una salida de su cara.
                faceCounts = currentState.faceCounts.plusRolls(recordValues),
                players = updatedPlayers,
                rollIndicator = indicator
            )
            // Con varios jugadores, si la partida sigue, el turno pasa al siguiente y los dados
            // vuelven a quedar libres para él.
            if (updated.isMultiplayer && updated.outcome == GameOutcome.IN_PROGRESS) {
                updated.copy(
                    currentPlayer = (index + 1) % updated.players.size,
                    lockedIndices = emptySet()
                )
            } else {
                updated
            }
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
                history = emptyList(),
                faceCounts = emptyMap(),
                players = List(currentState.players.size) { PlayerStats() },
                currentPlayer = 0,
                rollIndicator = RollIndicator.NONE
            )
        }
    }

    /**
     * Fija cuántas personas juegan (de [MIN_PLAYERS] a [MAX_PLAYERS]). Si cambia la cantidad,
     * la partida empieza de cero, porque los puntos de unos jugadores no valen para otros.
     */
    fun setPlayerCount(count: Int) {
        val valid = count.coerceIn(MIN_PLAYERS, MAX_PLAYERS)
        _uiState.update { currentState ->
            if (currentState.players.size == valid) {
                currentState
            } else {
                currentState.copy(
                    currentDiceValues = List(currentState.diceCount) { 1 },
                    lockedIndices = emptySet(),
                    history = emptyList(),
                    faceCounts = emptyMap(),
                    players = List(valid) { PlayerStats() },
                    currentPlayer = 0,
                    rollIndicator = RollIndicator.NONE
                )
            }
        }
    }
}
