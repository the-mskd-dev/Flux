package com.mskd.flux.core.util.images

import coil3.request.ImageRequest

interface ImageRequestFactory {
    fun build(url: String, onEnd: (String) -> Unit): ImageRequest
}