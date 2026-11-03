package com.mskd.flux.features.images.domain

import coil3.request.ImageRequest
import coil3.size.Size

interface ImageRequestFactory {
    /** [size] must match the size of the display request to share its memory cache entry, null for the default size. */
    fun build(url: String, onEnd: (String) -> Unit, size: Size? = null): ImageRequest
}