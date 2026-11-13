package com.mskd.flux.features.player.domain.model

/**
 * Technical description of the Media being played, read-only in the Settings sheet.
 * Every value is optional: null means the value could not be read, and is shown as a placeholder.
 */
data class MediaInfo(
    val container: String? = null,
    val videoCodec: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val frameRate: Float? = null,
    /** Video bitrate, in bits per second. */
    val bitrate: Int? = null,
    val audioCodec: String? = null,
    val channels: Int? = null,
    /** In Hz. */
    val sampleRate: Int? = null,
)
