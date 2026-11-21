package com.mskd.flux.features.player.data.mapper

import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.media3.common.text.Cue
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.SubtitleBitmap
import com.mskd.flux.features.player.domain.model.SubtitleCue
import java.io.ByteArrayOutputStream

/**
 * Turns a Media3 cue into the platform-independent [SubtitleCue].
 *
 * The only place that knows Media3 cue types. Style is read per cue: a span covering part of the text
 * still styles the whole cue, which is a known limit of the model.
 */
fun Cue.toSubtitleCue(): SubtitleCue {
    val style = text.textStyle()

    return SubtitleCue(
        text = text?.toString(),
        color = style.color,
        bold = style.bold,
        italic = style.italic,
        line = line.takeIf { lineType == Cue.LINE_TYPE_FRACTION }?.fractionOrNull(),
        lineAnchor = lineAnchor.toCueAnchor(default = CueAnchor.END),
        position = position.fractionOrNull(),
        positionAnchor = positionAnchor.toCueAnchor(default = CueAnchor.CENTER),
        bitmap = bitmap?.toSubtitleBitmap(width = size.fractionOrNull(), height = bitmapHeight.fractionOrNull()),
    )
}

/** Style carried by the spans of a cue's text. */
data class CueTextStyle(val color: Int?, val bold: Boolean, val italic: Boolean)

/**
 * Reads color and weight from the spans of a cue text. Kept apart from [Cue] because Media3 copies
 * a [Spanned] into its own span storage when a cue is built, so spans are read from the text itself.
 */
fun CharSequence?.textStyle(): CueTextStyle {
    val spanned = this as? Spanned ?: return CueTextStyle(color = null, bold = false, italic = false)
    val styles = spanned.getSpans(0, spanned.length, StyleSpan::class.java).map { it.style }
    return CueTextStyle(
        color = spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java).lastOrNull()?.foregroundColor,
        bold = styles.any { it == Typeface.BOLD || it == Typeface.BOLD_ITALIC },
        italic = styles.any { it == Typeface.ITALIC || it == Typeface.BOLD_ITALIC },
    )
}

/** Media3 reports unknown dimensions as a sentinel outside the frame; only a real fraction is kept. */
private fun Float.fractionOrNull(): Float? = takeIf { it in 0f..1f }

private fun Int.toCueAnchor(default: CueAnchor): CueAnchor = when (this) {
    Cue.ANCHOR_TYPE_START -> CueAnchor.START
    Cue.ANCHOR_TYPE_MIDDLE -> CueAnchor.CENTER
    Cue.ANCHOR_TYPE_END -> CueAnchor.END
    else -> default
}

/** An image cue without a known size cannot be placed honestly, so it is dropped. */
private fun Bitmap.toSubtitleBitmap(width: Float?, height: Float?): SubtitleBitmap? {
    if (width == null || height == null) return null
    val png = ByteArrayOutputStream().also { compress(Bitmap.CompressFormat.PNG, 100, it) }
    return SubtitleBitmap(png.toByteArray(), widthFraction = width, heightFraction = height)
}
