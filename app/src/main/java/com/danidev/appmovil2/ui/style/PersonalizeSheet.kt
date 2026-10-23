package com.danidev.appmovil2.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danidev.appmovil2.R
import com.danidev.appmovil2.ui.player.StyledNameField

/**
 * Hoja "Personalizar": nombre del jugador (HU-12) y estilo visual de la app.
 *
 * @param style Estilo vigente.
 * @param onStyleChange Se invoca al elegir otro estilo; el cambio se aplica al instante.
 * @param onDismiss Se invoca al cerrar la hoja.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizeSheet(
    style: AppStyle,
    onStyleChange: (AppStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = tokens.surface,
        contentColor = tokens.ink
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.personalize_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tokens.ink
            )

            StyledNameField()

            SectionLabel(stringResource(R.string.personalize_style))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StyleOption(
                    option = AppStyle.BALANCED,
                    label = stringResource(R.string.style_balanced),
                    selected = style == AppStyle.BALANCED,
                    onClick = { onStyleChange(AppStyle.BALANCED) },
                    modifier = Modifier.weight(1f)
                )
                StyleOption(
                    option = AppStyle.PLAYFUL,
                    label = stringResource(R.string.style_playful),
                    selected = style == AppStyle.PLAYFUL,
                    onClick = { onStyleChange(AppStyle.PLAYFUL) },
                    modifier = Modifier.weight(1f)
                )
            }

            BackgroundSection()
        }
    }
}

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppTokens.current.muted
    )
}

/** Tarjeta con una vista previa en miniatura del estilo [option]. */
@Composable
private fun StyleOption(
    option: AppStyle,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = LocalAppTokens.current
    val preview = appTokens(option, current.isDark)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(role = Role.RadioButton, onClick = onClick)
    ) {
        // La miniatura se dibuja con los valores del estilo que representa, no con el vigente.
        CompositionLocalProvider(LocalAppTokens provides preview) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(preview.bgTop, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StyledCard(radius = 10.dp, modifier = Modifier.size(width = 36.dp, height = 26.dp)) {}
                        StyledCard(
                            radius = 10.dp,
                            color = preview.highlight.takeIf { preview.isPlayful } ?: preview.surface,
                            modifier = Modifier.size(width = 36.dp, height = 26.dp)
                        ) {}
                    }
                    StyledCard(
                        radius = 12.dp,
                        color = preview.accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    ) {}
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = current.ink
            )
            if (selected) {
                Spacer(Modifier.width(4.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .background(current.accent, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = current.onAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
