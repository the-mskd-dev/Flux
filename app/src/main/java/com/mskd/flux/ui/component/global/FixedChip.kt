package com.mskd.flux.ui.component.global

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
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview

@Composable
fun FixedChip(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .height(32.dp)
            .background(backgroundColor)
            .padding(horizontal = FluxUI.Space.medium),
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
    FluxTheme {
        Box(modifier = Modifier.padding(all = FluxUI.Space.large)) {
            FixedChip(text = "FixedChip")
        }
    }
}