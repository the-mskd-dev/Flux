package com.mskd.flux.features.player.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.SubtitleCue
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import kotlin.math.roundToInt

/** Vertical position of an explicitly positioned cue when only horizontal placement is provided. */
private const val DEFAULT_LINE = 0.95f

private data class SubtitleLayers(
    val bitmaps: List<SubtitleCue>,
    val positionedText: List<SubtitleCue>,
    val bottomText: List<SubtitleCue>,
)

/**
 * Subtitles over the video frame, which is the player box (its ratio follows the video).
 *
 * Renders three layers: rasterized bitmap cues at authored normalized bounds, explicitly positioned
 * text cues at authored coordinates, and a centred bottom column stacking all unpositioned plain
 * and inline-styled text cues so simultaneous lines never overlap.
 */
@Composable
fun PlayerSubtitles(
    cues: () -> List<SubtitleCue>,
    smallText: Boolean,
    modifier: Modifier = Modifier
) {
    val current = cues()
    val layers = remember(current) { current.partitionIntoLayers() }

    Box(modifier = modifier.fillMaxSize()) {
        if (layers.bitmaps.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                layers.bitmaps.forEach { BitmapCue(cue = it) }
            }
        }

        if (layers.positionedText.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                layers.positionedText.forEach { PositionedTextCue(cue = it, smallText = smallText) }
            }
        }

        if (layers.bottomText.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = FluxUI.Space.large)
                    .padding(bottom = FluxUI.Space.large),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
                ) {
                    layers.bottomText.forEach { cue ->
                        CuePill(cue = cue, smallText = smallText)
                    }
                }
            }
        }
    }
}

private fun List<SubtitleCue>.partitionIntoLayers(): SubtitleLayers {
    val bitmaps = mutableListOf<SubtitleCue>()
    val positionedText = mutableListOf<SubtitleCue>()
    val bottomText = mutableListOf<SubtitleCue>()

    for (cue in this) {
        when {
            cue.bitmap != null -> if (cue.bitmap.hasValidDimensions) bitmaps += cue
            cue.text.isNullOrBlank() -> Unit
            cue.isPositioned -> positionedText += cue
            else -> bottomText += cue
        }
    }

    return SubtitleLayers(
        bitmaps = bitmaps,
        positionedText = positionedText,
        bottomText = bottomText,
    )
}

@Composable
private fun BitmapCue(cue: SubtitleCue) {
    val bitmap = cue.bitmap ?: return
    if (!bitmap.hasValidDimensions) return
    val image = remember(bitmap.image) { bitmap.image.toImageBitmap() }

    PositionedCueLayout(
        cue = cue,
        measureChild = { measurable, constraints ->
            measurable.measure(
                Constraints.fixed(
                    (bitmap.widthFraction * constraints.maxWidth).roundToInt(),
                    (bitmap.heightFraction * constraints.maxHeight).roundToInt(),
                )
            )
        },
    ) {
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
        )
    }
}

@Composable
private fun PositionedTextCue(cue: SubtitleCue, smallText: Boolean) {
    PositionedCueLayout(
        cue = cue,
        measureChild = { measurable, constraints ->
            measurable.measure(
                Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
            )
        },
    ) {
        CuePill(cue = cue, smallText = smallText)
    }
}

/** Places one cue inside the frame: [SubtitleCue.position] and [SubtitleCue.line] are fractions, anchored by their edge. */
@Composable
private fun PositionedCueLayout(
    cue: SubtitleCue,
    measureChild: MeasureScope.(Measurable, Constraints) -> Placeable,
    content: @Composable () -> Unit,
) {
    Layout(content = content) { measurables, constraints ->
        val frameWidth = constraints.maxWidth
        val frameHeight = constraints.maxHeight
        val child = measurables.firstOrNull() ?: return@Layout layout(frameWidth, frameHeight) {}

        val placeable = measureChild(child, constraints)
        val x = (cue.position ?: 0.5f) * frameWidth - cue.positionAnchor.anchorShare() * placeable.width
        val y = (cue.line ?: DEFAULT_LINE) * frameHeight - cue.lineAnchor.anchorShare() * placeable.height

        layout(frameWidth, frameHeight) {
            placeable.place(x.roundToInt(), y.roundToInt())
        }
    }
}

@Composable
private fun CuePill(cue: SubtitleCue, smallText: Boolean) {
    val annotatedText = remember(cue.text, cue.spans) { cue.toAnnotatedString() }
    val base = if (smallText) Text.Style.contentLabel() else Text.Style.contentBody()

    Text.Annotated(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.extraSmall)
            .background(color = Color.Black.copy(.5f))
            .padding(horizontal = FluxUI.Space.small, vertical = FluxUI.Space.extraSmall),
        text = annotatedText,
        color = Color.White,
        textAlign = TextAlign.Center,
        style = base.copy(lineHeight = base.fontSize * 1.2f),
    )
}

private fun SubtitleCue.toAnnotatedString(): AnnotatedString {
    val raw = text.orEmpty()
    if (spans.isEmpty() || raw.isEmpty()) return AnnotatedString(raw)

    return buildAnnotatedString {
        append(raw)
        spans.forEach { span ->
            val normalized = span.normalizedFor(raw.length) ?: return@forEach
            addStyle(
                style = SpanStyle(
                    color = normalized.color?.let(::Color) ?: Color.Unspecified,
                    fontWeight = if (normalized.bold) FontWeight.Bold else null,
                    fontStyle = if (normalized.italic) FontStyle.Italic else null,
                    textDecoration = if (normalized.underline) TextDecoration.Underline else null,
                ),
                start = normalized.start,
                end = normalized.end,
            )
        }
    }
}

/** Share of the child that sits before the given position: START 0, CENTER half, END the whole child. */
private fun CueAnchor.anchorShare(): Float = when (this) {
    CueAnchor.START -> 0f
    CueAnchor.CENTER -> 0.5f
    CueAnchor.END -> 1f
}
