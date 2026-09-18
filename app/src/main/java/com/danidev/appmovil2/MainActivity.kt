package com.danidev.appmovil2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.theme.Appmovil2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Appmovil2Theme {
                MoverDados()
            }
        }
    }
}

@Composable
fun MoverDados(diceViewModel: DiceViewModel = viewModel()){
    val uiState by diceViewModel.uiState.collectAsState()
    DadoConBotonImagen(
        diceValue = uiState.currentDiceValue,
        onRollClick = { diceViewModel.rollDice() }
    )
}

@Composable
fun DadoConBotonImagen(
    diceValue: Int,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
){
    val imageResource = when (diceValue) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(imageResource), contentDescription = diceValue.toString())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRollClick){
            Text(stringResource(R.string.roll))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoverDadosPreview() {
    Appmovil2Theme {
        DadoConBotonImagen(
            diceValue = 1,
            onRollClick = {}
        )
    }
}
