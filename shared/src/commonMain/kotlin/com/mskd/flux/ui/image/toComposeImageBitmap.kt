package com.mskd.flux.ui.image

import androidx.compose.ui.graphics.ImageBitmap
import coil3.Image

expect fun Image.toComposeImageBitmap(): ImageBitmap