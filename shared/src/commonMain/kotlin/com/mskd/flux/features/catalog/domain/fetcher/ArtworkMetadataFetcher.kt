package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.mapper.toDomain
import com.mskd.flux.features.catalog.domain.model.ArtworkFiles
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface ArtworkMetadataFetcher {
    suspend fun fetch(folders: List<CatalogFolder>, onProgress: () -> Unit): List<ArtworkFiles>
}

class ArtworkMetadataFetcherImpl(
    private val tmdb: TmdbDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)
) : ArtworkMetadataFetcher {

    private companion object { const val TAG = "ArtworkFolderResolver" }

    override suspend fun fetch(
        folders: List<CatalogFolder>,
        onProgress: () -> Unit
    ): List<ArtworkFiles> {

        val artworkFiles = supervisorScope {

            folders.map { folder ->

                async (dispatcher) {

                    try {

                        val tmdbArtwork = tmdb.getTmdbArtwork(file = folder.files.first())
                        val artwork = tmdbArtwork?.toDomain() ?: Artwork.UNKNOWN

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