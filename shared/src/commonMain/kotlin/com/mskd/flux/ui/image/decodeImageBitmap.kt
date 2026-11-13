package com.mskd.flux.ui.image

import androidx.compose.ui.graphics.ImageBitmap

/** Decodes an encoded image (PNG bytes) into an [ImageBitmap] for Compose, or null when the bytes are not an image. */
expect fun decodeImageBitmap(encoded: ByteArray): ImageBitmap?
