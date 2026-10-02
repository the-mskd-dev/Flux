package com.mskd.flux.features.images.data

import coil3.request.ImageRequest
import coil3.video.videoFrameMillis
import coil3.video.videoFramePercent

actual fun ImageRequest.Builder.videoFrame(
    currentTimeMillis: Long?,
    fallbackPercent: Double
): ImageRequest.Builder {
    return if (currentTimeMillis != null) videoFrameMillis(currentTimeMillis)
    else videoFramePercent(fallbackPercent)
}