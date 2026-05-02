package com.mskd.flux.useCases

import android.util.Log
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.timeDescription
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.minutes

interface MediaProgressUC {
    suspend fun changeMediaStatus(media: Media, status: Status)
    suspend fun saveProgress(media: Media, progress: Long)
}

class MediaProgressUCImpl(
    private val artworkRepository: ArtworkRepository,
    private val userRepository: UserRepository
) : MediaProgressUC {

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
                if (newStatus == Status.WATCHED) userRepository.removeFromRecentlyWatched(media.artworkId)
                else userRepository.addToRecentlyWatched(media.artworkId)

                // Save in DB
                artworkRepository.saveMovie(updatedMedia)
            }
            is Episode -> {

                // Add/Remove from recently watched
                if (!updatedMedia.isUnknown) {
                    val episodes = artworkRepository.flow.first().episodes
                    val lastEpisode = episodes.lastEpisode
                    if (lastEpisode.id == updatedMedia.id && newStatus == Status.WATCHED)
                        userRepository.removeFromRecentlyWatched(updatedMedia.artworkId)
                    else
                        userRepository.addToRecentlyWatched(updatedMedia.artworkId)
                }

                // Save in DB
                artworkRepository.saveEpisode(updatedMedia)
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

    //endregion

    //region Private Methods

    private suspend fun changeMovieStatus(movie: Movie, status: Status) {

        val movieUpdated = movie.copy(
            status = status,
            currentTime = 0L
        )

        artworkRepository.saveMovie(movieUpdated) // Save status in DB

        Log.i(TAG, "${movie.title} is now ${movie.status}")

    }

    private suspend fun changeEpisodeStatus(episode: Episode, status: Status) {

        val updatedEpisode = episode.copy(
            status = status,
            currentTime = 0L
        )

        // Remove from recently watched if last episode is watched
        val lastEpisode = artworkRepository.flow.first().episodes.lastEpisode
        if (lastEpisode.id == updatedEpisode.id && status == Status.WATCHED)
            userRepository.removeFromRecentlyWatched(episode.artworkId)

        artworkRepository.saveEpisodes(listOf(updatedEpisode)) // Save status in DB

        Log.i(TAG, "${episode.title} season ${episode.season} episode ${episode.number} is now ${episode.status}")

    }

    //endregion

    companion object {
        const val TAG = "MediaProgressUCImpl"
    }

}