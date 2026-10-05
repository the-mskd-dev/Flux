package com.mskd.flux.presentation.image

import androidx.compose.ui.graphics.ImageBitmap
import coil3.Image

expect fun Image.toComposeImageBitmap(): ImageBitmap