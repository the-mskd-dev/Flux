package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.model.media.ContentType
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val db: FluxDao
) {

    data class Content(
        val mediaOverview: MediaOverview?,
        val movie: Movie? = null,
        val episodes: List<Episode>? = null
    )

    suspend fun getMedia(mediaId: Long) : Content {

        val media = db.getOverview(mediaId)
        var movie: Movie? = null
        var episodes: List<Episode>? = null

        withContext(Dispatchers.IO) {
            when (media?.type) {
                ContentType.MOVIE -> {
                    movie = db.getMovie(mediaId)
                }
                ContentType.SHOW -> {
                    episodes = db.getEpisodes(mediaId)
                }
                else -> {}
            }
        }

        return Content(
            mediaOverview = media,
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