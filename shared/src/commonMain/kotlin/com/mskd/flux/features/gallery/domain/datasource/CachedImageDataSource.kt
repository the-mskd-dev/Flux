package com.mskd.flux.features.gallery.domain.datasource

/**
 * Raw bytes of images already downloaded for display.
 */
interface CachedImageDataSource {

    /**
     * @return the bytes of the image at [url], or null if it is not in the cache.
     */
    suspend fun get(url: String): ByteArray?

}
