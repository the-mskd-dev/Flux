package com.mskd.flux.features.images.data

import coil3.request.ImageRequest

expect fun ImageRequest.Builder.videoFrame(
    currentTimeMillis: Long?,
    fallbackPercent: Double
): ImageRequest.Builder