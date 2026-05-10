package com.mskd.flux.useCases.progress

import android.util.Log
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.getPreviousEpisodesFor
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.timeDescription
import kotlin.time.Duration.Companion.minutes

interface ProgressUC {

    suspend fun saveProgress(media: Media, progress: Long)
    suspend fun changeMediaStatus(media: Media, status: Status)
    suspend fun markPreviousEpisodesAsWatchedFor(episode: Episode)
    suspend fun resetProgress(artwork: Artwork)
}

class ProgressUCImpl(
    private val database: DatabaseRepository,
    private val user: UserRepository
) : ProgressUC {

    //region Public Methods

    override suspend fun saveProgress(
        media: Media,
        progress: Long
    ) {

        val newStatus = if (progress >= (media.duration * Constants.PLAYER.PROGRESS_THRESHOLD).minutes.inWholeMilliseconds) Status.WATCHED else Status.IS_WATCHING
        val newTime = if (newStatus == Status.WATCHED) 0L else progress

        val updatedMedia = when (media) {
            is Movie -> media.copy(currentTime = newTime, status = newStatus)
            is Episode -> media.copy(currentTime = newTime, status = newStatus)
        }

        when (updatedMedia) {
            is Movie -> {

                // Add/Remove from recently watched
                if (newStatus == Status.WATCHED) user.removeFromRecentlyWatched(media.artworkId)
                else user.addToRecentlyWatched(media.artworkId)

                // Save in DB
                database.saveMovies(listOf(updatedMedia))
            }
            is Episode -> {

                // Add/Remove from recently watched
                if (!updatedMedia.isUnknown) {
                    val episodes = database.getEpisodes(artworkId = media.artworkId)
                    val lastEpisode = episodes.lastEpisode
                    if (lastEpisode.id == updatedMedia.id && newStatus == Status.WATCHED)
                        user.removeFromRecentlyWatched(updatedMedia.artworkId)
                    else
                        user.addToRecentlyWatched(updatedMedia.artworkId)
                }

                // Save in DB
                database.saveEpisodes(listOf(updatedMedia))
            }
        }

        Log.i(TAG, "${updatedMedia.title} saved at ${progress.timeDescription()}")

    }

    override suspend fun changeMediaStatus(
        media: Media,
        status: Status
    ) {

        when (media) {
            is Movie -> changeMovieStatus(movie = media, status = status)
            is Episode -> changeEpisodeStatus(episode = media, status = status)
        }

    }

    override suspend fun markPreviousEpisodesAsWatchedFor(episode: Episode) {

        var episodesToSave: List<Episode>

        val previousEpisodes = database
            .getEpisodes(artworkId = episode.artworkId)
            .getPreviousEpisodesFor(episode)
            .filter { it.status != Status.WATCHED }

        if (previousEpisodes.isEmpty())
            return

        episodesToSave = previousEpisodes.map {
            it.copy(
                status = Status.WATCHED,
                currentTime = 0L
            )
        }

        database.saveEpisodes(episodesToSave) // Save status in DB

        Log.i(TAG, "${episodesToSave.size} episodes marked as watched")

    }

    override suspend fun resetProgress(artwork: Artwork) {
        when (artwork.type) {
            ContentType.SHOW -> {

                val episodes = database.getEpisodes(artworkId = artwork.id)
                val updatedEpisodes = episodes
                    .filter { it.status != Status.TO_WATCH || it.currentTime != 0L }
                    .map { it.copy(currentTime = 0L, status = Status.TO_WATCH) }

                database.saveEpisodes(episodes = updatedEpisodes)

            }
            ContentType.MOVIE -> {

                database.getMovie(artworkId = artwork.id)?.let { movie ->
                    val updatedMovie = movie.copy(currentTime = 0L, status = Status.TO_WATCH)

                    database.saveMovies(listOf(updatedMovie))
                }

            }
        }

        user.removeFromRecentlyWatched(artworkId = artwork.id)

    }

    //endregion

    //region Private Methods

    private suspend fun changeMovieStatus(movie: Movie, status: Status) {

        val movieUpdated = movie.copy(
            status = status,
            currentTime = 0L
        )

        if (status == Status.WATCHED)
            user.removeFromRecentlyWatched(movie.artworkId)

        database.saveMovies(listOf(movieUpdated)) // Save status in DB

        Log.i(TAG, "${movie.title} is now ${movie.status}")

    }

    private suspend fun changeEpisodeStatus(episode: Episode, status: Status) {

        val updatedEpisode = episode.copy(
            status = status,
            currentTime = 0L
        )

        // Remove from recently watched if last episode is watched
        val episodes = database.getEpisodes(artworkId = episode.artworkId)
        if (episodes.isNotEmpty()) {

            val lastEpisode = episodes.lastEpisode
            if (lastEpisode.id == updatedEpisode.id && status == Status.WATCHED)
                user.removeFromRecentlyWatched(episode.artworkId)

            database.saveEpisodes(listOf(updatedEpisode)) // Save status in DB

        }


        Log.i(TAG, "${episode.title} season ${episode.season} episode ${episode.number} is now ${episode.status}")

    }

    //endregion

    companion object {
        const val TAG = "MediaProgressUCImpl"
    }

}