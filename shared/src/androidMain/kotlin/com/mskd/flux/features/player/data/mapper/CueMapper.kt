package com.mskd.flux.features.player.data.mapper

import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.media3.common.text.Cue
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.SubtitleBitmap
import com.mskd.flux.features.player.domain.model.SubtitleCue
import com.mskd.flux.features.player.domain.model.SubtitleImage
import com.mskd.flux.features.player.domain.model.SubtitleTextSpan

/**
 * Turns a Media3 cue into the platform-independent [SubtitleCue].
 *
 * The only place that knows Media3 cue types.
 */
fun Cue.toSubtitleCue(): SubtitleCue {
    val mappedLine = line.takeIf { lineType == Cue.LINE_TYPE_FRACTION }?.fractionOrNull()
    val mappedPositionAnchor = positionAnchor.toCueAnchor(default = CueAnchor.CENTER)
    val rawPosition = position.fractionOrNull()
    // Media3's WebvttCueParser populates position=0.5f (CENTER) even for unpositioned cues
    // whose vertical line is unset or LINE_TYPE_NUMBER. Treat a default-centred horizontal
    // coordinate without a fractional vertical line as unpositioned so plain WebVTT cues
    // stack in the default bottom band instead of overlapping.
    val mappedPosition = rawPosition.takeUnless {
        bitmap == null && mappedLine == null && it == 0.5f && mappedPositionAnchor == CueAnchor.CENTER
    }

    return SubtitleCue(
        text = text?.toString(),
        spans = text.toSubtitleSpans(),
        line = mappedLine,
        lineAnchor = lineAnchor.toCueAnchor(default = CueAnchor.END),
        position = mappedPosition,
        positionAnchor = mappedPositionAnchor,
        bitmap = bitmap?.toSubtitleBitmap(width = size, height = bitmapHeight),
    )
}

private fun CharSequence?.toSubtitleSpans(): List<SubtitleTextSpan> {
    val spanned = this as? Spanned ?: return emptyList()
    if (spanned.isEmpty()) return emptyList()

    return spanned.getSpans(0, spanned.length, CharacterStyle::class.java)
        .orEmpty()
        .mapNotNull { rawSpan -> spanned.toSubtitleTextSpan(rawSpan) }
}

private fun Spanned.toSubtitleTextSpan(rawSpan: CharacterStyle): SubtitleTextSpan? {
    val start = getSpanStart(rawSpan)
    val end = getSpanEnd(rawSpan)
    val span = when (rawSpan) {
        is ForegroundColorSpan -> SubtitleTextSpan(start = start, end = end, color = rawSpan.foregroundColor)
        is StyleSpan -> SubtitleTextSpan(
            start = start,
            end = end,
            bold = rawSpan.style == Typeface.BOLD || rawSpan.style == Typeface.BOLD_ITALIC,
            italic = rawSpan.style == Typeface.ITALIC || rawSpan.style == Typeface.BOLD_ITALIC,
        )
        is UnderlineSpan -> SubtitleTextSpan(start = start, end = end, underline = true)
        else -> return null
    }
    return span.normalizedFor(length)
}

/** Media3 reports unknown dimensions as a sentinel outside the frame; only a real fraction is kept. */
private fun Float.fractionOrNull(): Float? = takeIf { it in 0f..1f }

private fun Int.toCueAnchor(default: CueAnchor): CueAnchor = when (this) {
    Cue.ANCHOR_TYPE_START -> CueAnchor.START
    Cue.ANCHOR_TYPE_MIDDLE -> CueAnchor.CENTER
    Cue.ANCHOR_TYPE_END -> CueAnchor.END
    else -> default
}

/** An image cue without a known size in (0, 1] cannot be placed honestly, so it is dropped. */
private fun Bitmap.toSubtitleBitmap(width: Float, height: Float): SubtitleBitmap? =
    SubtitleBitmap(
        image = SubtitleImage(this),
        widthFraction = width,
        heightFraction = height,
    ).takeIf { it.hasValidDimensions }
