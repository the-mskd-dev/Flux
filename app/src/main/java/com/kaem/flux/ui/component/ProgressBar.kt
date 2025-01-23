package com.kaem.flux.ui.component

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.utils.extensions.minToMs

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    artwork: Artwork
) {
    LinearProgressIndicator(
        modifier = modifier,
        progress = { (artwork.currentTime.toFloat() / artwork.duration.minToMs) },
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}