package com.mskd.flux.ui.component

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.utils.extensions.minToMs

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    media: Media
) {
    LinearProgressIndicator(
        modifier = modifier,
        color = color,
        progress = { (media.currentTime.toFloat() / media.duration.minToMs) },
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}