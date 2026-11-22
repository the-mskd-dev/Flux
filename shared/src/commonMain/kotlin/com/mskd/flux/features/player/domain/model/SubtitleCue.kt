package com.mskd.flux.features.player.domain.model

/** Which edge of a cue sits on its position: the start, the middle or the end of the frame axis. */
enum class CueAnchor { START, CENTER, END }

/** An image cue (ASS/SSA via libass, PGS, VobSub, DVB): a zero-copy image handle and its size as a fraction of the video frame. */
data class SubtitleBitmap(
    val image: SubtitleImage,
    val widthFraction: Float,
    val heightFraction: Float,
) {
    /** True when both normalized frame fractions lie strictly inside (0, 1]. */
    val hasValidDimensions: Boolean
        get() = widthFraction > 0f && widthFraction <= 1f && heightFraction > 0f && heightFraction <= 1f
}

/** Inline formatting applied to a character range `[start, end)` of [SubtitleCue.text]. */
data class SubtitleTextSpan(
    val start: Int,
    val end: Int,
    /** ARGB color from the file, null when the span does not override color. */
    val color: Int? = null,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
) {
    /** True when this span carries at least one formatting override. */
    val hasStyle: Boolean
        get() = color != null || bold || italic || underline

    /** Returns a copy clamped to `[0, textLength]`, or null when empty or carrying no style. */
    fun normalizedFor(textLength: Int): SubtitleTextSpan? {
        if (!hasStyle || textLength <= 0) return null
        val clampedStart = start.coerceIn(0, textLength)
        val clampedEnd = end.coerceIn(0, textLength)
        if (clampedStart >= clampedEnd) return null
        return if (clampedStart == start && clampedEnd == end) this else copy(start = clampedStart, end = clampedEnd)
    }
}

/**
 * One subtitle on screen, independent of the playback engine.
 *
 * Text cues without explicit [line] or [position] render in the default bottom band with any inline
 * [spans]; cues with [line] or [position] render at their authored coordinates; cues with [bitmap]
 * render as rasterized images.
 */
data class SubtitleCue(
    val text: String? = null,
    /** Inline formatting spans over [text]; independent of explicit cue positioning. */
    val spans: List<SubtitleTextSpan> = emptyList(),
    /** Vertical position as a fraction of the frame height, from the top; null for the default bottom band. */
    val line: Float? = null,
    val lineAnchor: CueAnchor = CueAnchor.END,
    /** Horizontal position as a fraction of the frame width; null for the centred default. */
    val position: Float? = null,
    val positionAnchor: CueAnchor = CueAnchor.CENTER,
    val bitmap: SubtitleBitmap? = null,
) {
    /** True when the file placed this cue at explicit coordinates rather than the default bottom band. */
    val isPositioned: Boolean
        get() = line != null || position != null
}
