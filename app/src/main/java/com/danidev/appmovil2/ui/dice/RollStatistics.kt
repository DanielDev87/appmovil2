package com.danidev.appmovil2.ui.dice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.theme.Appmovil2Theme

/*
 * HU-13: Estadísticas básicas de tiradas.
 *
 * "Como jugador, quiero ver un resumen de cuántas veces ha salido cada número en la
 * sesión, para verificar la aleatoriedad de los dados."
 */

/**
 * Suma una salida por cada valor de [rolls] al conteo por cara.
 *
 * Devuelve un mapa nuevo (el original no se modifica), de modo que el estado siga
 * siendo inmutable y Compose detecte el cambio.
 */
fun Map<Int, Int>.plusRolls(rolls: List<Int>): Map<Int, Int> {
    val result = toMutableMap()
    rolls.forEach { face -> result[face] = (result[face] ?: 0) + 1 }
    return result
}

/**
 * Sección con una tarjeta por cada cara del dado que indica cuántas veces ha salido
 * (por ejemplo, "El 6 · 4 veces"), para comprobar que el azar reparte los resultados.
 *
 * La tarjeta de la cara que más ha salido se resalta, y una barra pequeña muestra
 * cuánto pesa cada cara frente a la más frecuente.
 *
 * @param counts Veces que ha salido cada cara (ver [DiceUiState.faceCounts]).
 * @param faces Cantidad de caras del dado (por defecto, un dado de 6).
 * @param modifier Modificador opcional para el contenedor.
 */
@Composable
fun RollStatistics(
    counts: Map<Int, Int>,
    modifier: Modifier = Modifier,
    faces: Int = 6
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val total = counts.values.sum()
    val maxCount = counts.values.maxOrNull() ?: 0

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.stats_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = onBackground
        )

        if (total == 0) {
            Text(
                text = stringResource(R.string.stats_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = onBackground
            )
        } else {
            val columns = columnsFor(faces)
            (1..faces).chunked(columns).forEach { rowFaces ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowFaces.forEach { face ->
                        FaceCountCard(
                            face = face,
                            count = counts[face] ?: 0,
                            maxCount = maxCount,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Completa la fila para que las tarjetas mantengan el mismo ancho.
                    repeat(columns - rowFaces.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Cantidad de tarjetas por fila: 3 para un dado de 6 caras y 4 para los de más caras. */
private fun columnsFor(faces: Int): Int = if (faces > 6) 4 else 3

/** Tarjeta de una cara: su número, cuántas veces salió y su peso frente a la más frecuente. */
@Composable
private fun FaceCountCard(
    face: Int,
    count: Int,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val isTop = count > 0 && count == maxCount
    // Texto completo para lectores de pantalla: "El 6 ha salido 4 veces".
    val description = pluralStringResource(R.plurals.stats_summary, count, face, count)

    Card(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isTop) 0.95f else 0.6f)
        ),
        border = BorderStroke(
            width = if (isTop) 2.dp else 1.dp,
            color = onBackground.copy(alpha = if (isTop) 0.8f else 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTop) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_face_label, face),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = onBackground
            )
            Text(
                text = pluralStringResource(R.plurals.stats_times, count, count),
                style = MaterialTheme.typography.bodyMedium,
                color = onBackground
            )
            // Barra: ancho proporcional al conteo de la cara más frecuente.
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(onBackground.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
            ) {
                if (maxCount > 0 && count > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(count.toFloat() / maxCount)
                            .height(6.dp)
                            .background(onBackground, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RollStatisticsPreview() {
    Appmovil2Theme {
        RollStatistics(counts = mapOf(1 to 2, 2 to 1, 3 to 0, 4 to 3, 5 to 1, 6 to 4))
    }
}

@Preview(showBackground = true)
@Composable
private fun RollStatisticsEmptyPreview() {
    Appmovil2Theme {
        RollStatistics(counts = emptyMap())
    }
}
