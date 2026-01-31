package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ArtworkRepository @Inject constructor(
    private val db: DatabaseDao
) {

    data class Content(
        val artwork: Artwork? = null,
        val movie: Movie? = null,
        val episodes: List<Episode> = emptyList()
    )

    private val _mediaId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val flow: Flow<Content> = _mediaId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { mediaId ->
            db.flowArtwork(artworkId = mediaId).flatMapLatest { artwork ->
                when (artwork?.type) {
                    ContentType.MOVIE -> {
                        db.flowMovie(mediaId).map { movie ->
                            Content(artwork = artwork, movie = movie)
                        }
                    }
                    ContentType.SHOW -> {
                        db.flowEpisodes(mediaId).map { episodes ->
                            Content(artwork = artwork, episodes = episodes)
                        }
                    }
                    else -> flowOf(Content(artwork = artwork))
                }
            }
        }
        .distinctUntilChanged()

    fun searchArtwork(mediaId: Long) {
        _mediaId.value = mediaId
    }

    suspend fun saveMovie(movie: Movie) {
        db.insertMovies(listOf(movie))
    }

    suspend fun saveEpisode(episode: Episode) {
        db.insertEpisodes(listOf(episode))
    }

    suspend fun saveEpisodes(episodes: List<Episode>) {
        db.insertEpisodes(episodes)
    }

}