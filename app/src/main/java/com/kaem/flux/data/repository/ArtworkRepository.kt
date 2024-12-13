package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.file.FilesDataSource
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ContentType
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ArtworkRepository @Inject constructor(
    private val db: DatabaseManager
) {

    data class Content(
        val artwork: Artwork,
        val movie: Movie? = null,
        val episodes: List<Episode>? = null
    )

    suspend fun getArtwork(artworkId: Long) : Content {

        val artwork = db.getArtwork(artworkId)
        var movie: Movie? = null
        var episodes: List<Episode>? = null

        when (artwork.type) {
            ContentType.MOVIE -> {
                movie = db.getMovie(artworkId)
            }
            ContentType.SHOW -> {
                episodes = db.getEpisodes(artworkId)
            }
        }

        return Content(
            artwork = artwork,
            movie = movie,
            episodes = episodes
        )

    }

    suspend fun saveMovie(movie: Movie) {
        db.saveMovies(listOf(movie))
    }

    suspend fun saveEpisode(episode: Episode) {
        db.saveEpisodes(listOf(episode))
    }

}