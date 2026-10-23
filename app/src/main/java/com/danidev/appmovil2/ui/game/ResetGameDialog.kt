package com.danidev.appmovil2.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledButton
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * Pregunta antes de empezar la partida de cero, porque se pierden los puntos, los lanzamientos y
 * el historial. Tiene el mismo diseño que el resto de ventanas de la app.
 */
@Composable
fun ResetGameDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val tokens = LocalAppTokens.current
    Dialog(onDismissRequest = onDismiss) {
        StyledCard(radius = 28.dp, modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.reset_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.ink,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.reset_message),
                    fontSize = 15.sp,
                    color = tokens.muted,
                    textAlign = TextAlign.Center
                )
                StyledButton(onClick = onConfirm, height = 54.dp) {
                    Text(
                        text = stringResource(R.string.reset_confirm),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.onAccent
                    )
                }
                Text(
                    text = stringResource(R.string.reset_cancel),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.ink,
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .padding(vertical = 6.dp)
                )
            }
        }
    }
}
