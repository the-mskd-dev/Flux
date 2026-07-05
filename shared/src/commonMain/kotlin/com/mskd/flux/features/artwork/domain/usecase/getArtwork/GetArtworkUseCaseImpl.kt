package com.mskd.flux.features.artwork.domain.usecase.getArtwork

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.core.domain.model.artwork.FullArtwork
import com.mskd.flux.features.artwork.domain.mapper.buildFullArtworkMovie
import com.mskd.flux.features.artwork.domain.mapper.buildFullArtworkShow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class GetArtworkUseCaseImpl(
    private val database: DatabaseRepository
) : GetArtworkUseCase {

    override suspend fun invoke(artworkId: Long): FullArtwork? {
        return database.getArtwork(artworkId = artworkId)?.let { artwork ->
            when (artwork.type) {
                ContentType.MOVIE -> {
                    database.getMovie(artworkId = artworkId)
                        ?.let { buildFullArtworkMovie(artwork = artwork, movie = it) }
                }
                ContentType.SHOW -> {
                    val (seasons, episodes) = coroutineScope {
                        val seasonsDeferred = async { database.getSeasons(artworkId = artworkId) }
                        val episodesDeferred = async { database.getEpisodes(artworkId = artworkId) }
                        seasonsDeferred.await() to episodesDeferred.await()
                    }

                    buildFullArtworkShow(
                        artwork = artwork,
                        seasons = seasons,
                        episodes = episodes
                    )
                }
            }
        }
    }

}