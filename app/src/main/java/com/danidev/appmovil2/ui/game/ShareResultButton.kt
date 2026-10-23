package com.danidev.appmovil2.ui.game

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import com.danidev.appmovil2.ui.style.StyledButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danidev.appmovil2.GameOutcome
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.dice.DiceUiState
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledCard

/**
 * HU-18: botón para compartir el resultado. En lugar de abrir directamente el selector del
 * sistema, muestra primero una ventana con el mensaje, con el mismo diseño de la app.
 */
@Composable
fun ShareResultButton(uiState: DiceUiState, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    var showDialog by remember { mutableStateOf(false) }

    StyledCard(
        radius = 16.dp,
        modifier = modifier
            .fillMaxWidth(0.55f)
            .clickable(role = Role.Button) { showDialog = true }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = tokens.ink,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.share_result),
                fontWeight = FontWeight.ExtraBold,
                color = tokens.ink
            )
        }
    }

    if (showDialog) {
        ShareDialog(uiState = uiState, onDismiss = { showDialog = false })
    }
}

/** Ventana centrada con el mensaje que se va a compartir y los botones para enviarlo o cerrar. */
@Composable
private fun ShareDialog(uiState: DiceUiState, onDismiss: () -> Unit) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val message = if (uiState.outcome == GameOutcome.WON) {
        stringResource(R.string.share_message_won, uiState.score, uiState.launches)
    } else {
        stringResource(R.string.share_message, uiState.score, uiState.targetScore, uiState.launches)
    }
    val chooserTitle = stringResource(R.string.share_chooser_title)
    val noApp = stringResource(R.string.share_no_app)

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
                    text = stringResource(R.string.share_dialog_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.ink
                )
                StyledCard(color = tokens.soft, radius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = message,
                        fontSize = 15.sp,
                        color = tokens.ink,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                StyledButton(
                    onClick = {
                        shareResult(context, message, chooserTitle, noApp)
                        onDismiss()
                    },
                    height = 54.dp
                ) {
                    Text(
                        text = stringResource(R.string.share_dialog_send),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.onAccent
                    )
                }
                Text(
                    text = stringResource(R.string.share_dialog_close),
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

/**
 * Lanza un Intent ACTION_SEND para enviar el texto a una app externa
 * (WhatsApp, Gmail, Mensajes, etc.).
 */
private fun shareResult(
    context: Context,
    message: String,
    chooserTitle: String,
    noAppMessage: String
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    val chooser = Intent.createChooser(sendIntent, chooserTitle)
    if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, noAppMessage, Toast.LENGTH_SHORT).show()
    }
}
