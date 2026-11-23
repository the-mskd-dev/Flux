package com.mskd.flux.features.player.domain.model

import androidx.compose.ui.graphics.ImageBitmap

actual class SubtitleImage(private val imageBitmap: ImageBitmap) {
    actual fun toImageBitmap(): ImageBitmap = imageBitmap

    override fun equals(other: Any?): Boolean =
        this === other || (other is SubtitleImage && imageBitmap === other.imageBitmap)

    override fun hashCode(): Int = imageBitmap.hashCode()
}
