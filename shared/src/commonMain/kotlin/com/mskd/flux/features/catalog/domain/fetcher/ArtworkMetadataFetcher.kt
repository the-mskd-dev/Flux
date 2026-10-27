package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.model.ArtworkFetchResult
import com.mskd.flux.features.catalog.domain.model.ArtworkWithFiles
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface ArtworkMetadataFetcher {
    suspend fun fetch(folders: List<CatalogFolder>, onProgress: () -> Unit): ArtworkFetchResult
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
    ): ArtworkFetchResult {

        val results = supervisorScope {

            folders.map { folder ->

                async (dispatcher) {

                    try {

                        val file = folder.files.first()

                        // The TMDB ID has priority, then the name
                        val artwork = getArtworkById(folder = folder, file = file) ?: api.getArtwork(file = file)

                        folder to ArtworkWithFiles(artwork = artwork, files = folder.files)

                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Not persisted: the folder is retried by the next Synchro légère
                        Trace.error(TAG, "Fail to get artwork for ${folder.files.first().name}", e)
                        folder to null
                    } finally {
                        onProgress()
                    }

                }

            }.awaitAll()

        }

        val found = results.mapNotNull { it.second }

        Trace.info(TAG, "Found ${found.size} artwork(s), ${results.size - found.size} failed folder(s)")

        return ArtworkFetchResult(
            found = found,
            failedFolders = results.filter { it.second == null }.map { it.first }
        )

    }

}