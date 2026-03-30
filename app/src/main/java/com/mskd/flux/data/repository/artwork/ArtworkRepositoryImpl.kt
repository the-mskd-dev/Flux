package com.mskd.flux.data.repository.artwork

import com.mskd.flux.data.ddb.DatabaseDao
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
import javax.inject.Inject

class ArtworkRepositoryImpl @Inject constructor(
    private val db: DatabaseDao
) : ArtworkRepository {

    private val _mediaId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val flow: Flow<ArtworkRepository.State> = _mediaId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { mediaId ->
            db.flowArtwork(artworkId = mediaId).flatMapLatest { artwork ->
                when (artwork?.type) {
                    ContentType.MOVIE -> {
                        db.flowMovie(mediaId).map { movie ->
                            ArtworkRepository.State(artwork = artwork, movie = movie)
                        }
                    }
                    ContentType.SHOW -> {
                        db.flowEpisodes(mediaId).map { episodes ->
                            ArtworkRepository.State(artwork = artwork, episodes = episodes)
                        }
                    }
                    else -> flowOf(ArtworkRepository.State(artwork = artwork))
                }
            }
        }
        .distinctUntilChanged()

    override fun searchArtwork(mediaId: Long) {
        _mediaId.value = mediaId
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

}