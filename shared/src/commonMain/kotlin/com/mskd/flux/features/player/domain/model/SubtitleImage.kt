package com.mskd.flux.features.player.domain.model

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Zero-copy platform-backed image handle for a rasterized subtitle cue.
 *
 * Equality is identity-based (O(1)) so consecutive animated frames propagate through
 * StateFlow without pixel hashing or PNG encoding.
 */
expect class SubtitleImage {
    fun toImageBitmap(): ImageBitmap
}
