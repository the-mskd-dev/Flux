package com.mskd.flux.features.player.domain.model

/** Which edge of a cue sits on its position: the start, the middle or the end of the frame axis. */
enum class CueAnchor { START, CENTER, END }

/** An image cue (PGS, VobSub, DVB): PNG bytes, and its size as a fraction of the video frame. */
class SubtitleBitmap(
    val pngBytes: ByteArray,
    val widthFraction: Float,
    val heightFraction: Float,
) {
    override fun equals(other: Any?): Boolean =
        other is SubtitleBitmap &&
            pngBytes.contentEquals(other.pngBytes) &&
            widthFraction == other.widthFraction &&
            heightFraction == other.heightFraction

    override fun hashCode(): Int =
        (pngBytes.contentHashCode() * 31 + widthFraction.hashCode()) * 31 + heightFraction.hashCode()
}

/**
 * One subtitle on screen, independent of the playback engine.
 *
 * Plain cues (no color, no weight, no position, no image) keep the default look: a centred pill at the bottom.
 */
data class SubtitleCue(
    val text: String? = null,
    /** ARGB color from the file, null when the file gives none. */
    val color: Int? = null,
    val bold: Boolean = false,
    val italic: Boolean = false,
    /** Vertical position as a fraction of the frame height, from the top; null for the default bottom band. */
    val line: Float? = null,
    val lineAnchor: CueAnchor = CueAnchor.END,
    /** Horizontal position as a fraction of the frame width; null for the centred default. */
    val position: Float? = null,
    val positionAnchor: CueAnchor = CueAnchor.CENTER,
    val bitmap: SubtitleBitmap? = null,
) {
    /** True when the file styled or placed the cue, so it must not be drawn with the default look. */
    val isStyled: Boolean
        get() = color != null || bold || italic || line != null || position != null || bitmap != null
}
