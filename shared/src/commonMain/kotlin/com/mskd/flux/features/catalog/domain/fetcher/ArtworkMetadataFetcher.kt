package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.model.ArtworkWithFiles
import com.mskd.flux.report.Trace
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

    private suspend fun getArtworkById(folder: CatalogFolder, file: UserFile): Artwork? {

        val tmdbId = folder.tmdbId ?: return null
        val type = if (file.isEpisode) ContentType.SHOW else ContentType.MOVIE

        return try {
            api.getArtworkById(id = tmdbId, type = type)
                ?.takeUnless { it.isUnknown }
                ?.also { Trace.info(TAG, "Artwork $tmdbId found by TMDB ID for ${file.name}") }
                ?: run {
                    Trace.info(TAG, "Artwork $tmdbId not found by TMDB ID for ${file.name}, search by name")
                    null
                }
        } catch (e: Exception) {
            Trace.error(TAG, "Fail to get artwork by TMDB ID $tmdbId for ${file.name}, search by name", e)
            null
        }

    }

    override suspend fun fetch(
        folders: List<CatalogFolder>,
        onProgress: () -> Unit
    ): List<ArtworkWithFiles> {

        val artworkWithFiles = supervisorScope {

            folders.map { folder ->

                async (dispatcher) {

                    try {

                        val file = folder.files.first()

                        // The TMDB ID has priority, then the name
                        val artwork = getArtworkById(folder = folder, file = file) ?: api.getArtwork(file = file)

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