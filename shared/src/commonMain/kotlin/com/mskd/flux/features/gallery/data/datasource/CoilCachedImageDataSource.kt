package com.mskd.flux.features.gallery.data.datasource

import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import com.mskd.flux.features.gallery.domain.datasource.CachedImageDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.use

/**
 * Reads the disk cache of Coil, which holds the raw response of the images already displayed, keyed by their URL.
 */
@OptIn(ExperimentalCoilApi::class)
class CoilCachedImageDataSource(
    private val diskCache: DiskCache,
) : CachedImageDataSource {

    override suspend fun get(url: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            diskCache.openSnapshot(url)?.use { snapshot ->
                diskCache.fileSystem.read(snapshot.data) { readByteArray() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // An unreadable entry is the same as a missing one, the network is the fallback
            null
        }
    }

}
