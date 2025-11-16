package com.kaem.flux.ui.component

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaem.flux.model.media.Media
import com.kaem.flux.utils.extensions.minToMs

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    media: Media
) {
    LinearProgressIndicator(
        modifier = modifier,
        progress = { (media.currentTime.toFloat() / media.duration.minToMs) },
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}