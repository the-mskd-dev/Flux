package com.mskd.flux.features.images.domain

import coil3.request.ImageRequest

interface ImageRequestFactory {
    fun build(url: String, onEnd: (String) -> Unit): ImageRequest
}