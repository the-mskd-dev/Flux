package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.model.ArtworkWithFiles
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface SeasonMetadataFetcher {
    suspend fun fetch(artworkWithFiles: List<ArtworkWithFiles>, onProgress: () -> Unit): List<Pair<Season, List<Episode>>>
}

class SeasonMetadataFetcherImpl(
    private val api: ApiRepository,
    private val dispatcher: CoroutineDispatcher
) : SeasonMetadataFetcher {

    private companion object { const val TAG = "SeasonMetadataFetcher" }

    override suspend fun fetch(artworkWithFiles: List<ArtworkWithFiles>, onProgress: () -> Unit): List<Pair<Season, List<Episode>>> {

        val folders = artworkWithFiles.filter { it.artwork.type == ContentType.SHOW && !it.artwork.isUnknown }

        return supervisorScope {

            folders.flatMap { (artwork, files) ->

                files
                    .map { it.season }
                    .distinct()
                    .filterNotNull()
                    .map { season ->

                        async(dispatcher) {

                            try {

                                api.getSeasonAndEpisodes(
                                    artworkId = artwork.id,
                                    season = season,
                                    files = files.filter { it.season == season }
                                )

                            } catch (e: Exception) {
                                Trace.error(TAG, "Fail to get season for artworkId ${artwork.id} - season $season", e)
                                null
                            } finally {
                                onProgress()
                            }

                        }

                    }.awaitAll().filterNotNull()

            }

        }

    }

}