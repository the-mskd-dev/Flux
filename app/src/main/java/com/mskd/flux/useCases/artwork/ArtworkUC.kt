package com.mskd.flux.useCases.artwork

import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface ArtworkUC {

    val flow: Flow<Content>

    fun searchArtwork(artworkId: Long)

    suspend fun saveMovie(movie: Movie)

    suspend fun saveEpisode(episode: Episode)

    suspend fun saveEpisodes(episodes: List<Episode>)

    suspend fun getArtwork(artworkId: Long) : Content

    sealed class Content {
        data class MOVIE(val artwork: Artwork, val movie: Movie) : Content()
        data class SHOW(val artwork: Artwork, val episodes: List<Episode>) : Content()
        data object ERROR : Content()
    }

}

class ArtworkUCImpl(
    private val database: DatabaseRepository
) : ArtworkUC {

    private val _artworkId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val flow: Flow<ArtworkUC.Content> = _artworkId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { mediaId ->
            database.flowArtwork(artworkId = mediaId).flatMapLatest { artwork ->
                when (artwork?.type) {
                    ContentType.MOVIE -> {
                        database.flowMovie(mediaId).map { movie ->
                            movie
                                ?.let { ArtworkUC.Content.MOVIE(artwork = artwork, movie = it) }
                                ?: ArtworkUC.Content.ERROR
                        }
                    }
                    ContentType.SHOW -> {
                        database.flowEpisodes(mediaId).map { episodes ->
                            ArtworkUC.Content.SHOW(artwork = artwork, episodes = episodes)
                        }
                    }
                    else -> flowOf(ArtworkUC.Content.ERROR)
                }
            }
        }
        .distinctUntilChanged()

    override fun searchArtwork(artworkId: Long) {
        _artworkId.value = artworkId
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

    override suspend fun getArtwork(artworkId: Long): ArtworkUC.Content {

        return database.getArtwork(artworkId = artworkId)?.let { artwork ->
            when (artwork.type) {
                ContentType.MOVIE -> {
                    database.getMovie(artworkId = artworkId)
                        ?.let { ArtworkUC.Content.MOVIE(artwork = artwork, movie = it) }
                        ?: ArtworkUC.Content.ERROR
                }
                ContentType.SHOW -> {
                    val episodes = database.getEpisodes(artworkId = artworkId)
                    ArtworkUC.Content.SHOW(
                        artwork = artwork,
                        episodes = episodes
                    )
                }
            }
        } ?: ArtworkUC.Content.ERROR

    }

}