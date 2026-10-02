package com.danidev.appmovil2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun MoverDados(
    diceViewModel: DiceViewModel = viewModel()
) {
    val uiState by diceViewModel.uiState.collectAsState()

    DadoConBotonImagen(
        diceValue = uiState.currentDiceValue,
        onRollClick = {
            diceViewModel.rollDice()
        }
    )
}

@Composable
fun DadoConBotonImagen(
    diceValue: Int,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotation by remember {
        mutableFloatStateOf(0f)
    }

    var diceSize by remember {
        mutableFloatStateOf(150f)
    }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ),
        label = "DiceRotation"
    )

    val imageResource = when (diceValue) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Tamaño del dado",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppRed
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Image(
            painter = painterResource(imageResource),
            contentDescription = "Dado mostrando el número $diceValue",
            modifier = Modifier
                .size(diceSize.dp)
                .graphicsLayer(
                    rotationZ = animatedRotation
                )
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Text(
            text = "Ajusta el tamaño",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Slider(
            value = diceSize,
            onValueChange = {
                diceSize = it
            },
            valueRange = 80f..240f,
            steps = 7,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = AppRed,
                activeTrackColor = AppRed,
                inactiveTrackColor = AppRed.copy(alpha = 0.25f)
            )
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "${diceSize.toInt()} dp",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppRed
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Más pequeño",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )

            Text(
                text = "Más grande",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppRed
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Button(
            onClick = {
                rotation += 360f
                onRollClick()
            },
            modifier = Modifier
                .width(180.dp)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppRed,
                contentColor = White
            )
        ) {
            Text(
                text = stringResource(R.string.roll),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val AppRed = Color(0xFFE21B2D)
private val White = Color(0xFFFFFFFF)

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