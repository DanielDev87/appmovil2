package com.danidev.appmovil2.ui.dice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.theme.Appmovil2Theme

/*
 * HU-011: Historial de lanzamientos.
 *
 * "Como usuario, quiero ver una lista desplazable con los últimos 10 resultados
 * obtenidos, para revisar tiros anteriores."
 *
 * Este archivo contiene únicamente la UI del historial. El estado (la lista y su
 * límite de 10 elementos) lo gestiona [DiceViewModel]; aquí solo se dibuja.
 */

// region Componente principal

/**
 * Lista horizontal desplazable con los últimos lanzamientos.
 *
 * Se actualiza sola en cada lanzamiento porque recibe [history] desde el
 * `StateFlow` de [DiceViewModel]: cuando cambia la lista, Compose recompone.
 *
 * @param history Lanzamientos a mostrar, del más reciente al más antiguo.
 * @param modifier Modificador opcional para el contenedor.
 */
@Composable
fun RollHistory(
    history: List<RollRecord>,
    modifier: Modifier = Modifier
) {
    // Estado de desplazamiento de la lista, necesario para volver al inicio.
    val listState = rememberLazyListState()

    // Cuando llega un lanzamiento nuevo (cambia el id del primero), se desplaza
    // al inicio para que el usuario lo vea aunque hubiera hecho scroll.
    LaunchedEffect(history.firstOrNull()?.id) {
        if (history.isNotEmpty()) listState.animateScrollToItem(0)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.roll_history_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (history.isEmpty()) {
            // Estado vacío: aún no se ha lanzado el dado.
            Text(
                text = stringResource(R.string.roll_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .height(RollHistoryItemSize)
                    .padding(top = 16.dp)
            )
        } else {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // `key` estable por id: permite animar la entrada/salida de cada
                // elemento aunque haya valores repetidos.
                items(items = history, key = { it.id }) { record ->
                    RollHistoryItem(
                        record = record,
                        isLatest = record.id == history.first().id,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

// endregion

// region Elemento de la lista

/** Tamaño de cada elemento del historial. */
private val RollHistoryItemSize = 56.dp

/**
 * Tarjeta que muestra la cara del dado de un lanzamiento del historial.
 *
 * @param record Lanzamiento a mostrar.
 * @param isLatest `true` si es el lanzamiento más reciente; se resalta con el
 * color primario del tema para distinguirlo del resto.
 * @param modifier Modificador opcional (usado para la animación de la lista).
 */
@Composable
private fun RollHistoryItem(
    record: RollRecord,
    isLatest: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(RollHistoryItemSize),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLatest) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLatest) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Image(
            painter = painterResource(diceImageRes(record.value)),
            contentDescription = record.value.toString(),
            modifier = Modifier
                .size(RollHistoryItemSize)
                .padding(8.dp)
        )
    }
}

// endregion

// region Previews

@Preview(showBackground = true)
@Composable
private fun RollHistoryPreview() {
    Appmovil2Theme {
        RollHistory(
            history = listOf(6, 2, 4, 4, 1, 5, 3, 6, 2, 1)
                .mapIndexed { index, value -> RollRecord(id = index.toLong(), value = value) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RollHistoryEmptyPreview() {
    Appmovil2Theme {
        RollHistory(history = emptyList())
    }
}

// endregion
