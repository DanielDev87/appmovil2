package com.danidev.appmovil2

const val TARGET_SCORE = 30
const val MAX_LAUNCHES = 10

enum class GameOutcome {
    IN_PROGRESS,
    WON,
    LAUNCH_LIMIT_REACHED
}

/** Límites que el jugador puede cambiar en Ajustes. */
const val MIN_TARGET_SCORE = 10
const val MAX_TARGET_SCORE = 200
const val MIN_MAX_LAUNCHES = 3
const val MAX_MAX_LAUNCHES = 50

/**
 * Resultado de la partida: gana quien llega a [targetScore] puntos; si antes se agotan los
 * [maxLaunches] lanzamientos, la partida termina sin victoria.
 */
fun determineGameOutcome(
    score: Int,
    launches: Int,
    targetScore: Int = TARGET_SCORE,
    maxLaunches: Int = MAX_LAUNCHES
): GameOutcome = when {
    score >= targetScore -> GameOutcome.WON
    launches >= maxLaunches -> GameOutcome.LAUNCH_LIMIT_REACHED
    else -> GameOutcome.IN_PROGRESS
}

/**
 * HU-09: Indicador de tiro afortunado.
 *
 * La cara más alta del dado (un 6 en el dado tradicional) se considera CRÍTICO.
 * Un 1 se considera PIFIA.
 * Cualquier otro resultado no muestra indicador.
 */
enum class RollIndicator {
    CRITICAL,
    FUMBLE,
    NONE
}

fun determineRollIndicator(result: Int, sides: Int = 6): RollIndicator = when (result) {
    sides -> RollIndicator.CRITICAL
    1 -> RollIndicator.FUMBLE
    else -> RollIndicator.NONE
}

/**
 * Indicador de un tiro con uno o varios dados: es crítico si algún dado sale en la cara más
 * alta y pifia si todos salen en 1; en cualquier otro caso no hay indicador.
 */
fun determineRollIndicator(results: List<Int>, sides: Int = 6): RollIndicator =
    determineRollIndicator(results, List(results.size) { sides })

/**
 * Igual que la anterior, pero cada dado puede tener un número de caras distinto: es crítico si algún
 * dado sale en su cara más alta y pifia si todos salen en 1.
 *
 * @param sides Caras de cada dado, en el mismo orden que [results].
 */
fun determineRollIndicator(results: List<Int>, sides: List<Int>): RollIndicator = when {
    results.isEmpty() -> RollIndicator.NONE
    results.indices.any { results[it] == sides.getOrElse(it) { 6 } } -> RollIndicator.CRITICAL
    results.all { it == 1 } -> RollIndicator.FUMBLE
    else -> RollIndicator.NONE
}


/**
 * Niveles de dificultad listos para elegir. Cada uno fija los lanzamientos disponibles y cuánto de lo
 * que se espera sacar en promedio hay que conseguir para ganar ([goalFactor]). Como la meta sale del
 * promedio del dado y de cuántos dados se usan, el nivel es igual de duro con un D4 que con un D20.
 *
 * Con un D6 y un solo dado, la probabilidad de ganar es aproximadamente: Fácil 96 %, Normal 84 %,
 * Difícil 10 % y Experto 3 %.
 *
 * @property maxLaunches Lanzamientos disponibles.
 * @property goalFactor Fracción del total esperado que hay que sumar: menos de 1 es más fácil que el
 * azar medio y más de 1, más difícil.
 */
enum class Difficulty(val maxLaunches: Int, val goalFactor: Double) {
    EASY(maxLaunches = 12, goalFactor = 0.75),
    NORMAL(maxLaunches = MAX_LAUNCHES, goalFactor = TARGET_SCORE / 35.0),
    HARD(maxLaunches = 7, goalFactor = 1.25),
    EXPERT(maxLaunches = 6, goalFactor = 1.4);

    /**
     * Puntos para ganar con un dado de [sides] caras y [diceCount] dados: el promedio por tiro por los
     * lanzamientos, por [goalFactor], dentro de los límites permitidos.
     */
    fun targetScore(sides: Int, diceCount: Int): Int =
        targetScore(List(diceCount) { sides }, modifier = 0)

    /**
     * Igual que la anterior para dados de caras distintas ([sides], una por dado) y un [modifier] que
     * se suma a cada tiro: el promedio por tiro es la suma de los promedios de cada dado más el
     * modificador.
     */
    fun targetScore(sides: List<Int>, modifier: Int): Int {
        val averagePerRoll = (sides.sumOf { (it + 1) / 2.0 } + modifier).coerceAtLeast(1.0)
        return Math.round(averagePerRoll * maxLaunches * goalFactor).toInt()
            .coerceIn(MIN_TARGET_SCORE, MAX_TARGET_SCORE)
    }
}

/** Cantidad máxima de dados que se pueden lanzar a la vez. */
const val MAX_DICE = 3

/** Menor y mayor modificador que se puede sumar a cada tiro. */
const val MIN_ROLL_MODIFIER = -5
const val MAX_ROLL_MODIFIER = 5

/** Cantidad mínima y máxima de jugadores en una partida. */
const val MIN_PLAYERS = 1
const val MAX_PLAYERS = 3

/**
 * Resultado de una partida con varios jugadores que se turnan: gana quien llega primero a
 * [targetScore] puntos; si todos agotan sus [maxLaunches] lanzamientos antes, la partida termina
 * y gana quien sumó más (ver `DiceUiState.winners`).
 */
fun determineMultiplayerOutcome(
    scores: List<Int>,
    launches: List<Int>,
    targetScore: Int = TARGET_SCORE,
    maxLaunches: Int = MAX_LAUNCHES
): GameOutcome = when {
    scores.any { it >= targetScore } -> GameOutcome.WON
    launches.all { it >= maxLaunches } -> GameOutcome.LAUNCH_LIMIT_REACHED
    else -> GameOutcome.IN_PROGRESS
}
