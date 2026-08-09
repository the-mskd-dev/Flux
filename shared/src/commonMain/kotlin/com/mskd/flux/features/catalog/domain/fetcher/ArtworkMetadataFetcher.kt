package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.model.ArtworkWithFiles
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface ArtworkMetadataFetcher {
    suspend fun fetch(folders: List<CatalogFolder>, onProgress: () -> Unit): List<ArtworkWithFiles>
}

class ArtworkMetadataFetcherImpl(
    private val api: ApiRepository,
    private val dispatcher: CoroutineDispatcher
) : ArtworkMetadataFetcher {

    private companion object { const val TAG = "ArtworkFolderResolver" }

    override suspend fun fetch(
        folders: List<CatalogFolder>,
        onProgress: () -> Unit
    ): List<ArtworkWithFiles> {

        val artworkWithFiles = supervisorScope {

            folders.map { folder ->

                async (dispatcher) {

                    try {

                        val artwork = api.getArtwork(file = folder.files.first())

                        ArtworkWithFiles(artwork = artwork, files = folder.files)

                    } catch (e: Exception) {
                        Trace.error(TAG, "Fail to get artwork for ${folder.files.first().name}", e)
                        ArtworkWithFiles(artwork = Artwork.UNKNOWN, files = folder.files)
                    } finally {
                        onProgress()
                    }

                }

            }.awaitAll()

        }

        Trace.info(TAG, "Found ${artworkWithFiles.size} artwork(s)")

        return artworkWithFiles

    }

}