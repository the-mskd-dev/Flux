package com.mskd.flux.features.player.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.boundaryFractions
import com.mskd.flux.features.player.domain.model.currentAt
import com.mskd.flux.ui.text.Text
import com.mskd.flux.utils.extensions.formatMinSec
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.chapter_number
import org.jetbrains.compose.resources.stringResource

private val ChapterGap = 4.dp

/**
 * Slider split at Chapter boundaries (a gap at the start of each Chapter but the first, so segments
 * are proportional to the Chapter durations). With fewer than 2 Chapters the track is continuous.
 * While [isScrubbing], a bubble above the thumb shows the time and the Chapter name under it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChapterSlider(
    modifier: Modifier = Modifier,
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    duration: Long,
    chapters: List<VideoChapter>,
    isScrubbing: Boolean,
    interactionSource: MutableInteractionSource,
    isPlaying: Boolean,
    enabled: Boolean = true,
    showWave: Boolean = true,
    waveAnimated: Boolean = true,
) {

    val boundaries = remember(chapters, duration) { chapters.boundaryFractions(duration) }
    var width by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.onSizeChanged { width = it.width }) {

        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = value(),
            enabled = enabled,
            onValueChange = onValueChange,
            valueRange = 0f..duration.toFloat().coerceAtLeast(0f),
            onValueChangeFinished = onValueChangeFinished,
            interactionSource = interactionSource,
            track = { sliderState ->
                if (showWave) {
                    val gap = with(LocalDensity.current) { ChapterGap.toPx() }
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithContent {
                                drawContent()
                                boundaries.forEach { fraction ->
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(fraction * size.width - gap / 2f, 0f),
                                        size = Size(gap, size.height),
                                        blendMode = BlendMode.Clear
                                    )
                                }
                            },
                        amplitude = { if (isPlaying && waveAnimated) 1f else 0f },
                        progress = { if (duration > 0) sliderState.value / duration else 0f },
                        stopSize = 10.dp,
                        waveSpeed = 20.dp
                    )
                }
            },
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    thumbSize = DpSize(4.dp, 22.dp)
                )
            },
        )

        if (isScrubbing && duration > 0 && width > 0) {
            val position = value().toLong().coerceIn(0L, duration)
            val chapter = chapters.takeIf { it.size >= 2 }?.currentAt(position)
            val name = chapter?.let {
                it.title ?: stringResource(Res.string.chapter_number, chapters.indexOf(it) + 1)
            }
            ScrubBubble(
                modifier = Modifier.align(Alignment.TopStart),
                trackWidth = width,
                fraction = position.toFloat() / duration,
                time = position.formatMinSec(),
                chapterName = name
            )
        }

    }

}

@Composable
private fun ScrubBubble(
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
