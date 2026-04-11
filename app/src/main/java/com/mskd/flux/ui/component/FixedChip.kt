package com.mskd.flux.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@Composable
fun FixedChip(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        modifier = Modifier
            .clip(Ui.Shape.Corner.Small)
            .height(32.dp)
            .background(backgroundColor)
            .padding(horizontal = Ui.Space.MEDIUM),
        contentAlignment = Alignment.Center
    ) {
        Text.Label.Medium(
            text = text,
            color = textColor
        )
    }
}

@FluxPreview
@Composable
fun FixedChip_Preview() {
    AppTheme {
        Box(modifier = Modifier.padding(all = Ui.Space.LARGE)) {
            FixedChip(text = stringResource(id = R.string.app_name))
        }
    }
}