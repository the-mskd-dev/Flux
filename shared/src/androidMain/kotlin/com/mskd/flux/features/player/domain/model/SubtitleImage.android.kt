package com.mskd.flux.features.player.domain.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual class SubtitleImage(private val bitmap: Bitmap) {
    actual fun toImageBitmap(): ImageBitmap = bitmap.asImageBitmap()

    override fun equals(other: Any?): Boolean =
        this === other || (other is SubtitleImage && bitmap === other.bitmap)

    override fun hashCode(): Int = System.identityHashCode(bitmap)
}
