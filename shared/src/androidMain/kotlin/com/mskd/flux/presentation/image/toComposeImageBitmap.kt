package com.mskd.flux.presentation.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Image
import coil3.toBitmap

actual fun Image.toComposeImageBitmap(): ImageBitmap {
    return this.toBitmap().asImageBitmap()
}