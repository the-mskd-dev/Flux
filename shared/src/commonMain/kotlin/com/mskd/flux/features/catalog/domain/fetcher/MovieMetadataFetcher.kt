package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.network.tmdb.domain.repository.ArtworkRemoteRepository
import com.mskd.flux.features.catalog.domain.model.ArtworkFiles
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface MovieMetadataFetcher {
    suspend fun fetch(artworkFiles: List<ArtworkFiles>, onProgress: () -> Unit): List<Media>
}

class MovieMetadataFetcherImpl(
    private val remoteRepository: ArtworkRemoteRepository,
    private val getFileDurationUseCase: GetFileDurationUseCase,
    private val dispatcher: CoroutineDispatcher
) : MovieMetadataFetcher {

    private companion object { const val TAG = "MovieMetadataFetcher" }

    override suspend fun fetch(
        artworkFiles: List<ArtworkFiles>,
        onProgress: () -> Unit
    ): List<Media> {

        val movies = supervisorScope {

            artworkFiles.filter { it.artwork.type == ContentType.MOVIE }.map { (artwork, files) ->

                async(dispatcher) {

                    try {

                        val file = files.first()

                        when {
                            artwork.id == Artwork.UNKNOWN_ID -> Episode(file = file)
                            else -> {

                                remoteRepository.getMovie(
                                    artworkId = artwork.id,
                                    file = file,
                                    fallbackDuration = { getFileDurationUseCase(file = file) }
                                ) ?: Episode(file = file, duration = getFileDurationUseCase(file = file))

                            }
                        }

                    } catch (e: Exception) {
                        Trace.error(TAG, "Fail to get movie from ${files.first().name}", e)
                        null
                    } finally {
                        onProgress()
                    }

                }

            }.awaitAll().filterNotNull()

        }

        Trace.info(TAG, "Found ${movies.size} movie(s)")

        return movies

    }

}