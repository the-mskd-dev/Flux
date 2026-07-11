package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.network.tmdb.data.remote.dto.EpisodeDto
import com.mskd.flux.features.catalog.domain.model.ArtworkFiles
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.remote.mapper.toDomain
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface SeasonMetadataFetcher {
    suspend fun fetch(artworkFiles: List<ArtworkFiles>): List<Pair<Season, List<EpisodeDto>>>
}

class SeasonMetadataFetcherImpl(
    private val tmdb: TmdbDataSource,
    private val dispatcher: CoroutineDispatcher
) : SeasonMetadataFetcher {

    private companion object { const val TAG = "SeasonMetadataFetcher" }

    override suspend fun fetch(artworkFiles: List<ArtworkFiles>): List<Pair<Season, List<EpisodeDto>>> {

        val folders = artworkFiles.filter { it.artwork.type == ContentType.SHOW && it.artwork.id != Artwork.UNKNOWN_ID }

        return supervisorScope {

            folders.flatMap { (artwork, files) ->

                files
                    .map { it.season }
                    .distinct()
                    .filterNotNull()
                    .map { season ->

                        async(dispatcher) {

                            try {

                                tmdb.getTmdbSeason(artworkId = artwork.id, season = season)?.let {
                                    it.toDomain(artworkId = artwork.id) to it.episodes
                                }

                            } catch (e: Exception) {
                                Trace.error(TAG, "Fail to get season for artworkId ${artwork.id} - season $season", e)
                                null
                            }

                        }

                    }.awaitAll().filterNotNull()

            }

        }

    }

}