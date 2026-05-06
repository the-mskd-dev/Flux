package com.mskd.flux.data.repository.artwork

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ArtworkRepositoryImpl @Inject constructor(
    private val db: DatabaseDao
) : ArtworkRepository {

    private val _artworkId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val flow: Flow<ArtworkRepository.Content> = _artworkId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { mediaId ->
            db.flowArtwork(artworkId = mediaId).flatMapLatest { artwork ->
                when (artwork?.type) {
                    ContentType.MOVIE -> {
                        db.flowMovie(mediaId).map { movie ->
                            movie
                                ?.let { ArtworkRepository.Content.MOVIE(artwork = artwork, movie = it) }
                                ?: ArtworkRepository.Content.ERROR
                        }
                    }
                    ContentType.SHOW -> {
                        db.flowEpisodes(mediaId).map { episodes ->
                            ArtworkRepository.Content.SHOW(artwork = artwork, episodes = episodes)
                        }
                    }
                    else -> flowOf(ArtworkRepository.Content.ERROR)
                }
            }
        }
        .distinctUntilChanged()

    override fun searchArtwork(artworkId: Long) {
        _artworkId.value = artworkId
    }

    override suspend fun saveMovie(movie: Movie) {
        db.insertMovies(listOf(movie))
    }

    override suspend fun saveEpisode(episode: Episode) {
        db.insertEpisodes(listOf(episode))
    }

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        db.insertEpisodes(episodes)
    }

    override suspend fun getArtwork(artworkId: Long): ArtworkRepository.Content {

        return db.getArtwork(artworkId = artworkId)?.let { artwork ->
            when (artwork.type) {
                ContentType.MOVIE -> {
                    db.getMovie(artworkId = artworkId)
                        ?.let { ArtworkRepository.Content.MOVIE(artwork = artwork, movie = it) }
                        ?: ArtworkRepository.Content.ERROR
                }
                ContentType.SHOW -> {
                    val episodes = db.getEpisodes(artworkId = artworkId)
                    ArtworkRepository.Content.SHOW(
                        artwork = artwork,
                        episodes = episodes
                    )
                }
            }
        } ?: ArtworkRepository.Content.ERROR

    }

}