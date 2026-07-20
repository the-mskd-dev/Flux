package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.network.tmdb.domain.repository.ArtworkRemoteRepository
import com.mskd.flux.features.catalog.domain.model.ArtworkFiles
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface ArtworkFolderFetcher {
    suspend fun fetch(folders: List<CatalogFolder>, onProgress: () -> Unit): List<ArtworkFiles>
}

class ArtworkFolderFetcherImpl(
    private val remoteRepository: ArtworkRemoteRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)
) : ArtworkFolderFetcher {

    private companion object { const val TAG = "ArtworkFolderResolver" }

    override suspend fun fetch(
        folders: List<CatalogFolder>,
        onProgress: () -> Unit
    ): List<ArtworkFiles> {

        val artworkFiles = supervisorScope {

            folders.map { folder ->

                async (dispatcher) {

                    try {

                        val artwork = remoteRepository.getArtwork(file = folder.files.first()) ?: Artwork.UNKNOWN

                        ArtworkFiles(artwork = artwork, files = folder.files)

                    } catch (e: Exception) {
                        Trace.error(TAG, "Fail to get artwork for ${folder.files.first().name}", e)
                        ArtworkFiles(artwork = Artwork.UNKNOWN, files = folder.files)
                    } finally {
                        onProgress()
                    }

                }

            }.awaitAll()

        }

        Trace.info(TAG, "Found ${artworkFiles.size} artwork(s)")

        return artworkFiles

    }

}