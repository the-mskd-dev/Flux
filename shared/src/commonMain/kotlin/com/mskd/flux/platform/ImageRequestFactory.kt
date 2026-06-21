package com.mskd.flux.platform

import coil3.request.ImageRequest

interface ImageRequestFactory {
    fun build(url: String, onEnd: (String) -> Unit): ImageRequest
}