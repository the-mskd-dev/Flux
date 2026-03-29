package com.kaem.flux.ui.component

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
import com.kaem.flux.R
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.FluxPreview

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
        FixedChip(text = stringResource(id = R.string.app_name))
    }
}