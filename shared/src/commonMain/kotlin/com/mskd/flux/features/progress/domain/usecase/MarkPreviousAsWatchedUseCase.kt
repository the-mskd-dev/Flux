package com.mskd.flux.features.progress.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.getPreviousEpisodesFor

class MarkPreviousAsWatchedUseCase(private val database: DatabaseRepository) {

    companion object {
        const val TAG = "MarkPreviousAsWatchedUseCase"
    }

    suspend operator fun invoke(episode: Episode) {

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

        Trace.info(TAG, "${episodesToSave.size} episodes marked as watched")

    }
}