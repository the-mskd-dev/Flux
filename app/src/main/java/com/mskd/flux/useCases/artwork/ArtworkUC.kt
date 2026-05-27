package com.mskd.flux.useCases.artwork

import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Season
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

interface ArtworkUC {

    fun flow(artworkId: Long) : Flow<State<FullArtwork>>

    suspend fun saveMovie(movie: Movie)

    suspend fun saveEpisode(episode: Episode)

    suspend fun saveEpisodes(episodes: List<Episode>)

    suspend fun getArtwork(artworkId: Long) : FullArtwork?

}

class ArtworkUCImpl(
    private val database: DatabaseRepository
) : ArtworkUC {

    private val _artworkId = MutableStateFlow<Long?>(null)

    override fun flow(artworkId: Long): Flow<State<FullArtwork>> {
        return combine(
            database.flowArtwork(artworkId),
            database.flowMovie(artworkId),
            database.flowSeasons(artworkId),
            database.flowEpisodes(artworkId)
        ) { artwork, movie, seasons, episodes ->

            when (artwork?.type) {
                ContentType.MOVIE -> {

                    movie?.let {
                        State.Content(content = buildFullArtworkMovie(artwork = artwork, movie = it))
                    } ?: State.Error

                }
                ContentType.SHOW -> {

                    State.Content(
                        content = buildFullArtworkShow(
                            artwork = artwork,
                            seasons = seasons,
                            episodes = episodes
                        )
                    )

                }
                null -> State.Error
            }

        }
    }

    override suspend fun saveMovie(movie: Movie) {
        database.saveMovies(listOf(movie))
    }

    override suspend fun saveEpisode(episode: Episode) {
        database.saveEpisodes(listOf(episode))
    }

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        database.saveEpisodes(episodes)
    }

    override suspend fun getArtwork(artworkId: Long): FullArtwork? {

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

    private fun buildFullArtworkMovie(artwork: Artwork, movie: Movie) : FullArtwork {
        return FullArtwork.FullMovie(
            resume = artwork,
            movie = movie
        )
    }

    private fun buildFullArtworkShow(artwork: Artwork, seasons: List<Season>, episodes: List<Episode>) : FullArtwork {

        val availableSeasons = seasons.map { it.season }
        val neededSeasons = episodes.map { it.season }.distinct()

        return FullArtwork.FullShow(
            resume = artwork,
            seasons = seasons.filter { s -> neededSeasons.contains(s.season) },
            episodes = if (artwork.isUnknown) episodes else episodes.filter { e -> availableSeasons.contains(e.season) }
        )
    }

}