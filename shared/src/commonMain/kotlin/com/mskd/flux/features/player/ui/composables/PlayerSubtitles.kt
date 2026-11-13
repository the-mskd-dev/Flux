package com.mskd.flux.features.player.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.SubtitleCue
import com.mskd.flux.ui.image.decodeImageBitmap
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** Vertical position of a styled cue that the file did not place: near the bottom of the frame. */
private const val DEFAULT_LINE = 0.95f

/**
 * Subtitles over the video frame, which is the player box (its ratio follows the video).
 *
 * Plain cues keep the default look: a centred pill at the bottom. Styled cues take the file's color,
 * weight and placement; image cues are drawn as bitmaps at the place they ask for.
 */
@Composable
fun PlayerSubtitles(
    cues: () -> List<SubtitleCue>,
    smallText: Boolean,
    modifier: Modifier = Modifier
) {
    val current = cues()
    val (styled, plain) = remember(current) { current.partition { it.isStyled } }

    Box(modifier = modifier.fillMaxSize()) {

        styled.forEach { StyledCue(cue = it, smallText = smallText) }

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FluxUI.Space.large)
                .padding(bottom = FluxUI.Space.large)
        ) {
            plain.forEach { cue ->
                DefaultCue(text = cue.text, smallText = smallText)
            }
        }

    }

}

@Composable
private fun DefaultCue(text: String?, smallText: Boolean) {
    val style = if (smallText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium
    CuePill(text = text, color = Color.White, style = style)
}

/** Places one cue inside the frame: [SubtitleCue.position] and [SubtitleCue.line] are fractions, anchored by their edge. */
@Composable
private fun StyledCue(cue: SubtitleCue, smallText: Boolean) {
    val bitmap = cue.bitmap
    // Decoded off the main thread; the cue is drawn once the image is ready
    val image by produceState<ImageBitmap?>(initialValue = null, bitmap) {
        value = bitmap?.let { withContext(Dispatchers.Default) { decodeImageBitmap(it.pngBytes) } }
    }

    val decoded = image
    Layout(
        content = {
            if (decoded != null) {
                Image(bitmap = decoded, contentDescription = null)
            } else if (bitmap == null) {
                StyledText(cue = cue, smallText = smallText)
            }
        }
    ) { measurables, constraints ->
        val frameWidth = constraints.maxWidth
        val frameHeight = constraints.maxHeight

        // An image that could not be decoded has nothing to place
        val child = measurables.firstOrNull() ?: return@Layout layout(frameWidth, frameHeight) {}

        val placeable = child.measure(
            if (bitmap != null) {
                Constraints.fixed(
                    (bitmap.widthFraction * frameWidth).roundToInt(),
                    (bitmap.heightFraction * frameHeight).roundToInt()
                )
            } else {
                Constraints(maxWidth = frameWidth, maxHeight = frameHeight)
            }
        )

        val x = (cue.position ?: 0.5f) * frameWidth - cue.positionAnchor.anchorShare() * placeable.width
        val y = (cue.line ?: DEFAULT_LINE) * frameHeight - cue.lineAnchor.anchorShare() * placeable.height

        layout(frameWidth, frameHeight) {
            placeable.place(x.roundToInt(), y.roundToInt())
        }
    }
}

@Composable
private fun StyledText(cue: SubtitleCue, smallText: Boolean) {
    val base = if (smallText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium
    val style = base.copy(
        fontWeight = if (cue.bold) FontWeight.Bold else base.fontWeight,
        fontStyle = if (cue.italic) FontStyle.Italic else base.fontStyle,
    )
    CuePill(text = cue.text, color = cue.color?.let { Color(it) } ?: Color.White, style = style)
}

@Composable
private fun CuePill(text: String?, color: Color, style: TextStyle) {
    Text.Adaptive(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.extraSmall)
            .background(color = Color.Black.copy(.8f))
            .padding(horizontal = FluxUI.Space.medium, vertical = FluxUI.Space.small),
        text = text,
        color = color,
        textAlign = TextAlign.Center,
        style = style
    )
}

/** Share of the child that sits before the given position: START 0, CENTER half, END the whole child. */
private fun CueAnchor.anchorShare(): Float = when (this) {
    CueAnchor.START -> 0f
    CueAnchor.CENTER -> 0.5f
    CueAnchor.END -> 1f
}
