package com.danidev.appmovil2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.danidev.appmovil2.ui.theme.Appmovil2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DadoTheme{
                MoverDados()
            }
        }
    }
}

@Composable
fun MoverDados(){
    DadoConBotonImagen()
}

@Preview(showBackground = true)
@Composable
fun DadoConBotonImagen(modifier: Modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)){
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(R.drawable.dice_1), contentDescription = "1")
        Button(onClick =  { }){
            Text(stringResource(R.string.roll))
        }
    }
}