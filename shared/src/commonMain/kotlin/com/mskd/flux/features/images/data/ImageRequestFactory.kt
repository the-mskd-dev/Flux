package com.mskd.flux.features.images.data

import coil3.request.ImageRequest

interface ImageRequestFactory {
    fun build(url: String, onEnd: (String) -> Unit): ImageRequest
}