package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtworkRepository @Inject constructor(
    private val db: FluxDao
) {

    data class Content(
        val artworkOverview: ArtworkOverview?,
        val movie: Movie? = null,
        val episodes: List<Episode>? = null
    )

    suspend fun getArtwork(artworkId: Long) : Content {

        val artwork = db.getOverview(artworkId)
        var movie: Movie? = null
        var episodes: List<Episode>? = null

        withContext(Dispatchers.IO) {
            when (artwork?.type) {
                ContentType.MOVIE -> {
                    movie = db.getMovie(artworkId)
                }
                ContentType.SHOW -> {
                    episodes = db.getEpisodes(artworkId)
                }
                else -> {}
            }
        }

        return Content(
            artworkOverview = artwork,
            movie = movie,
            episodes = episodes
        )

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