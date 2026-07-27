package com.mskd.flux.features.progress.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.lastEpisode

class ChangeMediaStatusUseCase(
    private val database: DatabaseRepository,
    private val user: UserDataStore
) {

    companion object {
        const val TAG = "ChangeMediaStatusUseCase"
    }

    suspend operator fun invoke(
        media: Media,
        status: Status
    ) {

        when (media) {
            is Movie -> changeMovieStatus(movie = media, status = status)
            is Episode -> changeEpisodeStatus(episode = media, status = status)
        }

    }

    //region Private Methods

    private suspend fun changeMovieStatus(movie: Movie, status: Status) {

        val movieUpdated = movie.copy(
            status = status,
            currentTime = 0L
        )

        if (status == Status.WATCHED)
            user.removeFromRecentlyWatched(movie.artworkId)

        database.saveMedias(listOf(movieUpdated)) // Save status in DB

        Trace.info(TAG, "${movie.title} is now ${movie.status}")

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

            database.saveMedias(listOf(updatedEpisode)) // Save status in DB

        }


        Trace.info(TAG, "${episode.title} season ${episode.season} episode ${episode.number} is now ${episode.status}")

    }

}