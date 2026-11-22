package com.mskd.flux.features.player.presentation

import androidx.media3.common.VideoSize

/**
 * Width / height of the picture as displayed, with non-square pixels (anamorphic video) applied,
 * or null while the size is unknown. Matches the rectangle Media3's `ContentFrame` fits the picture into.
 */
val VideoSize.displayAspectRatio: Float?
    get() {
        if (width <= 0 || height <= 0) return null
        val par = if (pixelWidthHeightRatio > 0f) pixelWidthHeightRatio else 1f
        return width * par / height
    }
