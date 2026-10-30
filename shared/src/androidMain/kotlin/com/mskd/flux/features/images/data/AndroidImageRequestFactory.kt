package com.mskd.flux.features.images.data

import android.content.Context
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Size
import com.mskd.flux.features.images.domain.ImageRequestFactory

class AndroidImageRequestFactory(private val context: Context) : ImageRequestFactory {
    override fun build(url: String, onEnd: (String) -> Unit, size: Size?): ImageRequest {
        return ImageRequest.Builder(context = context)
            .data(url)
            .apply { size?.let { size(it) } }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .listener(
                onSuccess = { _, _ -> onEnd(url) },
                onError   = { _, _ -> onEnd(url) },
                onCancel  = { _    -> onEnd(url) },
            )
            .build()
    }

}