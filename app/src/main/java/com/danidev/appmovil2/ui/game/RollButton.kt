package com.danidev.appmovil2.ui.game

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.style.LocalAppTokens
import com.danidev.appmovil2.ui.style.StyledButton

/** Botón principal para lanzar los dados. */
@Composable
fun RollButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    StyledButton(
        onClick = onClick,
        enabled = enabled,
        color = if (tokens.isPlayful) tokens.highlight else tokens.accent,
        modifier = modifier
    ) {
        Text(
            text = stringResource(if (tokens.isPlayful) R.string.roll_playful else R.string.roll),
            fontSize = if (tokens.isPlayful) 28.sp else 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tokens.onAccent
        )
    }
}
