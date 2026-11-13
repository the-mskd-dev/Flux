package com.mskd.flux.features.player.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.text.Text

/** Chip shown above the thumb while scrubbing: the time and, when known, the Chapter name. */
@Composable
fun ChapterChip(
    modifier: Modifier,
    trackWidth: Int,
    fraction: Float,
    time: String,
    chapterName: String?,
) {

    // Centered on the thumb, kept inside the track, and placed above the slider.
    Layout(
        modifier = modifier,
        content = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text.Card.Label(text = time)
                    chapterName?.let { Text.Card.Label(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            }
        }
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(minWidth = 0, minHeight = 0, maxWidth = trackWidth)
        )
        layout(trackWidth, 0) {
            val x = (fraction * trackWidth - placeable.width / 2f)
                .coerceIn(0f, (trackWidth - placeable.width).coerceAtLeast(0).toFloat())
            placeable.place(x.toInt(), -placeable.height - 8.dp.roundToPx())
        }
    }

}
