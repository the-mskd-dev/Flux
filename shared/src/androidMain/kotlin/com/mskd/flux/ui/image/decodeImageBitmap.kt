package com.mskd.flux.ui.image

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeImageBitmap(encoded: ByteArray): ImageBitmap {
    return BitmapFactory.decodeByteArray(encoded, 0, encoded.size).asImageBitmap()
}
