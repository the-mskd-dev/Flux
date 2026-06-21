package com.mskd.flux.platform

import android.content.Context
import coil3.request.CachePolicy
import coil3.request.ImageRequest

class AndroidImageRequestFactory(private val context: Context) : ImageRequestFactory {
    override fun build(url: String, onEnd: (String) -> Unit): ImageRequest {
        return ImageRequest.Builder(context = context)
            .data(url)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .listener(
                onSuccess = { _, _ -> onEnd(url) },
                onError   = { _, _ -> onEnd(url) },
                onCancel  = { _    -> onEnd(url) },
            )
            .build()
    }

}