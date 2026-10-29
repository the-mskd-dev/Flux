package com.mskd.flux.features.gallery.domain.datasource

/**
 * Raw bytes of images fetched from the network.
 */
interface RemoteImageDataSource {

    /**
     * @throws Exception if the image cannot be fetched.
     */
    suspend fun fetch(url: String): ByteArray

}
