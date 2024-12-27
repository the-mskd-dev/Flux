package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtworkRepository @Inject constructor(
    private val db: DatabaseManager
) {

    data class Content(
        val artworkOverview: ArtworkOverview,
        val movie: Movie? = null,
        val episodes: List<Episode>? = null
    )

    suspend fun getArtwork(artworkId: Long) : Content {

        val artwork = db.getOverview(artworkId)
        var movie: Movie? = null
        var episodes: List<Episode>? = null

        withContext(Dispatchers.IO) {
            when (artwork.type) {
                ContentType.MOVIE -> {
                    movie = db.getMovie(artworkId)
                }
                ContentType.SHOW -> {
                    episodes = db.getEpisodes(artworkId)
                }
            }
        }

        return Content(
            artworkOverview = artwork,
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

    suspend fun saveEpisodes(episodes: List<Episode>) {
        db.saveEpisodes(episodes)
    }

}