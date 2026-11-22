package com.mskd.flux.features.player.domain.selection

/**
 * ID given to each external subtitle attached to a Media, so its Track is recognized as external.
 *
 * MergingMediaSource (and nested FfmpegSubtitleSourceFactory + DefaultMediaSourceFactory) prepends one
 * or more "<sourceIndex>:" prefixes to the Track ID (e.g. "2:flux-ext-1", "0:1:flux-ext-3").
 */
object ExternalSubtitleTrackId {

    private const val PREFIX = "flux-ext-"
    private val REGEX = Regex("^(?:\\d+:)*${Regex.escape(PREFIX)}(\\d+)$")

    fun of(externalOrder: Int): String = "$PREFIX$externalOrder"

    /** External order encoded in [trackId], null for an embedded Track. */
    fun externalOrderOf(trackId: String?): Int? {
        if (trackId == null) return null
        return REGEX.matchEntire(trackId)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }
}
