package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.model.media.ContentType
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val db: DatabaseDao
) {

    data class Content(
        val mediaOverview: MediaOverview?,
        val movie: Movie? = null,
        val episodes: List<Episode> = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMediaFlow(mediaId: Long) : Flow<Content> {
        return db.getOverviewFlow(mediaId = mediaId).flatMapLatest { overview ->
            when (overview?.type) {
                ContentType.MOVIE -> {
                    db.getMovieFlow(mediaId).map { movie ->
                        Content(mediaOverview = overview, movie = movie)
                    }
                }
                ContentType.SHOW -> {
                    db.getEpisodesFlow(mediaId).map { episodes ->
                        Content(mediaOverview = overview, episodes = episodes)
                    }
                }
                else -> flowOf(Content(mediaOverview = overview))
            }
        }
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